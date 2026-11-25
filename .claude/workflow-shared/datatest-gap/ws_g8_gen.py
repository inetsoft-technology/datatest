"""Add the G8 sample worksheets to the ws storage zip: DB/nonEquiJoin and DB/stringCompare (used by WSData_Spec
nonEquiJoin, stringCompare; see docs/test-gap-assessment/ws.md, G8).

usage: python ws_g8_gen.py <src.zip> <dst.zip>
       src = wsStorage.zip without these worksheets (git show a8742b46:ws/src/test/resources/config/wsStorage.zip),
       dst = the zip to write. After replacing
       ws/src/test/resources/config/wsStorage.zip run ./mvnw clean -pl ws.

Every case is built twice on the same data: once so that it is pushed down to the database, once so that it runs in
memory. A join runs in memory when one side has a script expression column (<t>m tables, PID_JS = field['PRODUCT_ID']);
a condition runs in memory when it is on a script expression copy of the column (NAME_JS = field['PRODUCT_NAME']).
The plan files (WorksheetTest.exportMergePlan) show which path each table took.

PRODUCTS is the same in every database (checked over JDBC). PRICE: product 4 and 18 75, 21 and 22 40, 13 35, 16 29,
20 79. PRODUCT_NAME: 23 '17 Inch LCD', 24 '19 inch LCD', 25 'NetStorage'.
"""
import sys, zipfile, hashlib, json, time
from datetime import datetime, timezone

# name, data source, PRODUCTS source, properties, upper-case names, id (dataType, sqlType), price sqlType,
# CAST to a BIGINT, CAST to a string, string concatenation
DBS = []
for name, prefix, prod, props, upper, id_t, price_sql, big, pstr, concat in [
    ('Oracle', 'Oracle', 'ORDERS.PRODUCTS', [('__schema__', 'ORDERS'), ('__table_type__', 'TABLE')], True, ('double', 2), 2,
     'CAST(%s AS NUMBER(19))', 'CAST(%s AS VARCHAR2(10))', '%s || %s'),
    ('MySQL', 'MySQL', 'orders.products', [('__table_type__', 'TABLE'), ('__catalog__', 'orders')], False, ('integer', -6), 3,
     'CAST(%s AS SIGNED)', 'CAST(%s AS CHAR(10))', 'CONCAT(%s, %s)'),
    ('Derby', 'Derby', 'APP.PRODUCTS', [('__schema__', 'APP'), ('__table_type__', 'TABLE')], True, ('double', 3), 3,
     'CAST(%s AS BIGINT)', 'TRIM(CAST(%s AS CHAR(10)))', '%s || %s'),
    ('PostgreSQL', 'PostgreSQL', 'ORDERS.products', [('__schema__', 'ORDERS'), ('__table_type__', 'TABLE')], False, ('double', 2), 2,
     'CAST(%s AS BIGINT)', 'CAST(%s AS VARCHAR(10))', '%s || %s'),
    ('SQLServer', 'SQL Server', 'master.orders.products',
     [('__schema__', 'orders'), ('__table_type__', 'TABLE'), ('__catalog__', 'master')], False, ('double', 2), 3,
     'CAST(%s AS BIGINT)', 'CAST(%s AS VARCHAR(10))', '%s + %s'),
    ('DB2', 'DB2', 'ORDERS.PRODUCTS', [('__schema__', 'ORDERS'), ('__table_type__', 'TABLE')], True, ('integer', 4), 3,
     'CAST(%s AS BIGINT)', 'CAST(%s AS VARCHAR(10))', '%s || %s'),
]:
    c = (lambda s: s.upper()) if upper else (lambda s: s.lower())
    DBS.append({'name': name, 'prefix': prefix, 'source': prod, 'props': props, 'big': big, 'pstr': pstr, 'concat': concat,
                'pid': (c('product_id'), id_t[0], id_t[1]), 'price': (c('price'), 'double', price_sql),
                'pname': (c('product_name'), None, 12)})

