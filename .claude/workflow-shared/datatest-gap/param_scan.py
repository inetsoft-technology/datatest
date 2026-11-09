#!/usr/bin/env python3
"""Static scan for parameter / variable-expression features (vspara-like modules).

Read-only, standard library only, prints Markdown. Reuses vs_backup_scan.load().

  python param_scan.py params <storage.zip>
      worksheet variable assemblies (xtype x single/multi x prompt), variables used in worksheet
      conditions (by operation code), SQL ${} / script parameter. use, and per-viewsheet counts of
      dynamic values ('=parameter.x', '$(Component)', other '=expr').
  python param_scan.py cases <Spec.groovy> [<Spec2.groovy> ...]
      per spec case: export method (executeVS = per-assembly TXT/PNG, exportAsPNG = whole page PNG),
      compare calls, bookmarks, parameter names/value counts, whether it sends no parameters.

Limits: regex based, STATIC. Condition operation codes are printed as numbers (see
inetsoft.uql.XCondition / Condition ops in product source before interpreting them).
"""
import collections
import importlib.util
import json
import os
import re
import sys
import zipfile
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))


def _scan():
    spec = importlib.util.spec_from_file_location('vs_backup_scan', os.path.join(HERE, 'vs_backup_scan.py'))
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def load_ws(zpath):
    """worksheet path -> raw XML text (asset type 1^2^)."""
    z = zipfile.ZipFile(zpath)
    bi = json.loads(z.read('blob-index.json'))
    out = {}
    for store, items in bi.items():
        for key, v in items.items():
            if key.startswith('1^2^') and v.get('digest'):
                d = v['digest']
                out[key.split('^')[3]] = z.read('blobs/%s/%s' % (d[:2], d[2:])).decode('utf8', 'replace')
    return out


def params(zpath):
    scan = _scan()
    ws = load_ws(zpath)
    vs = scan.load(zpath)
    print('# Parameter scan: %s\n' % os.path.basename(zpath))
    rows = collections.Counter()
    per_ws = {}
    for n, s in ws.items():
        vs_ = re.findall(r'<variable\s+name="([^"]*)"\s+type="(\w+)"([^>]*)>', s)
        per_ws[n] = len(vs_)
        for name, t, rest in vs_:
            x = re.search(r'xtype="(\w+)"', rest)
            rows[(x.group(1) if x else '-',
                  'multi' if 'multipleSelection="true"' in rest else 'single',
                  'prompt' if 'prompt="true"' in rest else 'noprompt')] += 1
    print('%d worksheets, %d with variable assemblies, %d variable assemblies\n'
          % (len(ws), sum(1 for v in per_ws.values() if v), sum(rows.values())))
    print('| xtype | single | multi | prompt | noprompt |\n|---|---|---|---|---|')
    xs = sorted({k[0] for k in rows})
    for x in xs:
        g = lambda a, b: sum(c for k, c in rows.items() if k[0] == x and k[a] == b)
        print('| %s | %d | %d | %d | %d |' % (x, g(1, 'single'), g(1, 'multi'), g(2, 'prompt'), g(2, 'noprompt')))
    ops = collections.Counter()
    nvar = 0
    for s in ws.values():
        for mm in re.finditer(r'<condition\b([^>]*)>(.*?)</condition>', s, re.S):
            if 'isvariable="true"' in mm.group(2):
                nvar += 1
                o = re.search(r'operation="(\d+)"', mm.group(1))
                ops[o.group(1) if o else '-'] += 1
    print('\nConditions using a variable: %d; by operation code: %s' % (nvar, dict(sorted(ops.items()))))
    print('Worksheets with SQL ${}: %d; with script `parameter.`: %d: %s' % (
        sum(1 for s in ws.values() if '${' in s),
        sum(1 for s in ws.values() if 'parameter.' in s),
        [n for n, s in ws.items() if 'parameter.' in s]))
    print('\n## Viewsheet dynamic values (per viewsheet)\n')
    print('| viewsheet | =parameter.x | $(Component) | other =expr |\n|---|---|---|---|')
    tp = tc = 0
    for k in sorted(vs):
        s = ET.tostring(vs[k], encoding='unicode')
        a = len(re.findall(r'=parameter\.', s))
        b = len(re.findall(r'\$\(\w+\)', s))
        c = len(re.findall(r'>=(?!parameter\.)[^<]+<', s))
        tp += bool(a)
        tc += bool(b)
        print('| %s | %d | %d | %d |' % (scan.vs_name(k), a, b, c))
    print('\nViewsheets using =parameter.: %d of %d; using $(Component): %d' % (tp, len(vs), tc))


def cases(paths):
    print('# Spec cases\n')
    print('| spec | case | vs | method | compare | bookmarks | params (name:values) |\n|---|---|---|---|---|---|---|')
    tot = collections.Counter()
    for p in paths:
        txt = open(p, encoding='utf-8').read()
        sname = os.path.basename(p).replace('.groovy', '')
        for mm in re.finditer(r"def '([^']+)'\(\)\s*\{(.*?)(?=\n\s*(?:/\*|/\*\*|def '|\}\s*$))", txt, re.S):
            name, body = mm.group(1), mm.group(2)
            vsm = re.search(r"(?:ViewsheetTest|VSExportTest)\('1\^128\^__NULL__\^([^']+)'", body)
            method = 'executeVS' if 'executeVS(' in body else 'exportAsPNG' if 'exportAsPNG(' in body \
                else 'exportAsPDF' if 'exportAsPDF(' in body \
                else 'VSExportTest(' + '+'.join(f for f, k in (('PNG', 'testExportAsPNG'), ('HTML', 'testExportAsHtml')) if k in body) + ')' \
                if 'VSExportTest(' in body else '?'
            cmp_ = ','.join(sorted(set('Data' if 'compareData' in l else 'HTML' if 'HTML' in l else 'Image'
                                       for l in re.findall(r'compare\w+', body)))) or '-'
            call = re.search(r"vstest\.(?:executeVS|exportAs\w+)\(([^\n]*)\)", body)
            bk_s = '(Home)'
            if call:
                arr = re.search(r"\[([^\]]*)\]\s+as String\[\]\)?\s*$", call.group(0))
                if arr:
                    bk_s = ','.join(re.findall(r"'([^']+)'", arr.group(1)))
            ps = re.findall(r"paras\.put\('([^']+)',\s*\[?([^\]\n]*)\]?(?:\s+as String\[\])?\)", body)
            ps_s = '; '.join('%s:%d' % (a, len(re.findall(r"'[^']*'", b)) or 1) for a, b in ps) or 'none'
            tot[method] += 1
            print('| %s | %s | %s | %s | %s | %s | %s |' % (
                sname, name, vsm.group(1) if vsm else '?', method, cmp_, bk_s, ps_s))
    print('\nCases by method: %s (total %d)' % (dict(tot), sum(tot.values())))


def main():
    if len(sys.argv) >= 3 and sys.argv[1] == 'params':
        params(sys.argv[2])
    elif len(sys.argv) >= 3 and sys.argv[1] == 'cases':
        cases(sys.argv[2:])
    else:
        print(__doc__)
        sys.exit(2)


if __name__ == '__main__':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass
    main()
