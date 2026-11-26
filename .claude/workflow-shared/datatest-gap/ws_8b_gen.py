"""Add the sample worksheets of ws.md section 8b to the ws storage zip: DB/crossSourceJoin, DB/bigNumbers,
DB/nullSemantics, DB/caseGrouping, DB/numericEdge, DB/hourGroup, DB/variableCache (used by the WSData_Spec cases of
the same names; see docs/test-gap-assessment/ws.md, 8b).

usage: python ws_8b_gen.py <src.zip> <dst.zip>
       src = the output of ws_ds_password.py (the last step of the chain of ws_g11_gen.py, ws_g8_gen.py, ws_ch_gen.py),
       dst = the zip to write. After replacing ws/src/test/resources/config/wsStorage.zip run ./mvnw clean -pl ws.

As in G8, most cases are built twice on the same data: pushed down to the database, and in memory (a script
expression copy of the column, or a script column that keeps a join or a grouping off SQL). Base tables that only
feed a join are hidden (not exported). Tables whose query fails, or whose rows are database-defined, are left out
(LEFT_OUT at the end, with the reason).
"""
import importlib.util, os, sys, zipfile, hashlib, json, time
from datetime import datetime, timezone

HERE = os.path.dirname(os.path.abspath(__file__))


def _load(name):
    spec = importlib.util.spec_from_file_location(name[:-3], os.path.join(HERE, name))
    m = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(m)
    return m


g8 = _load('ws_g8_gen.py')
g11 = _load('ws_g11_gen.py')
g11.DATE_NAMES.update({8: 'Hour', 521: 'HourOfDay'})
ref, expr, physical, join, esc = g8.ref, g8.expr, g8.physical, g8.join, g8.esc
FIELD = "field['%s']"
EQUAL, ONE_OF, LESS, GREATER, BETWEEN, INNER = 1, 2, 3, 4, 5, 3
DBS = {d['name']: d for d in g11.DBS}
NAMES = ['Oracle', 'MySQL', 'Derby', 'PostgreSQL', 'SQLServer', 'DB2', 'Informix']
MOD = {n: 'MOD(%s, 2)' for n in NAMES}
MOD['SQLServer'] = '(%s %% 2)'
MOD['Derby'] = 'MOD(CAST(%s AS INT), 2)'
CONCAT = {n: '%s || %s' for n in NAMES}
CONCAT['MySQL'] = 'CONCAT(%s, %s)'
CONCAT['SQLServer'] = '%s + %s'
BIG = {n: 'CAST(%s AS BIGINT) + 9007199254740992' for n in NAMES}
BIG['Oracle'] = '%s + 9007199254740992'
BIG['MySQL'] = 'CAST(%s AS SIGNED) + 9007199254740992'
REAL = {'Oracle': 'CAST(%s AS BINARY_FLOAT)', 'MySQL': '(%s + 0E0)', 'Derby': 'CAST(%s AS REAL)',
        'PostgreSQL': 'CAST(%s AS REAL)', 'SQLServer': 'CAST(%s AS REAL)', 'DB2': 'CAST(%s AS REAL)',
        'Informix': 'CAST(%s AS SMALLFLOAT)'}
INT = {n: 'CAST(2000000000 AS INTEGER)' for n in NAMES}
INT['MySQL'] = 'CAST(2000000000 AS SIGNED)'
OR = '<junction junction="1" level="0"/>\n'
GSORT = ('<groupSort sort="1" interval="1.0" option="1" others="1" sortByCol="0" info="false" cinfo="false"><otherLabel>'
         '<![CDATA[Others]]></otherLabel>\n<manualOrderList class="inetsoft.util.ItemList"></manualOrderList></groupSort>\n')


def tdb(n, table):
    """a g8-style db dict for one source table of a g11 database"""
    d = DBS[n]
    return {'prefix': d['prefix'], 'source': d[table][0], 'props': d['props']}, d[table][1]


