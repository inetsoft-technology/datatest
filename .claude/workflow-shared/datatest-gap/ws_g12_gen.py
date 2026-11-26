"""Add the G12 sample worksheets to the ws storage zip: DB/variableTypes, DB/variableTypes2, DB/subqueryConditions, DB/avgInteger,
DB/avgKeepType, DB/relativeDates (used by WSData_Spec.variableTypes, subqueryConditions, avgInteger, relativeDates and
WSProperty_Spec 'property_formula.avg.keep.type'; see docs/test-gap-assessment/ws.md, G12).

usage: python ws_g12_gen.py <src.zip> <dst.zip>
       src = the output of ws_8b_gen.py (the last step before this one in the zip chain, see the README),
       dst = the zip to write. After replacing ws/src/test/resources/config/wsStorage.zip run ./mvnw clean -pl ws.

As in G8 and 8b, a case is built pushed down and in memory (a script copy of the column) on the same data. Tables whose
query fails are left out (LEFT_OUT at the end, with the bug numbers).
"""
import importlib.util, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('ws_8b_gen', os.path.join(HERE, 'ws_8b_gen.py'))
b = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(b)
g8, g11 = b.g8, b.g11
ref, expr, physical, tdb, cond, grouped = b.ref, b.expr, b.physical, b.tdb, b.cond, b.grouped
hidden_table = b.hidden_table
FIELD = "field['%s']"
EQUAL, ONE_OF, LESS, GREATER, STARTING_WITH, IS_NULL, DATE_IN = 1, 2, 3, 4, 6, 8, 11
NAMES = b.NAMES


def var_cond(r, op, name, xtype, ctype=None):
    return ('<condition level="0" class="inetsoft.uql.asset.AssetCondition" type="%s" negated="false" operation="%d" '
            'equal="false">\n%s<condition_data isvariable="true" name="%s" prompt="false" sortValue="true" label="%s" '
            'toolTip="" xtype="%s"/>\n</condition>\n') % (ctype or xtype, op, r, name, name, xtype)


def sub_cond(r, ctype, op, query, sub_col, negated=False, main=None, sub=None):
    corr = '<mainRef>\n%s</mainRef>\n<subRef>\n%s</subRef>\n' % (main, sub) if main is not None else ''
    return ('<condition level="0" class="inetsoft.uql.asset.AssetCondition" type="%s" negated="%s" operation="%d" '
            'equal="false">\n%s<condition_data subQuery="true">\n<subQueryValue>\n<query><![CDATA[%s]]></query>\n%s%s'
            '</subQueryValue>\n</condition_data>\n</condition>\n') % (
        ctype, 'true' if negated else 'false', op, r, query, sub_col, corr)


