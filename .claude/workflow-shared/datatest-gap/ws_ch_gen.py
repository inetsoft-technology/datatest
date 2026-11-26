"""Add the ClickHouse sample worksheet DB/clickhouseSamples to the ws storage zip (used by WSData_Spec.clickhouseSamples;
see docs/test-gap-assessment/ws.md, G8 / G11). The ClickHouse test server has no ORDERS / PRODUCTS, so the cases of
G8 and G11 that differ on ClickHouse are built on default.base_type (20 rows, fixed data).

usage: python ws_ch_gen.py <src.zip> <dst.zip>
       src = the output of ws_g8_gen.py; then run ws_ds_password.py on dst. Chain: ws_g11_gen.py -> ws_g8_gen.py ->
       ws_ch_gen.py -> ws_ds_password.py.

The table XML is written with the helpers of ws_g8_gen.py and ws_g11_gen.py (imported; their zip writing runs only
when they are run as scripts), so it is the same as in the other samples. Each case is built twice where it matters: pushed to ClickHouse, and in memory (a script
expression copy of the column, or a script column in the tables of a join / union).

default.base_type: id 1-20 (Int32); string_col lower-case words (1 hello, 2 world, 3 click, 4 house, 5 data, 9 insert,
16 alter, ...); float_col Float32 with repeats (0.866 for 9, 13, 20; 1.414 for 3, 14; 1.732 for 4, 12; 2.718 for 2, 15);
decimal_col Decimal(18,4) (1 123.456, 3 345.678, 4 901.234, 5 567.89, 9 234.567, ...); date_col 2025-01-01 ... 2026-08-08;
boolean_col true for odd id.
"""
import importlib.util, os, sys, zipfile, hashlib, json, time
from datetime import datetime, timezone

HERE = os.path.dirname(os.path.abspath(__file__))


def load(name):
    """import a sibling generator as a module (its zip writing only runs as a script)"""
    spec = importlib.util.spec_from_file_location(name[:-3], os.path.join(HERE, name))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return vars(module)


g8 = load('ws_g8_gen.py')
g11 = load('ws_g11_gen.py')
ref8, expr, physical, join8 = g8['ref'], g8['expr'], g8['physical'], g8['join']
EQUAL, STARTING_WITH, CONTAINS, LIKE, ONE_OF, IS_NULL, GE = 1, 6, 7, 13, 2, 8, 35
UNION, MINUS = 1536, 4608
FIELD = "field['%s']"

PROPS = [('__query_folder__', 'default'), ('__table_type__', 'TABLE'), ('__catalog__', 'default')]
COLS = {'id': ('id', 'integer', 4), 's': ('string_col', None, 12), 'f': ('float_col', 'double', 7),
        'd': ('decimal_col', 'double', 3), 'dt': ('date_col', 'date', 91), 'b': ('boolean_col', 'boolean', 16)}
# the same table for both helper sets: ws_g8_gen's physical(db, ...) and ws_g11_gen's functions taking (db, 'T', ...)
DB = {'name': 'CH', 'prefix': 'Clickhouse', 'source': 'default.base_type', 'props': PROPS,
      'T': ('default.base_type', COLS)}
ID, S, F, D, DT, B = (COLS[k] for k in ('id', 's', 'f', 'd', 'dt', 'b'))

out = []
y = 20

