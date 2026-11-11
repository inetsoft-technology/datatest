#!/usr/bin/env python3
"""Static scan of worksheets in a StyleBI backup zip, for the ws datatest module.

Read-only. Standard library only. Prints Markdown to stdout.

  python ws_scan.py inventory  <storage.zip> [<Spec.groovy>...]
                     (with specs: only the visible tables of the worksheets the specs run, leaving
                     out @Ignore'd features and specs skipped on Linux by @IgnoreIf({ os.linux }))
  python ws_scan.py crosscheck <storage.zip> <baseline-root> <Spec.groovy>...
  python ws_scan.py capability <storage.zip> <Spec.groovy>...
  python ws_scan.py flagcheck  <storage.zip>

inventory   per table assembly: query type (the AssetQuery class that runs it), processing steps
            (pre / post / ranking conditions, group and aggregate, expression columns SQL vs script,
            range columns, sort, distinct, max rows, variables), join / concatenation operators and
            the flags that steer the execution path (mergeable, visibleTable, runtimeSelected).
            Matrix: query type x processing step, counted in tables and in worksheets.
crosscheck  spec feature -> worksheet -> baseline folder: worksheets no feature uses (over ALL the
            specs given, so pass every spec that shares the zip), features whose worksheet is missing,
            and per feature the visible tables (each one is exported as <table>.txt) against the
            baseline txt files.
capability  what the PRODUCT can do (lists read from its source, with file:line) against what the
            worksheets use: table classes, non-table assemblies (variable, named group, date range,
            condition), condition operations by place (table / named group / date range assembly),
            join and set operators, aggregate formulas, date group levels, relative date condition
            classes, variable styles and types, column reference classes, patterns in script
            expression columns. Counts for all worksheets and for the CI scope; zero rows are flagged.
            The lists are hard-coded (PRODUCT_REF); re-read the product before trusting a "zero".
flagcheck   cross-check what inventory reads against the derived flags on assemblyInfo
            (hasAggregate, hasExpression, hasCondition, hasSort) and the crosstab column selection.

Semantics verified against product source (community core, origin/main f0ef913306):
  TableAssemblyInfo.writeAttributes writes mergeable and visibleTable ONLY when false (defaults true),
  and distinct / live / runtimeSelected / hasXxx ONLY when true.
  ColumnRef.sql defaults to TRUE and is written only when false: an expression column with no sql
  attribute is a SQL expression, sql="false" is a script expression.
  Operator codes: XConstants / TableAssemblyOperator; condition codes: XCondition;
  source types: XSourceInfo.

Limits (check before trusting a number):
  * Whether a query is really pushed down to SQL is decided at run time (AssetQuery.isQueryMergeable,
    data source capabilities); mergeable="false" only says the user turned it off. The "path" columns
    are static signals, not the executed path. Confirm with a run.
  * Hidden tables (visibleTable="false") are not exported by WorksheetTest, so their steps are only
    checked through the visible tables built on them.
  * capability sees only what is written in the XML. A value passed at run time (a variable's value, the
    relative date range name InRange_Spec passes) is not visible; neither is what a script does.
  * crosscheck reads executeWS('<asset id>', ...) with a literal asset id; other calls
    (importCSVToEMTable, exportVSComponentData, non-literal ids) are listed as not mapped.
"""
import collections
import json
import os
import re
import sys
import zipfile
import xml.etree.ElementTree as ET

QUERY_OF = {
    'BoundTableAssembly': 'BoundQuery',
    'PhysicalBoundTableAssembly': 'PhysicalBoundQuery',
    'SQLBoundTableAssembly': 'SQLBoundQuery',
    'TabularTableAssembly': 'TabularBoundQuery',
    'QueryBoundTableAssembly': 'BoundQuery',
    'EmbeddedTableAssembly': 'EmbeddedQuery',
    'SnapshotEmbeddedTableAssembly': 'EmbeddedQuery',
    'MirrorTableAssembly': 'MirrorQuery',
    'RelationalJoinTableAssembly': 'JoinQuery',
    'MergeJoinTableAssembly': 'JoinQuery',
    'CompositeTableAssembly': 'JoinQuery',
    'ConcatenatedTableAssembly': 'ConcatenatedQuery',
    'RotatedTableAssembly': 'RotatedQuery',
    'UnpivotTableAssembly': 'UnpivotQuery',
    'CubeTableAssembly': 'CubeQuery',
    'DataTableAssembly': 'DataQuery',
}
SOURCE_TYPE = {'0': 'query', '1': 'model', '2': 'report', '3': 'parameter', '5': 'cube',
               '6': 'embedded', '7': 'asset', '8': 'physical table', '16': 'datasource',
               '64': 'vs assembly', '-1': 'none'}
OPERATOR = {1: 'join', 3: 'inner join', 7: 'left join', 11: 'right join', 15: 'full join',
            17: 'not-equal join', 33: '> join', 35: '>= join', 65: '< join', 67: '<= join',
            129: 'merge join', 257: 'cross join', 512: 'concatenation', 1536: 'union',
            2560: 'intersect', 4608: 'minus'}