def cond(r, ctype, op, values, negated=False):
    return ('<condition level="0" class="inetsoft.uql.asset.AssetCondition" type="%s" negated="%s" operation="%d" '
            'equal="false">\n%s%s\n</condition>\n') % (ctype, 'true' if negated else 'false', op, r, ''.join(
                '<condition_data><![CDATA[%s]]></condition_data>' % esc(v) for v in values))


def var_cond(r, name):
    return ('<condition level="0" class="inetsoft.uql.asset.AssetCondition" type="string" negated="false" operation="1" '
            'equal="false">\n%s<condition_data isvariable="true" name="%s" prompt="false" sortValue="true" label="%s" '
            'toolTip="" xtype="string"/>\n</condition>\n') % (r, name, name)


def agg_info(group_xml, aggs):
    g = '<dataRef class="inetsoft.uql.asset.GroupRef"  timeSeries="false" >%s%s</dataRef>' % (group_xml, GSORT)
    a = ''.join('<dataRef class="inetsoft.uql.asset.AggregateRef"  n="1">%s<formula><![CDATA[%s]]></formula>\n</dataRef>'
                % (r, f) for f, r in aggs)
    return ('<groupInfo>\n<groupInfo crosstab="false">\n<groups>\n%s</groups>\n<aggregates>\n%s</aggregates>\n'
            '</groupInfo>\n</groupInfo>\n') % (g, a)


def grouped(t, group_xml, aggs):
    t = t.replace('hasExpression="true">', 'hasExpression="true" aggregate="true" hasAggregate="true">', 1)
    return t.replace('<conditionAssemblies>', agg_info(group_xml, aggs) + '<conditionAssemblies>', 1)


def grouped_join(jt, group_xml, aggs):
    jt = jt.replace('hierarchical="false">', 'hierarchical="false" aggregate="true" hasAggregate="true">', 1)
    return jt.replace('<tables>', agg_info(group_xml, aggs) + '<tables>', 1)


def distinct(t):
    return t.replace('hasExpression="true">', 'hasExpression="true" distinct="true">', 1)


def hidden_col(x):
    return x.replace('<dataRef class="inetsoft.uql.asset.ColumnRef" ', '<dataRef class="inetsoft.uql.asset.ColumnRef"  visible="false"', 1)


def hidden_table(t):
    return t.replace('pixelHeight="60"', 'pixelHeight="60" visibleTable="false"', 1)


def sortref(r, desc=False):
    return '<dataRef class="inetsoft.uql.asset.SortRef"  order="%d">%s</dataRef>' % (2 if desc else 1, r)


def sorted_max(t, sorts, maxrow):
    t = t.replace('hasExpression="true">', 'hasExpression="true" maxRow="%d" hasSort="true">' % maxrow, 1)
    return t.replace('<conditionAssemblies>', '<sortInfo>\n<sortInfo>\n%s</sortInfo>\n</sortInfo>\n<conditionAssemblies>'
                     % ''.join(sorts), 1)


# ---- DB/crossSourceJoin (8b item 1): equi joins across two databases, in memory. CUSTOMER_ID (NUMBER / INT /
# DOUBLE / ...): 35 rows each; ORDER_DATE (DATE against TIMESTAMP) of orders 12001, 12003, 12171: 3 rows each.
xsrc = []
for j, n in enumerate(NAMES):
    db, cm = tdb(n, 'CUSTOMERS')
    xsrc.append(hidden_table(physical(db, 'C_' + n, [ref(cm['id']), ref(cm['state'])], [], 20 + j * 140, 20)))
    db, om = tdb(n, 'ORDERS')
    xsrc.append(hidden_table(physical(db, 'O_' + n, [ref(om['oid']), ref(om['date'])],
                                      [(ref(om['oid']), om['oid'][1], ONE_OF, [12001, 12003, 12171])], 20 + j * 140, 140)))