# ---- DB/variableTypes (G12 item 1): variables of the types the variable dialog offers, values passed by the case
# (vInt 2, vInts 2 and 4, vShort 2, vByte 2, vLong 2, vDbl 2.0, vStr '2', vHalf 2.5, vD15 0.15, vF15 0.15f, vChar 'N',
# vBig 2^53 + 3; vNull has no value). CATEGORY_ID 2: products 9, 10, 11 (3 rows); 2 or 4: 6 rows; HALF = CATEGORY_ID + 0.5.
vt = []
for j, n in enumerate(NAMES):
    x = 20 + j * 140
    db, pm = tdb(n, 'PRODUCTS')
    pid, cat = pm['pid'], pm['cat']
    cat_js = expr('CAT_JS', FIELD % cat[0], cat[1] or 'double', sql=False)
    half = expr('HALF', '%s + 0.5' % (FIELD % cat[0]), 'double')
    half_js = expr('HALF_JS', FIELD % 'HALF', 'double', sql=False)
    for k, (case, op, var, xt, on_half) in enumerate([
            ('intCat', EQUAL, 'vInt', 'integer', False), ('intIn', ONE_OF, 'vInts', 'integer', False),
            ('shortCat', EQUAL, 'vShort', 'short', False), ('byteCat', EQUAL, 'vByte', 'byte', False),
            ('longCat', EQUAL, 'vLong', 'long', False), ('dblCat', EQUAL, 'vDbl', 'double', False),
            ('strCat', EQUAL, 'vStr', 'string', False), ('nullCat', EQUAL, 'vNull', 'integer', False),
            ('intHalfEq', EQUAL, 'vInt', 'integer', True), ('intHalfGt', GREATER, 'vInt', 'integer', True),
            ('dblHalf', EQUAL, 'vHalf', 'double', True)]):
        col, mcol = (half, half_js) if on_half else (ref(cat), cat_js)
        base = [ref(pid), ref(cat)] + ([half] if on_half else [])
        vt.append(physical(db, '%s_%s' % (case, n), base, [var_cond(col, op, var, xt)], x, 20 + k * 120))
        vt.append(physical(db, '%sMem_%s' % (case, n), base + [mcol], [var_cond(mcol, op, var, xt)], x, 80 + k * 120))
    db, om = tdb(n, 'ORDERS')
    oid, disc = om['oid'], om['disc']
    disc_js = expr('DISC_JS', FIELD % disc[0], 'double', sql=False)
    sel = cond(ref(oid), oid[1], ONE_OF, g11.ORDER_IDS)
    for k, (case, var, xt) in enumerate([('dblDisc', 'vD15', 'double'), ('fltDisc', 'vF15', 'float')]):
        vt.append(physical(db, '%s_%s' % (case, n), [ref(oid), ref(disc)], [sel, var_cond(ref(disc), EQUAL, var, xt)],
                           x, 1400 + k * 120))
        vt.append(physical(db, '%sMem_%s' % (case, n), [ref(oid), ref(disc), disc_js],
                           [sel, var_cond(disc_js, EQUAL, var, xt)], x, 1460 + k * 120))
    db, cm = tdb(n, 'CUSTOMERS')
    st = ref(cm['state'])
    st_js = expr('ST_JS', FIELD % cm['state'][0], 'string', sql=False)
    vt.append(physical(db, 'charSw_%s' % n, [ref(cm['id']), st], [var_cond(st, STARTING_WITH, 'vChar', 'char', 'string')],
                       x, 1700))
    vt.append(physical(db, 'charSwMem_%s' % n, [ref(cm['id']), st, st_js],
                       [var_cond(st_js, STARTING_WITH, 'vChar', 'char', 'string')], x, 1760))
    if n != 'Informix':
        big = expr('BIG', b.BIG[n] % (FIELD % cm['id'][0]), 'long')
        big_js = expr('BIG_JS', FIELD % 'BIG', 'long', sql=False)
        vt.append(physical(db, 'longBig_%s' % n, [ref(cm['id']), big], [var_cond(big, EQUAL, 'vBig', 'long')], x, 1820))
        vt.append(physical(db, 'longBigMem_%s' % n, [ref(cm['id']), big, big_js],
                           [var_cond(big_js, EQUAL, 'vBig', 'long')], x, 1880))

