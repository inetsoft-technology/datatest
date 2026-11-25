"""Add the G11 cross-database sample worksheets to the ws storage zip: DB/setOperation, DB/fullOuterJoin,
DB/statAggregate, DB/dateGroup and DB/maxRows (used by WSData_Spec setOperation, fullOuterJoin, statAggregate,
dateGroup, maxRows; see docs/test-gap-assessment/ws.md, G11).

usage: python ws_g11_gen.py <src.zip> <dst.zip>
       src = wsStorage.zip without these worksheets (git show 0a2ddb4d:ws/src/test/resources/config/wsStorage.zip),
       dst = the zip to write. After replacing ws/src/test/resources/config/wsStorage.zip run ./mvnw clean -pl ws.

The worksheet XML is written the way the composer stores it (checked against existing assets; a second aggregate
of one column uses an expression column <col>_<n> = field['<col>'] like AggregateDialogService). Only the new
worksheets and their blob-index entries (and the DB folder entries) are added; every other zip entry is copied.
The timestamp is fixed, so running it again gives the same blobs.

Same data in every database (checked over JDBC): CUSTOMERS (35 rows, no NULLs), PRODUCTS (25 rows,
DESCRIPTION NULL for products 1, 15, 16; other columns differ), ORDERS (700 rows, DISCOUNT (2,2) with 69 NULLs;
DB2 has one more row, 12101, not among the picked orders).
"""
import sys, zipfile, hashlib, json, time
from datetime import datetime, timezone

# per database: table name prefix, data source, and per source table: source, properties, columns
# column: (name in the table, ColumnRef dataType or None, AttributeRef sqlType)
DBS = []
for name, prefix, cust, prod, ords, props, upper, num_type, num_sql, char_sql, dec_sql in [
    ('Oracle', 'Oracle', 'ORDERS.CUSTOMERS', 'ORDERS.PRODUCTS', 'ORDERS.ORDERS',
     [('__schema__', 'ORDERS'), ('__table_type__', 'TABLE')], True, 'double', 2, 12, 2),
    ('MySQL', 'MySQL', 'orders.customers', 'orders.products', 'orders.orders',
     [('__table_type__', 'TABLE'), ('__catalog__', 'orders')], False, 'integer', -6, 1, 3),
    ('Derby', 'Derby', 'APP.CUSTOMERS', 'APP.PRODUCTS', 'APP.ORDERS',
     [('__schema__', 'APP'), ('__table_type__', 'TABLE')], True, 'double', 3, 1, 3),
    ('PostgreSQL', 'PostgreSQL', 'ORDERS.customers', 'ORDERS.products', 'ORDERS.orders',
     [('__schema__', 'ORDERS'), ('__table_type__', 'TABLE')], False, 'double', 2, 12, 2),
    ('SQLServer', 'SQL Server', 'master.orders.customers', 'master.orders.products', 'master.orders.orders',
     [('__schema__', 'orders'), ('__table_type__', 'TABLE'), ('__catalog__', 'master')], False, 'double', 2, 1, 3),
    # DB2 in DB/statAggregate, DB/dateGroup and DB/maxRows (not in setOperation, fullOuterJoin): INTEGER id columns (AVG of INTEGER is INTEGER in DB2), DISCOUNT DECIMAL(3,2);
    # its ORDERS has one more row (12101) than the others, not among the picked orders
    ('DB2', 'DB2', 'ORDERS.CUSTOMERS', 'ORDERS.PRODUCTS', 'ORDERS.ORDERS',
     [('__schema__', 'ORDERS'), ('__table_type__', 'TABLE')], True, 'integer', 4, 12, 3),
    # Informix in all five worksheets, appended last so the other databases' tables and positions stay as they were.
    # Its data can only be read through the worksheet path (a direct JDBC read needs org.bson, which the driver plugin
    # does not ship), so its results are checked against the other databases' instead of a JDBC read.
    ('Informix', 'Informix', 'customers', 'products', 'orders',
     [('__schema__', 'informix'), ('__table_type__', 'TABLE')], False, 'integer', 4, 1, 3),
]:
    c = (lambda s: s.upper()) if upper else (lambda s: s.lower())
    DBS.append({
        'name': name, 'prefix': prefix, 'props': props, 'stat_only': name == 'DB2',
        'CUSTOMERS': (cust, {'id': (c('customer_id'), num_type, num_sql), 'state': (c('state'), None, char_sql)}),
        # STATE is VARCHAR in Oracle and PostgreSQL, CHAR(2) elsewhere; DESCRIPTION is VARCHAR everywhere
        'PRODUCTS': (prod, {'pid': (c('product_id'), num_type, num_sql), 'cat': (c('category_id'), num_type, num_sql),
                            'desc': (c('description'), None, 12)}),
        # ORDER_ID is INT in MySQL; DISCOUNT is NUMBER/DECIMAL(2,2) everywhere
        'ORDERS': (ords, {'oid': (c('order_id'), num_type, 4 if name == 'MySQL' else num_sql),
                          'cust': (c('customer_id'), num_type, num_sql), 'emp': (c('employee_id'), num_type, num_sql),
                          'disc': (c('discount'), 'double', dec_sql),
                          # DATE in Oracle, MySQL, Derby, DB2; timestamp in PostgreSQL, datetime in SQL Server
                          'date': (c('order_date'), 'timeInstant' if name in ('PostgreSQL', 'SQLServer') else 'date',
                                   93 if name in ('PostgreSQL', 'SQLServer') else 91)}),
    })