for i, (a, b) in enumerate([('Oracle', 'MySQL'), ('MySQL', 'SQLServer'), ('PostgreSQL', 'DB2'), ('Derby', 'Informix'),
                            ('Oracle', 'PostgreSQL'), ('MySQL', 'DB2')]):
    ca, cb = DBS[a]['CUSTOMERS'][1], DBS[b]['CUSTOMERS'][1]
    xsrc.append(join('id_%s_%s' % (a, b), 'C_' + a, [(ca['id'][0], ca['id'][1]), (ca['state'][0], None)],
                     'C_' + b, [(cb['id'][0], cb['id'][1]), (cb['state'][0], None)], ca['id'][0], cb['id'][0], INNER,
                     20 + i * 140, 260))
for i, (a, b) in enumerate([('MySQL', 'PostgreSQL'), ('SQLServer', 'Oracle'), ('Derby', 'DB2')]):
    oa, ob = DBS[a]['ORDERS'][1], DBS[b]['ORDERS'][1]
    xsrc.append(join('date_%s_%s' % (a, b), 'O_' + a, [(oa['oid'][0], oa['oid'][1]), (oa['date'][0], oa['date'][1])],
                     'O_' + b, [(ob['oid'][0], ob['oid'][1]), (ob['date'][0], ob['date'][1])], oa['date'][0], ob['date'][0],
                     INNER, 20 + i * 140, 380))

# ---- DB/bigNumbers (8b item 2): BIG = CUSTOMER_ID + 2^53, 35 distinct values (Informix not sampled). Read as Long on
# MySQL, PostgreSQL, Derby, DB2, as BigDecimal on Oracle and SQL Server, where the table holds it as a double (#82378).
big_t = []
for j, n in enumerate([m for m in NAMES if m != 'Informix']):
    db, cm = tdb(n, 'CUSTOMERS')
    x = 20 + j * 140
    big = expr('BIG', BIG[n] % (FIELD % cm['id'][0]), 'long')
    js = hidden_col(expr('S_JS', FIELD % cm['state'][0], 'string', sql=False))
    base = [ref(cm['id']), ref(cm['state']), big]
    # group by BIG pushed: 35 groups. With Mode (not pushable) the SQL expression is run as JavaScript (#82381).
    big_t.append(grouped(physical(db, 'grp_' + n, base, [], x, 20), big, [('Count', ref(cm['id']))]))
    big_t.append(grouped(physical(db, 'grpMode_' + n, base, [], x, 80), big,
                         [('Count', ref(cm['id'])), ('Mode', ref(cm['state']))]))
    # BIG = 2^53 + 3: pushed, and in memory (OR with a condition on a script column): customer 3 only.
    # In memory BigDecimal compares as double: customers 3, 4, 5 on Oracle and SQL Server (#82378).
    c_big = cond(big, 'long', EQUAL, [9007199254740995])
    big_t.append(physical(db, 'cond_' + n, base, [c_big], x, 140))
    big_t.append(physical(db, 'condMem_' + n, base + [js], [c_big + OR + cond(js, 'string', EQUAL, ['zz'])], x, 200))
    # in-memory join on BIG: 35 rows; grouped on BIG in memory: 35 groups, 19 on Oracle and SQL Server (#82378)
    big_t.append(hidden_table(physical(db, 'L_' + n, base + [js], [], x, 260)))
    big_t.append(hidden_table(physical(db, 'R_' + n, base + [js], [], x, 320)))
    cols = [(cm['id'][0], cm['id'][1]), (cm['state'][0], None), ('BIG', 'long')]
    big_t.append(join('joinMem_' + n, 'L_' + n, cols, 'R_' + n, cols, 'BIG', 'BIG', INNER, x, 380))
    big_t.append(grouped_join(join('joinGrp_' + n, 'L_' + n, cols, 'R_' + n, cols, 'BIG', 'BIG', INNER, x, 440),
                              ref(('BIG', 'long', 12), 'L_' + n),
                              [('Count', ref((cm['id'][0], cm['id'][1], 12), 'L_' + n)),
                               ('Mode', ref((cm['state'][0], None, 12), 'L_' + n))]))