# ---- DB/subqueryConditions (G12 item 2) on the 16 G11 orders; DISCOUNT is NULL for 12004, 12307, 12310, 12314, 12319
sq = []
for j, n in enumerate(NAMES):
    x, y = 20 + j * 140, 20
    db, om = tdb(n, 'ORDERS')
    oid, disc, cust = om['oid'], om['disc'], om['cust']
    disc_js = expr('DISC_JS', FIELD % disc[0], 'double', sql=False)
    sel = cond(ref(oid), oid[1], ONE_OF, g11.ORDER_IDS)
    sq.append(hidden_table(physical(db, 'SubAll_' + n, [ref(oid), ref(disc)], [sel], x, y)))
    sq.append(hidden_table(physical(db, 'SubLow_' + n, [ref(oid), ref(disc)], [sel, cond(ref(disc), 'double', LESS, [0.1])],
                                    x, y + 60)))
    sq.append(hidden_table(physical(db, 'SubNull_' + n, [ref(oid), ref(cust), ref(disc)],
                                    [sel, cond(ref(disc), 'double', IS_NULL, [])], x, y + 120)))
    sq.append(hidden_table(physical(db, 'SubOne_' + n, [ref(oid), ref(disc)],
                                    [cond(ref(oid), oid[1], ONE_OF, [12004])], x, y + 180)))
    sq.append(hidden_table(physical(db, 'SubTwo_' + n, [ref(oid), ref(disc)],
                                    [cond(ref(oid), oid[1], ONE_OF, [12416, 12367])], x, y + 240)))
    # in / not in a subquery with NULL (11 / 0 rows), not in one without NULL (DISCOUNT < 0.1: 6 rows)
    for k, (case, sub, op, neg) in enumerate([('in', 'SubAll_', ONE_OF, False), ('notIn', 'SubAll_', ONE_OF, True),
                                              ('notInLow', 'SubLow_', ONE_OF, True), ('gtOne', 'SubOne_', GREATER, False),
                                              ('gtTwo', 'SubTwo_', GREATER, False)]):
        sq.append(physical(db, '%s_%s' % (case, n), [ref(oid), ref(disc)],
                           [sel, sub_cond(ref(disc), 'double', op, sub + n, ref(disc), neg)], x, y + 300 + k * 120))
        sq.append(physical(db, '%sMem_%s' % (case, n), [ref(oid), ref(disc), disc_js],
                           [sel, sub_cond(disc_js, 'double', op, sub + n, ref(disc), neg)], x, y + 360 + k * 120))
    # cross-source: not in a subquery of the next database (in memory): 0 rows (its DISCOUNT has NULL)
    m = NAMES[(j + 1) % len(NAMES)]
    sq.append(physical(db, 'notInX_%s' % n, [ref(oid), ref(disc)],
                       [sel, sub_cond(ref(disc), 'double', ONE_OF, 'SubAll_' + m, ref(tdb(m, 'ORDERS')[1]['disc']), True)],
                       x, y + 900))
    # correlated: customers with (3) / without (32) one of the 16 orders whose DISCOUNT is NULL
    odb = db
    db, cm = tdb(n, 'CUSTOMERS')
    cid = ref(cm['id'])
    cid_js = expr('CID_JS', FIELD % cm['id'][0], cm['id'][1] or 'double', sql=False)
    for k, neg in enumerate([False, True]):
        tag = 'corrNotIn' if neg else 'corrIn'
        sq.append(physical(db, '%s_%s' % (tag, n), [cid, ref(cm['state'])],
                           [sub_cond(cid, cm['id'][1] or 'double', ONE_OF, 'SubNull_' + n, ref(cust), neg, main=cid,
                                     sub=ref(cust))], x, y + 960 + k * 120))
        sq.append(physical(db, '%sMem_%s' % (tag, n), [cid, ref(cm['state']), cid_js],
                           [sub_cond(cid_js, cm['id'][1] or 'double', ONE_OF, 'SubNull_' + n, ref(cust), neg, main=cid,
                                     sub=ref(cust))], x, y + 1020 + k * 120))

# ---- DB/avgInteger, DB/avgKeepType (G12 item 3): Average of IV = CAST(CUSTOMER_ID AS INTEGER) by STATE, pushed and in
# memory. Two worksheets with the same tables, so a result cached by one run is not reused under the other property.
INTC = {n: 'CAST(%s AS INTEGER)' for n in NAMES}
INTC['MySQL'] = 'CAST(%s AS SIGNED)'