ONE_OF, IS_NULL = 2, 8
UNION, MINUS, FULL_JOIN = 1536, 4608, 15
AND, OR = 0, 1


def ref(col, entity=None, alias=None):
    attr, dtype, sqltype = col
    dt = ' dataType="%s"' % dtype if dtype else ''
    ent = ' entity="%s"' % entity if entity else ''
    view = '%s.%s' % (entity, attr) if entity else attr
    al = '<alias><![CDATA[%s]]></alias>\n' % alias if alias else ''
    return ('<dataRef class="inetsoft.uql.asset.ColumnRef" %s sqlType="12"><dataRef class="inetsoft.uql.erm.AttributeRef"%s '
            'attribute="%s" sqlType="%d" ><view><![CDATA[%s]]></view>\n</dataRef>%s</dataRef>') % (dt, ent, attr, sqltype, view, al)


def selections(refs):
    return ('<normalColumnSelection>\n<ColumnSelection>\n%s</ColumnSelection>\n</normalColumnSelection>\n'
            '<crosstabColumnSelection>\n<ColumnSelection>\n%s<property><name><![CDATA[null]]></name>'
            '<value><![CDATA[false]]></value></property>\n</ColumnSelection>\n</crosstabColumnSelection>\n') % (refs, refs)


def esc(v):
    return ''.join('~_%02x_~' % ord(ch) for ch in str(v))


def conditions(items):
    """items: list of (column ref xml, op, values) joined by the junctions between them: [c1, junction, c2, ...]"""
    out = []
    for it in items:
        if isinstance(it, int):
            out.append('<junction junction="%d" level="0"/>\n' % it)
            continue
        r, op, values = it
        dtype = 'string' if 'dataType=' not in r.split('><')[0] else r.split('dataType="')[1].split('"')[0]
        out.append('<condition level="0" class="inetsoft.uql.asset.AssetCondition" type="%s" negated="false" '
                   'operation="%d" equal="false">\n%s%s\n</condition>\n' % (
                       dtype, op, r, ''.join('<condition_data><![CDATA[%s]]></condition_data>' % esc(v) for v in values)))
    return '<preConditions>\n<conditions>\n%s</conditions>\n</preConditions>\n' % ''.join(out)


def physical(db, table, name, cols, cond, x, y):
    source, colmap = db[table]
    refs = ''.join(ref(colmap[k]) for k in cols)
    p = ''.join('<property>\n<key><![CDATA[%s]]></key><value><![CDATA[%s]]></value></property>\n' % kv for kv in db['props'])
    return ('<assembly class="inetsoft.uql.asset.PhysicalBoundTableAssembly">\n'
            '<assemblyInfo class="inetsoft.uql.asset.internal.BoundTableAssemblyInfo" pixelOffX="%d" pixelOffY="%d" '
            'pixelWidth="100" pixelHeight="60" hasCondition="true">\n'
            '<name><![CDATA[%s]]></name><class><![CDATA[inetsoft.uql.asset.PhysicalBoundTableAssembly]]></class>\n%s'
            '<source>\n<sourceInfo class="inetsoft.uql.asset.SourceInfo" type="8">\n<prefix><![CDATA[%s]]></prefix>\n'
            '<source><![CDATA[%s]]></source>\n<properties>\n%s</properties>\n</sourceInfo>\n</source>\n'
            '</assemblyInfo>%s<mvForceAppendUpdates>false</mvForceAppendUpdates>\n<conditionAssemblies>\n'
            '</conditionAssemblies>\n</assembly>') % (x, y, name, selections(refs), db['prefix'], source, p, conditions(cond))


