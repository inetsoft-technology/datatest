#!/usr/bin/env python3
"""Static scan of a StyleBI backup zip (blob storage) used by datatest viewsheet specs.

Read-only. Standard library only. Prints Markdown to stdout.

  python vs_backup_scan.py inventory  <storage.zip>
  python vs_backup_scan.py crosscheck <storage.zip> <Spec.groovy> <baseline-dir>
  python vs_backup_scan.py strictcheck <storage.zip> <Spec.groovy> <baseline-dir>

strictcheck STATIC prediction of which baseline files a run would not export (ViewsheetTest is
            strict since #203: every baseline file must be exported). Predicts the exported file
            names from the spec's executeVS/exportAsPNG calls and the viewsheet's exportable
            assemblies, then diffs against the baseline folder. Running the spec gives the real
            answer and is preferred; use this only as a quick first look.

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

Applicability (the script was written for vscalc; check this before trusting it on a module):
  inventory   viewsheet/bookmark/assembly/bound-table/script counts are generic. The calc-field
              tables only mean something for calc-field modules; other modules need their own
              matrix dimensions, which this script does not compute.
  crosscheck  maps a case to a viewsheet only when the test object is built with a literal asset
              id. Cases that build it from a variable are reported in a "Not mapped" line, and
              their viewsheets can show up as false orphans. The "calc fields" columns are
              vscalc-specific.
  strictcheck rows it cannot predict are marked "not predicted" with the reason (subclass with
              its own exports, executeVS bookmarks not literal) and left out of the totals.
              exportAsPDF output (<vs>.pdf, <vs>-N.png) is not predicted and is left out of the diff.
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
# ViewsheetTest and its subclasses as used by the specs. Subclasses add exports of their own
# (VSCalcTest *_CALC.png, VSFormImportTest *_Import.png, VSActionsTest per-action files).
TEST_NEW = re.compile(r"new\s+((?:Dc)?ViewsheetTest|VSCalcTest|VSActionsTest|VSFormImportTest)\(\s*([^,)]*)")
SUBCLASSES_WITH_OWN_EXPORTS = {'VSCalcTest', 'VSActionsTest', 'VSFormImportTest'}


def spec_features(path):
    """feature name -> (test class, asset id or None when not a literal) for the first test object."""
    text = open(path, encoding='utf8', errors='ignore').read()
    parts = re.split(r"\n\s*def\s+'([^']+)'\s*\(", text)
    out = {}
    for i in range(1, len(parts), 2):
        m = TEST_NEW.search(parts[i + 1])
        if m:
            lit = re.fullmatch(r"\s*'([^']+)'\s*", m.group(2))
            out[parts[i]] = (m.group(1), lit.group(1) if lit else None)
    return out


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
        return 'other'
    if asm == 'Crosstab':
        if 'conditions' in t:
            return 'condition'
        if t & {'rowheaders', 'runtime_rowheaders'}:
            return 'row'
        if t & {'colheaders', 'runtime_colheaders'}:
            return 'col'
        if t & {'aggregates', 'runtime_aggregates'}:
            return 'aggr'
        return 'other'
    if asm == 'CalcTable':
        if 'columnsVisibility' in t:
            return None
        for k, r in (('cellBinding', 'cell-binding'), ('conditions', 'condition')):
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
    """(assemblyClass, role) -> set(calc names)

    <ainfo> (Chart/Crosstab/CalcTable AggregateInfo) is the candidate field list that the binding
    pane shows for the source table: every calc field of that table appears there whether or not it
    is bound, so it is skipped. Real bindings are xrefs/yrefs/aesthetics (Chart), rowheaders/
    colheaders/aggregates (Crosstab) and tableLayout cellBinding (CalcTable)."""
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
            if a.tag in ('allcalc', 'allaggr', 'ainfo'):
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
    # a calc field can be used only through another calc field's expression (TopFormula =
    # Mode([BaseFormula])+10); that is a real use, not dead code
    def in_other_expr(d):
        pat = re.compile(r"\[%s\]|\['%s'\]" % (re.escape(d[1]), re.escape(d[1])))
        return any(o[0] == d[0] and o[1] != d[1] and pat.search(o[5] or '') for o in defs_all)
    unbound = [d for d in defs_all if (d[0], d[1]) not in used]
    indirect = [(d[0], d[1]) for d in unbound if in_other_expr(d)]
    unused = [(d[0], d[1]) for d in unbound if not in_other_expr(d)]
    print('Defined but not referenced by any assembly or other calc field: %d %s\n'
          % (len(unused), unused[:10]))
    print('Referenced only through another calc field\'s expression: %d %s\n'
          % (len(indirect), indirect[:10]))
    print('Components NOT exported by ViewsheetTest.executeVS0 (check the module base class): '
          + ', '.join(sorted(c for c in tot if c not in EXPORTED)))


def parse_spec(path):
    """feature name -> asset id (first ViewsheetTest-family test object with a literal asset id)."""
    return {f: a for f, (_, a) in spec_features(path).items() if a}


def crosscheck(zpath, spec, baseline):
    vss = load(zpath)
    byid = {asset_id(k): r for k, r in vss.items()}
    feats = parse_spec(spec)
    print('# Crosscheck: %s\n' % os.path.basename(spec))
    print('%d viewsheets in zip, %d spec features with a viewsheet\n' % (len(byid), len(feats)))
    used_ids = set(feats.values())
    orphans = sorted(i.split('^')[3] for i in byid if i not in used_ids)
    missing = sorted((f, a) for f, a in feats.items() if a not in byid)
    unmapped = sorted(f for f, (_, a) in spec_features(spec).items() if not a)
    print('Viewsheets with NO spec feature: %s' % (orphans or 'none'))
    if unmapped:
        print('Not mapped (asset id is not a literal, so its viewsheet may be a FALSE orphan above): %s'
              % unmapped)
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
        where = collections.defaultdict(set)
        if root is not None:
            for (asm, _), ns in usages(root, names).items():
                for n in ns:
                    where[n].add(asm)
        def label(n):
            if not where[n]:
                return '%s (no binding)' % n
            if not where[n] & EXPORTED:
                return '%s (only %s: not exported)' % (n, '/'.join(sorted(where[n])))
            return '%s (in %s: header may differ)' % (n, '/'.join(sorted(where[n] & EXPORTED)))
        rows.append((f, 'yes' if os.path.isdir(d) else 'NO BASELINE', txt, png, len(names),
                     '%d/%d' % (len(names) - len(absent), len(names)),
                     ', '.join(label(n) for n in absent[:4])))
    table(rows, ['feature', 'baseline dir', 'txt', 'png', 'calc fields', 'in baseline txt', 'not in baseline'])
    print('"not in baseline" is a substring match on the calc-field name. The note says why it may be '
          'absent: "no binding" (defined, or used only inside another calc field), "not exported" '
          '(only in components executeVS0 does not export: no data assertion, only PNG), "header may differ" '
          '(bound in an exported component, usually shown as a date level or named group: check '
          'the txt by hand).')


def spec_bookmarks(path):
    """feature -> (asset id, executeVS bookmarks, exportAsPNG bookmarks or None, uses exportAsPDF,
    reason the exports cannot be predicted or None).

    executeVS bookmarks: [] when the case does not call executeVS; a null list means (Home).
    The bookmark argument is picked by call shape (ViewsheetTest signatures):
      executeVS(params, bks)                      -> 2nd argument
      exportAsPNG(params, bks)                    -> 2nd argument
      exportAsPNG(bks, params, match, expand)     -> 1st argument
    It must be null or a list of plain string literals; anything else (a variable, a call, an
    unknown argument count) makes the case "not predicted" instead of guessing."""
    text = open(path, encoding='utf8', errors='ignore').read()
    parts = re.split(r"\n\s*def\s+'([^']+)'\s*\(", text)
    out = {}
    pairs = {'(': ')', '[': ']', '{': '}'}

    def call_args(body, method):
        """Argument lists (split at top-level commas) of every call to method in body.
        Parentheses, brackets, braces and quoted strings are matched, so nesting depth is not
        limited. A call that is not closed is returned as None."""
        calls = []
        for m in re.finditer(r"\b" + method + r"\(", body):
            i, stack, quote, args, cur = m.end(), ['('], None, [], ''
            while i < len(body) and stack:
                c = body[i]
                if quote:
                    if c == '\\':
                        cur += body[i:i + 2]
                        i += 2
                        continue
                    if c == quote:
                        quote = None
                elif c in ('"', "'"):
                    quote = c
                elif c in pairs:
                    stack.append(c)
                elif c in pairs.values():
                    stack.pop()
                    if not stack:
                        break
                elif c == ',' and len(stack) == 1:
                    args.append(cur.strip())
                    cur = ''
                    i += 1
                    continue
                cur += c
                i += 1
            if stack:
                calls.append(None)
                continue
            if cur.strip() or args:
                args.append(cur.strip())
            calls.append(args)
        return calls

    def bookmark_arg(method, args):
        if args is None:
            return None
        if method == 'executeVS' and len(args) == 2:
            return args[1]
        if method == 'exportAsPNG' and len(args) == 2:
            return args[1]
        if method == 'exportAsPNG' and len(args) == 4:
            return args[0]
        return None

    def bookmarks(method, args):
        """(bookmark names, ok). ok is False when they cannot be read statically."""
        arg = bookmark_arg(method, args)
        if arg is None:
            return [], False
        if arg == 'null':
            return ['(Home)'], True
        m = re.fullmatch(r"\[(.*?)\](?:\s+as\s+String\[\])?", arg, re.S)
        if not m:
            return [], False
        items = [x.strip() for x in m.group(1).split(',') if x.strip()]
        if not all(re.fullmatch(r"'[^']*'|\"[^\"]*\"", x) for x in items):
            return [], False
        return [x[1:-1] for x in items], True

    for i in range(1, len(parts), 2):
        body = parts[i + 1]
        a = TEST_NEW.search(body)
        if not a:
            continue
        cls = a.group(1)
        lit = re.fullmatch(r"\s*'([^']+)'\s*", a.group(2))
        reason = None
        if not lit:
            reason = 'asset id not literal'
        elif cls in SUBCLASSES_WITH_OWN_EXPORTS:
            reason = 'subclass %s has its own exports' % cls
        ex_bks, png_bks = [], None
        for args in call_args(body, 'executeVS'):
            b, ok = bookmarks('executeVS', args)
            ex_bks += [x for x in b if x not in ex_bks]
            if not ok and not reason:
                reason = 'executeVS bookmarks not readable: %s' % (', '.join(args) if args else 'unclosed call')
        for args in call_args(body, 'exportAsPNG'):
            b, ok = bookmarks('exportAsPNG', args)
            png_bks = (png_bks or []) + [x for x in b if x not in (png_bks or [])]
            if not ok and not reason:
                reason = 'exportAsPNG bookmarks not readable: %s' % (', '.join(args) if args else 'unclosed call')
        pdf = bool(call_args(body, 'exportAsPDF'))
        out[parts[i]] = (lit.group(1) if lit else None, ex_bks, png_bks, pdf, reason)
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
    rows, skipped, risky, nobase, pdfs = [], [], 0, 0, 0
    for f, (aid, bks, png, pdf, reason) in sorted(feats.items()):
        root = byid.get(aid) if aid else None
        if reason or root is None:
            skipped.append((f, reason or 'viewsheet not in zip'))
            continue
        vsname = aid.split('^')[3].split('/')[-1]
        have = set(bms.get(aid, {}))
        missing_bk = [b for b in bks + (png or []) if b not in have and b not in ('Home', '(Home)')]
        d = os.path.join(baseline, f)
        base = set()
        if os.path.isdir(d):
            for r, _, fs in os.walk(d):
                base |= {x for x in fs if (x.endswith('.txt') or x.endswith('.png')) and not x.endswith('_diff.png')}
        if pdf:
            # exportAsPDF writes <vs>.pdf and renders <vs>-N.png pages; the page count is not predicted
            pdfs += 1
            base = {x for x in base if not re.fullmatch(re.escape(vsname) + r'-\d+\.png', x)}
        pred = exported_names(root, bks, png, vsname)
        extra, absent = sorted(base - pred), sorted(pred - base)
        risky += bool(extra)
        nobase += bool(absent)
        rows.append((f, ','.join(bks) or 'no executeVS call', 'yes' if png is not None else '',
                     'yes' if pdf else '',
                     len(base), len(pred), len(extra), ', '.join(extra[:3]),
                     len(absent), ', '.join(absent[:3]), ', '.join(missing_bk)))
    table(rows, ['feature', 'executeVS bookmarks', 'exportAsPNG', 'exportAsPDF (pages ignored)',
                 'baseline files', 'predicted exports', 'baseline NOT predicted', 'examples',
                 'predicted NOT in baseline', 'examples', 'bookmark missing in zip'])
    print('Features whose baseline has files the run is not predicted to export (strict mode would fail '
          'on these, if the prediction is right): %d of %d predicted' % (risky, len(rows)))
    print('Features where predicted exports have no baseline file (CompareUtil fails these with '
          '"not found"): %d of %d predicted' % (nobase, len(rows)))
    if pdfs:
        print('Features with exportAsPDF: %d; their <vs>-N.png pages are left out of the diff.' % pdfs)
    if skipped:
        print('\n**Not predicted: %d of %d features.** Run them instead.\n' % (len(skipped), len(feats)))
        table(skipped, ['feature', 'reason'])
    print('Prediction limits: assembly visibility scripts, bookmark-specific visibility and assemblies '
          'only present in a bookmark state are not evaluated. Confirm by running the spec.')


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
