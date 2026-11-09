#!/usr/bin/env python3
"""Static scan of a StyleBI backup zip (blob storage) used by datatest viewsheet specs.

Read-only. Standard library only. Prints Markdown to stdout.

  python vs_backup_scan.py inventory  <storage.zip>
  python vs_backup_scan.py crosscheck <storage.zip> <Spec.groovy> <baseline-dir>
  python vs_backup_scan.py strictcheck <storage.zip> <Spec.groovy> <baseline-dir>

strictcheck STATIC prediction of what a strict base class (DcViewsheetTest: every baseline file must
            be exported by the run) would complain about. Predicts the exported file names from the
            spec's bookmarks and the viewsheet's exportable assemblies, then diffs against the
            baseline folder. A prediction, not a run: visibility scripts and bookmark states are
            not evaluated, so confirm by running before acting on it.

inventory   per-viewsheet assembly counts, bound source tables, and a calc-field matrix
            (kind x dataType, expression class, usage position, defined-but-unused).
crosscheck  spec feature -> viewsheet -> baseline folder. Reports viewsheets with no case,
            cases whose viewsheet is missing from the zip, cases with no baseline, and
            calc-field names that never appear in the case's baseline .txt files.

Semantics that were verified against product source (CalculateRef.java):
  baseOnDetail defaults to TRUE and is written ONLY when false. So an XML CalculateRef with
  no baseOnDetail attribute is DETAIL (row-level); baseOnDetail="false" is AGGREGATE.
Limits: positions are classified from XML ancestry, name matching is textual. Treat counts as
"present / absent", not as exact usage counts, and confirm anything surprising by hand.
"""
import collections
import json
import os
import re
import sys
import zipfile
import xml.etree.ElementTree as ET

AGG_FN = {'Sum', 'Count', 'Max', 'Min', 'Average', 'First', 'Last', 'Mode', 'Correlation',
          'DistinctCount', 'NthMostFrequent', 'NthSmallest', 'NthLargest', 'PthPercentile',
          'WeightedAverage', 'Median', 'Variance', 'StandardDeviation', 'Covariance', 'Product'}
# components whose data ViewsheetTest.executeVS0 exports (verify per module before trusting)
EXPORTED = {'Chart', 'Table', 'Crosstab', 'CalcTable'}


def load(zpath):
    z = zipfile.ZipFile(zpath)
    bi = json.loads(z.read('blob-index.json'))
    out = {}
    for store, items in bi.items():
        for key, v in items.items():
            # '1^128^' = viewsheet asset. '4^16384^' keys are VSBookmark storage (see load_bookmarks),
            # NOT autosave copies (an earlier version of this comment was wrong).
            if key.startswith('1^128^') and v.get('digest'):
                d = v['digest']
                out[key] = ET.fromstring(z.read('blobs/%s/%s' % (d[:2], d[2:])))
    return out


def load_bookmarks(zpath):
    """asset id ('1^128^__NULL__^<path>') -> {bookmark name: decoded viewsheet-state XML root}.

    Bookmark storage keys look like 4^16384^<user>^1__128____NULL____<path with / as __>__<org>^<org>;
    each <bookmark> holds the viewsheet state as hex-encoded XML (selections, calendar range, ...)."""
    z = zipfile.ZipFile(zpath)
    bi = json.loads(z.read('blob-index.json'))
    out = {}
    for store, items in bi.items():
        for key, v in items.items():
            m = re.match(r'4\^16384\^[^^]*\^1__128____NULL____(.+?)__[^_^]+\^', key)
            if not m or not v.get('digest'):
                continue
            d = v['digest']
            root = ET.fromstring(z.read('blobs/%s/%s' % (d[:2], d[2:])))
            states = {}
            for b in root.findall('bookmark'):
                try:
                    states[b.findtext('name')] = ET.fromstring(
                        bytes.fromhex((b.findtext('value') or '').strip()).decode('utf8', 'replace'))
                except Exception:
                    states[b.findtext('name')] = None
            out['1^128^__NULL__^' + m.group(1).replace('__', '/')] = states
    return out


def short(cls):
    return cls.rsplit('.', 1)[-1].replace('VSAssembly', '')


def vs_name(key):
    return key.split('^')[3]


def asset_id(key):
    """Spec asset ids omit the trailing organization segment: '1^128^__NULL__^<path>'."""
    return '^'.join(key.split('^')[:4])