EQUAL, STARTING_WITH, CONTAINS, LIKE, ONE_OF = 1, 6, 7, 13, 2
GE = 35
FIELD = "field['%s']"


def ref(col, entity=None, alias=None):
    attr, dtype, sqltype = col
    dt = ' dataType="%s"' % dtype if dtype else ''
    ent = ' entity="%s"' % entity if entity else ''
    view = '%s.%s' % (entity, attr) if entity else attr
    al = '<alias><![CDATA[%s]]></alias>\n' % alias if alias else ''
    return ('<dataRef class="inetsoft.uql.asset.ColumnRef" %s sqlType="12"><dataRef class="inetsoft.uql.erm.AttributeRef"%s '
            'attribute="%s" sqlType="%d" ><view><![CDATA[%s]]></view>\n</dataRef>%s</dataRef>') % (dt, ent, attr, sqltype, view, al)


def expr(name, text, dtype, sql=True):
    return ('<dataRef class="inetsoft.uql.asset.ColumnRef" %s dataType="%s" sqlType="12"><dataRef class="inetsoft.uql.erm.ExpressionRef" '
            'name="%s" refType="0" ><![CDATA[%s]]>\n<view><![CDATA[%s]]></view>\n</dataRef></dataRef>') % (
        '' if sql else ' sql="false"', dtype, name, text, name)


def selections(refs):
    return ('<normalColumnSelection>\n<ColumnSelection>\n%s</ColumnSelection>\n</normalColumnSelection>\n'
            '<crosstabColumnSelection>\n<ColumnSelection>\n%s<property><name><![CDATA[null]]></name>'
            '<value><![CDATA[false]]></value></property>\n</ColumnSelection>\n</crosstabColumnSelection>\n') % (refs, refs)


def esc(v):
    return ''.join('~_%02x_~' % ord(ch) for ch in str(v))


def conditions(items):
    """items: [(column ref xml, condition type, op, values)], joined with AND"""
    out = []
    for i, (r, ctype, op, values) in enumerate(items):
        if i:
            out.append('<junction junction="0" level="0"/>\n')
        out.append('<condition level="0" class="inetsoft.uql.asset.AssetCondition" type="%s" negated="false" '
                   'operation="%d" equal="false">\n%s%s\n</condition>\n' % (
                       ctype, op, r, ''.join('<condition_data><![CDATA[%s]]></condition_data>' % esc(v) for v in values)))
    return '<preConditions>\n<conditions>\n%s</conditions>\n</preConditions>\n' % ''.join(out)


def physical(db, name, refs, cond, x, y):
    p = ''.join('<property>\n<key><![CDATA[%s]]></key><value><![CDATA[%s]]></value></property>\n' % kv for kv in db['props'])
    return ('<assembly class="inetsoft.uql.asset.PhysicalBoundTableAssembly">\n'
            '<assemblyInfo class="inetsoft.uql.asset.internal.BoundTableAssemblyInfo" pixelOffX="%d" pixelOffY="%d" '
            'pixelWidth="100" pixelHeight="60" hasCondition="true" hasExpression="true">\n'
            '<name><![CDATA[%s]]></name><class><![CDATA[inetsoft.uql.asset.PhysicalBoundTableAssembly]]></class>\n%s'
            '<source>\n<sourceInfo class="inetsoft.uql.asset.SourceInfo" type="8">\n<prefix><![CDATA[%s]]></prefix>\n'
            '<source><![CDATA[%s]]></source>\n<properties>\n%s</properties>\n</sourceInfo>\n</source>\n'
            '</assemblyInfo>%s<mvForceAppendUpdates>false</mvForceAppendUpdates>\n<conditionAssemblies>\n'
            '</conditionAssemblies>\n</assembly>') % (x, y, name, selections(''.join(refs)), db['prefix'], db['source'], p,
                                                     conditions(cond))