CONDITION = {0: 'none', 1: 'equal to', 2: 'one of', 3: 'less than', 4: 'greater than', 5: 'between',
             6: 'starting with', 7: 'contains', 8: 'null', 9: 'top n', 10: 'bottom n',
             11: 'date in', 12: 'pseudo', 13: 'like', 1024: 'correlated'}
DATE_GROUP = {0: 'none', 1: 'day', 2: 'week', 3: 'month', 4: 'quarter', 5: 'year', 6: 'second',
              7: 'minute', 8: 'hour', 9: 'am/pm', 10: 'millisecond', 513: 'quarter of year',
              514: 'month of year', 515: 'week of year', 516: 'week of month', 517: 'day of year',
              518: 'day of month', 519: 'day of week', 520: 'am/pm of day', 521: 'hour of day',
              522: 'minute of hour', 523: 'second of minute'}
STEPS = ['pre condition', 'post condition', 'ranking', 'group/aggregate', 'expression (SQL)',
         'expression (script)', 'range column', 'sort', 'distinct', 'max rows', 'variable',
         'mergeable=false']


def short(cls):
    return (cls or '').rsplit('.', 1)[-1]


def load_worksheets(zpath):
    """asset id -> worksheet XML root; also data source name -> type."""
    z = zipfile.ZipFile(zpath)
    bi = json.loads(z.read('blob-index.json'))
    wss, dss = {}, {}
    for store, items in bi.items():
        for key, v in items.items():
            if not v.get('digest'):
                continue
            d = v['digest']
            cls = (v.get('metadata') or {}).get('className', '')
            if key.startswith('1^2^'):
                wss[key] = ET.fromstring(z.read('blobs/%s/%s' % (d[:2], d[2:])))
            elif cls == 'inetsoft.uql.XDataSourceWrapper':
                try:
                    root = ET.fromstring(z.read('blobs/%s/%s' % (d[:2], d[2:])))
                except ET.ParseError:
                    continue
                for ds in root.iter('datasource'):
                    if ds.get('name'):
                        dss[ds.get('name')] = ds.get('type') or '?'
    return wss, dss


def asset_path(key):
    return key.split('^')[3]


def tables(root):
    for a in root.iter('assembly'):
        c = short(a.get('class'))
        if c.endswith('TableAssembly'):
            yield c, a


def table_facts(cls, a):
    info = a.find('assemblyInfo')
    attr = info.attrib if info is not None else {}
    f = {'class': cls, 'query': QUERY_OF.get(cls, '? ' + cls),
         'name': info.findtext('name') if info is not None else None,
         'visible': attr.get('visibleTable') != 'false',
         'mergeable': attr.get('mergeable') != 'false',
         'runtimeSelected': attr.get('runtimeSelected') == 'true',
         'steps': set(), 'conds': collections.Counter(), 'ops': collections.Counter(),
         'formulas': collections.Counter(), 'dategroups': collections.Counter(),
         'exprs': collections.Counter(), 'source': None, 'datasource': None}
    si = info.find('source/sourceInfo') if info is not None else None
    if si is not None:
        f['source'] = SOURCE_TYPE.get(si.get('type'), si.get('type'))
        f['datasource'] = si.findtext('prefix')
    ds = info.find('datasource') if info is not None else None
    if ds is not None and ds.get('name'):
        f['datasource'] = ds.get('name')

    for tag, step in (('preConditions', 'pre condition'), ('postConditions', 'post condition'),
                      ('topNConditions', 'ranking')):
        el = a.find(tag)
        if el is None:
            continue
        cs = el.findall('conditions/condition')
        if cs:
            f['steps'].add(step)
        for c in cs:
            try:
                f['conds'][(step, CONDITION.get(int(c.get('operation')), c.get('operation')))] += 1
            except (TypeError, ValueError):
                pass
            for cd in c.iter('condition_data'):
                if cd.get('isvariable') == 'true':
                    f['steps'].add('variable')
    if info is not None and info.find('query/query_jdbc/variable') is not None:
        f['steps'].add('variable')

    gi = a.find('groupInfo/groupInfo')
    if gi is not None:
        ae, ge = gi.find('aggregates'), gi.find('groups')
        aggs = list(ae) if ae is not None else []
        grps = list(ge) if ge is not None else []
        if aggs or grps:
            f['steps'].add('group/aggregate')
        for ag in aggs:
            f['formulas'][(ag.findtext('formula') or '').strip() or '?'] += 1
        for g in grps:
            dg = g.get('dateGroup')
            if dg is not None:
                f['dategroups'][DATE_GROUP.get(int(dg), dg)] += 1

    cols = a.find('assemblyInfo/normalColumnSelection/ColumnSelection')
    for cr in (cols if cols is not None else []):
        if cr.tag != 'dataRef':
            continue
        inner = cr.find('dataRef')
        ic = short(inner.get('class')) if inner is not None else ''
        if ic == 'ExpressionRef':
            kind = 'expression (SQL)' if cr.get('sql') != 'false' else 'expression (script)'
            f['steps'].add(kind)
            f['exprs'][kind] += 1
        elif ic in ('DateRangeRef', 'NumericRangeRef', 'NamedRangeRef'):
            f['steps'].add('range column')
            f['exprs'][ic] += 1

    st = a.find('sortInfo/sortInfo')
    if st is not None and len(st):
        f['steps'].add('sort')
    if attr.get('distinct') == 'true':
        f['steps'].add('distinct')
    if attr.get('maxRow'):
        f['steps'].add('max rows')
    if not f['mergeable']:
        f['steps'].add('mergeable=false')
    for op in a.iter('operator'):
        try:
            f['ops'][OPERATOR.get(int(op.get('operation')), op.get('operation'))] += 1
        except (TypeError, ValueError):
            pass
    return f


