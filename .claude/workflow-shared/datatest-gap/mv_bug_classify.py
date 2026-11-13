#!/usr/bin/env python3
"""Classify Redmine bugs about the MV (materialized view) feature by root-cause bucket, and check
which of them are mentioned in the datatest mv specs and in the product unit tests.

Input: the JSON written by redmine_fetch.py (id, subject, status, tracker, category, project,
created_on, fixed_version). Fetch with e.g. --subject MV, --subject materializ, --subject incremental
and merge the files (the script accepts several files and de-duplicates by id).

Usage:
  python mv_bug_classify.py <issues.json> [<issues2.json> ...] --since 2025-11-13
         [--datatest <repo>/mv/src/test/groovy] [--product <repo>/community/core/src/test/java]
         [--list BUCKET] [--sample N] [--seed S] [--noise]

What it does, in order (every step prints its count so the sample conditions can be stated):
  1. keep tracker Bug, created_on >= --since
  2. drop noise (reason shown with --noise): AI plugin / Wiz tool bugs, EM dialog UI, hits where
     "MV" is not the feature (storage upgrade, H2 file name, date formatting, ...)
  3. classify the rest by TITLE keywords, first match wins (rules below); the description is NOT read
  4. with --datatest / --product: for each bug, does its number appear in the specs / unit tests
     ("mentioned" is not "asserted": nothing here reads the test body)
  --sample N prints N random bugs per bucket (use it for the manual check of the rules)

Limits: titles only; Chinese/English mixed titles; the rules were written for the 2025-11 .. 2026-11
bugs of StyleBI 2.1.0 and will need review for other periods (spark MV bugs, old 13.x wording).
"""
import argparse, collections, glob, json, os, random, re

CLOSED = {'Closed', 'Rejected', 'Ignored'}

# --- noise: (label, regex on subject) or category names
NOISE_CATS = {'AI Plugin', 'AI Wiz', 'AI Assistant'}
NOISE = [
    ('AI plugin / Wiz tool', re.compile(r'wiz-plugin|ai plugin|\[#WIZ|set_mv_conditions|apply_mv_changes|get_mv_status|MCP tool|admin-reviewer', re.I)),
    ('EM dialog / UI chrome', re.compile(r'resize handle|Escape that closes|EM dialog|Materialize dialog|confirm dialog|catalog entry|characterization\]', re.I)),
    ('MV is not the feature', re.compile(r'DateTypeFormatter|SRPrincipal|STOMP handlers|setPageBreakLevels|storage upgrade|inetsoftdb\.mv\.db|XBigObjectColumn|examples\.zip', re.I)),
]

BUCKETS = [  # first match wins, order matters
    ('MV 代码缺陷（配置项、桩、从不调用）', r'properties promise|is a stub|is never called|silently disables'),
    ('增量更新与删除', r'incremental|delete condition|last block was full|while an incremental|Second incremental|rows of the deleted|deleted rows|MV update'),
    ('滑块、选择与书签', r'slider|selection list|selection state|selection tree|calendar|bookmark|selection of the MV'),
    ('空值与 NOT 条件（MV 与实时结果不同）', r'not equal|not contains|empty value|rows with an empty|\bNOT IN\b|is ignored after the MV'),
    ('VPM、权限与跨租户', r'VPM|permis|cross-tenant|another org|org(anization)?[- ](boundary|scoped|id)|owner-scoped|MATERIALIZATION ACCESS|clone[d]? organization|cloned|organization|guest|site admin'),
    ('日期、类型与时区', r'time ?zone|timestamp|TimeInstant|\bhour\b|Long variable|2147483647|saturates|DateMVColumn|MVUIntColumn|date (type|column|level|range|comparison)'),
    ('公共 API（REST）', r'public API|\bAPI\b|No thread-bound request|users list is split'),
    ('并发、集群与文件', r'concurrent|race|swap|temp files|mvcolswap|System\.gc|deadlock|XJobPool|thread'),
    ('性能与内存', r'slow|hashCode|collision|performance|throttled|scans? .* from|inserts at index 0|resize|full list|memory leak|cache leak|Leak'),
    ('MV 没被使用或读不到数据', r"can't get data|can't load|not used|never used|have no data|no data|still quer|is not read"),
    ('使用 MV 时的异常（NPE、越界、报错）', r'nullpointer|\bNPE\b|\bNP\b|IndexOutOf|out of bounds|Cannot invoke|uncaught|Script error'),
    ('创建与更新失败（构建、cycle、后台任务）', r'creat(e|ing)? ?mv|creation|MV (creation|build|task)|build|data cycle|cycle|mv task|MVAction|failed|fails|hangs|swallowed|Stage 2|query-start|load failure|Materialize|cluster'),
    ('其他结果与 MV 不一致（分组、join、子查询、聚合）', r'after the MV|after mv|when the MV|data changed|total value lost|convert to|group|join|subquery|aggregate|variance|crosstab|worksheet|viewsheet'),
]
BUCKETS = [(n, re.compile(p, re.I)) for n, p in BUCKETS]


