"""Fetch Redmine issues matching a filter and save a slim JSON list (for bug_classify.py).

Credentials are read from the environment only; nothing is printed or written about them:
    REDMINE_URL   e.g. https://issues.example.com   (no trailing path)
    REDMINE_KEY   a Redmine REST API key

Usage:
    python redmine_fetch.py --subject script --out issues.json
    python redmine_fetch.py --subject script --category-id 12 --out issues_vs.json

Prints total_count reported by the server and how many were saved; if they differ it says so.
NOT yet run against a live server (written from the Redmine REST API docs): check the first run's counts.
"""
import argparse
import json
import os
import sys
import time
import urllib.parse
import urllib.request

KEEP = ('id', 'subject', 'status', 'tracker', 'category', 'project', 'created_on', 'updated_on', 'fixed_version')


def get(base, key, params):
    url = base.rstrip('/') + '/issues.json?' + urllib.parse.urlencode(params, doseq=True)
    req = urllib.request.Request(url, headers={'X-Redmine-API-Key': key, 'Accept': 'application/json'})
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.load(r)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--subject', help="substring in the subject (sent as '~text')")
    ap.add_argument('--category-id')
    ap.add_argument('--tracker-id')
    ap.add_argument('--out', required=True)
    ap.add_argument('--page', type=int, default=100)
    a = ap.parse_args()
    base, key = os.environ.get('REDMINE_URL'), os.environ.get('REDMINE_KEY')
    if not base or not key:
        sys.exit('set REDMINE_URL and REDMINE_KEY in the environment')
    params = {'status_id': '*', 'limit': a.page, 'sort': 'id:asc'}  # id order: stable under paging
    if a.subject:
        params['subject'] = '~' + a.subject
    if a.category_id:
        params['category_id'] = a.category_id
    if a.tracker_id:
        params['tracker_id'] = a.tracker_id
    out, offset, total = {}, 0, None
    while total is None or offset < total:
        params['offset'] = offset
        d = get(base, key, params)
        total = d.get('total_count', 0)
        rows = d.get('issues', [])
        if not rows:
            break
        for i in rows:
            out[i['id']] = {k: i.get(k) for k in KEEP}
        offset += len(rows)
        time.sleep(0.2)
    json.dump(list(out.values()), open(a.out, 'w', encoding='utf-8'), ensure_ascii=False)
    print('server total_count=%s saved=%d -> %s' % (total, len(out), a.out))
    if total != len(out):
        print('WARNING: saved count differs from total_count')


if __name__ == '__main__':
    main()