def schema_infos(left, right):
    return ('<schemaTableInfos>\n'
            '<schemaTableInfo>\n<tablename><![CDATA[%s]]></tablename>\n<info left="0.000000" top="0.000000" width="150.000000"></info>\n</schemaTableInfo>\n'
            '<schemaTableInfo>\n<tablename><![CDATA[%s]]></tablename>\n<info left="225.000000" top="0.000000" width="150.000000"></info>\n</schemaTableInfo>\n'
            '</schemaTableInfos>\n') % (left, right)


def join(name, left, lcols, right, rcols, lkey, rkey, op, x, y):
    """lcols / rcols: [(column name, dataType)] of the two tables; right columns named like a left one get _1"""
    lnames = set(n for n, _ in lcols)
    refs = ''.join(ref((n, t, 12), left) for n, t in lcols)
    refs += ''.join(ref((n, t, 12), right, n + '_1' if n in lnames else None) for n, t in rcols)
    lt, rt = dict(lcols), dict(rcols)
    return ('<assembly class="inetsoft.uql.asset.RelationalJoinTableAssembly">\n'
            '<assemblyInfo class="inetsoft.uql.asset.internal.CompositeTableAssemblyInfo" pixelOffX="%d" pixelOffY="%d" '
            'pixelWidth="100" pixelHeight="60" hierarchical="false">\n'
            '<name><![CDATA[%s]]></name><class><![CDATA[inetsoft.uql.asset.RelationalJoinTableAssembly]]></class>\n%s'
            '<operators>\n<pairOperator>\n<pair>\n<leftTableAssembly><![CDATA[%s]]></leftTableAssembly>\n'
            '<rightTableAssembly><![CDATA[%s]]></rightTableAssembly>\n</pair>\n<tableAssemblyOperator>\n'
            '<operator operation="%d" distinct="false" leftTable="%s" rightTable="%s">\n'
            '<leftColumn>\n%s</leftColumn>\n<rightColumn>\n%s</rightColumn>\n</operator>\n'
            '</tableAssemblyOperator>\n</pairOperator>\n</operators>\n%s</assemblyInfo><mvForceAppendUpdates>false</mvForceAppendUpdates>\n<tables>\n'
            '<oneTableAssembly><![CDATA[%s]]></oneTableAssembly>\n<oneTableAssembly><![CDATA[%s]]></oneTableAssembly>\n'
            '</tables>\n</assembly>') % (x, y, name, selections(refs), left, right, op, left, right,
                                          ref((lkey, lt[lkey], 12)), ref((rkey, rt[rkey], 12)), schema_infos(left, right),
                                          left, right)