def table(rows, header):
    print('| ' + ' | '.join(header) + ' |')
    print('|' + '---|' * len(header))
    for r in rows:
        print('| ' + ' | '.join(str(x) for x in r) + ' |')
    print()


LINUX_SKIP = re.compile(r'^@IgnoreIf\(\{\s*os\.linux\s*\}\)')


def annotations_above(before):
    """The annotation lines directly above a declaration (blank lines and // comments in
    between are allowed), nearest first. `before` is the text up to the declaration."""
    out = []
    for line in reversed(before.splitlines()):
        s = line.strip()
        if not s or s.startswith('//'):
            continue
        if s.startswith('@'):
            out.append(s)
            continue
        break
    return out


def used_worksheets(specs):
    """asset ids run by executeWS in the features CI runs: a spec is skipped when its CLASS
    carries @IgnoreIf({ os.linux }) (the Linux CI runner); a feature is skipped when it carries
    @Ignore or @IgnoreIf({ os.linux })."""
    used, skipped = set(), []
    for spec in specs:
        text = open(spec, encoding='utf8', errors='ignore').read()
        name = os.path.splitext(os.path.basename(spec))[0]
        cls = re.search(r'^\s*(?:public\s+)?class\s+\w+', text, re.M)
        # the class annotations: everything between the last import and the class declaration
        # (annotations such as @SreeHome span several lines, so annotations_above is not enough)
        imports = list(re.finditer(r'^\s*import\s.*$', text[:cls.start()] if cls else '', re.M))
        block = text[imports[-1].end() if imports else 0:cls.start()] if cls else ''
        if cls and any(LINUX_SKIP.match(l.strip()) for l in block.splitlines()):
            skipped.append(name + ' (whole spec, class-level @IgnoreIf os.linux)')
            continue
        parts = re.split(r"\n\s*def\s+'([^']+)'\s*\(", text)
        for i in range(1, len(parts), 2):
            notes = annotations_above(parts[i - 1])
            if any(re.match(r'@Ignore\b(?!If)', a) for a in notes):
                skipped.append('%s / %s (@Ignore)' % (name, parts[i]))
                continue
            if any(LINUX_SKIP.match(a) for a in notes):
                skipped.append('%s / %s (@IgnoreIf os.linux)' % (name, parts[i]))
                continue
            used.update(re.findall(r"\.executeWS\(\s*'([^']+)'", parts[i + 1]))
    return used, skipped


