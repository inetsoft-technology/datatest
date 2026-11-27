"""Add the dialect execution check worksheets to the ws storage zip: DB/dialectSetOps, DB/dialectJoins, DB/dialectJoins2,
DB/dialectOrderBy, DB/dialectGroupBy, DB/dialectDates, DB/dialectTimestamps, DB/dialectTimes (used by the WSData_Spec cases of the same names; see
docs/test-gap-assessment/ws.md, section 8).

usage: python ws_dialect_gen.py <src.zip> <dst.zip>
       src = the output of ws_g12_gen.py (the step before this one in the zip chain, see the README),
       dst = the zip to write. After replacing ws/src/test/resources/config/wsStorage.zip run ./mvnw clean -pl ws.

Every SQL operation sqlhelper.xml (and the helpers' code) declares for a database gets a small table that uses it,
on Oracle, MySQL, Derby, PostgreSQL, SQL Server, DB2 and Informix. Each table <case>_<db> has a twin <case>Mem_<db>
with mergeable="false" (the user's "do not merge" switch): the same table computed in memory from the same source
rows. That works for joins and set operations; a physical table stays in SQL with it, so the twins of sorts, groups
and date groups use a script copy of the sorted / grouped column instead (a date range column also keeps its SQL date
function in a table that is not merged). exportMergePlan records which path each one took, so an operation a database declares unsupported shows up as
not merged, and a supported one as merged.
"""
import importlib.util, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('ws_8b_gen', os.path.join(HERE, 'ws_8b_gen.py'))
b = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(b)
g8, g11 = b.g8, b.g11
ref, expr, physical, tdb, cond, esc = b.ref, b.expr, b.physical, b.tdb, b.cond, b.esc
hidden_table, hidden_col, grouped, sorted_max, sortref = b.hidden_table, b.hidden_col, b.grouped, b.sorted_max, b.sortref
FIELD = "field['%s']"
ONE_OF = 2
NAMES = b.NAMES
INNER, LEFT, RIGHT, FULL, NE, GT, GE, LT, LE, CROSS = 3, 7, 11, 15, 17, 33, 35, 65, 67, 257
UNION, INTERSECT, MINUS = 1536, 2560, 4608

def mem(t, name, new):
    """the in-memory twin of a table: renamed, mergeable="false" on its assembly info"""
    t = t.replace('<name><![CDATA[%s]]></name>' % name, '<name><![CDATA[%s]]></name>' % new, 1)
    return t.replace('pixelHeight="60"', 'pixelHeight="60" mergeable="false"', 1)


def pair(out, t, name):
    out.append(t)
    i = name.rindex('_')
    out.append(mem(t, name, name[:i] + 'Mem' + name[i:]))


def concat(name, left, right, cols, op, distinct, x, y):
    """cols: [(column name, dataType)] of the left table (the right one has the same shape)"""
    refs = ''.join(ref((c, t, 12), left) for c, t in cols)
    return ('<assembly class="inetsoft.uql.asset.ConcatenatedTableAssembly">\n'
            '<assemblyInfo class="inetsoft.uql.asset.internal.CompositeTableAssemblyInfo" pixelOffX="%d" pixelOffY="%d" '
            'pixelWidth="100" pixelHeight="60" hierarchical="false">\n'
            '<name><![CDATA[%s]]></name><class><![CDATA[inetsoft.uql.asset.ConcatenatedTableAssembly]]></class>\n%s'
            '<operators>\n<pairOperator>\n<pair>\n<leftTableAssembly><![CDATA[%s]]></leftTableAssembly>\n'
            '<rightTableAssembly><![CDATA[%s]]></rightTableAssembly>\n</pair>\n<tableAssemblyOperator>\n'
            '<operator operation="%d" distinct="%s" leftTable="" rightTable="">\n</operator>\n'
            '</tableAssemblyOperator>\n</pairOperator>\n</operators>\n%s</assemblyInfo><mvForceAppendUpdates>false'
            '</mvForceAppendUpdates>\n<tables>\n<oneTableAssembly><![CDATA[%s]]></oneTableAssembly>\n'
            '<oneTableAssembly><![CDATA[%s]]></oneTableAssembly>\n</tables>\n</assembly>') % (
        x, y, name, g8.selections(refs), left, right, op, 'true' if distinct else 'false', g8.schema_infos(left, right),
        left, right)


def ctype(col):
    return col[1] or 'string'


