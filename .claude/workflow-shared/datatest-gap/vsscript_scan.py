"""Static scanner for the vsscript module: spec cases x zip assets x product script API.
Usage: python vsscript_scan.py cases|zip|api|usage|zero <Scriptable...>|declzero
Only valid for vsscript's VSScriptTest/printVS spec style. Text matches are upper bounds; zero hits are reliable only for
the script path (see docs/test-gap-assessment/vsscript.md). Product source: env PRODUCT_CORE (default below).
"""
import sys, re, glob, os, json, collections
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import vs_backup_scan as v

DT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', '..'))
ZIP = os.path.join(DT, 'vsscript', 'src', 'test', 'resources', 'config', 'vsscriptStorage.zip')
SPECS = sorted(glob.glob(os.path.join(DT, 'vsscript', 'src', 'test', 'groovy', '**', '*_Spec.groovy'), recursive=True))
# Product source (branch main) is required for api/usage/zero/declzero: set PRODUCT_CORE to the product's `core` directory.
_CORE = os.environ.get('PRODUCT_CORE')
PROD = os.path.join(_CORE or '', 'src', 'main', 'java', 'inetsoft', 'report', 'script', 'viewsheet')
_USAGE = 'usage: PRODUCT_CORE=<product>/community/core python vsscript_scan.py cases|zip|api|usage|zero <Scriptable...>|declzero'
_CMDS = ('cases', 'zip', 'api', 'usage', 'zero', 'declzero')
if __name__ == '__main__' and (len(sys.argv) < 2 or sys.argv[1] not in _CMDS):
    sys.exit(_USAGE)
if __name__ == '__main__' and sys.argv[1] != 'cases' and sys.argv[1] != 'zip' and not _CORE:
    sys.exit('PRODUCT_CORE is not set. ' + _USAGE)


def read(p):
    return open(p, encoding='utf-8', errors='replace').read()


def lit(raw):
    """concatenate groovy string literals in `raw` ('..' + '..')."""
    out = []
    for a, b in re.findall(r"'((?:[^'\\]|\\.)*)'|\"((?:[^\"\\]|\\.)*)\"", raw):
        s = a or b
        out.append(s.replace('\\n', '\n').replace('\\"', '"').replace("\\'", "'"))
    return ''.join(out)


def parse_cases(path):
    t = read(path)
    parts = re.split(r"\n\s+def\s+(?:'([^']+)'|\"([^\"]+)\")\s*\(\s*\)\s*\{", t)
    cases = []
    for i in range(1, len(parts), 3):
        name = parts[i] or parts[i + 1]
        body = parts[i + 2]
        c = dict(spec=os.path.relpath(path, DT).split('cases' + os.sep)[-1], name=name, body=body)
        c['vs'] = re.findall(r"'(1\^128\^[^']*)'", body)
        c['handlers'] = re.findall(r"HANDLER:\s*'([^']*)'", body)
        cmds = []
        for m in re.finditer(r"COMMAND:\s*((?:'(?:[^'\\]|\\.)*'|\"(?:[^\"\\]|\\.)*\"|\s|\+)+)", body):
            cmds.append(lit(m.group(1)))
        c['cmds'] = cmds
        c['printvs'] = len(re.findall(r"\.printVS\(", body))
        c['compare'] = re.findall(r"compareImage\(([^)]*)\)", body, re.S)
        cases.append(c)
    return cases


def all_cases():
    out = []
    for sp in SPECS:
        out += parse_cases(sp)
    return out


def zip_assemblies():
    root = v.load(ZIP)
    res = {}
    for key, r in root.items():
        d = {}
        for a in r.iter('assembly'):
            c = a.get('class', '')
            if not c.endswith('VSAssembly'):
                continue
            info = a.find('assemblyInfo')
            nm = info.find('name').text if info is not None and info.find('name') is not None else None
            sc = info.find('script') if info is not None else None
            d[nm] = (v.short(c), (sc.text or '').strip() if sc is not None else '')
        res[v.asset_id(key)] = dict(asm=d, root=r)
    return res


def prod_api():
    """class -> (extends, set(props)); props from addProperty("x" / addFunctionProperty(cls,"x"."""
    cls = {}
    for f in glob.glob(PROD + r'\*.java'):
        t = read(f)
        n = os.path.basename(f)[:-5]
        m = re.search(r'class\s+' + n + r'\s+extends\s+(\w+)', t)
        props = set(re.findall(r'addProperty\(\s*"(\w+)"', t)) | set(
            re.findall(r'addFunctionProperty\(\s*[\w.]+\s*,\s*"(\w+)"', t))
        cls[n] = (m.group(1) if m else None, props)
    return cls