# ---- string conditions: pushed (on the column or a SQL expression) and in memory (a script copy)
# UP = upper(string_col): ClickHouse compares case-sensitively, memory does not (string.compare.casesensitive=false)
up = expr('UP', 'upper(%s)' % (FIELD % S[0]), 'string')
up_js = expr('UP_JS', "%s.toUpperCase()" % (FIELD % S[0]), 'string', sql=False)
s_js = expr('S_JS', FIELD % S[0], 'string', sql=False)
pad = expr('PAD', "concat(%s, ' ')" % (FIELD % S[0]), 'string')
pad_js = expr('PAD_JS', "%s + ' '" % (FIELD % S[0]), 'string', sql=False)
empty = expr('EMPTY', "if(%s < 4, '', %s)" % (FIELD % ID[0], FIELD % S[0]), 'string')
empty_js = expr('EMPTY_JS', "%s < 4 ? '' : %s" % (FIELD % ID[0], FIELD % S[0]), 'string', sql=False)
STRING_CASES = [
    # name, pushed refs/column, memory refs/column, op, value
    ('eqUp', up, up_js, EQUAL, 'hello'),          # case: ClickHouse none, memory 1
    ('swUp', up, up_js, STARTING_WITH, 'he'),
    ('ctUp', up, up_js, CONTAINS, 'ell'),
    ('likeUp', up, up_js, LIKE, '%llo'),
    ('swUnder', None, s_js, STARTING_WITH, 'hel_'),   # no ESCAPE on ClickHouse: _ is a wildcard there
    ('ctPercent', None, s_js, CONTAINS, 'l%o'),       # % too: '%l%o%' matches hello
    ('likeBs', None, s_js, LIKE, 'hel\\lo'),          # backslash: the LIKE escape of ClickHouse
    ('likeBr', None, s_js, LIKE, 'h[e]llo'),          # no character class in ClickHouse
    ('padEq', pad, pad_js, EQUAL, 'hello'),           # trailing space kept by ClickHouse and memory
    ('nullEmpty', empty, empty_js, IS_NULL, None),    # String is not Nullable: '' is not NULL there; memory: null
]
for j, (case, pushed, mem, op, value) in enumerate(STRING_CASES):
    values = [] if value is None else [value]
    pcol = pushed if pushed is not None else ref8(S)
    prefs = [ref8(ID), ref8(S)] + ([pushed] if pushed is not None else [])
    out += [physical(DB, 'CH_' + case, prefs, [(pcol, 'string', op, values)], 20 + j * 140, y),
            physical(DB, 'CH_' + case + 'Mem', [ref8(ID), ref8(S), mem], [(mem, 'string', op, values)], 20 + j * 140,
                     y + 120)]
# text of other types, in memory only (ClickHouse has no LIKE on a number or a date: Illegal type)
y += 240
for j, (case, col, value) in enumerate([('swNumMem', ID, '2'), ('ctDecMem', D, '.4'), ('swDateMem', DT, '2026')]):
    js = expr(col[0] + '_JS', FIELD % col[0], col[1], sql=False)
    out.append(physical(DB, 'CH_' + case, [ref8(ID), ref8(col) if col is not ID else '', js],
                        [(js, col[1], STARTING_WITH if case != 'ctDecMem' else CONTAINS, [value])], 20 + j * 140, y))

# ---- >= joins (G8 nonEquiJoin): L = id 1, 3, 5 (decimal 123.456, 345.678, 567.89), R = id 1, 9, 4 (123.456, 234.567,
# 901.234). BIG = toInt64(decimal_col), HALF = toFloat64(decimal_col) + 0.5, PSTR = toString(decimal_col),
# RUP = upper(string_col) on R.
y += 240
big = expr('BIG', 'toInt64(%s)' % (FIELD % D[0]), 'long')
# ClickHouse does not add a Float64 literal to a Decimal: toFloat64 first
half = expr('HALF', 'toFloat64(%s) + 0.5' % (FIELD % D[0]), 'double')
pstr = expr('PSTR', 'toString(%s)' % (FIELD % D[0]), 'string')
rup = expr('RUP', 'upper(%s)' % (FIELD % S[0]), 'string')
js = expr('ID_JS', FIELD % ID[0], 'integer', sql=False)
lcond = [(ref8(ID), 'integer', ONE_OF, [1, 3, 5])]
rcond = [(ref8(ID), 'integer', ONE_OF, [1, 9, 4])]
lrefs = [ref8(ID), ref8(S), ref8(D), big]
rrefs = [ref8(ID), ref8(S), ref8(D), half, pstr, rup]
lcols = [('id', 'integer'), ('string_col', None), ('decimal_col', 'double'), ('BIG', 'long')]
rcols = [('id', 'integer'), ('string_col', None), ('decimal_col', 'double'), ('HALF', 'double'), ('PSTR', 'string'),
         ('RUP', 'string')]