def grouped(db, table, name, cols, cond, group, aggs, x, y):
    """physical table with one group column and aggregates; aggs: (formula, column key, secondary column key or None)"""
    source, colmap = db[table]
    refs = ''.join(ref(colmap[k]) for k in cols)
    # as the composer's aggregate dialog does (AggregateDialogService.updateAggregateInfo): the first aggregate of a
    # column uses the column, every further one gets a new SQL expression column <col>_<n> = field['<col>']
    used, agg_refs = {}, []
    for f, col, sec in aggs:
        attr, dtype, _ = colmap[col]
        k = used.get(col, 0)
        used[col] = k + 1
        if k == 0:
            agg_refs.append(ref(colmap[col]))
            continue
        copy = '%s_%d' % (attr, k)
        e = ('<dataRef class="inetsoft.uql.asset.ColumnRef"  dataType="%s" sqlType="12"><dataRef class="inetsoft.uql.erm.ExpressionRef" '
             'name="%s" refType="0" ><![CDATA[field[\'%s\']]]>\n<view><![CDATA[%s]]></view>\n</dataRef></dataRef>') % (dtype, copy, attr, copy)
        refs += e
        agg_refs.append(e)
    p = ''.join('<property>\n<key><![CDATA[%s]]></key><value><![CDATA[%s]]></value></property>\n' % kv for kv in db['props'])
    g = ('<dataRef class="inetsoft.uql.asset.GroupRef"  timeSeries="false" >%s<groupSort sort="1" interval="1.0" option="1" '
         'others="1" sortByCol="0" info="false" cinfo="false"><otherLabel><![CDATA[Others]]></otherLabel>\n'
         '<manualOrderList class="inetsoft.util.ItemList"></manualOrderList></groupSort>\n</dataRef>') % ref(colmap[group])
    a = ''.join('<dataRef class="inetsoft.uql.asset.AggregateRef"  n="1">%s%s<formula><![CDATA[%s]]></formula>\n</dataRef>' % (
        r, '<secondaryColumn>\n%s</secondaryColumn>\n' % ref(colmap[sec]) if sec else '', f)
        for r, (f, col, sec) in zip(agg_refs, aggs))
    return ('<assembly class="inetsoft.uql.asset.PhysicalBoundTableAssembly">\n'
            '<assemblyInfo class="inetsoft.uql.asset.internal.BoundTableAssemblyInfo" pixelOffX="%d" pixelOffY="%d" '
            'pixelWidth="100" pixelHeight="60" aggregate="true" hasAggregate="true" hasCondition="true">\n'
            '<name><![CDATA[%s]]></name><class><![CDATA[inetsoft.uql.asset.PhysicalBoundTableAssembly]]></class>\n%s'
            '<source>\n<sourceInfo class="inetsoft.uql.asset.SourceInfo" type="8">\n<prefix><![CDATA[%s]]></prefix>\n'
            '<source><![CDATA[%s]]></source>\n<properties>\n%s</properties>\n</sourceInfo>\n</source>\n'
            '</assemblyInfo>%s<mvForceAppendUpdates>false</mvForceAppendUpdates>\n'
            '<groupInfo>\n<groupInfo crosstab="false">\n<groups>\n%s</groups>\n<aggregates>\n%s</aggregates>\n'
            '</groupInfo>\n</groupInfo>\n<conditionAssemblies>\n</conditionAssemblies>\n</assembly>') % (
        x, y, name, selections(refs), db['prefix'], source, p, conditions(cond), g, a)


DATE_NAMES = {5: 'Year', 4: 'Quarter', 3: 'Month', 2: 'Week', 1: 'Day', 513: 'QuarterOfYear', 514: 'MonthOfYear',
              515: 'WeekOfYear', 518: 'DayOfMonth', 519: 'DayOfWeek'}


def date_ref(col, option):
    """the ColumnRef of a date grouping, as the composer creates it (DateRangeRef, autoCreate)"""
    attr, dtype, sqltype = col
    rtype = 'integer' if option & 512 else dtype
    return ('<dataRef class="inetsoft.uql.asset.ColumnRef"  dataType="%s" sqlType="12"><dataRef class="inetsoft.uql.asset.DateRangeRef"  '
            'dateOption="%d" originalType="%s" autoCreate="true"><![CDATA[ ]]>\n<dataRef class="inetsoft.uql.erm.AttributeRef" '
            'attribute="%s" sqlType="%d" ><view><![CDATA[%s]]></view>\n</dataRef><attribute><![CDATA[%s(%s)]]></attribute>\n'
            '</dataRef></dataRef>') % (rtype, option, dtype, attr, sqltype, attr, DATE_NAMES[option], attr)