def avg_tables():
    out = []
    for j, n in enumerate(NAMES):
        db, cm = tdb(n, 'CUSTOMERS')
        iv = expr('IV', INTC[n] % (FIELD % cm['id'][0]), 'integer')
        iv_js = expr('IV_JS', FIELD % 'IV', 'integer', sql=False)
        base = [ref(cm['id']), ref(cm['state']), iv]
        out.append(grouped(physical(db, 'avg_%s' % n, base, [], 20 + j * 140, 20), ref(cm['state']), [('Average', iv)]))
        out.append(grouped(physical(db, 'avgMem_%s' % n, base + [iv_js], [], 20 + j * 140, 80), ref(cm['state']),
                           [('Average', iv_js)]))
    return out


# ---- DB/relativeDates (G12 item 4): points relative to today (00:00), each condition keeps only the points whose
# membership does not depend on the day of the run. PRODUCT_ID 1..13 picks the point; the date is hidden, the export
# shows PRODUCT_ID and POINT. pg_: computed by PostgreSQL (its session time zone is the JVM's) and pushed down;
# pgMem_: a script copy of the same column; js_: computed by a script from the JVM time (in memory, Derby source).
POINTS = ['today', 'm1d', 'p1d', 'm7d', 'p7d', 'm14d', 'lastMonth15', 'nextMonth15', 'm3Month15', 'm13Month15',
          'lastYearDay', 'nextYearDay', 'lastMonth1']
PG = {'today': 'CURRENT_DATE', 'm1d': 'CURRENT_DATE - 1', 'p1d': 'CURRENT_DATE + 1', 'm7d': 'CURRENT_DATE - 7',
      'p7d': 'CURRENT_DATE + 7', 'm14d': 'CURRENT_DATE - 14'}
for p, months in [('lastMonth15', -1), ('nextMonth15', 1), ('m3Month15', -3), ('m13Month15', -13)]:
    PG[p] = "CAST(date_trunc('month', CURRENT_DATE) + INTERVAL '%d month' + INTERVAL '14 day' AS DATE)" % months
for p, years in [('lastYearDay', -1), ('nextYearDay', 1)]:
    PG[p] = ('make_date(CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS INT) + %d, CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS INT), '
             'LEAST(CAST(EXTRACT(DAY FROM CURRENT_DATE) AS INT), 28))') % years
PG['lastMonth1'] = "CAST(date_trunc('month', CURRENT_DATE) - INTERVAL '1 month' AS DATE)"
JS = {'today': '', 'm1d': 'd.setDate(d.getDate() - 1);', 'p1d': 'd.setDate(d.getDate() + 1);',
      'm7d': 'd.setDate(d.getDate() - 7);', 'p7d': 'd.setDate(d.getDate() + 7);', 'm14d': 'd.setDate(d.getDate() - 14);',
      'lastMonth15': 'd.setDate(15); d.setMonth(d.getMonth() - 1);', 'nextMonth15': 'd.setDate(15); d.setMonth(d.getMonth() + 1);',
      'm3Month15': 'd.setDate(15); d.setMonth(d.getMonth() - 3);', 'm13Month15': 'd.setDate(15); d.setMonth(d.getMonth() - 13);',
      'lastYearDay': 'd.setDate(Math.min(d.getDate(), 28)); d.setFullYear(d.getFullYear() - 1);',
      'nextYearDay': 'd.setDate(Math.min(d.getDate(), 28)); d.setFullYear(d.getFullYear() + 1);',
      'lastMonth1': 'd.setDate(1); d.setMonth(d.getMonth() - 1);'}
