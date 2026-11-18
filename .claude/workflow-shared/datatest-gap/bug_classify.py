"""Classify Redmine bugs (title only) into root-cause buckets relevant to vsscript.
Input: JSON list of issues (id, subject, category, tracker, status, created_on).
Usage: python bug_classify.py <issues.json> [--list BUCKET]
"""
import json, re, sys, collections

KEEP_CATS = {'Visual Composer', 'Viewsheet Execution', 'Viewer', 'Other', 'Report Engine', 'User Portal',
             'Query Engine', 'Worksheet Execution', 'Style Studio', None, '-'}
# editor / UI / library / docs items: not script runtime behaviour
UI = re.compile(r'editor|dialog|auto ?complete|inline help|check syntax|syntax not|script function.*(tab|permission|save|recent|tree)|'
                r'recent|rename|script library|assets tree|permission|<Feature|tooltip of|hint text|icon', re.I)
NOISE = re.compile(r'flaky|jest|spec\.ts|unitcase|deployment|gulp|CLAUDE\.md|RxJS|memory leak|ai-assistant|XML Escaping|REST API|secret|script-chat|ssh|aws|installer|\bAI plugin|wiz-plugin|ai-report|ai-regression|ai-verify|script pane|script-compoent|script function|new script', re.I)
RPT = re.compile(r'report script|\bband\b|section|\[#RPT Report|presenter|onPageBreak|SectionBand', re.I)

BUCKETS = [  # first match wins; order matters
    ('请求新增脚本属性/脚本支持(Feature 记在 Bug 里)', r'add script|script support|should (add|supply).*script|script (is )?(not )?supported|add .*script for'),
    ('引擎/沙箱/异常(GraalJS、Rhino、锁、超时、报错日志)',
     r'graal|rhino|sandbox|write lock|timeout|interrupt|time ?zone|script error|error log|javascript failed|script execution'),
    ('全局函数与计算函数边界(CALC、字符串/日期/数组函数)',
     r'CALC\.|\bunion\(|\bsplit\(|rtrim|ltrim|formatNumber|inArray|mapList|toList|\bMath\.|substring|replace\(|indexOf|'
     r'date function|dateAdd|parseDate|toDate|format\(|sort\(|\barray\b|includes'),
    ('数据访问函数(runQuery、createConnection、getImage、worksheet 读写)',
     r'runQuery|createConnection|getImage|saveWorksheet|appendRow|setCellValue|refreshData|dynamic script|addImage'),
    ('触发时机/顺序(onInit、onRefresh、onClick、onLoad、onSubmit)',
     r'onInit|oninit|onRefresh|onrefresh|onClick|onclick|onLoad|onload|onSubmit|before|after.*script|order|first load|event'),
    ('参数与嵌入 viewsheet(parameter、thisParameter、embedded)',
     r'parameter|embedded|thisViewsheet|thisParam|Viewsheet1|nested|parent'),
    ('Chart 脚本 API(bindingInfo、SchemaElement、EGraph、轴/图例/系列)',
     r'chart|bindingInfo|SchemaElement|EGraph|GraphElement|axis|legend|y2|plot|trend|box ?plot|stack|bar|line style|shape'),
    ('表/交叉表/自由表脚本(单元格、列宽、公式)',
     r'freehand|crosstab|calc ?table|table|cell|column width|setColumnWidth|header|row|\.data\['),
    ('选择/输入组件脚本(selectedObject、values/labels、Calendar、Slider…)',
     r'selection|combo|radio|check ?box|slider|calendar|spinner|textinput|selectedObject|selectedLabel|labels|values|list|tree|tab\b|group ?container|submit|form'),
    ('属性设置不生效(visible/format/font/hyperlink/size…)',
     r"visible|format|font|color|hyperlink|alpha|border|size|position|enabled|can't|cannot|not work|didn't|doesn't|not appl|ignored|no effect|not take"),
]
BUCKETS = [(n, re.compile(p, re.I)) for n, p in BUCKETS]
EXPORT = re.compile(r'export|pdf|print|excel|email|schedul|portal|viewer|bookmark|mobile', re.I)


def classify(i):
    cat = (i.get('category') or {}).get('name')
    t = i['subject']
    if i['tracker']['name'] != 'Bug' or cat not in KEEP_CATS:
        return None
    if UI.search(t) or RPT.search(t) or NOISE.search(t):
        return None
    for n, p in BUCKETS:
        if p.search(t):
            return n
    return '未归类'


if __name__ == '__main__':
    A = json.load(open(sys.argv[1], encoding='utf-8'))
    C = collections.defaultdict(list)
    for i in A:
        b = classify(i)
        if b:
            C[b].append(i)
    tot = sum(len(v) for v in C.values())
    print('input', len(A), 'kept', tot)
    for n, v in sorted(C.items(), key=lambda kv: -len(kv[1])):
        recent = sum(1 for i in v if i['created_on'][:4] >= '2025')
        openn = sum(1 for i in v if i['status']['name'] in ('New', 'In Progress', 'Resolved'))
        exp = sum(1 for i in v if EXPORT.search(i['subject']))
        print('%-48s %4d  since2025 %3d  open/resolved %3d  export/portal-word %3d' % (n, len(v), recent, openn, exp))
    if '--list' in sys.argv:
        b = sys.argv[sys.argv.index('--list') + 1]
        for n, v in C.items():
            if n.startswith(b):
                for i in v[:int(sys.argv[-1]) if sys.argv[-1].isdigit() else 40]:
                    print(i['id'], i['created_on'][:4], i['status']['name'][:6], i['subject'][:110])