def expr_class(e):
    e = (e or '').strip()
    m = re.fullmatch(r"field\['(.+)'\]", e, re.S)
    if m:
        f = re.match(r'([A-Za-z]+)\(', m.group(1))
        if not f:
            return 'field-ref'
        return 'agg-fn' if f.group(1) in AGG_FN else 'datepart-fn'
    if e.startswith('CALC.'):
        return 'date-fn(CALC.)'
    if e.startswith('concat'):
        return 'string-fn'
    return 'other'


def role(asm, tags):
    t = tags
    if asm == 'Chart':
        if t & {'dataColumnsVisibility', 'columnsVisibility'}:
            return None
        if 'VSAestheticRef' in t:
            for k in ('color', 'shape', 'size', 'text'):
                if k in t:
                    return 'visual:' + k
            return 'visual:other'
        if 'axisDescriptor' in t:
            return 'axis-format'
        if 'xrefs' in t:
            return 'x'
        if 'yrefs' in t:
            return 'y'
        if 'groupInfo' in t:
            return 'bound-dim' if 'groups' in t else 'bound-aggr'
        return 'other'
    if asm == 'Crosstab':
        if 'conditions' in t:
            return 'condition'
        if t & {'rowHeaders', 'rows'}:
            return 'row'
        if t & {'colHeaders', 'cols'}:
            return 'col'
        if 'groups' in t:
            return 'dim'
        if t & {'aggregates', 'runtime_aggregates'}:
            return 'aggr'
        return 'other'
    if asm == 'CalcTable':
        if 'columnsVisibility' in t:
            return None
        for k, r in (('cellBinding', 'cell-binding'), ('conditions', 'condition'),
                     ('groups', 'dim'), ('aggregates', 'aggr')):
            if k in t:
                return r
        return 'other'
    if asm == 'Table':
        return 'condition' if 'conditions' in t else 'column'
    if 'conditions' in t:
        return 'condition'
    for k in ('titleInfo', 'bindingInfo', 'timeInfo', 'dataRefs'):
        if k in t:
            return 'title-text' if k == 'titleInfo' else k
    return 'bound'


def calc_defs(root):
    """name -> (kind, dataType, exprClass, expression)"""
    defs = {}
    ac = root.find('.//allcalc')
    if ac is None or ac.find('values') is None:
        return defs
    for val in ac.find('values'):
        cr = val.find('dataRef')
        ex = cr.find("dataRef[@class='inetsoft.uql.erm.ExpressionRef']") if cr is not None else None
        if ex is None or not ex.get('name'):
            continue
        bod = cr.get('baseOnDetail')
        kind = 'detail' if bod is None else ('aggregate(onDetail)' if bod == 'true' else 'aggregate')
        defs[ex.get('name')] = (kind, ex.get('dataType') or 'string',
                                expr_class(ex.text), (ex.text or '').strip())
    return defs


def usages(root, names):
    """(assemblyClass, role) -> set(calc names)"""
    parent = {c: p for p in root.iter() for c in p}
    res = collections.defaultdict(set)
    for e in root.iter():
        if e.tag == 'dataRef' and e.get('class', '').endswith('ExpressionRef'):
            continue
        hit = next((v for v in list(e.attrib.values()) + [(e.text or '').strip()] if v in names), None)
        if not hit:
            continue
        a, tags, asm = e, set(), None
        while a in parent:
            a = parent[a]
            if a.tag in ('allcalc', 'allaggr'):
                asm = 'DEF'
                break
            if a.tag == 'assembly' and a.get('class', '').endswith('VSAssembly'):
                asm = short(a.get('class'))
                break
            if a.tag != 'dataRef':
                tags.add(a.tag)
        if asm in (None, 'DEF'):
            continue
        r = role(asm, tags)
        if r:
            res[(asm, r)].add(hit)
    return res


def table(rows, header):
    print('| ' + ' | '.join(header) + ' |')
    print('|' + '|'.join('---' for _ in header) + '|')
    for r in rows:
        print('| ' + ' | '.join(str(c) for c in r) + ' |')
    print()


def sources(root):
    s = set()
    for a in root.iter('assembly'):
        if a.get('class', '').endswith('VSAssembly'):
            si = a.find('.//sourceInfo')
            if si is None:
                si = a.find('.//source')
            if si is not None:
                s.add((si.findtext('source') or si.get('source') or '?'))
    return sorted(s)