# ---------------------------------------------------------------- DB/dialectSetOps
# PRODUCTS: A = products 1-16, B = products 10-25. CATEGORY_ID repeats inside each side and across them;
# DESCRIPTION is NULL for 1, 15, 16 (NULL in both sides: set operations treat NULLs as equal, unlike joins).
# union, union all (keep duplicate rows), intersect, minus.
setops = []
for j, n in enumerate(NAMES):
    x, y = 20 + j * 140, 20
    db, pm = tdb(n, 'PRODUCTS')
    pid = pm['pid']
    for k, (col, key) in enumerate([('cat', pm['cat']), ('desc', pm['desc'])]):
        A, B = 'A%s_%s' % (col, n), 'B%s_%s' % (col, n)
        # PRODUCT_ID is in the tables (hidden) so that the condition on it applies
        setops.append(hidden_table(physical(db, A, [hidden_col(ref(pid)), ref(key)], [(ref(pid), pid[1], ONE_OF, list(range(1, 17)))], x, y)))
        setops.append(hidden_table(physical(db, B, [hidden_col(ref(pid)), ref(key)], [(ref(pid), pid[1], ONE_OF, list(range(10, 26)))], x, y + 60)))
        # "Keep duplicate rows" is offered for union only (intersect and minus are always distinct in the dialog)
        for m, (op, opname, distinct) in enumerate([(UNION, 'union', True), (UNION, 'unionAll', False),
                                                    (INTERSECT, 'intersect', True), (MINUS, 'minus', True)]):
            case = '%s%s_%s' % (opname, col.capitalize(), n)
            pair(setops, concat(case, A, B, [(key[0], key[1])], op, distinct, x, y + 120 + m * 120), case)
        y += 900

# ---------------------------------------------------------------- DB/dialectJoins
# PRODUCTS: L = products 1-8, R = products 5-12, joined on CATEGORY_ID (repeated keys) with every join type;
# on DESCRIPTION (NULL for product 1 in L): NULL never matches.
JOINS = [('inner', INNER), ('left', LEFT), ('right', RIGHT), ('full', FULL), ('ne', NE), ('gt', GT), ('ge', GE),
         ('lt', LT), ('le', LE), ('cross', CROSS)]
joins = []
for j, n in enumerate(NAMES):
    x, y = 20 + j * 140, 20
    db, pm = tdb(n, 'PRODUCTS')
    pid, cat, desc = pm['pid'], pm['cat'], pm['desc']
    L, R = 'L_' + n, 'R_' + n
    cols = [(pid[0], pid[1]), (cat[0], cat[1]), (desc[0], None)]
    joins.append(hidden_table(physical(db, L, [ref(pid), ref(cat), ref(desc)], [(ref(pid), pid[1], ONE_OF, list(range(1, 9)))], x, y)))
    joins.append(hidden_table(physical(db, R, [ref(pid), ref(cat), ref(desc)], [(ref(pid), pid[1], ONE_OF, list(range(5, 13)))], x, y + 60)))
    for k, (jn, op) in enumerate(JOINS):
        case = '%s_%s' % (jn, n)
        pair(joins, g8.join(case, L, cols, R, cols, cat[0], cat[0], op, x, y + 120 + k * 120), case)
    for k, (jn, op) in enumerate([('innerNull', INNER), ('leftNull', LEFT), ('fullNull', FULL)]):
        case = '%s_%s' % (jn, n)
        pair(joins, g8.join(case, L, cols, R, cols, desc[0], desc[0], op, x, y + 1400 + k * 120), case)

# ---------------------------------------------------------------- DB/dialectOrderBy
# The export sorts every table, so an ORDER BY only shows through the rows a max row count keeps. The twins sort (and
# group) a script copy of the column, which keeps them in memory (mergeable="false" does not take a physical table off
# SQL). In memory the max rows apply after the sort (#82389, fixed), so the twins keep the same rows.
#   sortHidden:   CUSTOMERS sorted by the hidden CUSTOMER_ID descending, max 5: the STATE of customers 35-31
#   sortDistinct: distinct STATE sorted descending, max 3: WA, TX, PA
#   sortAggKey:   Count(CUSTOMER_ID) by STATE, sorted by STATE descending (a sort on the grouped table, as the
#                 composer does it), max 3: WA, TX, PA
#   sortAggValue: the same sorted by the count descending, max 1: NJ (6, the only largest)