def date_grouped(db, table, name, cols, cond, groups, aggs, x, y, mergeable=True):
    """physical table grouped by groups = [(column key, date option or None)], aggs = [(formula, column key)];
    mergeable=False groups a script expression copy of the date column instead, so the date part is computed in memory"""
    source, colmap = db[table]
    refs = ''.join(ref(colmap[k]) for k in cols)
    grefs = []
    for k, option in groups:
        if option and not mergeable:
            # group a script expression column that copies the date: the date part can only be computed in memory
            attr, dtype, _ = colmap[k]
            js = attr + '_JS'
            expr = ('<dataRef class="inetsoft.uql.erm.ExpressionRef" name="%s" refType="0" ><![CDATA[field[\'%s\']]]>\n'
                    '<view><![CDATA[%s]]></view>\n</dataRef>') % (js, attr, js)
            refs += '<dataRef class="inetsoft.uql.asset.ColumnRef"  sql="false" dataType="%s" sqlType="12">%s</dataRef>' % (dtype, expr)
            r = ('<dataRef class="inetsoft.uql.asset.ColumnRef"  dataType="%s" sqlType="12"><dataRef class="inetsoft.uql.asset.DateRangeRef"  '
                 'dateOption="%d" originalType="%s" autoCreate="true"><![CDATA[ ]]>\n%s<attribute><![CDATA[%s(%s)]]></attribute>\n'
                 '</dataRef></dataRef>') % ('integer' if option & 512 else dtype, option, dtype, expr, DATE_NAMES[option], js)
            refs += r
            grefs.append('<dataRef class="inetsoft.uql.asset.GroupRef"  timeSeries="false"  dateGroup="%d" >%s</dataRef>' % (option, r))
        elif option:
            r = date_ref(colmap[k], option)
            refs += r
            grefs.append('<dataRef class="inetsoft.uql.asset.GroupRef"  timeSeries="false"  dateGroup="%d" >%s</dataRef>' % (option, r))
        else:
            grefs.append(('<dataRef class="inetsoft.uql.asset.GroupRef"  timeSeries="false" >%s<groupSort sort="1" interval="1.0" '
                          'option="1" others="1" sortByCol="0" info="false" cinfo="false"><otherLabel><![CDATA[Others]]></otherLabel>\n'
                          '<manualOrderList class="inetsoft.util.ItemList"></manualOrderList></groupSort>\n</dataRef>') % ref(colmap[k]))
    a = ''.join('<dataRef class="inetsoft.uql.asset.AggregateRef"  n="1">%s<formula><![CDATA[%s]]></formula>\n</dataRef>' % (
        ref(colmap[col]), f) for f, col in aggs)
    p = ''.join('<property>\n<key><![CDATA[%s]]></key><value><![CDATA[%s]]></value></property>\n' % kv for kv in db['props'])
    return ('<assembly class="inetsoft.uql.asset.PhysicalBoundTableAssembly">\n'
            '<assemblyInfo class="inetsoft.uql.asset.internal.BoundTableAssemblyInfo" pixelOffX="%d" pixelOffY="%d" '
            'pixelWidth="100" pixelHeight="60" aggregate="true"%s hasAggregate="true" hasCondition="true">\n'
            '<name><![CDATA[%s]]></name><class><![CDATA[inetsoft.uql.asset.PhysicalBoundTableAssembly]]></class>\n%s'
            '<source>\n<sourceInfo class="inetsoft.uql.asset.SourceInfo" type="8">\n<prefix><![CDATA[%s]]></prefix>\n'
            '<source><![CDATA[%s]]></source>\n<properties>\n%s</properties>\n</sourceInfo>\n</source>\n'
            '</assemblyInfo>%s<mvForceAppendUpdates>false</mvForceAppendUpdates>\n'
            '<groupInfo>\n<groupInfo crosstab="false">\n<groups>\n%s</groups>\n<aggregates>\n%s</aggregates>\n'
            '</groupInfo>\n</groupInfo>\n<conditionAssemblies>\n</conditionAssemblies>\n</assembly>') % (
        x, y, '', name, selections(refs), db['prefix'], source, p,
        conditions(cond), ''.join(grefs), a)


def schema_infos(left, right):
    return ('<schemaTableInfos>\n'
            '<schemaTableInfo>\n<tablename><![CDATA[%s]]></tablename>\n<info left="0.000000" top="0.000000" width="150.000000"></info>\n</schemaTableInfo>\n'
            '<schemaTableInfo>\n<tablename><![CDATA[%s]]></tablename>\n<info left="225.000000" top="0.000000" width="150.000000"></info>\n</schemaTableInfo>\n'
            '</schemaTableInfos>\n') % (left, right)


def concat(db, table, name, left, right, cols, op, x, y):
    colmap = db[table][1]
    refs = ''.join(ref(colmap[k], left) for k in cols)
    return ('<assembly class="inetsoft.uql.asset.ConcatenatedTableAssembly">\n'
            '<assemblyInfo class="inetsoft.uql.asset.internal.CompositeTableAssemblyInfo" pixelOffX="%d" pixelOffY="%d" '
            'pixelWidth="100" pixelHeight="60" hierarchical="false">\n'
            '<name><![CDATA[%s]]></name><class><![CDATA[inetsoft.uql.asset.ConcatenatedTableAssembly]]></class>\n%s'
            '<operators>\n<pairOperator>\n<pair>\n<leftTableAssembly><![CDATA[%s]]></leftTableAssembly>\n'
            '<rightTableAssembly><![CDATA[%s]]></rightTableAssembly>\n</pair>\n<tableAssemblyOperator>\n'
            '<operator operation="%d" distinct="true" leftTable="" rightTable="">\n</operator>\n'
            '</tableAssemblyOperator>\n</pairOperator>\n</operators>\n%s</assemblyInfo><mvForceAppendUpdates>false</mvForceAppendUpdates>\n<tables>\n'
            '<oneTableAssembly><![CDATA[%s]]></oneTableAssembly>\n<oneTableAssembly><![CDATA[%s]]></oneTableAssembly>\n'
            '</tables>\n</assembly>') % (x, y, name, selections(refs), left, right, op, schema_infos(left, right), left, right)