def inventory(zpath):
    vss = load(zpath)
    print('# Inventory: %s\n' % os.path.basename(zpath))
    bms = load_bookmarks(zpath)
    print('%d viewsheets; %d bookmarks in %d bookmark stores (bookmarks hold saved selection state)\n'
          % (len(vss), sum(len(b) for b in bms.values()), len(bms)))
    rows, tot, defs_all, use_all = [], collections.Counter(), [], collections.defaultdict(lambda: collections.defaultdict(set))
    scripts = 0
    for key in sorted(vss, key=vs_name):
        root = vss[key]
        n = vs_name(key)
        cnt = collections.Counter(short(a.get('class')) for a in root.iter('assembly')
                                  if a.get('class', '').endswith('VSAssembly'))
        tot.update(cnt)
        d = calc_defs(root)
        for k, v in d.items():
            defs_all.append((n, k) + v)
        for k, v in usages(root, set(d)).items():
            use_all[k][n] |= v
        scripts += len(root.findall('.//script'))
        rows.append((n, sum(cnt.values()), ', '.join('%s %d' % kv for kv in sorted(cnt.items())),
                     len(d), ', '.join(sources(root))))
    table(rows, ['viewsheet', 'assemblies', 'by type', 'calc fields', 'bound table'])
    print('Assembly totals: ' + ', '.join('%s %d' % kv for kv in tot.most_common()))
    print('\n`<script` elements: %d\n' % scripts)

    print('## Calc fields: kind x dataType (%d definitions)\n' % len(defs_all))
    kinds = sorted({d[2] for d in defs_all})
    types = sorted({d[3] for d in defs_all})
    table([[k] + [sum(1 for d in defs_all if d[2] == k and d[3] == t) for t in types] +
           [sum(1 for d in defs_all if d[2] == k)] for k in kinds], ['kind'] + types + ['total'])
    print('kind: `detail` = no baseOnDetail attribute (row-level); `aggregate` = baseOnDetail="false".\n')
    print('## Calc fields: expression class x kind\n')
    table([[ec] + [sum(1 for d in defs_all if d[4] == ec and d[2] == k) for k in kinds]
           for ec in sorted({d[4] for d in defs_all})], ['expression class'] + kinds)
    print('## Usage position -> viewsheets covering it (of %d)\n' % len(vss))
    dmap = {(d[0], d[1]): d for d in defs_all}
    rows = []
    for pos in sorted(use_all):
        vsmap = use_all[pos]
        dts = collections.Counter(dmap[(v, n)][3] for v, ns in vsmap.items() for n in ns)
        rows.append(('%s:%s' % pos, len(vsmap), dict(dts)))
    table(rows, ['assembly:position', 'viewsheets', 'calc dataTypes seen'])
    used = {(v, n) for m in use_all.values() for v, ns in m.items() for n in ns}
    unused = [(d[0], d[1]) for d in defs_all if (d[0], d[1]) not in used]
    print('Defined but not referenced by any assembly: %d %s\n' % (len(unused), unused[:10]))
    print('Components NOT exported by ViewsheetTest.executeVS0 (check the module base class): '
          + ', '.join(sorted(c for c in tot if c not in EXPORTED)))


def parse_spec(path):
    """feature name -> asset id (first ViewsheetTest/DcViewsheetTest asset in the feature block)."""
    text = open(path, encoding='utf8', errors='ignore').read()
    parts = re.split(r"\n\s*def\s+'([^']+)'\s*\(", text)
    out = {}
    for i in range(1, len(parts), 2):
        m = re.search(r"new\s+(?:Dc)?ViewsheetTest\(\s*'([^']+)'", parts[i + 1])
        if m:
            out[parts[i]] = m.group(1)
    return out


def crosscheck(zpath, spec, baseline):
    vss = load(zpath)
    byid = {asset_id(k): r for k, r in vss.items()}
    feats = parse_spec(spec)
    print('# Crosscheck: %s\n' % os.path.basename(spec))
    print('%d viewsheets in zip, %d spec features with a viewsheet\n' % (len(byid), len(feats)))
    used_ids = set(feats.values())
    orphans = sorted(i.split('^')[3] for i in byid if i not in used_ids)
    missing = sorted((f, a) for f, a in feats.items() if a not in byid)
    print('Viewsheets with NO spec feature: %s' % (orphans or 'none'))
    print('Spec features whose viewsheet is NOT in the zip: %s\n' % (missing or 'none'))
    rows = []
    for f, a in sorted(feats.items()):
        root = byid.get(a)
        d = os.path.join(baseline, f)
        txt = png = 0
        text = ''
        if os.path.isdir(d):
            for r, _, fs in os.walk(d):
                for x in fs:
                    if x.endswith('.png'):
                        png += 1
                    elif x.endswith('.txt'):
                        txt += 1
                        text += open(os.path.join(r, x), encoding='utf8', errors='ignore').read()
        names = set(calc_defs(root)) if root is not None else set()
        absent = sorted(n for n in names if n not in text)
        rows.append((f, 'yes' if os.path.isdir(d) else 'NO BASELINE', txt, png, len(names),
                     '%d/%d' % (len(names) - len(absent), len(names)), ', '.join(absent[:4])))
    table(rows, ['feature', 'baseline dir', 'txt', 'png', 'calc fields', 'in baseline txt', 'not in baseline'])
    print('"not in baseline" is a substring match: a calc field used only as a condition, or shown '
          'under another header, is a false positive. Confirm by hand.')