def load(paths):
    out = {}
    for p in paths:
        for i in json.load(open(p, encoding='utf-8')):
            out[i['id']] = i
    return out


def noise_reason(i):
    cat = (i.get('category') or {}).get('name')
    if cat in NOISE_CATS:
        return 'category ' + cat
    for label, rx in NOISE:
        if rx.search(i['subject']):
            return label
    return None


def classify(i):
    for n, rx in BUCKETS:
        if rx.search(i['subject']):
            return n
    return '未归类'


def mentions(dirs, ids, exts):
    """id -> set of files that contain the 5-digit number as a whole token"""
    found = collections.defaultdict(set)
    if not ids:          # an empty alternation would match everywhere
        return found
    pat = re.compile(r'(?<![0-9])(%s)(?![0-9])' % '|'.join(str(i) for i in ids))
    for d in dirs:
        for root, _, files in os.walk(d):
            if os.sep + 'target' in root or '/target' in root:
                continue
            for f in files:
                if f.endswith(exts):
                    try:
                        t = open(os.path.join(root, f), encoding='utf-8', errors='replace').read()
                    except OSError:
                        continue
                    for m in set(pat.findall(t)):
                        found[int(m)].add(f)
    return found


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('files', nargs='+')
    ap.add_argument('--since')
    ap.add_argument('--datatest', action='append', default=[])
    ap.add_argument('--product', action='append', default=[])
    ap.add_argument('--list')
    ap.add_argument('--sample', type=int, default=0)
    ap.add_argument('--seed', type=int, default=7)
    ap.add_argument('--noise', action='store_true')
    a = ap.parse_args()

    allb = load(a.files)
    print('input (after de-dup):', len(allb))
    bugs = [i for i in allb.values() if i['tracker']['name'] == 'Bug']
    print('tracker Bug:', len(bugs))
    if a.since:
        bugs = [i for i in bugs if i['created_on'][:10] >= a.since]
        print('created since %s: %d' % (a.since, len(bugs)))
    kept, dropped = [], collections.defaultdict(list)
    for i in bugs:
        r = noise_reason(i)
        if r:
            dropped[r].append(i)
        else:
            kept.append(i)
    print('dropped as noise: %d  %s' % (sum(len(v) for v in dropped.values()), {k: len(v) for k, v in dropped.items()}))
    print('kept:', len(kept))
    if a.noise:
        for r, v in dropped.items():
            for i in sorted(v, key=lambda x: x['id']):
                print('  noise[%s] %d %s' % (r, i['id'], i['subject'][:95]))

    ids = [i['id'] for i in kept]
    dt = mentions(a.datatest, ids, ('.groovy',)) if a.datatest else {}
    pr = mentions(a.product, ids, ('.java',)) if a.product else {}

    C = collections.defaultdict(list)
    for i in kept:
        C[classify(i)].append(i)
    hdr = '%-44s %4s %5s %6s' % ('bucket', 'all', 'open', 'closed')
    if a.datatest or a.product:
        hdr += ' %8s %8s %7s %9s' % ('datatest', 'product', 'both', 'neither')
    print('\n' + hdr)
    order = [n for n, _ in BUCKETS] + ['未归类']
    for n in order:
        v = C.get(n, [])
        if not v:
            continue
        op = sum(1 for i in v if i['status']['name'] not in CLOSED)
        line = '%-44s %4d %5d %6d' % (n, len(v), op, len(v) - op)
        if a.datatest or a.product:
            d = sum(1 for i in v if i['id'] in dt)
            p = sum(1 for i in v if i['id'] in pr)
            b = sum(1 for i in v if i['id'] in dt and i['id'] in pr)
            z = sum(1 for i in v if i['id'] not in dt and i['id'] not in pr)
            line += ' %8d %8d %7d %9d' % (d, p, b, z)
        print(line)
    if a.list:
        for n, v in C.items():
            if n.startswith(a.list):
                for i in sorted(v, key=lambda x: x['id']):
                    print(i['id'], i['status']['name'][:6], 'dt' if i['id'] in dt else '--', 'pr' if i['id'] in pr else '--', i['subject'][:100])
    if a.sample:
        rnd = random.Random(a.seed)
        for n in order:
            v = C.get(n, [])
            for i in rnd.sample(v, min(a.sample, len(v))):
                print('sample[%s] %d %s' % (n[:12], i['id'], i['subject'][:100]))


if __name__ == '__main__':
    main()