def join(db, table, name, left, right, lcols, rcols, on, cond, x, y):
    """left columns keep their names, right columns get the composer's _1 alias"""
    colmap = db[table][1]
    refs = ''.join(ref(colmap[k], left) for k in lcols)
    refs += ''.join(ref(colmap[k], right, colmap[k][0] + '_1') for k in rcols)
    has = ' hasCondition="true"' if cond else ''
    return ('<assembly class="inetsoft.uql.asset.RelationalJoinTableAssembly">\n'
            '<assemblyInfo class="inetsoft.uql.asset.internal.CompositeTableAssemblyInfo" pixelOffX="%d" pixelOffY="%d" '
            'pixelWidth="100" pixelHeight="60" hierarchical="false"%s>\n'
            '<name><![CDATA[%s]]></name><class><![CDATA[inetsoft.uql.asset.RelationalJoinTableAssembly]]></class>\n%s'
            '<operators>\n<pairOperator>\n<pair>\n<leftTableAssembly><![CDATA[%s]]></leftTableAssembly>\n'
            '<rightTableAssembly><![CDATA[%s]]></rightTableAssembly>\n</pair>\n<tableAssemblyOperator>\n'
            '<operator operation="%d" distinct="false" leftTable="%s" rightTable="%s">\n'
            '<leftColumn>\n%s</leftColumn>\n<rightColumn>\n%s</rightColumn>\n</operator>\n'
            '</tableAssemblyOperator>\n</pairOperator>\n</operators>\n%s</assemblyInfo>%s<mvForceAppendUpdates>false</mvForceAppendUpdates>\n<tables>\n'
            '<oneTableAssembly><![CDATA[%s]]></oneTableAssembly>\n<oneTableAssembly><![CDATA[%s]]></oneTableAssembly>\n'
            '</tables>\n</assembly>') % (x, y, has, name, selections(refs), left, right, FULL_JOIN, left, right,
                                          ref(colmap[on]), ref(colmap[on]), schema_infos(left, right),
                                          conditions(cond) if cond else '', left, right)


setop, fulljoin = [], []
for i, db in enumerate([d for d in DBS if not d['stat_only']]):
    n, y = db['name'], 20 + i * 240
    C, P = db['CUSTOMERS'][1], db['PRODUCTS'][1]
    # set operations, CUSTOMERS: A = STATE one of NJ, NY, CA (14 rows), B = NY, TX (8 rows)
    setop += [physical(db, 'CUSTOMERS', n + '_A', ['id', 'state'], [(ref(C['state']), ONE_OF, ['NJ', 'NY', 'CA'])], 20, y),
              physical(db, 'CUSTOMERS', n + '_B', ['id', 'state'], [(ref(C['state']), ONE_OF, ['NY', 'TX'])], 160, y),
              concat(db, 'CUSTOMERS', n + '_union', n + '_A', n + '_B', ['id', 'state'], UNION, 300, y),
              concat(db, 'CUSTOMERS', n + '_minus', n + '_A', n + '_B', ['id', 'state'], MINUS, 440, y)]
    # set operations, PRODUCTS.DESCRIPTION only: NULL rows and duplicate rows inside one side.
    # DA = 3 values or NULL (6 rows, 3 NULL), DB = 2 values or NULL (5 rows, 3 NULL)
    d = ref(P['desc'])
    setop += [physical(db, 'PRODUCTS', n + '_DA', ['desc'],
                       [(d, ONE_OF, ['Database browser', 'Application server', 'Java game toolkit']), OR, (d, IS_NULL, [])], 20, y + 120),
              physical(db, 'PRODUCTS', n + '_DB', ['desc'],
                       [(d, ONE_OF, ['Java game toolkit', 'Learn ABC']), OR, (d, IS_NULL, [])], 160, y + 120),
              concat(db, 'PRODUCTS', n + '_Dunion', n + '_DA', n + '_DB', ['desc'], UNION, 300, y + 120),
              concat(db, 'PRODUCTS', n + '_Dminus', n + '_DA', n + '_DB', ['desc'], MINUS, 440, y + 120)]
    # full outer join, PRODUCTS: PA = CATEGORY_ID one of 1, 3 (9 rows), PB = 1, 2 (9 rows)
    pa, pb, ca, cb = n + '_PA', n + '_PB', n + '_CA', n + '_CB'
    cols = ['pid', 'cat', 'desc']
    fulljoin += [physical(db, 'PRODUCTS', pa, cols, [(ref(P['cat']), ONE_OF, [1, 3])], 20, y),
                 physical(db, 'PRODUCTS', pb, cols, [(ref(P['cat']), ONE_OF, [1, 2])], 160, y),
                 # the join table always shows every column of both sides, so the duplicate case
                 # joins tables that have CATEGORY_ID only
                 physical(db, 'PRODUCTS', ca, ['cat'], [(ref(P['cat']), ONE_OF, [1, 3])], 20, y + 120),
                 physical(db, 'PRODUCTS', cb, ['cat'], [(ref(P['cat']), ONE_OF, [1, 2])], 160, y + 120),
                 # NULL keys never match
                 join(db, 'PRODUCTS', n + '_joinNull', pa, pb, cols, cols, 'desc', None, 300, y),
                 # many to many, identical result rows
                 join(db, 'PRODUCTS', n + '_joinDup', ca, cb, ['cat'], ['cat'], 'cat', None, 300, y + 120),
                 # condition on the NULL-extended side, applied after the join
                 join(db, 'PRODUCTS', n + '_joinCond', pa, pb, cols, cols, 'desc',
                      [(ref(P['pid'], pb), IS_NULL, [])], 440, y)]