# (case, DateCondition class, attributes, label, points kept / expected in)
DCONDS = [
    ('lastYear', 'YearCondition', 'year="1"', 'Last year', ['today', 'lastYearDay', 'nextYearDay'], ['lastYearDay']),
    ('thisYear', 'YearCondition', 'year="0"', 'This year', ['today', 'lastYearDay', 'nextYearDay'], ['today']),
    ('nextYear', 'YearCondition', 'year="-1"', 'Next year', ['today', 'lastYearDay', 'nextYearDay'], ['nextYearDay']),
    ('thisQuarter', 'QuarterCondition', 'year="0" quarter="0"', 'This quarter', ['today', 'm3Month15', 'lastYearDay'], ['today']),
    ('lastQuarter', 'QuarterCondition', 'year="0" quarter="1"', 'Last quarter', ['today', 'm3Month15'], ['m3Month15']),
    ('thisQuarterLastYear', 'QuarterCondition', 'year="1" quarter="0"', 'This quarter last year', ['today', 'lastYearDay'],
     ['lastYearDay']),
    ('thisMonth', 'MonthCondition', 'year="0" month="0"', 'This month', ['today', 'lastMonth15', 'nextMonth15'], ['today']),
    ('lastMonth', 'MonthCondition', 'year="0" month="1"', 'Last month', ['today', 'lastMonth15', 'nextMonth15'], ['lastMonth15']),
    ('nextMonth', 'MonthCondition', 'year="0" month="-1"', 'Next month', ['today', 'lastMonth15', 'nextMonth15'], ['nextMonth15']),
    ('thisMonthLastYear', 'MonthCondition', 'year="1" month="0"', 'This month last year', ['today', 'lastYearDay', 'm13Month15'],
     ['lastYearDay']),
    ('lastMonthLastYear', 'MonthCondition', 'year="1" month="1"', 'Last month last year', ['lastYearDay', 'm13Month15'],
     ['m13Month15']),
    # Last N months is a rolling window (the same day N months ago to today), not whole months
    ('last3Months', 'MonthsCondition', 'from="0" to="4"', 'Last 3 months', ['today', 'lastMonth15', 'm13Month15'],
     ['today', 'lastMonth15']),
    ('last12Months', 'MonthsCondition', 'from="0" to="13"', 'Last 12 months', ['today', 'lastMonth15', 'm13Month15'],
     ['today', 'lastMonth15']),
    ('thisWeek', 'WeekCondition', 'week="0"', 'This week', ['today', 'm7d', 'p7d'], ['today']),
    ('lastWeek', 'WeekCondition', 'week="1"', 'Last week', ['today', 'm7d', 'm14d'], ['m7d']),
    ('nextWeek', 'WeekCondition', 'week="-1"', 'Next week', ['today', 'p7d'], ['p7d']),
    ('weekBeforeLast', 'WeekCondition', 'week="2"', 'Week before last week', ['m7d', 'm14d'], ['m14d']),
    ('last4Weeks', 'WeeksCondition', 'from="0" to="5"', 'Last 4 weeks', ['today', 'm7d', 'm14d', 'p7d'], ['today', 'm7d', 'm14d']),
    ('today', 'DayCondition', 'day="0"', 'Today', ['today', 'm1d', 'p1d'], ['today']),
    ('yesterday', 'DayCondition', 'day="1"', 'Yesterday', ['today', 'm1d', 'p1d'], ['m1d']),
    ('tomorrow', 'DayCondition', 'day="-1"', 'Tomorrow', ['today', 'm1d', 'p1d'], ['p1d']),
    ('yearToDate', 'ToDateCondition', 'year="0" quarter="0" month="0" start="YEAR"', 'Year to date',
     ['today', 'p1d', 'lastYearDay'], ['today']),
    ('yearToDateLastYear', 'ToDateCondition', 'year="1" quarter="0" month="0" start="YEAR"', 'Year to date last year',
     ['today', 'lastYearDay'], ['lastYearDay']),
    ('quarterToDate', 'ToDateCondition', 'year="0" quarter="0" month="0" start="QUARTER"', 'Quarter to date',
     ['today', 'p1d', 'm3Month15'], ['today']),
    ('monthToDate', 'ToDateCondition', 'year="0" quarter="0" month="0" start="MONTH"', 'Month to date',
     ['today', 'p1d', 'lastMonth15'], ['today']),
    ('monthToDateLastMonth', 'ToDateCondition', 'year="0" quarter="0" month="1" start="MONTH"', 'Month to date last month',
     ['today', 'lastMonth1'], ['lastMonth1'])]