def inventory(zpath, specs=None):
    wss, dss = load_worksheets(zpath)
    scope = None
    if specs:
        ids, skipped = used_worksheets(specs)
        scope = {k for k in wss if re.sub(r'\^[^^]*$', '', k) in ids}
    facts = []
    for key in sorted(wss):
        if scope is not None and key not in scope:
            continue
        for cls, a in tables(wss[key]):
            f = table_facts(cls, a)
            f['ws'] = asset_path(key)
            if scope is not None and not f['visible']:
                continue
            facts.append(f)
    print('# Worksheet inventory: %s\n' % os.path.basename(zpath))
    if scope is not None:
        print('Scope: visible tables (exported by WorksheetTest) of the %d worksheets run by executeWS in '
              'the %d specs given, without the skipped ones: %s\n' % (len(scope), len(specs), skipped))
        wss = {k: v for k, v in wss.items() if k in scope}
    print('%d worksheets, %d table assemblies (%d visible, %d hidden)\n' % (
        len(wss), len(facts), sum(f['visible'] for f in facts), sum(not f['visible'] for f in facts)))

    print('## Query type (AssetQuery class) by table class\n')
    c = collections.Counter((f['query'], f['class']) for f in facts)
    table([(q, cl, n, sum(1 for f in facts if f['class'] == cl and f['visible']))
           for (q, cl), n in sorted(c.items())], ['query type', 'table class', 'tables', 'visible'])

    print('## Query type x processing step (tables; worksheets in brackets)\n')
    qs = sorted({f['query'] for f in facts})
    rows = []
    for q in qs:
        fs = [f for f in facts if f['query'] == q]
        row = [q, len(fs)]
        for s in STEPS:
            hit = [f for f in fs if s in f['steps']]
            row.append('%d (%d)' % (len(hit), len({f['ws'] for f in hit})) if hit else '0')
        rows.append(row)
    table(rows, ['query type', 'tables'] + STEPS)

    print('## Join and concatenation operators\n')
    oc = collections.Counter()
    for f in facts:
        oc.update(f['ops'])
    table(sorted(oc.items(), key=lambda x: -x[1]), ['operator', 'count'])

    print('## Conditions by kind and operator\n')
    cc = collections.Counter()
    for f in facts:
        cc.update(f['conds'])
    table([(k, o, n) for (k, o), n in sorted(cc.items())], ['condition', 'operator', 'count'])

    print('## Aggregate formulas\n')
    fc = collections.Counter()
    for f in facts:
        fc.update(f['formulas'])
    table(sorted(fc.items(), key=lambda x: -x[1]), ['formula', 'count'])

    print('## Date group levels\n')
    dc = collections.Counter()
    for f in facts:
        dc.update(f['dategroups'])
    table(sorted(dc.items(), key=lambda x: -x[1]), ['date group', 'count'])

    print('## Expression and range columns\n')
    ec = collections.Counter()
    for f in facts:
        ec.update(f['exprs'])
    table(sorted(ec.items(), key=lambda x: -x[1]), ['column kind', 'count'])

    print('## Bound sources\n')
    sc = collections.Counter((f['query'], f['source'], f['datasource'], dss.get(f['datasource'], ''))
                             for f in facts if f['source'] is not None)
    table([(q, s, d, t, n) for (q, s, d, t), n in sorted(sc.items(), key=lambda x: str(x))],
          ['query type', 'source type', 'data source', 'ds type', 'tables'])
    print('Data sources in the zip: ' + ', '.join('%s (%s)' % kv for kv in sorted(dss.items())))
    print('\nPath signals are static (see the file header): mergeable=false is a user setting; '
          'whether a query is pushed down to SQL is decided at run time.')


def spec_features(path):
    text = open(path, encoding='utf8', errors='ignore').read()
    parts = re.split(r"\n\s*def\s+'([^']+)'\s*\(", text)
    out = {}
    for i in range(1, len(parts), 2):
        body = parts[i + 1]
        ids = re.findall(r"\.executeWS\(\s*'([^']+)'", body)
        other = re.findall(r"\.(importCSVToEMTable|exportVSComponentData|executeWS)\(\s*([^'\s,)][^,)]*)", body)
        csv = re.findall(r"\.(importCSVToEMTable|exportVSComponentData)\(\s*'([^']+)'", body)
        out[parts[i]] = (ids, csv, [o[0] for o in other])
    return out


def crosscheck(zpath, baseline, specs):
    wss, _ = load_worksheets(zpath)
    byid = {re.sub(r'\^[^^]*$', '', k): k for k in wss}
    print('# Worksheet crosscheck: %d specs\n' % len(specs))
    used, rows, unmapped, missing = set(), [], [], []
    unmapped_txt = [0]
    for spec in specs:
        name = os.path.splitext(os.path.basename(spec))[0]
        for feat, (ids, csv, other) in spec_features(spec).items():
            for kind, aid in csv:
                k = byid.get(aid)
                if k:
                    used.add(k)
            d = os.path.join(baseline, name, feat)
            base = set(os.listdir(d)) if os.path.isdir(d) else None
            if other:
                unmapped.append('%s / %s: %s' % (name, feat, ', '.join(sorted(set(other)))))
                unmapped_txt[0] += sum(len([f for f in fs if f.endswith('.txt')])
                                       for _, _, fs in os.walk(d)) if base is not None else 0
            for aid in ids:
                k = byid.get(aid)
                if k is None:
                    missing.append('%s / %s: %s' % (name, feat, aid))
                    continue
                used.add(k)
            if not ids:
                continue
            vis_all = set()
            for aid in ids:
                k = byid.get(aid)
                if k:
                    vis_all |= {f['name'] for f in (table_facts(c, a) for c, a in tables(wss[k]))
                                if f['visible'] and f['name']}
            txt = {x for x in (base or set()) if x.endswith('.txt')}
            plain = {t + '.txt' for t in vis_all}
            rows.append((name, feat, len(ids), len(vis_all), 'NO BASELINE' if base is None else len(txt),
                         ', '.join(sorted(plain - txt))[:80],
                         ', '.join(sorted(t for t in txt if t not in plain))[:80]))
    orphans = sorted(asset_path(k) for k in wss if k not in used)
    print('Worksheets no feature uses (over the %d specs given): %d %s\n' % (len(specs), len(orphans), orphans))
    print('Features whose worksheet is not in the zip: %s\n' % (missing or 'none'))
    print('Not mapped (non-literal asset id or a call this scan does not model): %d features, '
          '%d baseline txt files not covered by this scan: %s\n'
          % (len(unmapped), unmapped_txt[0], unmapped or 'none'))
    table(rows, ['spec', 'feature', 'worksheets', 'visible tables', 'baseline txt',
                 'visible table without <table>.txt', 'baseline txt not named after a visible table'])
    print('Names are matched literally. WorksheetTest also writes <bk>_<table>.txt, '
          '<table>_<value>.txt (in range cases) and VS/<assembly>.txt, so the last two columns are '
          'candidates to read, not findings.')