# statistical aggregates: ORDERS grouped by CUSTOMER_ID over 16 picked orders (DISCOUNT values):
# 1: 0.1 (one value); 3: NULL, NULL (all NULL); 12: NULL, 0.15 (one value left after the NULL);
# 7: NULL, NULL, 0, 0.05, 0.25, 0.15, 0.05 (NULLs, a duplicate); 19: 0, 0.2, 0, 0.25 (even count, EMPLOYEE_ID all 2).
# Picked so that no result is a tie at the 4th decimal (the export rounds to 4 decimals and a tie would round
# either way with the last-bit differences between SQL and memory).
ORDER_IDS = [12455, 12307, 12310, 12319, 12054, 12004, 12314, 12112, 12367, 12453, 12473, 12559,
             12142, 12155, 12225, 12416]
BASIC = [('Sum', 'disc', None), ('Average', 'disc', None), ('Count', 'disc', None), ('DistinctCount', 'disc', None),
         ('Min', 'disc', None), ('Max', 'disc', None),
         # INTEGER in DB2, DECIMAL(1,0) in Derby: AVG of these types keeps the type there
         ('Average', 'emp', None)]
VAR = [('Variance', 'disc', None), ('StandardDeviation', 'disc', None), ('PopulationVariance', 'disc', None),
       ('PopulationStandardDeviation', 'disc', None)]
CORR = [('Covariance', 'disc', 'emp'), ('Correlation', 'disc', 'emp')]
MEM = [('Median', 'disc', None), ('Mode', 'disc', None)]
stat = []
for i, db in enumerate(DBS):
    n, y = db['name'], 20 + i * 120
    O = db['ORDERS'][1]
    cond = [(ref(O['oid']), ONE_OF, ORDER_IDS)]
    cols = ['oid', 'cust', 'emp', 'disc']
    stat += [grouped(db, 'ORDERS', n + '_basic', cols, cond, 'cust', BASIC, 20, y),
             grouped(db, 'ORDERS', n + '_var', cols, cond, 'cust', VAR, 160, y),
             grouped(db, 'ORDERS', n + '_corr', cols, cond, 'cust', CORR, 300, y),
             # Median and Mode are never SQL, so every aggregate of this table is computed in memory
             grouped(db, 'ORDERS', n + '_mem', cols, cond, 'cust', BASIC + VAR + CORR + MEM, 440, y)]

# date grouping: one order on each of 15 dates around the year ends (ORDER_DATE is the same in every database),
# grouped by <date option>(ORDER_DATE) and ORDER_ID, so every row shows where one known date falls.
# 2002-01-05 Sat, 2002-01-06 Sun, 2002-12-26 Thu, 2002-12-30 Mon, 2003-01-01 Wed, 2003-01-05 Sun, 2003-01-06 Mon,
# 2003-12-27 Sat, 2003-12-31 Wed, 2004-01-02 Fri, 2004-02-29 Sun, 2004-12-27 Mon, 2005-01-01 Sat, 2005-01-04 Tue,
# 2005-01-08 Sat
DATE_ORDER_IDS = [12001, 12003, 12171, 12172, 12173, 12174, 12175, 12358, 12359, 12360, 12394, 12545, 12549, 12550, 12552]
DATE_TABLES = [('weekOfYear', 515), ('dayOfWeek', 519), ('week', 2), ('quarter', 4)]
dategroup = []
for i, db in enumerate(DBS):
    n, y = db['name'], 20 + i * 120
    O = db['ORDERS'][1]
    cond = [(ref(O['oid']), ONE_OF, DATE_ORDER_IDS)]
    cols = ['oid', 'cust', 'date']
    for j, (t, option) in enumerate(DATE_TABLES):
        dategroup.append(date_grouped(db, 'ORDERS', '%s_%s' % (n, t), cols, cond, [('date', option), ('oid', None)],
                                      [('Count', 'cust')], 20 + j * 140, y))
    # the same grouping on a script expression copy of ORDER_DATE: the date part is computed in memory on the same data
    for j, (t, option) in enumerate(DATE_TABLES[:3]):
        dategroup.append(date_grouped(db, 'ORDERS', '%s_%sNoSQL' % (n, t), cols, cond, [('date', option), ('oid', None)],
                                      [('Count', 'cust')], 580 + j * 140, y, mergeable=False))

