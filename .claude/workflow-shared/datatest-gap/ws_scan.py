#!/usr/bin/env python3
"""Static scan of worksheets in a StyleBI backup zip, for the ws datatest module.

Read-only. Standard library only. Prints Markdown to stdout.

  python ws_scan.py inventory  <storage.zip> [<Spec.groovy>...]
                     (with specs: only the visible tables of the worksheets the specs run, leaving
                     out @Ignore'd features and specs skipped on Linux by @IgnoreIf({ os.linux }))
  python ws_scan.py crosscheck <storage.zip> <baseline-root> <Spec.groovy>...

inventory   per table assembly: query type (the AssetQuery class that runs it), processing steps
            (pre / post / ranking conditions, group and aggregate, expression columns SQL vs script,
            range columns, sort, distinct, max rows, variables), join / concatenation operators and
            the flags that steer the execution path (mergeable, visibleTable, runtimeSelected).
            Matrix: query type x processing step, counted in tables and in worksheets.
crosscheck  spec feature -> worksheet -> baseline folder: worksheets no feature uses (over ALL the
            specs given, so pass every spec that shares the zip), features whose worksheet is missing,
            and per feature the visible tables (each one is exported as <table>.txt) against the
            baseline txt files.

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


def used_worksheets(specs):
    """asset ids run by executeWS in features that are not @Ignore'd, in specs that are not
    @IgnoreIf({ os.linux }) (skipped on the Linux CI runner)."""
    used, skipped = set(), []
    for spec in specs:
        text = open(spec, encoding='utf8', errors='ignore').read()
        name = os.path.splitext(os.path.basename(spec))[0]
        if re.search(r'^\s*@IgnoreIf\(\{\s*os\.linux\s*\}\)', text, re.M):
            skipped.append(name + ' (whole spec, @IgnoreIf os.linux)')
            continue
        parts = re.split(r"\n\s*def\s+'([^']+)'\s*\(", text)
        for i in range(1, len(parts), 2):
            head = parts[i - 1][-200:]
            if re.search(r'@Ignore\b(?!If)', head.splitlines()[-1] if head.splitlines() else '') or \
               re.search(r'@Ignore\b(?!If)[^\n]*\n\s*$', head):
                skipped.append('%s / %s (@Ignore)' % (name, parts[i]))
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
    for spec in specs:
        name = os.path.splitext(os.path.basename(spec))[0]
        for feat, (ids, csv, other) in spec_features(spec).items():
            if other:
                unmapped.append('%s / %s: %s' % (name, feat, ', '.join(sorted(set(other)))))
            for kind, aid in csv:
                k = byid.get(aid)
                if k:
                    used.add(k)
            d = os.path.join(baseline, name, feat)
            base = set(os.listdir(d)) if os.path.isdir(d) else None
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
    print('Not mapped (non-literal asset id or a call this scan does not model): %s\n' % (unmapped or 'none'))
    table(rows, ['spec', 'feature', 'worksheets', 'visible tables', 'baseline txt',
                 'visible table without <table>.txt', 'baseline txt not named after a visible table'])
    print('Names are matched literally. WorksheetTest also writes <bk>_<table>.txt, '
          '<table>_<value>.txt (in range cases) and VS/<assembly>.txt, so the last two columns are '
          'candidates to read, not findings.')


def main(argv):
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass
    if len(argv) >= 3 and argv[1] == 'inventory':
        inventory(argv[2], argv[3:])
    elif len(argv) >= 5 and argv[1] == 'crosscheck':
        crosscheck(argv[2], argv[3], argv[4:])
    else:
        print(__doc__)
        return 2
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