# ---------------------------------------------------------------------------------------------
# capability: what the product can do (from its source) against what the assets and specs use.
# The lists below were read from the product source at COMMUNITY_CORE_REF; the number after the
# colon is the line of the constant or class declaration. They go stale when the product changes:
# re-read the source and update the lists before trusting a "zero" row.
# ---------------------------------------------------------------------------------------------
PRODUCT_REF = 'community/core/src/main/java/inetsoft/ @ 333566db8 (main, 2026-11-11)'

# concrete table classes (ScriptedTableAssembly is an interface implemented by TabularTableAssembly)
CAP_TABLE_CLASSES = [
    ('BoundTableAssembly', 'uql/asset/BoundTableAssembly.java:40'),
    ('PhysicalBoundTableAssembly', 'uql/asset/PhysicalBoundTableAssembly.java:26'),
    ('SQLBoundTableAssembly', 'uql/asset/SQLBoundTableAssembly.java:45'),
    ('QueryBoundTableAssembly', 'uql/asset/QueryBoundTableAssembly.java:34'),
    ('EmbeddedTableAssembly', 'uql/asset/EmbeddedTableAssembly.java:41'),
    ('SnapshotEmbeddedTableAssembly', 'uql/asset/SnapshotEmbeddedTableAssembly.java:62'),
    ('DataTableAssembly', 'uql/asset/DataTableAssembly.java:35'),
    ('TabularTableAssembly', 'uql/asset/TabularTableAssembly.java:46'),
    ('CubeTableAssembly', 'uql/asset/CubeTableAssembly.java:36'),
    ('MirrorTableAssembly', 'uql/asset/MirrorTableAssembly.java:41'),
    ('RelationalJoinTableAssembly', 'uql/asset/RelationalJoinTableAssembly.java:32'),
    ('MergeJoinTableAssembly', 'uql/asset/MergeJoinTableAssembly.java:31'),
    ('ConcatenatedTableAssembly', 'uql/asset/ConcatenatedTableAssembly.java:36'),
    ('RotatedTableAssembly', 'uql/asset/RotatedTableAssembly.java:38'),
    ('UnpivotTableAssembly', 'uql/asset/UnpivotTableAssembly.java:39'),
]
CAP_OTHER_ASSEMBLIES = [
    ('DefaultVariableAssembly', 'uql/asset/DefaultVariableAssembly.java:42'),
    ('MirrorVariableAssembly', 'uql/asset/MirrorVariableAssembly.java:41'),
    ('DefaultNamedGroupAssembly', 'uql/asset/DefaultNamedGroupAssembly.java:41'),
    ('MirrorNamedGroupAssembly', 'uql/asset/MirrorNamedGroupAssembly.java:41'),
    ('DefaultDateRangeAssembly', 'uql/asset/DefaultDateRangeAssembly.java:42'),
    ('MirrorDateRangeAssembly', 'uql/asset/MirrorDateRangeAssembly.java:42'),
    ('DefaultConditionAssembly', 'uql/asset/DefaultConditionAssembly.java:39'),
    ('MirrorConditionAssembly', 'uql/asset/MirrorConditionAssembly.java:39'),
]
CAP_CONDITIONS = [  # uql/XCondition.java
    ('none', 38), ('equal to', 46), ('one of', 52), ('less than', 60), ('greater than', 68), ('between', 76),
    ('starting with', 82), ('contains', 88), ('null', 94), ('top n', 100), ('bottom n', 106), ('date in', 112),
    ('pseudo', 118), ('like', 124), ('correlated', 129)]
# uql/asset/TableAssemblyOperator.java; JOIN (:47) and CONCATENATION (:95) are base bits, not choices
CAP_JOINS = [
    ('inner join', 51), ('left join', 55), ('right join', 59), ('full join', 63), ('not-equal join', 67),
    ('> join', 71), ('>= join', 75), ('< join', 79), ('<= join', 83), ('merge join', 87), ('cross join', 91),
    ('union', 99), ('intersect', 103), ('minus', 107)]