# ---------------------------------------------------------------- DB/nonEquiJoin
# <db>_L: PRODUCTS 4, 21, 13, 16 (PRICE 75, 40, 35, 29); <db>_R: 18, 22, 20 (PRICE 75, 40, 79).
# Extra SQL expression columns: L.BIG = PRICE cast to BIGINT, L.LOW and R.LOW = LOWER(PRODUCT_NAME),
# R.HALF = PRICE + 0.5, R.PSTR = PRICE cast to a string. <db>_Lm, <db>_Rm add the script column PID_JS, so every
# join of them runs in memory. Joins (<db>_<case> pushed, <db>_<case>Mem in memory):
#   ge:      L.PRICE >= R.PRICE           ties are kept: (4, 18), (4, 22), (21, 22)
#   bigHalf: L.BIG >= R.HALF              a whole number against x.5: only (4, 22) (75 >= 40.5)
#   caseCmp: L.PRODUCT_NAME >= R.LOW      upper-case names against lower-case ones: binary order says none,
#                                         case-insensitive order says 6
#   strNum (memory only): L.PRICE >= R.PSTR   a number against a string holding a number
nej = []
for i, db in enumerate(DBS):
    n, y = db['name'], 20 + i * 240
    pid, price, pname = db['pid'], db['price'], db['pname']
    big = expr('BIG', db['big'] % (FIELD % price[0]), 'long')
    llow = expr('LOW', 'LOWER(%s)' % (FIELD % pname[0]), 'string')
    half = expr('HALF', '%s + 0.5' % (FIELD % price[0]), 'double')
    pstr = expr('PSTR', db['pstr'] % (FIELD % price[0]), 'string')
    js = expr('PID_JS', FIELD % pid[0], pid[1], sql=False)
    lcond = [(ref(pid), pid[1], ONE_OF, [4, 21, 13, 16])]
    rcond = [(ref(pid), pid[1], ONE_OF, [18, 22, 20])]
    lrefs = [ref(pid), ref(price), ref(pname), big, llow]
    rrefs = [ref(pid), ref(price), ref(pname), half, pstr, llow]
    lcols = [(pid[0], pid[1]), (price[0], 'double'), (pname[0], None), ('BIG', 'long'), ('LOW', 'string')]
    rcols = [(pid[0], pid[1]), (price[0], 'double'), (pname[0], None), ('HALF', 'double'), ('PSTR', 'string'),
             ('LOW', 'string')]
    L, R, Lm, Rm = n + '_L', n + '_R', n + '_Lm', n + '_Rm'
    nej += [physical(db, L, lrefs, lcond, 20, y), physical(db, R, rrefs, rcond, 20, y + 120),
            physical(db, Lm, lrefs + [js], lcond, 160, y), physical(db, Rm, rrefs + [js], rcond, 160, y + 120)]
    lcolsm, rcolsm = lcols + [('PID_JS', pid[1])], rcols + [('PID_JS', pid[1])]
    x = 300
    for case, lk, rk in [('ge', price[0], price[0]), ('bigHalf', 'BIG', 'HALF'), ('caseCmp', pname[0], 'LOW')]:
        nej += [join(n + '_' + case, L, lcols, R, rcols, lk, rk, GE, x, y),
                join(n + '_' + case + 'Mem', Lm, lcolsm, Rm, rcolsm, lk, rk, GE, x, y + 120)]
        x += 140
    nej.append(join(n + '_strNumMem', Lm, lcolsm, Rm, rcolsm, price[0], 'PSTR', GE, x, y + 120))

# ---------------------------------------------------------------- DB/stringCompare
# PRODUCTS with one string condition each. <db>_<case> puts it on PRODUCT_NAME (pushed down), <db>_<case>Mem on the
# script expression copy NAME_JS (in memory, same data). PAD = PRODUCT_NAME with one trailing space (SQL expression),
# PAD_JS its script copy.
#   eq:    PRODUCT_NAME = 'netstorage'           case
#   sw:    starting with 'net'                   case
#   ct:    contains 'inch'                       case: '19 inch LCD', '17 Inch LCD'
#   like:  like '%lcd'                           case
#   padEq: PAD = 'NetStorage'                    trailing space
STRING_CASES = [('eq', EQUAL, 'netstorage', False), ('sw', STARTING_WITH, 'net', False), ('ct', CONTAINS, 'inch', False),
                ('like', LIKE, '%lcd', False), ('padEq', EQUAL, 'NetStorage', True)]
strc = []
for i, db in enumerate(DBS):
    n, y = db['name'], 20 + i * 240
    pid, pname = db['pid'], db['pname']
    pad = expr('PAD', db['concat'] % (FIELD % pname[0], "' '"), 'string')
    name_js = expr('NAME_JS', FIELD % pname[0], 'string', sql=False)
    pad_js = expr('PAD_JS', "%s + ' '" % (FIELD % pname[0]), 'string', sql=False)
    for j, (case, op, value, padded) in enumerate(STRING_CASES):
        if padded:
            strc += [physical(db, '%s_%s' % (n, case), [ref(pid), ref(pname), pad], [(pad, 'string', op, [value])], 20 + j * 140, y),
                     physical(db, '%s_%sMem' % (n, case), [ref(pid), ref(pname), pad_js], [(pad_js, 'string', op, [value])],
                              20 + j * 140, y + 120)]
        else:
            strc += [physical(db, '%s_%s' % (n, case), [ref(pid), ref(pname)], [(ref(pname), 'string', op, [value])],
                              20 + j * 140, y),
                     physical(db, '%s_%sMem' % (n, case), [ref(pid), ref(pname), name_js],
                              [(name_js, 'string', op, [value])], 20 + j * 140, y + 120)]

