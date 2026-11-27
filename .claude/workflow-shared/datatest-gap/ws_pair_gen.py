"""Add the pushed / in-memory pair worksheets of ws.md 8c step 5 to the ws storage zip, for Oracle, MySQL, Derby,
PostgreSQL, SQL Server, DB2 and Informix (used by WSPair_Spec; see docs/test-gap-assessment/ws.md, section 8):
DB/pairDates1_<db> (TS, DT), DB/pairDates2_<db> (TM, PDT, PD), DB/pairNumbers1_<db> (INT, DEC, CAT) and
DB/pairNumbers2_<db> (BIG, RL).

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
  INT an INTEGER made the same way: 0, negatives, duplicates, +-2147483647 (-2^31 is NULL in an Informix INTEGER)
  DEC a DECIMAL(12,4): 2.5 and 2.5000, 1 against 1.0000, 0.1, -0.0001, 12345678.9999
  CAT CATEGORY_ID (the real column: double on Oracle, Derby, PostgreSQL, SQL Server, integer elsewhere)
  BIG a BIGINT (NUMBER(19) on Oracle) around 2^53 and 2^63 - 1
  RL  a REAL (as in ws_8b_gen.py: BINARY_FLOAT, SMALLFLOAT; DOUBLE on MySQL): 0.5 exact, 0.1 and 0.15 not, 1E10
Operators: =, one of, <, <=, >, >=, between, is null, starting with (one number column), each also negated.
Condition values have no milliseconds (the condition dialog has none).
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

# ---------------------------------------------------------------- numbers (batch 2)
P53 = 2 ** 53
INT = {1: 0, 2: 0, 3: -5, 4: 7, 5: 100, 6: 2147483647, 7: -2147483647, 8: 7, 9: 1, 10: -1, 11: 99}
DEC = {1: '2.5', 2: '2.5000', 3: '0.1', 4: '-0.0001', 5: '12345678.9999', 6: '0', 7: '1', 8: '2.4999', 9: '2.5001',
       10: '-2.5', 11: '100'}
BIG = {1: P53, 2: P53, 3: P53 + 1, 4: P53 + 2, 5: P53 + 3, 6: -(P53 + 1), 7: 0, 8: 1, 9: 2 ** 63 - 1, 10: -1,
       11: P53 - 1}
RL = {1: '0.1', 2: '0.1', 3: '0.15', 4: '0.5', 5: '1', 6: '-0.1', 7: '2.718', 8: '100.25', 9: '0', 10: '0.3333333',
      11: '1E10'}


def int_lit(n, v):
    return ('CAST(%s AS SIGNED)' if n == 'MySQL' else 'CAST(%s AS INTEGER)') % v


def dec_lit(n, v):
    return ('CAST(%s AS NUMBER(12, 4))' if n == 'Oracle' else 'CAST(%s AS DECIMAL(12, 4))') % v


def big_lit(n, v):
    return {'Oracle': 'CAST(%s AS NUMBER(19))', 'MySQL': 'CAST(%s AS SIGNED)'}.get(n, 'CAST(%s AS BIGINT)') % v


def real_lit(n, v):
    return b.REAL[n] % v


OPS.update({
    'int': [('Eq', EQUAL, [7], False),
            ('EqMax', EQUAL, [2147483647], False),
            ('OneOf', ONE_OF, [0, 7, 99], False),
            ('Lt', LESS, [0], False),
            ('Le', LESS, [0], True),
            ('Gt', GREATER, [7], False),
            ('Ge', GREATER, [7], True),
            ('Between', BETWEEN, [-5, 7], False),
            ('Null', IS_NULL, [], False),
            ('Sw', STARTING_WITH, ['1'], False)],
    'dec': [('Eq', EQUAL, ['2.5'], False),
            ('EqOne', EQUAL, ['1'], False),
            ('EqBig', EQUAL, ['12345678.9999'], False),
            ('OneOf', ONE_OF, ['0.1', '2.5'], False),
            ('Lt', LESS, ['2.5'], False),
            ('Le', LESS, ['2.5'], True),
            ('Gt', GREATER, ['2.5'], False),
            ('Ge', GREATER, ['2.5'], True),
            ('Between', BETWEEN, ['-2.5', '2.5'], False),
            ('Null', IS_NULL, [], False)],
    'cat': [('Eq', EQUAL, [3], False),
            ('OneOf', ONE_OF, [1, 3], False),
            ('Lt', LESS, [3], False),
            ('Le', LESS, [3], True),
            ('Gt', GREATER, [3], False),
            ('Ge', GREATER, [3], True),
            ('Between', BETWEEN, [2, 4], False),
            ('Sw', STARTING_WITH, ['1'], False)],
    'big': [('Eq', EQUAL, [P53 + 1], False),
            ('EqMax', EQUAL, [2 ** 63 - 1], False),
            ('OneOf', ONE_OF, [P53, P53 + 3], False),
            ('Lt', LESS, [P53 + 1], False),
            ('Le', LESS, [P53 + 1], True),
            ('Gt', GREATER, [P53 + 1], False),
            ('Ge', GREATER, [P53 + 1], True),
            ('Between', BETWEEN, [P53, P53 + 2], False),
            ('Null', IS_NULL, [], False)],
    'rl': [('Eq', EQUAL, ['0.5'], False),
           ('EqInexact', EQUAL, ['0.1'], False),
           ('OneOf', ONE_OF, ['0.15', '2.718'], False),
           ('Lt', LESS, ['0.5'], False),
           ('Le', LESS, ['0.5'], True),
           ('Gt', GREATER, ['0.1'], False),
           ('Ge', GREATER, ['0.1'], True),
           ('Between', BETWEEN, ['0.1', '1'], False),
           ('Null', IS_NULL, [], False)],
})

F_JS = expr('F_JS', '0', 'integer', sql=False, visible=False)
NEVER = cond(F_JS, 'integer', EQUAL, [1])
OR = '<junction junction="1" level="0"/>\n'

sheets = []


def add_sheets(name, n, db, pid, cols, parts):
    """one worksheet DB/<name><part>_<n> per part: each must run in less than the 3 minutes a runtime worksheet
    lives without a heartbeat"""
    for part, part_cols in enumerate(parts, 1):
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
        sheets.append(g8.worksheet('DB/%s%d_%s' % (name, part, n), primary, out))


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
    add_sheets('pairDates', n, db, pid, cols, [('ts', 'dt'), ('tm', 'pdt', 'pd')])

for n in NAMES:
    db, pm = tdb(n, 'PRODUCTS')
    pid = pm['pid']
    cols = {'int': (expr('INT', case_sql(n, pid[0], INT, int_lit), 'integer'), 'integer'),
            'dec': (expr('DEC', case_sql(n, pid[0], DEC, dec_lit), 'double'), 'double'),
            'cat': (ref(pm['cat']), pm['cat'][1]),
            'big': (expr('BIG', case_sql(n, pid[0], BIG, big_lit), 'long'), 'long'),
            'rl': (expr('RL', case_sql(n, pid[0], RL, real_lit), 'float'), 'float')}
    add_sheets('pairNumbers', n, db, pid, cols, [('int', 'dec', 'cat'), ('big', 'rl')])


def main():
    g8.sheets[:] = sheets
    g8.main()


if __name__ == '__main__':
    main()