# uql/asset/AggregateFormula.java; the name is what the worksheet XML writes (getFormulaName)
CAP_FORMULAS = [
    ('none', 43), ('Average', 47), ('Count', 51), ('DistinctCount', 55), ('Max', 59), ('Min', 63), ('Sum', 67),
    ('First', 71), ('Last', 75), ('Median', 79), ('Mode', 83), ('Correlation', 87), ('Covariance', 91),
    ('Variance', 95), ('StandardDeviation', 99), ('PopulationVariance', 104),
    ('PopulationStandardDeviation', 108), ('WeightedAverage', 113), ('Product', 117), ('Concat', 121),
    ('NthLargest', 125), ('NthSmallest', 129), ('NthMostFrequent', 133), ('PthPercentile', 137),
    ('SumSQFormula', 141), ('SumWTFormula', 145), ('Aggregate', 149)]
CAP_DATE_LEVELS = [  # uql/XConstants.java; 'none' (:69) is not a level
    ('year', 73), ('quarter', 77), ('month', 81), ('week', 85), ('day', 89), ('am/pm', 93), ('hour', 97),
    ('minute', 101), ('second', 105), ('millisecond', 109), ('quarter of year', 118), ('month of year', 122),
    ('week of year', 126), ('week of month', 130), ('day of year', 134), ('day of month', 138),
    ('day of week', 142), ('am/pm of day', 146), ('hour of day', 150), ('minute of hour', 154),
    ('second of minute', 158)]
CAP_DATE_CONDITIONS = [  # uql/asset/DateCondition.java (inner classes)
    ('ToDateCondition', 525), ('YearCondition', 709), ('QuarterCondition', 841), ('NthQuarterCondition', 1006),
    ('NthHalfYearCondition', 1170), ('MonthCondition', 1343), ('NthMonthCondition', 1504),
    ('WeekCondition', 1671), ('WeeksCondition', 1808), ('DayCondition', 1977), ('MonthsCondition', 2121),
    ('DaysCondition', 2288), ('PartDateCondition', 2443)]
CAP_VAR_STYLES = [  # uql/schema/UserVariable.java
    ('NONE', 50), ('COMBOBOX', 54), ('LIST', 58), ('RADIO_BUTTONS', 62), ('CHECKBOXES', 66),
    ('DATE_COMBOBOX', 70)]
CAP_VAR_TYPES = [  # uql/schema/XSchema.java (the XML xtype); 'character' (:70) is an alias of char
    ('string', 46), ('boolean', 50), ('float', 54), ('double', 58), ('decimal', 62), ('char', 66), ('byte', 74),
    ('short', 78), ('integer', 82), ('long', 86), ('timeInstant', 90), ('date', 94), ('time', 98)]
# column reference classes that can appear in a column selection (SQLExpressionRef is an interface
# implemented by the range refs, it never appears as a class name in the XML)
CAP_COLUMN_REFS = [
    ('ExpressionRef', 'uql/erm/ExpressionRef.java:35'), ('DateRangeRef', 'uql/asset/DateRangeRef.java:53'),
    ('NumericRangeRef', 'uql/asset/NumericRangeRef.java:40'), ('NamedRangeRef', 'uql/asset/NamedRangeRef.java:38')]
VAR_STYLE_NAME = {'0': 'NONE', '1': 'COMBOBOX', '2': 'LIST', '3': 'RADIO_BUTTONS', '4': 'CHECKBOXES',
                  '5': 'DATE_COMBOBOX'}
SCRIPT_PATTERNS = [
    ('new Date(', r'new\s+Date\s*\('), ("new Date(field[", r'new\s+Date\s*\(\s*field\s*\['),
    ('year(', r'\byear\s*\('), ('month(', r'\bmonth\s*\('), ('dateAdd(', r'\bdateAdd\s*\('),
    ("field['..']", r'field\s*\['), ('getFullYear()', r'getFullYear\s*\('),
    ('var <name> =', r'\bvar\s+\w+\s*='), ('data[ / tables[ / ws.', r'\bdata\s*\[|\btables?\s*\[|\bws\.')]


def _scope(wss, specs):
    ids, skipped = used_worksheets(specs)
    return {k for k in wss if re.sub(r'\^[^^]*$', '', k) in ids}, skipped


def _cap_table(title, ref, items, c, notes=None):
    """items: [(name, position)], c: (all, ci) Counters keyed by name."""
    print('### %s  (%s)\n' % (title, ref))
    rows = []
    for name, pos in items:
        a, b = c[0][name], c[1][name]
        where = pos if isinstance(pos, str) else ':%d' % pos
        rows.append((name, where, a, b, 'ZERO in CI' + (' and everywhere' if a == 0 else '') if b == 0 else ''))
    table(rows, ['item', 'product', 'all worksheets', 'CI scope', ''])
    known = {n for n, _ in items}
    extra = [(k, c[0][k], c[1][k]) for k in c[0] if k not in known]
    if extra:
        print('Not in the list: %s\n' % ', '.join('%s (%d / %d)' % e for e in extra))
    if notes:
        print(notes + '\n')