lm, rm = lcols + [('ID_JS', 'integer')], rcols + [('ID_JS', 'integer')]
out += [physical(DB, 'CH_L', lrefs, lcond, 20, y), physical(DB, 'CH_R', rrefs, rcond, 20, y + 120),
        physical(DB, 'CH_Lm', lrefs + [js], lcond, 160, y), physical(DB, 'CH_Rm', rrefs + [js], rcond, 160, y + 120)]
x = 300
for case, lk, rk in [('ge', 'decimal_col', 'decimal_col'), ('bigHalf', 'BIG', 'HALF'), ('caseCmp', 'string_col', 'RUP')]:
    out += [join8('CH_' + case, 'CH_L', lcols, 'CH_R', rcols, lk, rk, GE, x, y),
            join8('CH_' + case + 'Mem', 'CH_Lm', lm, 'CH_Rm', rm, lk, rk, GE, x, y + 120)]
    x += 140
out.append(join8('CH_strNumMem', 'CH_Lm', lm, 'CH_Rm', rm, 'decimal_col', 'PSTR', GE, x, y + 120))

# ---- full outer join (G11): PA = id 1, 2, 3, PB = id 2, 3, 4, on id. ClickHouse fills the side without a match with
# default values (join_use_nulls = 0): 0 and '' instead of NULL. <..>Mem: both sides have a script column.
y += 240
pa_cond = [(g11['ref'](ID), ONE_OF, [1, 2, 3])]
pb_cond = [(g11['ref'](ID), ONE_OF, [2, 3, 4])]


def ptable11(name, keys, cond, x, y, extra=''):
    return g11['physical'](DB, 'T', name, keys, [c for c in cond], x, y).replace(
        '</ColumnSelection>\n</normalColumnSelection>', extra + '</ColumnSelection>\n</normalColumnSelection>', 1)


id_js11 = ('<dataRef class="inetsoft.uql.asset.ColumnRef"  sql="false" dataType="integer" sqlType="12">'
           '<dataRef class="inetsoft.uql.erm.ExpressionRef" name="ID_JS" refType="0" ><![CDATA[field[\'id\']]]>\n'
           '<view><![CDATA[ID_JS]]></view>\n</dataRef></dataRef>')
out += [ptable11('CH_PA', ['id', 's'], pa_cond, 20, y), ptable11('CH_PB', ['id', 's'], pb_cond, 160, y),
        ptable11('CH_PAm', ['id', 's'], pa_cond, 20, y + 120, id_js11),
        ptable11('CH_PBm', ['id', 's'], pb_cond, 160, y + 120, id_js11),
        g11['join'](DB, 'T', 'CH_fullJoin', 'CH_PA', 'CH_PB', ['id', 's'], ['id', 's'], 'id', None, 300, y),
        g11['join'](DB, 'T', 'CH_fullJoinMem', 'CH_PAm', 'CH_PBm', ['id', 's'], ['id', 's'], 'id', None, 300, y + 120)]

# ---- union / minus (G11): A = id 2, 3, 4, 9, B = id 13, 14, 15, 20, float_col only. Duplicates across and inside the
# sides: union (distinct) = 0.866, 1.414, 1.732, 2.718; minus = 1.732. ClickHouse 22.1 needs UNION ALL / DISTINCT.
y += 240
a_cond = [(g11['ref'](ID), ONE_OF, [2, 3, 4, 9])]
b_cond = [(g11['ref'](ID), ONE_OF, [13, 14, 15, 20])]
f_js11 = ('<dataRef class="inetsoft.uql.asset.ColumnRef"  sql="false" dataType="double" sqlType="12">'
          '<dataRef class="inetsoft.uql.erm.ExpressionRef" name="F_JS" refType="0" ><![CDATA[field[\'float_col\']]]>\n'
          '<view><![CDATA[F_JS]]></view>\n</dataRef></dataRef>')