# a small numeric SQL expression column TWICE = id * 2 with '=' and '>' (the value was bound as a string, #82379, fixed)
for j, n in enumerate(NAMES):
    db, cm = tdb(n, 'CUSTOMERS')
    tw = expr('TWICE', '%s * 2' % (FIELD % cm['id'][0]), 'integer')
    big_t.append(physical(db, 'twiceEq_' + n, [ref(cm['id']), tw], [cond(tw, 'integer', EQUAL, [4])], 20 + j * 140, 560))
    big_t.append(physical(db, 'twiceGt_' + n, [ref(cm['id']), tw], [cond(tw, 'integer', GREATER, [66])], 20 + j * 140, 620))

# ---- DB/nullSemantics (8b items 3, 4). PRODUCTS.DESCRIPTION is NULL for products 1, 15, 16; DISCOUNT NULL for some
# of the G11 orders. Negated conditions exclude NULL on both paths (21, 20, 8, 7 rows).
nul = []
for j, n in enumerate(NAMES):
    x = 20 + j * 140
    db, pm = tdb(n, 'PRODUCTS')
    d = ref(pm['desc'])
    djs = expr('DESC_JS', FIELD % pm['desc'][0], 'string', sql=False)
    for k, (case, op, vals) in enumerate([('neEq', EQUAL, ['Learn ABC']), ('notIn', ONE_OF, ['Learn ABC', 'Chess game'])]):
        nul.append(physical(db, '%s_%s' % (case, n), [ref(pm['pid']), d], [cond(d, 'string', op, vals, True)], x, 20 + k * 120))
        nul.append(physical(db, '%sMem_%s' % (case, n), [ref(pm['pid']), d, djs],
                            [cond(djs, 'string', op, vals, True)], x, 80 + k * 120))
    # NULL position with max rows 4: sorted by DESCRIPTION, then PRODUCT_ID (so the kept rows are defined).
    # Ascending: NULL first on MySQL, SQL Server, Informix and in memory, last on Oracle, PostgreSQL, DB2, Derby.
    # In memory the max rows cut the source before the sort (#82389): the first 4 products, sorted.
    for k, desc in enumerate([False, True]):
        tag = 'Desc' if desc else 'Asc'
        nul.append(sorted_max(physical(db, 'nullSort%s_%s' % (tag, n), [ref(pm['pid']), d], [], x, 260 + k * 120),
                              [sortref(d, desc), sortref(ref(pm['pid']))], 4))
        nul.append(sorted_max(physical(db, 'nullSort%sMem_%s' % (tag, n), [ref(pm['pid']), d, djs], [], x, 320 + k * 120),
                              [sortref(djs, desc), sortref(ref(pm['pid']))], 4))
    db, om = tdb(n, 'ORDERS')
    disc = ref(om['disc'])
    discjs = expr('DISC_JS', FIELD % om['disc'][0], 'double', sql=False)
    sel = cond(ref(om['oid']), om['oid'][1], ONE_OF, g11.ORDER_IDS)
    for k, (case, op, vals) in enumerate([('neNum', EQUAL, [0]), ('notBetween', BETWEEN, [0.1, 0.2])]):
        nul.append(physical(db, '%s_%s' % (case, n), [ref(om['oid']), disc], [sel, cond(disc, 'double', op, vals, True)],
                            x, 500 + k * 120))
        nul.append(physical(db, '%sMem_%s' % (case, n), [ref(om['oid']), disc, discjs],
                            [sel, cond(discjs, 'double', op, vals, True)], x, 560 + k * 120))