def with_sort(t, sorts, maxrow):
    """add a sort on the table and a max row count (after grouped())"""
    t = t.replace('hasAggregate="true">', 'hasAggregate="true" maxRow="%d" hasSort="true">' % maxrow, 1)
    return t.replace('<groupInfo>', '<sortInfo>\n<sortInfo>\n%s</sortInfo>\n</sortInfo>\n<groupInfo>' % ''.join(sorts), 1)


orderby = []
for j, n in enumerate(NAMES):
    x, y = 20 + j * 140, 20
    db, cm = tdb(n, 'CUSTOMERS')
    cid, st = cm['id'], cm['state']
    cid_js = hidden_col(expr('CID_JS', FIELD % cid[0], cid[1] or 'double', sql=False))
    st_js = expr('ST_JS', FIELD % st[0], 'string', sql=False)
    orderby.append(sorted_max(physical(db, 'sortHidden_' + n, [hidden_col(ref(cid)), ref(st)], [], x, y),
                              [sortref(ref(cid), True)], 5))
    orderby.append(sorted_max(physical(db, 'sortHiddenMem_' + n, [hidden_col(ref(cid)), ref(st), cid_js], [], x, y + 60),
                              [sortref(cid_js, True)], 5))
    for k, (name, cols, col) in enumerate([('sortDistinct_', [ref(st)], ref(st)),
                                           ('sortDistinctMem_', [hidden_col(ref(st)), st_js], st_js)]):
        t = b.distinct(physical(db, name + n, cols, [], x, y + 120 + k * 60))
        t = t.replace('distinct="true">', 'distinct="true" maxRow="3" hasSort="true">', 1).replace(
            '<conditionAssemblies>', '<sortInfo>\n<sortInfo>\n%s</sortInfo>\n</sortInfo>\n<conditionAssemblies>'
            % sortref(col, True), 1)
        orderby.append(t)
    for k, (case, by_value, maxrow) in enumerate([('sortAggKey', False, 3), ('sortAggValue', True, 1)]):
        for m, mem in enumerate([False, True]):
            g = st_js if mem else ref(st)
            cols = [ref(cid), ref(st)] + ([st_js] if mem else [])
            t = grouped(physical(db, '%s%s_%s' % (case, 'Mem' if mem else '', n), cols, [], x, y + 240 + (k * 2 + m) * 60),
                        g, [('Count', ref(cid))])
            orderby.append(with_sort(t, [sortref(ref(cid) if by_value else g, True)], maxrow))

# ---------------------------------------------------------------- DB/dialectGroupBy
#   grpExpr:       group by the SQL expression LOWER(STATE), Count; the twin groups a script copy of it
#   multiDistinct: two DistinctCount (CUSTOMER_ID, EMPLOYEE_ID) in one query, by DISCOUNT, on the 16 G11 orders; the
#                  twin counts script copies of the two columns
LOWER = {n: 'LOWER(%s)' for n in NAMES}
grp = []
for j, n in enumerate(NAMES):
    x, y = 20 + j * 140, 20
    db, cm = tdb(n, 'CUSTOMERS')
    low = expr('LST', LOWER[n] % (FIELD % cm['state'][0]), 'string')
    low_js = expr('LST_JS', FIELD % 'LST', 'string', sql=False)
    base = [ref(cm['id']), ref(cm['state']), low]
    grp.append(grouped(physical(db, 'grpExpr_' + n, base, [], x, y), low, [('Count', ref(cm['id']))]))
    grp.append(grouped(physical(db, 'grpExprMem_' + n, base + [low_js], [], x, y + 60), low_js, [('Count', ref(cm['id']))]))
    db, om = tdb(n, 'ORDERS')
    sel = cond(ref(om['oid']), om['oid'][1], ONE_OF, g11.ORDER_IDS)
    base = [ref(om['oid']), ref(om['cust']), ref(om['emp']), ref(om['disc'])]
    cust_js = expr('CUST_JS', FIELD % om['cust'][0], om['cust'][1] or 'double', sql=False)
    emp_js = expr('EMP_JS', FIELD % om['emp'][0], om['emp'][1] or 'double', sql=False)
    grp.append(grouped(physical(db, 'multiDistinct_' + n, base, [sel], x, y + 120), ref(om['disc']),
                       [('DistinctCount', ref(om['cust'])), ('DistinctCount', ref(om['emp']))]))
    grp.append(grouped(physical(db, 'multiDistinctMem_' + n, base + [cust_js, emp_js], [sel], x, y + 180), ref(om['disc']),
                       [('DistinctCount', cust_js), ('DistinctCount', emp_js)]))