# the condition column must be in the table: id is there, hidden
hid = g11['ref'](ID).replace('<dataRef class="inetsoft.uql.asset.ColumnRef" ',
                             '<dataRef class="inetsoft.uql.asset.ColumnRef"  visible="false"', 1)
out += [ptable11('CH_A', ['f'], a_cond, 20, y, hid), ptable11('CH_B', ['f'], b_cond, 160, y, hid),
        ptable11('CH_Am', ['f'], a_cond, 20, y + 120, hid + f_js11),
        ptable11('CH_Bm', ['f'], b_cond, 160, y + 120, hid + f_js11),
        g11['concat'](DB, 'T', 'CH_union', 'CH_A', 'CH_B', ['f'], UNION, 300, y),
        g11['concat'](DB, 'T', 'CH_minus', 'CH_A', 'CH_B', ['f'], MINUS, 440, y)]
# the memory union / minus: the script column F_JS (same value as float_col) keeps duplicate rows duplicate
mcols_xml = g11['ref'](F, 'CH_Am') + g11['ref'](('F_JS', 'double', 12), 'CH_Am')
for name, op, x in [('CH_unionMem', UNION, 300), ('CH_minusMem', MINUS, 440)]:
    t = g11['concat'](DB, 'T', name, 'CH_Am', 'CH_Bm', ['f'], op, x, y + 120)
    t = t.replace(g11['selections'](g11['ref'](F, 'CH_Am')), g11['selections'](mcols_xml))
    out.append(t)

# ---- statistics (G11): grouped by boolean_col (10 rows each), on float_col; second column decimal_col
y += 240
BASIC = [('Sum', 'f', None), ('Average', 'f', None), ('Count', 'f', None), ('DistinctCount', 'f', None),
         ('Min', 'f', None), ('Max', 'f', None)]
VAR = [('Variance', 'f', None), ('StandardDeviation', 'f', None), ('PopulationVariance', 'f', None),
       ('PopulationStandardDeviation', 'f', None)]
CORR = [('Covariance', 'f', 'd'), ('Correlation', 'f', 'd')]
MEM = [('Median', 'f', None), ('Mode', 'f', None)]
cols = ['id', 'b', 'f', 'd']
for j, (case, aggs) in enumerate([('basic', BASIC), ('var', VAR), ('corr', CORR), ('mem', BASIC + VAR + CORR + MEM)]):
    out.append(g11['grouped'](DB, 'T', 'CH_' + case, cols, [], 'b', aggs, 20 + j * 140, y))

# ---- date grouping (G11): date_col by WeekOfYear, DayOfWeek, Quarter plus id; NoSQL on a script copy (memory)
y += 240
for j, (case, option) in enumerate([('weekOfYear', 515), ('dayOfWeek', 519), ('quarter', 4)]):
    for k, mergeable in enumerate([True, False]):
        out.append(g11['date_grouped'](DB, 'T', 'CH_%s%s' % (case, '' if mergeable else 'NoSQL'), ['id', 'dt'], [],
                                       [('dt', option), ('id', None)], [('Count', 'id')], 20 + j * 140, y + k * 120,
                                       mergeable=mergeable))

# ---- max rows (G11): top = id descending, max 5 (20..16); distinct = float_col descending, max 3 (3.141, 3.14,
# 2.718); each joined with the whole table on its key so its SQL becomes a subquery wrapped by the limit
y += 240
out += [g11['ptable'](DB, 'T', 'CH_C', ['id', 's'], 20, y + 120),
        g11['ptable'](DB, 'T', 'CH_top', ['id', 's'], 20, y, sort=[('id', True)], maxrow=5),
        g11['ptable'](DB, 'T', 'CH_distinct', ['f'], 160, y, sort=[('f', True)], distinct=True, maxrow=3),
        g11['inner_join'](DB, 'CH_topJoin', 'CH_top', ['id', 's'], 'T', 'CH_C', ['id', 's'], 'T', 'id', 'id', 160, y + 120)]