def ptable(db, table, name, cols, x, y, sort=(), distinct=False, maxrow=0, group=None, aggs=(), group_desc=False):
    """physical table with optional sort [(key, desc)], distinct, max rows, one group column and aggregates"""
    source, colmap = db[table]
    refs = ''.join(ref(colmap[k]) for k in cols)
    p = ''.join('<property>\n<key><![CDATA[%s]]></key><value><![CDATA[%s]]></value></property>\n' % kv for kv in db['props'])
    attrs = ''
    if maxrow:
        attrs += ' maxRow="%d"' % maxrow
    if distinct:
        attrs += ' distinct="true"'
    if group:
        attrs += ' aggregate="true" hasAggregate="true"'
    if sort:
        attrs += ' hasSort="true"'
    extra = ''
    if sort:
        extra += '<sortInfo>\n<sortInfo>\n%s</sortInfo>\n</sortInfo>\n' % ''.join(
            '<dataRef class="inetsoft.uql.asset.SortRef"  order="%d">%s</dataRef>' % (2 if desc else 1, ref(colmap[k]))
            for k, desc in sort)
    if group:
        extra += ('<groupInfo>\n<groupInfo crosstab="false">\n<groups>\n<dataRef class="inetsoft.uql.asset.GroupRef"  '
                  'timeSeries="false" >%s<groupSort sort="%d" interval="1.0" option="1" others="1" sortByCol="0" info="false" '
                  'cinfo="false"><otherLabel><![CDATA[Others]]></otherLabel>\n<manualOrderList class="inetsoft.util.ItemList">'
                  '</manualOrderList></groupSort>\n</dataRef></groups>\n<aggregates>\n%s</aggregates>\n</groupInfo>\n</groupInfo>\n') % (
            ref(colmap[group]), 2 if group_desc else 1,
            ''.join('<dataRef class="inetsoft.uql.asset.AggregateRef"  n="1">%s<formula><![CDATA[%s]]></formula>\n</dataRef>' % (
                ref(colmap[c]), f) for f, c in aggs))
    return ('<assembly class="inetsoft.uql.asset.PhysicalBoundTableAssembly">\n'
            '<assemblyInfo class="inetsoft.uql.asset.internal.BoundTableAssemblyInfo" pixelOffX="%d" pixelOffY="%d" '
            'pixelWidth="100" pixelHeight="60"%s>\n'
            '<name><![CDATA[%s]]></name><class><![CDATA[inetsoft.uql.asset.PhysicalBoundTableAssembly]]></class>\n%s'
            '<source>\n<sourceInfo class="inetsoft.uql.asset.SourceInfo" type="8">\n<prefix><![CDATA[%s]]></prefix>\n'
            '<source><![CDATA[%s]]></source>\n<properties>\n%s</properties>\n</sourceInfo>\n</source>\n'
            '</assemblyInfo><mvForceAppendUpdates>false</mvForceAppendUpdates>\n%s<conditionAssemblies>\n'
            '</conditionAssemblies>\n</assembly>') % (x, y, attrs, name, selections(refs), db['prefix'], source, p, extra)


def inner_join(db, name, left, lcols, ltable, right, rcols, rtable, lkey, rkey, x, y):
    """left (built on ltable columns) inner join right (rtable columns); right columns named like a left one get _1"""
    lmap, rmap = db[ltable][1], db[rtable][1]
    lnames = set(lmap[k][0] for k in lcols)
    refs = ''.join(ref(lmap[k], left) for k in lcols)
    refs += ''.join(ref(rmap[k], right, rmap[k][0] + '_1' if rmap[k][0] in lnames else None) for k in rcols)
    return ('<assembly class="inetsoft.uql.asset.RelationalJoinTableAssembly">\n'
            '<assemblyInfo class="inetsoft.uql.asset.internal.CompositeTableAssemblyInfo" pixelOffX="%d" pixelOffY="%d" '
            'pixelWidth="100" pixelHeight="60" hierarchical="false">\n'
            '<name><![CDATA[%s]]></name><class><![CDATA[inetsoft.uql.asset.RelationalJoinTableAssembly]]></class>\n%s'
            '<operators>\n<pairOperator>\n<pair>\n<leftTableAssembly><![CDATA[%s]]></leftTableAssembly>\n'
            '<rightTableAssembly><![CDATA[%s]]></rightTableAssembly>\n</pair>\n<tableAssemblyOperator>\n'
            '<operator operation="3" distinct="false" leftTable="%s" rightTable="%s">\n'
            '<leftColumn>\n%s</leftColumn>\n<rightColumn>\n%s</rightColumn>\n</operator>\n'
            '</tableAssemblyOperator>\n</pairOperator>\n</operators>\n%s</assemblyInfo><mvForceAppendUpdates>false</mvForceAppendUpdates>\n<tables>\n'
            '<oneTableAssembly><![CDATA[%s]]></oneTableAssembly>\n<oneTableAssembly><![CDATA[%s]]></oneTableAssembly>\n'
            '</tables>\n</assembly>') % (x, y, name, selections(refs), left, right, left, right,
                                          ref(lmap[lkey]), ref(rmap[rkey]), schema_infos(left, right), left, right)


