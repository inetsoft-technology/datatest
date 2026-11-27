"""Add the pushed / in-memory pair worksheets of ws.md 8c step 5 to the ws storage zip: DB/pairDates1_<db> (TS, DT) and DB/pairDates2_<db> (TM, PDT, PD) for Oracle,
MySQL, Derby, PostgreSQL, SQL Server, DB2 and Informix (used by WSPair_Spec.pairDates; see docs/test-gap-assessment/ws.md,
section 8).

usage: python ws_pair_gen.py <src.zip> <dst.zip>
       src = the output of ws_dialect_gen.py (the step before this one in the zip chain, see the README),
       dst = the zip to write. After replacing ws/src/test/resources/config/wsStorage.zip run ./mvnw clean -pl ws.

Every condition gets two tables on the same rows: <case>_<db>, where the condition is pushed to the database, and
<case>Mem_<db>, where it is ORed with a condition on a hidden script column that is never true, so the whole condition
list stays in memory and is evaluated on the same column (a script copy would change the values: the copy of an Oracle
DATE loses its time). WSPair_Spec compares each pair and writes one summary per database.

Columns (PRODUCTS, one row per PRODUCT_ID):
  TS  a TIMESTAMP made by an SQL CASE on PRODUCT_ID 1-11 (NULL for the others): duplicates, midnight, a leap day,
      milliseconds (.997, .500), the turn of a day and of a year
  DT  a DATE made the same way; on Oracle the DATE keeps a time of day (rows 3, 9, 10)
  TM  a TIME made the same way (no TIME type on Oracle); milliseconds where the database keeps them
  PDT PRODUCE_DATETIME, PD PRODUCE_DATE (the real columns; PRODUCE_DATE is a TIMESTAMP in PostgreSQL and SQL Server)
Operators: =, one of, <, <=, >, >=, between, is null, starting with, each also negated. Condition values have no
milliseconds (the condition dialog has none).
"""
import importlib.util, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('ws_dialect_gen', os.path.join(HERE, 'ws_dialect_gen.py'))
dg = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(dg)
b, g8 = dg.b, dg.g8
ref, expr, physical, tdb, esc = b.ref, b.expr, b.physical, b.tdb, b.esc
NAMES = b.NAMES
EQUAL, ONE_OF, LESS, GREATER, BETWEEN, STARTING_WITH, IS_NULL = 1, 2, 3, 4, 5, 6, 8

TS = {1: '2020-02-29 00:00:00', 2: '2020-02-29 00:00:00', 3: '2020-02-29 13:45:30', 4: '2020-02-29 23:59:59.997',
      5: '2020-03-01 00:00:00', 6: '2020-03-01 00:00:00.500', 7: '2019-12-31 23:59:59', 8: '2021-01-01 12:00:00',
      9: '1999-12-31 23:59:59', 10: '2020-02-28 12:00:00', 11: '2020-03-01 13:45:30'}
# a time of day only on Oracle (DATE keeps it); the other databases store the day
DT = {1: '2020-02-29', 2: '2020-02-29', 3: '2020-02-29 13:45:30', 4: '2020-03-01', 5: '2019-12-31', 6: '2021-01-01',
      7: '1999-12-31', 8: '2020-02-28', 9: '2020-02-29 00:00:01', 10: '2020-03-01 23:59:59', 11: '2020-12-31'}
# Derby and DB2 TIME have no fraction (the literal is cut to seconds)
TM = {1: '00:00:00', 2: '00:00:00', 3: '13:45:30', 4: '23:59:59', 5: '12:00:00', 6: '00:00:00.500', 7: '09:09:09',
      8: '13:45:30.250', 9: '00:00:01', 10: '11:59:59', 11: '12:00:01'}