def date_cond(col, cls, attrs, label):
    return ('<condition level="0" class="inetsoft.uql.asset.DateCondition$%s" type="date" negated="false" operation="%d" '
            'equal="false" %s>\n%s<name><![CDATA[%s]]></name>\n<label><![CDATA[%s]]></label>\n</condition>\n') % (
        cls, DATE_IN, attrs, col, label, label)


rd = []
point_name = "%s[%s - 1]" % (repr(POINTS).replace("'", '"'), FIELD)
for src, n in [('pg', 'PostgreSQL'), ('js', 'Derby')]:
    db, pm = tdb(n, 'PRODUCTS')
    pid = pm['pid']
    pname = expr('POINT', point_name % pid[0], 'string', sql=False)
    if src == 'pg':
        pt = expr('PT', 'CASE %s %s END' % (FIELD % pid[0], ' '.join('WHEN %d THEN %s' % (i + 1, PG[p])
                                                                       for i, p in enumerate(POINTS))), 'date', visible=False)
    else:
        pt = expr('PT', 'var d = new Date(); d.setHours(0, 0, 0, 0); switch(%s) { %s } d' % (
            FIELD % pid[0], ' '.join('case %d: %s break;' % (i + 1, JS[p]) for i, p in enumerate(POINTS))),
            'date', sql=False, visible=False)
    pt_js = expr('PT_JS', FIELD % 'PT', 'date', sql=False, visible=False)
    for k, (case, cls, attrs, label, keep, _) in enumerate(DCONDS):
        sel = (ref(pid), pid[1], ONE_OF, [POINTS.index(p) + 1 for p in keep])
        x, y = 20 + k * 140, 20 if src == 'pg' else 260
        rd.append(physical(db, '%s_%s' % (src, case), [ref(pid), pname, pt], [sel, date_cond(pt, cls, attrs, label)], x, y))
        if src == 'pg':
            rd.append(physical(db, 'pgMem_%s' % case, [ref(pid), pname, pt, pt_js], [sel, date_cond(pt_js, cls, attrs, label)],
                               x, y + 60))

# left out: the query fails today
LEFT_OUT = {
    # '>' against a subquery of two rows: a scalar subquery error on every database (#82411; in memory kept)
    **{'gtTwo_%s' % n: '#82411' for n in NAMES},
}


def keep(assemblies):
    return [a for a in assemblies if a.split('<name><![CDATA[', 1)[1].split(']]>', 1)[0] not in LEFT_OUT]


g8.NOW = b.NOW
# two worksheets: a runtime worksheet without a heartbeat expires after 3 minutes, and all seven databases in one took
# about that long
VT2 = ('SQLServer', 'DB2', 'Informix')


def by_db(assemblies, dbs, inside):
    return [a for a in assemblies if (a.split('<name><![CDATA[', 1)[1].split(']]>', 1)[0].rsplit('_', 1)[1] in dbs) == inside]


sheets = [g8.worksheet('DB/variableTypes', 'intCat_Oracle', keep(by_db(vt, VT2, False))),
          g8.worksheet('DB/variableTypes2', 'intCat_SQLServer', keep(by_db(vt, VT2, True))),
          g8.worksheet('DB/subqueryConditions', 'in_Oracle', keep(sq)),
          g8.worksheet('DB/avgInteger', 'avg_Oracle', avg_tables()),
          g8.worksheet('DB/avgKeepType', 'avg_Oracle', avg_tables()),
          g8.worksheet('DB/relativeDates', 'pg_today', keep(rd))]


def main():
    g8.sheets[:] = sheets
    g8.main()


if __name__ == '__main__':
    main()