# ---- DB/caseGrouping (8b item 5): MIX = STATE upper-case for odd ids, lower-case for even; PAD = STATE plus a
# trailing space for even ids. Pushed: grouped by the database rule; in memory (script copy): case-insensitive,
# trailing space kept (documented in #82382). Distinct on a table with a script column is pushed over the hidden
# columns the script reads (#82380): the visible rows are not de-duplicated.
cg = []
for j, n in enumerate(NAMES):
    db, cm = tdb(n, 'CUSTOMERS')
    idf, st = FIELD % cm['id'][0], FIELD % cm['state'][0]
    x = 20 + j * 140
    mix = expr('MIX', 'CASE WHEN %s = 1 THEN UPPER(%s) ELSE LOWER(%s) END' % (MOD[n] % idf, st, st), 'string')
    pad = expr('PAD', 'CASE WHEN %s = 1 THEN %s ELSE %s END' % (MOD[n] % idf, st, CONCAT[n] % (st, "' '")), 'string')
    mix_js = expr('MIX_JS', "%s %% 2 == 1 ? %s.toUpperCase() : %s.toLowerCase()" % (idf, st, st), 'string', sql=False)
    pad_js = expr('PAD_JS', "%s %% 2 == 1 ? %s : %s + ' '" % (idf, st, st), 'string', sql=False)
    js = hidden_col(expr('S_JS', st, 'string', sql=False))
    for k, (case, col, col_js) in enumerate([('mix', mix, mix_js), ('pad', pad, pad_js)]):
        y = 20 + k * 300
        cg.append(grouped(physical(db, 'grp_%s_%s' % (case, n), [ref(cm['id']), ref(cm['state']), col], [], x, y), col,
                          [('Count', ref(cm['id']))]))
        cg.append(grouped(physical(db, 'grpJs_%s_%s' % (case, n), [ref(cm['id']), ref(cm['state']), col_js], [], x, y + 60),
                          col_js, [('Count', ref(cm['id']))]))
        hid = [hidden_col(ref(cm['id'])), hidden_col(ref(cm['state']))]
        cg.append(distinct(physical(db, 'dist_%s_%s' % (case, n), hid + [col], [], x, y + 120)))
        cg.append(distinct(physical(db, 'distMem_%s_%s' % (case, n), hid + [col, js], [], x, y + 180)))
        cg.append(distinct(physical(db, 'distJs_%s_%s' % (case, n), hid + [col_js], [], x, y + 240)))

# ---- DB/numericEdge (8b items 6, 8)
num = []
for j, n in enumerate(NAMES):
    x = 20 + j * 140
    # a single-precision float one of 0.15 (orders 12054, 12473): DB2 compares REAL with a DOUBLE 0.15 and keeps
    # no row pushed; the other databases and memory keep both
    db, om = tdb(n, 'ORDERS')
    real = expr('RDISC', REAL[n] % (FIELD % om['disc'][0]), 'float')
    real_js = expr('RDISC_JS', FIELD % 'RDISC', 'float', sql=False)
    sel = cond(ref(om['oid']), om['oid'][1], ONE_OF, g11.ORDER_IDS)
    num.append(physical(db, 'real_%s' % n, [ref(om['oid']), ref(om['disc']), real], [sel, cond(real, 'float', ONE_OF, [0.15])], x, 20))
    num.append(physical(db, 'realMem_%s' % n, [ref(om['oid']), ref(om['disc']), real, real_js],
                        [sel, cond(real_js, 'float', ONE_OF, [0.15])], x, 80))
    # INTV = 2,000,000,000 (INTEGER) per customer, Sum by STATE: NJ 1.2E10. Pushed SUM(INTEGER) overflows on SQL Server,
    # DB2, Derby (#82390, left out); the Sum of a script copy is computed in memory on every database.
    db, cm = tdb(n, 'CUSTOMERS')
    intv = expr('INTV', INT[n], 'integer')
    base = [ref(cm['id']), ref(cm['state']), intv]
    num.append(grouped(physical(db, 'sumInt_%s' % n, base, [], x, 140), ref(cm['state']), [('Sum', intv)]))
    intv_js = expr('INTV_JS', FIELD % 'INTV', 'integer', sql=False)
    num.append(grouped(physical(db, 'sumIntMem_%s' % n, base + [intv_js], [], x, 200), ref(cm['state']), [('Sum', intv_js)]))