def ts_lit(n, v):
    return {'Oracle': "TIMESTAMP '%s'", 'MySQL': "CAST('%s' AS DATETIME(3))", 'Derby': "TIMESTAMP('%s')",
            'PostgreSQL': "CAST('%s' AS TIMESTAMP)", 'SQLServer': "CAST('%s' AS DATETIME)", 'DB2': "TIMESTAMP('%s')",
            'Informix': 'DATETIME(%s) YEAR TO FRACTION(3)'}[n] % v


def dt_lit(n, v):
    if n == 'Oracle':
        return "TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS')" % (v if len(v) > 10 else v + ' 00:00:00')
    day = v[:10]
    if n == 'Informix':
        y, m, d = day.split('-')
        return 'MDY(%d, %d, %d)' % (int(m), int(d), int(y))
    return ("DATE('%s')" if n in ('Derby', 'DB2') else "CAST('%s' AS DATE)") % day


def tm_lit(n, v):
    if n in ('Derby', 'DB2'):
        return "TIME('%s')" % v[:8]
    if n == 'Informix':
        return 'DATETIME(%s) HOUR TO FRACTION(3)' % (v if '.' in v else v + '.000')
    return {'MySQL': "CAST('%s' AS TIME(3))", 'PostgreSQL': "CAST('%s' AS TIME)",
            'SQLServer': "CAST('%s' AS TIME(3))"}[n] % v


def case_sql(n, pid, vals, lit):
    return 'CASE' + ''.join(' WHEN %s = %d THEN %s' % (pid, k, lit(n, v)) for k, v in sorted(vals.items())) + ' END'


def cond(r, ctype, op, values, negated=False, equal=False):
    return ('<condition level="0" class="inetsoft.uql.asset.AssetCondition" type="%s" negated="%s" operation="%d" '
            'equal="%s">\n%s%s\n</condition>\n') % (ctype, str(negated).lower(), op, str(equal).lower(), r, ''.join(
                '<condition_data><![CDATA[%s]]></condition_data>' % esc(v) for v in values))


def ts(v):
    return "{ts '%s'}" % v


def d(v):
    return "{d '%s'}" % v


def t(v):
    return "{t '%s'}" % v


# (case suffix, op, values, equal)
OPS = {
    'ts': [('Eq', EQUAL, [ts('2020-02-29 00:00:00')], False),
           ('EqMs', EQUAL, [ts('2020-02-29 23:59:59')], False),
           ('OneOf', ONE_OF, [ts('2020-02-29 00:00:00'), ts('2020-03-01 00:00:00')], False),
           ('Lt', LESS, [ts('2020-03-01 00:00:00')], False),
           ('Le', LESS, [ts('2020-03-01 00:00:00')], True),
           ('Gt', GREATER, [ts('2020-03-01 00:00:00')], False),
           ('Ge', GREATER, [ts('2020-03-01 00:00:00')], True),
           ('Between', BETWEEN, [ts('2020-02-29 00:00:00'), ts('2020-03-01 00:00:00')], False),
           ('Null', IS_NULL, [], False),
           ('Sw', STARTING_WITH, ['2020-02'], False)],
    'dt': [('Eq', EQUAL, [d('2020-02-29')], False),
           ('OneOf', ONE_OF, [d('2020-02-29'), d('2020-03-01')], False),
           ('Lt', LESS, [d('2020-03-01')], False),
           ('Le', LESS, [d('2020-03-01')], True),
           ('Gt', GREATER, [d('2020-03-01')], False),
           ('Ge', GREATER, [d('2020-03-01')], True),
           ('Between', BETWEEN, [d('2020-02-29'), d('2020-03-01')], False),
           ('Null', IS_NULL, [], False),
           ('Sw', STARTING_WITH, ['2020-02'], False)],
    'tm': [('Eq', EQUAL, [t('00:00:00')], False),
           ('OneOf', ONE_OF, [t('00:00:00'), t('13:45:30')], False),
           ('Lt', LESS, [t('12:00:00')], False),
           ('Le', LESS, [t('12:00:00')], True),
           ('Gt', GREATER, [t('12:00:00')], False),
           ('Ge', GREATER, [t('12:00:00')], True),
           ('Between', BETWEEN, [t('00:00:00'), t('12:00:00')], False),
           ('Null', IS_NULL, [], False),
           ('Sw', STARTING_WITH, ['13:45'], False)],
    'pdt': [('Eq', EQUAL, [ts('2001-01-01 01:01:01')], False),
            ('OneOf', ONE_OF, [ts('2001-01-01 01:01:01'), ts('2006-01-01 09:09:09')], False),
            ('Lt', LESS, [ts('2001-01-01 05:17:17')], False),
            ('Le', LESS, [ts('2001-01-01 05:17:17')], True),
            ('Gt', GREATER, [ts('2006-01-01 05:17:17')], False),
            ('Ge', GREATER, [ts('2006-01-01 05:17:17')], True),
            ('Between', BETWEEN, [ts('2001-01-01 00:00:00'), ts('2001-01-01 05:17:17')], False),
            ('Sw', STARTING_WITH, ['2006'], False)],
    'pd': [('Eq', EQUAL, [d('2001-01-01')], False),
           ('OneOf', ONE_OF, [d('2000-12-12'), d('2006-01-01')], False),
           ('Lt', LESS, [d('2001-01-01')], False),
           ('Le', LESS, [d('2001-01-01')], True),
           ('Gt', GREATER, [d('2001-01-01')], False),
           ('Between', BETWEEN, [d('2000-12-12'), d('2001-01-01')], False),
           ('Sw', STARTING_WITH, ['2006'], False)],
}