# ---------------------------------------------------------------- DB/dialectDates
# Every date group level that has an SQL function somewhere in sqlhelper.xml on a TIMESTAMP column
# (PRODUCTS.PRODUCE_DATETIME), and the date levels on a DATE column (ORDERS.ORDER_DATE of the 16 G11 orders; TIMESTAMP in
# PostgreSQL and SQL Server); the group dialog offers no time level for a DATE column. Count.
LEVELS = [(5, 'Year'), (4, 'Quarter'), (3, 'Month'), (2, 'Week'), (1, 'Day'), (8, 'Hour'), (7, 'Minute'), (6, 'Second'),
          (513, 'QuarterOfYear'), (514, 'MonthOfYear'), (515, 'WeekOfYear'), (518, 'DayOfMonth'), (519, 'DayOfWeek'),
          (521, 'HourOfDay'), (522, 'MinuteOfHour'), (523, 'SecondOfMinute')]
g11.DATE_NAMES.update(dict(LEVELS))
dates, stamps, times = [], [], []
for j, n in enumerate(NAMES):
    d = DBS = dict(g11.DBS[[x['name'] for x in g11.DBS].index(n)])
    c = (lambda s: s.upper()) if n in ('Oracle', 'Derby', 'DB2') else (lambda s: s.lower())
    d['P7'] = (d['PRODUCTS'][0], dict(d['PRODUCTS'][1], pdt=(c('produce_datetime'), 'timeInstant', 93)))
    for k, (opt, lname) in enumerate(LEVELS):
        for m, (tbl, col, key, cnd) in enumerate([('ORDERS', 'od', 'date', [(g11.ref(d['ORDERS'][1]['oid']), ONE_OF, g11.ORDER_IDS)]),
                                                 ('P7', 'pdt', 'pdt', [])]):
            if col == 'od' and opt in (8, 7, 6, 521, 522, 523):
                continue
            case = '%s%s_%s' % (col, lname, n)
            cnt = 'oid' if tbl == 'ORDERS' else 'pid'
            y = 20 + (k * 2 + m) * 120
            out = dates if col == 'od' else times if opt in (8, 7, 6, 521, 522, 523) else stamps
            out.append(g11.date_grouped(d, tbl, case, [cnt, key], cnd, [(key, opt)], [('Count', cnt)], 20 + j * 140, y))
            # in memory: grouped on a script copy of the date column (mergeable="false" would still compute the date
            # part in SQL)
            out.append(g11.date_grouped(d, tbl, '%s%sMem_%s' % (col, lname, n), [cnt, key], cnd, [(key, opt)],
                                          [('Count', cnt)], 20 + j * 140, y + 60, mergeable=False))

LEFT_OUT = {}


J2 = ('SQLServer', 'DB2', 'Informix')


def by_db(assemblies, dbs, inside):
    return [a for a in assemblies if (a.split('<name><![CDATA[', 1)[1].split(']]>', 1)[0].rsplit('_', 1)[1] in dbs) == inside]


def keep(assemblies):
    return [a for a in assemblies if a.split('<name><![CDATA[', 1)[1].split(']]>', 1)[0] not in LEFT_OUT]


g8.NOW = b.NOW
sheets = [g8.worksheet('DB/dialectSetOps', 'unionCat_Oracle', keep(setops)),
          # joins in two worksheets (all seven databases took about 3 minutes, as long as a runtime worksheet lives)
          g8.worksheet('DB/dialectJoins', 'inner_Oracle', keep(by_db(joins, J2, False))),
          g8.worksheet('DB/dialectJoins2', 'inner_SQLServer', keep(by_db(joins, J2, True))),
          g8.worksheet('DB/dialectOrderBy', 'sortHidden_Oracle', keep(orderby)),
          g8.worksheet('DB/dialectGroupBy', 'grpExpr_Oracle', keep(grp)),
          g8.worksheet('DB/dialectDates', 'odYear_Oracle', keep(dates)),
          # three worksheets: a runtime worksheet without a heartbeat expires after 3 minutes, so each one must run in
          # less (RuntimeSheet.isTimeout; the test framework sends no heartbeat)
          g8.worksheet('DB/dialectTimestamps', 'pdtYear_Oracle', keep(stamps)),
          g8.worksheet('DB/dialectTimes', 'pdtHour_Oracle', keep(times))]


def main():
    g8.sheets[:] = sheets
    g8.main()


if __name__ == '__main__':
    main()