def api_full(cls, n):
    seen, props = set(), set()
    while n in cls and n not in seen:
        seen.add(n)
        props |= cls[n][1]
        n = cls[n][0]
    return props


if __name__ == '__main__':
    cmd = sys.argv[1]
    if cmd == 'cases':
        tot = 0
        for sp in SPECS:
            cs = parse_cases(sp)
            tot += len(cs)
            print(os.path.relpath(sp, DT), len(cs))
        print('total', tot)
    elif cmd == 'zip':
        z = zip_assemblies()
        tot = collections.Counter()
        for k, d in z.items():
            tot.update(t for t, s in d['asm'].values())
        print(len(z), 'vs;', sum(tot.values()), 'assemblies')
        print(tot.most_common())
        print('assemblies with a script stored in the asset:', sum(1 for d in z.values() for t, s in d['asm'].values() if s))
    elif cmd == 'api':
        c = prod_api()
        for n in sorted(c):
            print(n, c[n][0], len(c[n][1]), len(api_full(c, n)))


TYPE2SCR = {  # assembly short type -> scriptable class
    'Chart': 'ChartVSAScriptable', 'Crosstab': 'CrosstabVSAScriptable', 'CalcTable': 'CalcTableVSAScriptable',
    'Table': 'TableVSAScriptable', 'EmbeddedTable': 'TableVSAScriptable', 'Text': 'TextVSAScriptable',
    'RadioButton': 'RadioButtonVSAScriptable', 'CheckBox': 'CheckBoxVSAScriptable', 'ComboBox': 'ComboBoxVSAScriptable',
    'Slider': 'SliderVSAScriptable', 'Spinner': 'SpinnerVSAScriptable', 'TextInput': 'TextInputVSAScriptable',
    'Gauge': 'GaugeVSAScriptable', 'Image': 'ImageVSAScriptable', 'Submit': 'SubmitVSAScriptable',
    'SelectionList': 'SelectionListVSAScriptable', 'SelectionTree': 'SelectionTreeVSAScriptable',
    'TimeSlider': 'RangeSliderVSAScriptable', 'Calendar': 'CalendarVSAScriptable',
    'CurrentSelection': 'SelectionContainerVSAScriptable', 'GroupContainer': 'GroupContainerVSAScriptable',
    'Tab': 'TabVSAScriptable', 'Rectangle': 'RectangleVSAScriptable', 'Line': 'LineVSAScriptable',
    'Oval': 'OvalVSAScriptable', 'Thermometer': 'ThermometerVSAScriptable', 'Cylinder': 'CylinderVSAScriptable',
    'SlidingScale': 'SlidingScaleVSAScriptable', 'Viewsheet': 'ViewsheetVSAScriptable',
}


def strip_comments(s):
    s = re.sub(r'/\*.*?\*/', ' ', s, flags=re.S)
    return re.sub(r'//[^\n]*', ' ', s)


def usage():
    z = zip_assemblies()
    cls = prod_api()
    cases = all_cases()
    spec_use = collections.defaultdict(collections.Counter)   # scriptable -> prop -> count (spec scripts)
    asset_use = collections.defaultdict(collections.Counter)  # scriptable -> prop -> count (asset scripts, referenced vs)
    ref_vs = set()
    unresolved = []
    for c in cases:
        ref_vs |= set(v.asset_id(x) if x.count('^') > 3 else x for x in c['vs'])
    # spec scripts
    for c in cases:
        asm = {}
        for x in c['vs']:
            if x in z:
                asm.update({k: t for k, (t, s) in z[x]['asm'].items()})
        hs = c['handlers']
        for i, cmd in enumerate(c['cmds']):
            h = hs[i] if i < len(hs) else None
            cmd = strip_comments(cmd)
            if h in asm:
                typ = asm[h]
            elif h in ('ONINIT', 'ONREFRESH'):
                typ = 'Viewsheet'
            else:
                typ = None
                unresolved.append((c['spec'], c['name'], h))
            # Name.prop references
            for m in re.finditer(r'\b([A-Za-z_]\w*)\.(\w+)', cmd):
                n, p = m.groups()
                if n in asm:
                    s = TYPE2SCR.get(asm[n])
                    if s and p in api_full(cls, s):
                        spec_use[s][p] += 1
            if typ and TYPE2SCR.get(typ):
                s = TYPE2SCR[typ]
                api = api_full(cls, s)
                for p in api:
                    n = len(re.findall(r'(?<![\w.])' + p + r'\b(?!\s*:)', cmd))
                    if n:
                        spec_use[s][p] += n
    # asset scripts of vs referenced by specs
    for x in ref_vs:
        if x in z:
            for nm, (t, s) in z[x]['asm'].items():
                if s and TYPE2SCR.get(t):
                    api = api_full(cls, TYPE2SCR[t])
                    s = strip_comments(s)
                    for p in api:
                        n = len(re.findall(r'(?<![\w.])' + p + r'\b(?!\s*:)', s))
                        if n:
                            asset_use[TYPE2SCR[t]][p] += n
    return z, cls, cases, spec_use, asset_use, ref_vs, unresolved