# fixed timestamp so that regenerating gives the same blobs
NOW = int(datetime(2026, 11, 25, tzinfo=timezone.utc).timestamp() * 1000)


def worksheet(path, primary, assemblies):
    key = '1^2^__NULL__^%s^host-org' % path
    xml = ('<?xml version="1.0" encoding="UTF-8"?>\n<?inetsoft-asset classname="inetsoft.uql.asset.Worksheet" identifier="%s"?>\n'
           '<worksheet modified="%d" modifiedBy="admin" createdBy="admin" created="%d" class="inetsoft.uql.asset.Worksheet" offline="false">\n'
           '<Version>14.0</Version>\n<worksheetInfo>\n<worksheetInfo class="inetsoft.uql.asset.WorksheetInfo"  inputmax="50000" '
           'messageLevels="Error,Warning,Info" worksheetMode="2">\n</worksheetInfo></worksheetInfo>\n'
           '<primary><![CDATA[%s]]></primary>\n<assemblies>\n%s\n</assemblies>\n</worksheet>\n') % (
        key, NOW, NOW, primary, '\n'.join('<oneAssembly>%s</oneAssembly>' % a for a in assemblies))
    return path, key, xml.encode('utf-8'), len(assemblies)


sheets = [worksheet('DB/nonEquiJoin', 'Oracle_ge', nej), worksheet('DB/stringCompare', 'Oracle_eq', strc)]

src, dst = sys.argv[1], sys.argv[2]
zin = zipfile.ZipFile(src)
raw = zin.read('blob-index.json').decode('utf-8')
idx = json.loads(raw)
assert json.dumps(idx, separators=(',', ':'), ensure_ascii=False) == raw
sp = idx['host-org__indexedStorage']
stamp = datetime.fromtimestamp(NOW / 1000, timezone.utc).strftime('%Y-%m-%dT%H:%M:%S.') + '%03dZ' % (NOW % 1000)
blobs = []
for path, key, data, count in sheets:
    assert key not in sp
    dg = hashlib.md5(data).hexdigest()
    sp[key] = {'path': key, 'digest': dg, 'length': len(data), 'lastModified': stamp,
               'metadata': {'@class': 'inetsoft.util.BlobIndexedStorage' + '$' + 'Metadata', 'className': 'inetsoft.uql.asset.Worksheet',
                            'identifier': key, 'folder': None}}
    sp['1^1^__NULL__^DB^host-org']['metadata']['folder']['entries'].append(
        {'scope': 1, 'type': 'WORKSHEET', 'user': None, 'path': path, 'alias': None, 'favoritesUser': '', 'identifier': key,
         'description': 'Data Worksheet/' + path, 'folder': False, 'createdUsername': 'admin', 'createdDate': NOW,
         'modifiedUsername': 'admin', 'modifiedDate': NOW, 'properties': {'isEdit': 'false', 'worksheet.type': '4'}})
    blobs.append(('blobs/%s/%s' % (dg[:2], dg[2:]), data))
    print('%s: %d tables, digest %s, %d bytes' % (path, count, dg, len(data)))
zout = zipfile.ZipFile(dst, 'w', zipfile.ZIP_DEFLATED)
for info in zin.infolist():
    payload = json.dumps(idx, separators=(',', ':'), ensure_ascii=False).encode('utf-8') if info.filename == 'blob-index.json' else zin.read(info.filename)
    zout.writestr(info, payload, compress_type=zipfile.ZIP_DEFLATED)
for name, data in blobs:
    zout.writestr(zipfile.ZipInfo(name, date_time=time.gmtime(NOW / 1000)[:6]), data, compress_type=zipfile.ZIP_DEFLATED)
zout.close()