def capability(zpath, specs):
    wss, dss = load_worksheets(zpath)
    scope, skipped = _scope(wss, specs)

    def counters():
        return collections.Counter(), collections.Counter()

    def add(c, key, aid, n=1):
        c[0][key] += n
        if aid in scope:
            c[1][key] += n

    print('# Capability diff: %s\n' % os.path.basename(zpath))
    print('Product source: %s. Counts are how often the capability appears in the worksheet XML '
          '(hidden tables included); "all" = every worksheet in the zip (%d), "CI scope" = the %d worksheets '
          'run by the features CI runs. Skipped: %s\n' % (PRODUCT_REF, len(wss), len(scope), skipped))
    print('Values passed at run time (variable values, the range names InRange_Spec passes) are NOT in the XML.\n')

    c = counters()
    for aid, root in wss.items():
        for cls, a in tables(root):
            add(c, cls, aid)
    _cap_table('Table assembly classes', 'concrete classes; ScriptedTableAssembly is an interface',
               CAP_TABLE_CLASSES, c)

    c = counters()
    names = {n for n, _ in CAP_OTHER_ASSEMBLIES}
    for aid, root in wss.items():
        for e in root.iter('assembly'):
            if short(e.get('class')) in names:
                add(c, short(e.get('class')), aid)
    _cap_table('Non-table assembly classes', 'uql/asset/*Assembly.java', CAP_OTHER_ASSEMBLIES, c,
               'ConditionAssembly nodes: the `conditionAssemblies` elements in the tables are empty containers.')

    # conditions by place
    place_of = {'preConditions': 'pre', 'postConditions': 'post', 'topNConditions': 'ranking',
                'namedGroupInfo': 'named group'}
    byplace, flags = collections.defaultdict(counters), counters()
    for aid, root in wss.items():
        parent = {ch: p for p in root.iter() for ch in p}
        for e in root.iter():
            if e.tag not in ('condition', 'xCondition') or e.get('operation') is None:
                continue
            # DateCondition$ leaves are counted in their own table; XConditionInfo is an info wrapper
            # (operation 1 placeholder) around a condition, not a condition
            if short(e.get('class')).startswith('DateCondition$') or short(e.get('class')) == 'XConditionInfo':
                continue
            try:
                op = CONDITION.get(int(e.get('operation')), e.get('operation'))
            except ValueError:
                continue
            place, x = 'other', e
            while x in parent:
                x = parent[x]
                if x.tag in place_of:
                    place = place_of[x.tag]
                    break
                if x.tag == 'assembly' and short(x.get('class')) == 'DefaultDateRangeAssembly':
                    place = 'date range assembly'
                    break
            add(byplace[place], op, aid)
            if e.get('negated') == 'true':
                add(flags, 'negated', aid)
            if e.get('equal') == 'true':
                add(flags, 'equal (>=, <=)', aid)
    print('### Condition operations by place  (uql/XCondition.java:38-129)\n')
    places = ['pre', 'post', 'ranking', 'named group', 'date range assembly', 'other']
    table([(n, 'XCondition.java:%d' % ln) + tuple('%d / %d' % (byplace[p][0][n], byplace[p][1][n]) for p in places)
           for n, ln in CAP_CONDITIONS], ['operation', 'product'] + places)
    print('cells are all / CI scope. Flags (all / CI): %s\n' % {k: (flags[0][k], flags[1][k]) for k in flags[0]})

    c_join, c_form, c_dg = counters(), counters(), counters()
    for aid, root in wss.items():
        for cls, a in tables(root):
            f = table_facts(cls, a)
            for src, dst in ((f['ops'], c_join), (f['formulas'], c_form), (f['dategroups'], c_dg)):
                for k, v in src.items():
                    add(dst, k, aid, v)
    _cap_table('Join and set operators', 'uql/asset/TableAssemblyOperator.java; base bits JOIN and CONCATENATION '
               'are not choices', CAP_JOINS, c_join)
    _cap_table('Aggregate formulas (27)', 'uql/asset/AggregateFormula.java', CAP_FORMULAS, c_form)
    _cap_table('Date group levels (21)', 'uql/XConstants.java', CAP_DATE_LEVELS, c_dg)

    c = counters()
    for aid, root in wss.items():
        for e in root.iter():
            cl = e.get('class') or ''
            if 'DateCondition$' in cl:
                add(c, cl.split('$')[-1], aid)
    _cap_table('Relative date conditions (DateCondition subclasses)', 'uql/asset/DateCondition.java',
               CAP_DATE_CONDITIONS, c,
               'Only the classes written in the XML are counted. A date-in table condition keeps its range '
               'name in a variable (empty condition_data), so the ranges InRange_Spec passes are not visible here.')

    c_style, c_type, c_flag = counters(), counters(), counters()
    for aid, root in wss.items():
        for v in root.iter('variable'):
            if v.get('xtype') is None:
                continue
            add(c_style, VAR_STYLE_NAME.get(v.get('style'), 'no style attribute'), aid)
            add(c_type, v.get('xtype'), aid)
            for k in ('multipleSelection', 'embedded', 'sortValue', 'prompt'):
                if v.get(k) == 'true':
                    add(c_flag, k, aid)
    _cap_table('Variable styles', 'uql/schema/UserVariable.java', CAP_VAR_STYLES, c_style,
               'Variables without a style attribute are listed under "no style attribute"; whether that '
               'means NONE was not checked. Options (all / CI): %s' % {k: (c_flag[0][k], c_flag[1][k]) for k in c_flag[0]})
    _cap_table('Variable data types', 'uql/schema/XSchema.java', CAP_VAR_TYPES, c_type)

    c = counters()
    for aid, root in wss.items():
        for cls, a in tables(root):
            sel = a.find('assemblyInfo/normalColumnSelection/ColumnSelection')
            for cr in (sel if sel is not None else []):
                inner = cr.find('dataRef') if cr.tag == 'dataRef' else None
                if inner is not None:
                    add(c, short(inner.get('class')), aid)
    _cap_table('Column reference classes', 'normalColumnSelection; AttributeRef (plain columns) not listed',
               CAP_COLUMN_REFS, c)

    # script expression columns: the script is the text of the inner ExpressionRef
    texts = []
    for aid, root in wss.items():
        for cls, a in tables(root):
            sel = a.find('assemblyInfo/normalColumnSelection/ColumnSelection')
            for cr in (sel if sel is not None else []):
                inner = cr.find('dataRef') if cr.tag == 'dataRef' else None
                if inner is not None and short(inner.get('class')) == 'ExpressionRef' and cr.get('sql') == 'false':
                    texts.append((aid in scope, inner.text or ''))
    print('### Script expression columns: %d (CI scope %d)\n' % (len(texts), sum(1 for s_, _ in texts if s_)))
    table([(k, sum(1 for _, t in texts if re.search(p, t)), sum(1 for s_, t in texts if s_ and re.search(p, t)))
           for k, p in SCRIPT_PATTERNS], ['pattern in the script', 'all', 'CI scope'])