# max rows: tables with a max row count, exported at the top level and joined (inner, on CUSTOMER_ID) with CUSTOMERS
# so that their SQL becomes a subquery wrapped by the database's row limit (rownum, limit, top, fetch first).
# Sorted against the physical order, so a limit applied without the sort picks other rows.
# <db>_top: ORDERS by ORDER_ID descending, max 5 -> 12700..12696
# <db>_distinct: distinct CUSTOMER_ID descending, max 5 -> 35..31
# <db>_groupSort: Count(ORDER_ID) by CUSTOMER_ID, sorted by CUSTOMER_ID descending, max 3 -> customers 35, 34, 33
# <db>_groupMem: Count(ORDER_ID) and Mode(EMPLOYEE_ID) by CUSTOMER_ID, no sort, max 3: Mode is never SQL, so group,
# sort and limit run in memory, groups ascending -> customers 1, 2, 3.
# Not included: the same grouped table without a sort and without Mode. Its SQL has GROUP BY but no ORDER BY, so the
# kept groups are whatever the database returns first (Oracle 1, 25, 30; MySQL 20, 26, 33; PostgreSQL 1, 4, 35;
# Derby, SQL Server, DB2 1, 2, 3 on 2026-11-24) - the known limit noted in #80446 - and would make unstable baselines.
maxrows = []
for i, db in enumerate(DBS):
    n, y = db['name'], 20 + i * 240
    maxrows += [ptable(db, 'CUSTOMERS', n + '_C', ['id', 'state'], 20, y + 120),
                ptable(db, 'ORDERS', n + '_top', ['oid', 'cust'], 20, y, sort=[('oid', True)], maxrow=5),
                ptable(db, 'ORDERS', n + '_distinct', ['cust'], 160, y, sort=[('cust', True)], distinct=True, maxrow=5),
                ptable(db, 'ORDERS', n + '_groupMem', ['cust', 'oid', 'emp'], 440, y, maxrow=3, group='cust',
                       aggs=[('Count', 'oid'), ('Mode', 'emp')]),
                ptable(db, 'ORDERS', n + '_groupSort', ['cust', 'oid'], 300, y, maxrow=3, group='cust',
                       aggs=[('Count', 'oid')], sort=[('cust', True)]),
                inner_join(db, n + '_topJoin', n + '_top', ['oid', 'cust'], 'ORDERS', n + '_C', ['id', 'state'], 'CUSTOMERS',
                           'cust', 'id', 160, y + 120),
                inner_join(db, n + '_distinctJoin', n + '_distinct', ['cust'], 'ORDERS', n + '_C', ['id', 'state'], 'CUSTOMERS',
                           'cust', 'id', 300, y + 120),
                inner_join(db, n + '_groupSortJoin', n + '_groupSort', ['cust', 'oid'], 'ORDERS', n + '_C', ['id', 'state'],
                           'CUSTOMERS', 'cust', 'id', 440, y + 120)]
    # Informix has no SQL row limit, so the limit goes to the JDBC statement through HINT_MAX_ROWS in the variable
    # table; the in-memory join runs both sides at once on one shared variable table, and the hint of one side reaches
    # or is removed by the other (rows vary from run to run), so its join tables are left out
    if n == 'Informix':
        maxrows = [a for a in maxrows if not any('<name><![CDATA[%s_%s]]>' % (n, j) in a
                                                  for j in ('topJoin', 'distinctJoin', 'groupSortJoin'))]

# fixed timestamp so that regenerating gives the same blobs
NOW =int(datetime(2026, 11, 24, tzinfo=timezone.utc).timestamp() * 1000)


def worksheet(path, primary, assemblies):
    key = '1^2^__NULL__^%s^host-org' % path
    xml = ('<?xml version="1.0" encoding="UTF-8"?>\n<?inetsoft-asset classname="inetsoft.uql.asset.Worksheet" identifier="%s"?>\n'
           '<worksheet modified="%d" modifiedBy="admin" createdBy="admin" created="%d" class="inetsoft.uql.asset.Worksheet" offline="false">\n'
           '<Version>14.0</Version>\n<worksheetInfo>\n<worksheetInfo class="inetsoft.uql.asset.WorksheetInfo"  inputmax="50000" '
           'messageLevels="Error,Warning,Info" worksheetMode="2">\n</worksheetInfo></worksheetInfo>\n'
           '<primary><![CDATA[%s]]></primary>\n<assemblies>\n%s\n</assemblies>\n</worksheet>\n') % (
        key, NOW, NOW, primary, '\n'.join('<oneAssembly>%s</oneAssembly>' % a for a in assemblies))
    return path, key, xml.encode('utf-8'), len(assemblies)


sheets = [worksheet('DB/setOperation', 'Oracle_union', setop), worksheet('DB/fullOuterJoin', 'Oracle_joinNull', fulljoin),
          worksheet('DB/statAggregate', 'Oracle_basic', stat), worksheet('DB/dateGroup', 'Oracle_weekOfYear', dategroup),
          worksheet('DB/maxRows', 'Oracle_topJoin', maxrows)]

def main():
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


if __name__ == '__main__':
    main()
