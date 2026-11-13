#!/usr/bin/env python3
"""MV-oriented static scan of the viewsheets in mv/src/test/resources/config/mvStorage.zip.

Read-only. Standard library only (reuses vs_backup_scan.load). Prints Markdown to stdout.

  python mv_vs_scan.py [<repo-root>]        # default: the datatest checkout this file lives in

For each viewsheet it reads the nodes the USER sets (not derived ones): the viewsheetInfo
attributes (full, associationEnabled, bypass, mvOnDemand, meta), timeInfo (single / composite,
rangeType, data type), calendar and selection assemblyInfo attributes, selection measures,
dimension / aggregate refs (formulaValue, percentageRValue, rankingOptRValue, dlevelRValue),
list-input and scalar bindings, calc-field expressions (@nomv, parameter., aggregate references).
Then it intersects each feature with the viewsheets that the mv specs reference by LITERAL asset
id, per profile (cases/<profile>/*_Spec.groovy).

Limits (read before trusting a number):
  * Static presence only. "Referenced by a spec" does not mean a case asserts the MV rule.
  * Specs that build the asset id from a variable are not seen, so a viewsheet can look unreferenced.
  * Written for mvStorage.zip (blob-index.json layout); node names were checked against that zip.
  * rangeType numbers are printed as stored, their meaning is not decoded.
  * The viewsheet-name keyword counts at the end (action words) are name-level only.
"""
import sys, re, glob, os, collections
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import vs_backup_scan as V

ROOT = os.path.abspath(sys.argv[1]) if len(sys.argv) > 1 else os.path.abspath(os.path.join(HERE, '..', '..', '..'))
docs = V.load(os.path.join(ROOT, 'mv', 'src', 'test', 'resources', 'config', 'mvStorage.zip'))

# spec literal ids -> profiles
prof_of = collections.defaultdict(set)
for f in glob.glob(os.path.join(ROOT, 'mv', 'src', 'test', 'groovy', 'inetsoft', 'test', 'mv', 'cases', '*', '*_Spec.groovy')):
    prof = os.path.basename(os.path.dirname(f))
    t = open(f, encoding='utf-8').read()
    for m in re.findall(r"1\^128\^__NULL__\^([^'\"]+)['\"]", t):
        prof_of[m].add(prof)


def name_of(key):
    m = re.match(r"1\^128\^__NULL__\^(.*?)(\^host-org)?$", key)
    return m.group(1) if m else key


def cls(a):
    return a.get('class', '').split('.')[-1].replace('VSAssembly', '')


feat = collections.defaultdict(set)   # feature -> set of vs names


def add(f, vs):
    feat[f].add(vs)


for key, r in docs.items():
    vs = name_of(key)
    vi = r.find('viewsheetInfo')
    if vi is not None and vi.find('viewsheetInfo') is not None:
        vi = vi.find('viewsheetInfo')
    if vi is not None:
        for a in ('full', 'associationEnabled', 'bypass', 'mvOnDemand', 'meta', 'warningIfNotHitMV'):
            v = vi.get(a)
            if v is not None:
                add('viewsheetInfo %s=%s' % (a, v), vs)
            elif a == 'warningIfNotHitMV':
                add('viewsheetInfo warningIfNotHitMV absent', vs)
    for asm in r.iter('assembly'):
        c = cls(asm)
        if c == 'Viewsheet':
            continue
        add('component ' + c, vs)
        ai = asm.find('assemblyInfo')
        if c in ('SelectionList', 'SelectionTree', 'TimeSlider', 'Calendar', 'CurrentSelection'):
            if ai is not None:
                if ai.get('adhoc') == 'true':
                    add('selection adhoc=true (%s)' % c, vs)
                st = ai.get('sourceType')
                if st not in (None, '-1', '0'):
                    add('selection sourceType=%s (%s)' % (st, c), vs)
        if c in ('SelectionList', 'SelectionTree'):
            m = asm.find('.//measureValue')
            if m is not None and (m.text or '').strip():
                add('selection with measure (%s)' % c, vs)
            if c == 'SelectionTree' and asm.find('.//parentID') is not None:
                add('selection tree parent-child', vs)
        if c == 'TimeSlider':
            if ai is not None and ai.get('logScale') == 'true':
                add('timeslider logScale=true', vs)
            for t in asm.iter('timeInfo'):
                k = t.get('class', '').split('.')[-1]
                if k == 'CompositeTimeInfo':
                    add('timeslider composite', vs)
                else:
                    dr = t.find('.//dataRef')
                    dt = dr.get('dataType') if dr is not None else '?'
                    add('timeslider single rangeType=%s dataType=%s' % (t.get('rangeType'), dt), vs)
        if c == 'Calendar' and ai is not None:
            add('calendar daySelection=%s' % ai.get('daySelection'), vs)
            add('calendar type=%s mode=%s' % (ai.get('type'), ai.get('mode')), vs)
        if c in ('Chart', 'Crosstab'):
            for p in asm.iter('percentageRValue'):
                if (p.text or '').strip() not in ('', '0'):
                    add('%s percentage option' % c.lower(), vs)
            for p in asm.iter('rankingOptRValue'):
                v = (p.text or '').strip()
                if v not in ('', '0'):
                    add('%s ranking option=%s' % (c.lower(), v), vs)
            for p in asm.iter('dlevelRValue'):
                v = (p.text or '').strip()
                if v not in ('', '0', 'None'):
                    add('%s date level=%s' % (c.lower(), v), vs)
            if asm.find('.//drill_filters') is not None and len(asm.find('.//drill_filters')) > 0:
                add('%s drill filters stored' % c.lower(), vs)
        if c in ('ComboBox', 'RadioButton', 'CheckBox'):
            bi = asm.find('.//bindingInfo')
            tb = asm.find('.//bindingInfo//table')
            if bi is not None and tb is not None and (tb.text or '').strip():
                add('list input bound to a table (%s)' % c, vs)
        if c in ('Text', 'Gauge', 'Thermometer', 'Cylinder'):
            ag = asm.find('.//aggregateValue')
            col = asm.find('.//columnValue')
            if col is not None and (col.text or '').strip():
                add('output scalar binding (%s) formula=%s' % (c, (ag.text or '').strip() if ag is not None else '?'), vs)
        if c in ('Table', 'Crosstab', 'Chart', 'CalcTable'):
            conds = [e for e in asm.iter('condition')]
            if conds:
                add('%s has a condition list' % c.lower(), vs)
    for p in r.iter('allcalc'):
        txt = ET.tostring(p, encoding='unicode')
        if '@nomv' in txt:
            add('calc field expression with @nomv', vs)
        if 'parameter.' in txt:
            add('calc field expression with parameter.', vs)
        if re.search(r"field\[(?:&apos;|')(?:Sum|Count|Average|Max|Min)\(", txt):
            add('calc field references an aggregate', vs)
    for e in r.iter('formulaValue'):
        v = (e.text or '').strip()
        if v in ('DistinctCount', 'Median', 'Mode', 'Product', 'Concat', 'NthLargest', 'NthSmallest', 'NthMostFrequent', 'PthPercentile'):
            add('non-combinable formula ' + v, vs)