if __name__ == '__main__' and sys.argv[1] == 'usage':
    z, cls, cases, su, au, ref, unres = usage()
    print('cases', len(cases), 'vs referenced', len(ref), 'in zip', len([x for x in ref if x in z]), 'not in zip', [x for x in ref if x not in z])
    print('unresolved handlers', unres[:20], len(unres))
    # which assembly types in referenced vs
    tc = collections.Counter(t for x in ref if x in z for t, s in z[x]['asm'].values())
    print('types in referenced vs', tc.most_common())
    for s in sorted(set(TYPE2SCR.values())):
        api = api_full(cls, s)
        zero = sorted(p for p in api if su[s][p] == 0 and au[s][p] == 0)
        used = sum(1 for p in api if su[s][p] or au[s][p])
        print('%-34s api %3d used %3d zero %3d' % (s, len(api), used, len(zero)))


def crosscheck_zero(types):
    """For zero-coverage props, count raw text hits in ALL spec text and ALL zip asset scripts (any vs)."""
    z, cls, cases, su, au, ref, unres = usage()
    spec_text = '\n'.join(strip_comments(read(p)) for p in SPECS)
    all_asset = '\n'.join(strip_comments(s) for d in z.values() for t, s in d['asm'].values() if s)
    for s in types:
        api = api_full(cls, s)
        zero = sorted(p for p in api if su[s][p] == 0 and au[s][p] == 0)
        hits = [(p, len(re.findall(r'\b' + p + r'\b', spec_text)), len(re.findall(r'\b' + p + r'\b', all_asset))) for p in zero]
        print(s, 'zero:', len(zero))
        print('   ', ', '.join('%s(spec%d/asset%d)' % h for h in hits))


if __name__ == '__main__' and sys.argv[1] == 'zero':
    crosscheck_zero(sys.argv[2:])


def declared_zero():
    z = zip_assemblies()
    cls = prod_api()
    spec_text = '\n'.join(strip_comments('\n'.join(c['cmds'])) for c in all_cases())
    raw_spec = '\n'.join(strip_comments(read(p)) for p in SPECS)
    all_asset = '\n'.join(strip_comments(s) for d in z.values() for t, s in d['asm'].values() if s)
    tot_decl = tot_zero = 0
    rows = []
    for n in sorted(cls):
        props = cls[n][1]
        if not props:
            continue
        zero, only_asset = [], []
        for p in sorted(props):
            pat = r'(?<![\w])' + p + r'\b'
            a = len(re.findall(pat, spec_text))
            b = len(re.findall(pat, all_asset))
            r = len(re.findall(pat, raw_spec))
            if a == 0 and b == 0 and r == 0:
                zero.append(p)
            elif a == 0 and r == 0:
                only_asset.append(p)
        rows.append((n, len(props), zero, only_asset))
        tot_decl += len(props)
        tot_zero += len(zero)
    for n, k, zero, oa in rows:
        print('%s declared %d, no text hit anywhere (spec scripts, spec text, ALL asset scripts): %d' % (n, k, len(zero)))
        if zero:
            print('    zero:', ', '.join(zero))
        if oa:
            print('    asset-script-only:', ', '.join(oa))
    print('TOTAL declared', tot_decl, 'zero', tot_zero)


if __name__ == '__main__' and sys.argv[1] == 'declzero':
    declared_zero()