# ---------------------------------------------------------------------------------------------
# flagcheck: cross-check what inventory reads against the derived flags on assemblyInfo
# ---------------------------------------------------------------------------------------------
def flagcheck(zpath):
    wss, dss = load_worksheets(zpath)
    special = ('ExpressionRef', 'DateRangeRef', 'NumericRangeRef', 'NamedRangeRef')

    def kinds(info, sel_name):
        sel = info.find(sel_name + '/ColumnSelection')
        k = collections.Counter()
        for cr in (sel if sel is not None else []):
            inner = cr.find('dataRef') if cr.tag == 'dataRef' else None
            if inner is not None and short(inner.get('class')) in special:
                k[short(inner.get('class'))] += 1
        return k

    mism, examples, n = collections.Counter(), collections.defaultdict(list), 0
    for aid, root in wss.items():
        for cls, a in tables(root):
            n += 1
            info = a.find('assemblyInfo')
            at, name = info.attrib, info.findtext('name')
            nk, ck = kinds(info, 'normalColumnSelection'), kinds(info, 'crosstabColumnSelection')
            if any(ck[k] > nk[k] for k in special):
                mism['crosstab selection has more than normal'] += 1
            gi = a.find('groupInfo/groupInfo')
            agg = False
            if gi is not None:
                ae, ge = gi.find('aggregates'), gi.find('groups')
                agg = bool((ae is not None and len(ae)) or (ge is not None and len(ge)))
            conds = any(a.find(t) is not None and a.findall(t + '/conditions/condition')
                        for t in ('preConditions', 'postConditions', 'topNConditions'))
            st = a.find('sortInfo/sortInfo')
            checks = (('hasAggregate', agg), ('hasExpression', nk['ExpressionRef'] > 0), ('hasCondition', conds),
                      ('hasSort', st is not None and len(st) > 0))
            for flag, scanned in checks:
                if scanned != (at.get(flag) == 'true'):
                    mism[flag] += 1
                    examples[flag].append('%s (scan %s, flag %s)' % (name, scanned, at.get(flag)))
    print('# Flag cross-check: %s\n' % os.path.basename(zpath))
    print('%d table assemblies. A flag is a derived value on assemblyInfo; the scan reads the user-set '
          'nodes (groupInfo, pre/post/topN conditions, sortInfo, normalColumnSelection). A mismatch means '
          'one of the two is wrong for that table: look at the XML before trusting either.\n' % n)
    table([(k, v, '; '.join(examples[k][:4])) for k, v in sorted(mism.items())] or [('(none)', 0, '')],
          ['check', 'tables', 'examples'])


def main(argv):
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass
    if len(argv) >= 3 and argv[1] == 'inventory':
        inventory(argv[2], argv[3:])
    elif len(argv) >= 5 and argv[1] == 'crosscheck':
        crosscheck(argv[2], argv[3], argv[4:])
    elif len(argv) >= 4 and argv[1] == 'capability':
        capability(argv[2], argv[3:])
    elif len(argv) == 3 and argv[1] == 'flagcheck':
        flagcheck(argv[2])
    else:
        print(__doc__)
        return 2
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