print('# MV-oriented viewsheet scan: mvStorage.zip (%d viewsheets)\n' % len(docs))
print('| feature | viewsheets in zip | referenced by a spec | profiles that reference one |')
print('|---|---|---|---|')
for f in sorted(feat):
    s = feat[f]
    used = [v for v in s if v in prof_of]
    profs = collections.Counter(p for v in used for p in prof_of[v])
    print('| %s | %d | %d | %s |' % (f, len(s), len(used), ', '.join('%s %d' % kv for kv in sorted(profs.items())) or '-'))

# action-ish names
print('\n## viewsheet names mentioning an action word (name level only)\n')
for w in ('brush', 'zoom', 'drill', 'adhoc', 'ad hoc', 'bookmark', 'detail', 'export'):
    names = [v for v in {name_of(k) for k in docs} if re.search(w, v, re.I)]
    used = [v for v in names if v in prof_of]
    print('- %s: %d in zip, %d referenced by a spec' % (w, len(names), len(used)))

# unique counts for grouped features
def uniq(prefix):
    s = set()
    for f, v in feat.items():
        if f.startswith(prefix):
            s |= v
    return len(s), len([x for x in s if x in prof_of])
print('\n## unique viewsheets per feature group\n')
for pre in ('output scalar binding', 'chart ranking option', 'crosstab ranking option', 'list input bound', 'chart date level', 'crosstab date level', 'timeslider single', 'non-combinable formula'):
    print('- %s: %d in zip, %d referenced' % (pre, *uniq(pre)))

# ---- bookmark state (actions are only visible here: a stored viewsheet has no brush / zoom / adhoc state)
bks = V.load_bookmarks(os.path.join(ROOT, 'mv', 'src', 'test', 'resources', 'config', 'mvStorage.zip'))
used_full = set('1^128^__NULL__^' + m for m in prof_of)
node_hits = collections.defaultdict(lambda: [0, set()])
for vs_key, states in bks.items():
    for bname, root in states.items():
        if root is None:
            continue
        for e in root.iter():
            for label, hit in (('selection state (SelectionList)', e.tag == 'SelectionList'),
                               ('brush (state_brush)', e.tag == 'state_brush'),
                               ('zoom (state_zoom)', e.tag == 'state_zoom'),
                               ('drill filter (drill_filter)', e.tag == 'drill_filter'),
                               ('adhoc (assemblyInfo adhoc="true")', e.get('adhoc') == 'true')):
                if hit:
                    node_hits[label][0] += 1
                    node_hits[label][1].add(vs_key)
print('\n## bookmark state (%d viewsheets with bookmarks, %d bookmarks)\n' % (len(bks), sum(len(v) for v in bks.values())))
print('| state | nodes | viewsheets | referenced by a spec |')
print('|---|---|---|---|')
for label, (n, vss) in sorted(node_hits.items()):
    print('| %s | %d | %d | %d |' % (label, n, len(vss), len([v for v in vss if v in used_full])))