# ---- DB/hourGroup (8b item 7): PRODUCE_DATETIME grouped by Hour and HourOfDay, pushed and in memory (no time zone
# shift between the database function and the JVM). Derby and Informix hold other times than the other databases.
hour = []
for j, n in enumerate(NAMES):
    d = DBS[n]
    c = (lambda s: s.upper()) if n in ('Oracle', 'Derby', 'DB2') else (lambda s: s.lower())
    d7 = dict(d)
    d7['P7'] = (d['PRODUCTS'][0], dict(d['PRODUCTS'][1], pdt=(c('produce_datetime'), 'timeInstant', 93)))
    for k, (opt, mergeable) in enumerate([(8, True), (8, False), (521, True), (521, False)]):
        hour.append(g11.date_grouped(d7, 'P7', '%s%s_%s' % (g11.DATE_NAMES[opt], '' if mergeable else 'Mem', n),
                                     ['pid', 'pdt'], [], [('pdt', opt)], [('Count', 'pid')], 20 + j * 140, 20 + k * 60,
                                     mergeable=mergeable))

# ---- DB/variableCache (8b item 10): STATE = $(st); the case runs it with NJ, NY, NJ (each run opens the worksheet
# again, so a result cached by a run with another value would show)
var = []
for j, n in enumerate(NAMES):
    db, cm = tdb(n, 'CUSTOMERS')
    var.append(physical(db, 'state_' + n, [ref(cm['id']), ref(cm['state'])], [var_cond(ref(cm['state']), 'st')],
                        20 + j * 140, 20))

# left out: the query fails (the export would be the placeholder table or the rows read before the failure), or the
# rows are database-defined
LEFT_OUT = {
    # bigNumbers: group by a SQL expression with Mode, the SQL text is run as JavaScript and fails (#82381); on Oracle
    # it runs without an error as double arithmetic (19 groups) and is kept
    'grpMode_MySQL': '#82381', 'grpMode_Derby': '#82381', 'grpMode_PostgreSQL': '#82381',
    'grpMode_SQLServer': '#82381', 'grpMode_DB2': '#82381',
    # numericEdge: SUM(INTEGER) pushed overflows (#82390)
    'sumInt_Derby': '#82390', 'sumInt_SQLServer': '#82390', 'sumInt_DB2': '#82390',
}


def keep(assemblies):
    out = []
    for a in assemblies:
        name = a.split('<name><![CDATA[', 1)[1].split(']]>', 1)[0]
        if name not in LEFT_OUT:
            out.append(a)
    return out


NOW = int(datetime(2026, 11, 26, tzinfo=timezone.utc).timestamp() * 1000)
g8.NOW = NOW
sheets = [g8.worksheet('DB/crossSourceJoin', 'id_Oracle_MySQL', keep(xsrc)),
          g8.worksheet('DB/bigNumbers', 'grp_Oracle', keep(big_t)),
          g8.worksheet('DB/nullSemantics', 'neEq_Oracle', keep(nul)),
          g8.worksheet('DB/caseGrouping', 'grp_mix_Oracle', keep(cg)),
          g8.worksheet('DB/numericEdge', 'real_Oracle', keep(num)),
          g8.worksheet('DB/hourGroup', 'Hour_Oracle', keep(hour)),
          g8.worksheet('DB/variableCache', 'state_Oracle', keep(var))]


def main():
    g8.sheets[:] = sheets
    g8.main()


if __name__ == '__main__':
    main()