# left out (their export would be the placeholder row of a failed query, or vary from run to run):
#   CH_corr: Covariance / Correlation SQL adds Float64 and Decimal ("0.0E0 + decimal_col"), rejected (#82307)
# (CH_union, #82306, and CH_topJoin, #82290, were left out until those were fixed)
LEFT_OUT = ('CH_corr',)
out = [a for a in out if not any('<name><![CDATA[%s]]>' % n in a for n in LEFT_OUT)]

# fixed timestamp so that regenerating gives the same blobs
NOW = int(datetime(2026, 11, 25, tzinfo=timezone.utc).timestamp() * 1000)
path = 'DB/clickhouseSamples'
key = '1^2^__NULL__^%s^host-org' % path
xml = ('<?xml version="1.0" encoding="UTF-8"?>\n<?inetsoft-asset classname="inetsoft.uql.asset.Worksheet" identifier="%s"?>\n'
       '<worksheet modified="%d" modifiedBy="admin" createdBy="admin" created="%d" class="inetsoft.uql.asset.Worksheet" offline="false">\n'
       '<Version>14.0</Version>\n<worksheetInfo>\n<worksheetInfo class="inetsoft.uql.asset.WorksheetInfo"  inputmax="50000" '
       'messageLevels="Error,Warning,Info" worksheetMode="2">\n</worksheetInfo></worksheetInfo>\n'
       '<primary><![CDATA[CH_eqUp]]></primary>\n<assemblies>\n%s\n</assemblies>\n</worksheet>\n') % (
    key, NOW, NOW, '\n'.join('<oneAssembly>%s</oneAssembly>' % a for a in out))
data = xml.encode('utf-8')

src, dst = sys.argv[1], sys.argv[2]
zin = zipfile.ZipFile(src)
raw = zin.read('blob-index.json').decode('utf-8')
idx = json.loads(raw)
assert json.dumps(idx, separators=(',', ':'), ensure_ascii=False) == raw
sp = idx['host-org__indexedStorage']
assert key not in sp
stamp = datetime.fromtimestamp(NOW / 1000, timezone.utc).strftime('%Y-%m-%dT%H:%M:%S.') + '%03dZ' % (NOW % 1000)
dg = hashlib.md5(data).hexdigest()
sp[key] = {'path': key, 'digest': dg, 'length': len(data), 'lastModified': stamp,
           'metadata': {'@class': 'inetsoft.util.BlobIndexedStorage' + '$' + 'Metadata', 'className': 'inetsoft.uql.asset.Worksheet',
                        'identifier': key, 'folder': None}}
sp['1^1^__NULL__^DB^host-org']['metadata']['folder']['entries'].append(
    {'scope': 1, 'type': 'WORKSHEET', 'user': None, 'path': path, 'alias': None, 'favoritesUser': '', 'identifier': key,
     'description': 'Data Worksheet/' + path, 'folder': False, 'createdUsername': 'admin', 'createdDate': NOW,
     'modifiedUsername': 'admin', 'modifiedDate': NOW, 'properties': {'isEdit': 'false', 'worksheet.type': '4'}})
zout = zipfile.ZipFile(dst, 'w', zipfile.ZIP_DEFLATED)
for info in zin.infolist():
    payload = json.dumps(idx, separators=(',', ':'), ensure_ascii=False).encode('utf-8') if info.filename == 'blob-index.json' else zin.read(info.filename)
    zout.writestr(info, payload, compress_type=zipfile.ZIP_DEFLATED)
zout.writestr(zipfile.ZipInfo('blobs/%s/%s' % (dg[:2], dg[2:]), date_time=time.gmtime(NOW / 1000)[:6]), data,
              compress_type=zipfile.ZIP_DEFLATED)
zout.close()
print('%s: %d tables, digest %s, %d bytes' % (path, len(out), dg, len(data)))