F_JS = expr('F_JS', '0', 'integer', sql=False, visible=False)
NEVER = cond(F_JS, 'integer', EQUAL, [1])
OR = '<junction junction="1" level="0"/>\n'

sheets = []
for n in NAMES:
    db, pm = tdb(n, 'PRODUCTS')
    pid = pm['pid']
    up = n in ('Oracle', 'Derby', 'DB2')
    pdt_type = 'timeInstant'
    pd_type = 'timeInstant' if n in ('PostgreSQL', 'SQLServer') else 'date'
    cols = {'ts': (expr('TS', case_sql(n, pid[0], TS, ts_lit), 'timeInstant'), 'timeInstant'),
            'dt': (expr('DT', case_sql(n, pid[0], DT, dt_lit), 'date'), 'date'),
            'pdt': (ref(('PRODUCE_DATETIME' if up else 'produce_datetime', pdt_type, 93)), pdt_type),
            'pd': (ref(('PRODUCE_DATE' if up else 'produce_date', pd_type, 93 if pd_type == 'timeInstant' else 91)), pd_type)}
    if n != 'Oracle':
        cols['tm'] = (expr('TM', case_sql(n, pid[0], TM, tm_lit), 'time'), 'time')
    # two worksheets per database: one with every table ran close to the 3 minutes a runtime worksheet lives
    for part, part_cols in ((1, ('ts', 'dt')), (2, ('tm', 'pdt', 'pd'))):
        out = []
        primary = None
        for col in part_cols:
            if col not in cols:
                continue
            r, ctype = cols[col]
            for suffix, op, values, equal in OPS[col]:
                for neg in (False, True):
                    case = '%s%s%s' % (col, suffix, 'Not' if neg else '')
                    c = cond(r, ctype, op, values, neg, equal)
                    y = 20 + len(out) // 2 * 60
                    primary = primary or '%s_%s' % (case, n)
                    out.append(physical(db, '%s_%s' % (case, n), [ref(pid), r], [c], 20, y))
                    out.append(physical(db, '%sMem_%s' % (case, n), [ref(pid), r, F_JS], [c + OR + NEVER], 160, y))
        sheets.append(g8.worksheet('DB/pairDates%d_%s' % (part, n), primary, out))


def main():
    g8.sheets[:] = sheets
    g8.main()


if __name__ == '__main__':
    main()