def spec_bookmarks(path):
    """feature -> (asset id, bookmarks passed to executeVS, bookmarks passed to exportAsPNG or None)."""
    text = open(path, encoding='utf8', errors='ignore').read()
    parts = re.split(r"\n\s*def\s+'([^']+)'\s*\(", text)
    out = {}

    def names(m):
        return re.findall(r"'([^']+)'", m.group(1)) if m else None
    for i in range(1, len(parts), 2):
        body = parts[i + 1]
        a = re.search(r"new\s+(?:Dc)?ViewsheetTest\(\s*'([^']+)'", body)
        if not a:
            continue
        ex = re.search(r"executeVS\(\s*\w+\s*,\s*\[([^\]]*)\]", body)
        png = re.search(r"exportAsPNG\(\s*\[([^\]]*)\]", body)
        out[parts[i]] = (a.group(1), names(ex) or ['(Home)'], names(png) if png else None)
    return out


def exported_names(root, bks, png_bks, vsname):
    """Predict ViewsheetTest.executeVS0/exportAsPNG output file names (see createExportFileByCase)."""
    files = set()
    for bk in bks:
        pre = '' if bk in ('Home', '(Home)') else bk + '_'
        for a in root.iter('assembly'):
            c = short(a.get('class', ''))
            if not a.get('class', '').endswith('VSAssembly') or c not in EXPORTED:
                continue
            vis = a.find('assemblyInfo/visible')
            if vis is not None and vis.get('value') == 'false':
                continue
            nm = a.findtext('assemblyInfo/name') or a.findtext('name')
            if not nm:
                continue
            files.add(pre + nm + '.txt')
            if c == 'Chart':
                files.add(pre + nm + '.png')
    if png_bks is not None:
        files.add(vsname + '.png')   # whole-viewsheet export, named by the viewsheet
    return files


def strictcheck(zpath, spec, baseline):
    vss = load(zpath)
    byid = {asset_id(k): r for k, r in vss.items()}
    bms = load_bookmarks(zpath)
    feats = spec_bookmarks(spec)
    print('# Strict-mode prediction: %s (STATIC, confirm by running)\n' % os.path.basename(spec))
    rows, risky, nobase = [], 0, 0
    for f, (aid, bks, png) in sorted(feats.items()):
        root = byid.get(aid)
        if root is None:
            continue
        have = set(bms.get(aid, {}))
        missing_bk = [b for b in bks + (png or []) if b not in have and b not in ('Home', '(Home)')]
        d = os.path.join(baseline, f)
        base = set()
        if os.path.isdir(d):
            for r, _, fs in os.walk(d):
                base |= {x for x in fs if (x.endswith('.txt') or x.endswith('.png')) and not x.endswith('_diff.png')}
        pred = exported_names(root, bks, png, aid.split('^')[3].split('/')[-1])
        extra, absent = sorted(base - pred), sorted(pred - base)
        risky += bool(extra)
        nobase += bool(absent)
        rows.append((f, ','.join(bks), len(base), len(pred), len(extra), ', '.join(extra[:3]),
                     len(absent), ', '.join(absent[:3]), ', '.join(missing_bk)))
    table(rows, ['feature', 'bookmarks run', 'baseline files', 'predicted exports',
                 'baseline NOT predicted', 'examples', 'predicted NOT in baseline', 'examples',
                 'bookmark missing in zip'])
    print('Features whose baseline has files the run is not predicted to export (strict mode would fail '
          'on these, if the prediction is right): %d of %d' % (risky, len(rows)))
    print('Features where predicted exports have no baseline file (today silently skipped): %d of %d\n'
          % (nobase, len(rows)))
    print('Prediction limits: assembly visibility scripts, bookmark-specific visibility and assemblies '
          'only present in a bookmark state are not evaluated.')


def main(argv):
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass
    if len(argv) >= 3 and argv[1] == 'inventory':
        inventory(argv[2])
    elif len(argv) >= 5 and argv[1] == 'crosscheck':
        crosscheck(argv[2], argv[3], argv[4])
    elif len(argv) >= 5 and argv[1] == 'strictcheck':
        strictcheck(argv[2], argv[3], argv[4])
    else:
        print(__doc__)
        return 2
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
