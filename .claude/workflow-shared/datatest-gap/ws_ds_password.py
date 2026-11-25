"""Set the password of a JDBC data source in the ws storage zip to the one the test database server is started with
(gcp-db-server-iac/docker-compose.yml), so the data source can log in to that server, and optionally replace the host
name in its URL.

usage: python ws_ds_password.py <src.zip> <dst.zip> <data source name> <compose service> <password env variable>
                                [<old host> <new host>]
       e.g. python ws_ds_password.py in.zip out.zip Clickhouse clickhouse CLICKHOUSE_PASSWORD
                clickhouse_server clickhouse-server
       The ClickHouse JDBC driver parses the URL as a URI, where "_" is not allowed in a host name: with
       clickhouse_server it finds no host and connects to localhost. ensure-db-server maps clickhouse-server too.

Only the data source's blob changes (its <password> CDATA, the URL host, digest and length in blob-index.json); every other entry
is copied. The password is read from the compose file and is not printed. Last step of the zip chain:
ws_g11_gen.py -> ws_g8_gen.py -> ws_ch_gen.py -> ws_ds_password.py.
"""
import sys, zipfile, hashlib, json, re, os

src, dst, ds_name, service, env = sys.argv[1:6]
hosts = sys.argv[6:8]
compose = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', '..', 'gcp-db-server-iac',
                       'docker-compose.yml')
text = open(compose, encoding='utf-8').read()
block = re.search(r'^  %s:\n((?:    .*\n|\s*\n)+)' % re.escape(service), text, re.M)
assert block, 'service %s not found' % service
m = re.search(r'%s=(\S+)' % re.escape(env), block.group(1)) or re.search(r'%s:\s*"?([^"\n]+)"?' % re.escape(env),
                                                                       block.group(1))
assert m, '%s not found in service %s' % (env, service)
password = m.group(1).strip()

zin = zipfile.ZipFile(src)
raw = zin.read('blob-index.json').decode('utf-8')
idx = json.loads(raw)
assert json.dumps(idx, separators=(',', ':'), ensure_ascii=False) == raw
key = '0^69^__NULL__^%s^host-org' % ds_name
entry = idx['host-org__indexedStorage'][key]
old_path = 'blobs/%s/%s' % (entry['digest'][:2], entry['digest'][2:])
xml = zin.read(old_path).decode('utf-8')
new_xml, n = re.subn(r'(<password><!\[CDATA\[)[^\]]*(\]\]></password>)', lambda g: g.group(1) + password + g.group(2), xml)
assert n == 1, 'expected one password element, found %d' % n
if hosts:
    new_xml, n = re.subn(r'((?:url|customUrl)="jdbc:[a-z]+://)%s(:)' % re.escape(hosts[0]),
                         lambda g: g.group(1) + hosts[1] + g.group(2), new_xml)
    assert n == 2, 'expected url and customUrl with host %s, found %d' % (hosts[0], n)
data = new_xml.encode('utf-8')
dg = hashlib.md5(data).hexdigest()
new_path = 'blobs/%s/%s' % (dg[:2], dg[2:])
entry['digest'], entry['length'] = dg, len(data)

zout = zipfile.ZipFile(dst, 'w', zipfile.ZIP_DEFLATED)
for info in zin.infolist():
    if info.filename == old_path:
        continue
    payload = json.dumps(idx, separators=(',', ':'), ensure_ascii=False).encode('utf-8') \
        if info.filename == 'blob-index.json' else zin.read(info.filename)
    zout.writestr(info, payload, compress_type=zipfile.ZIP_DEFLATED)
old_info = zin.getinfo(old_path)
zout.writestr(zipfile.ZipInfo(new_path, date_time=old_info.date_time), data, compress_type=zipfile.ZIP_DEFLATED)
zout.close()
print('%s: password set from %s/%s%s, digest %s' % (key, service, env, ', host ' + ' -> '.join(hosts) if hosts else '', dg))
