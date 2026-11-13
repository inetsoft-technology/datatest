"""Mutation check for the vsscript state specs (see docs/test-gap-assessment/vsscript-state-guide.md).

For each mutation: compile a changed copy of ONE product class, put its class files into the local Maven copy
of inetsoft-core (backed up first), run the relevant spec, record which cases went red, restore the jar.
The original jar is restored after every mutation and its sha256 is verified at the end.

WARNING: it temporarily overwrites the inetsoft-core jar in your local Maven repository. Do not run other
builds that use that jar while it runs. If it is killed mid-run, copy <WORK>_orig.jar back over the jar.

Configuration (environment variables, defaults are for the original author's machine):
  M2_REPO        local Maven repository            (default D:/m2-1)
  PRODUCT_CORE   product community/core directory  (default E:/inetsoft/sr14_0/stylebi-main/community/core)
  JAVAC          path of javac (JDK 21)            (default: javac on PATH)
  CP_FILE        file with the vsscript test classpath, from
                 ./mvnw -o dependency:build-classpath -pl vsscript -Dmdep.outputFile=<file>
  MUT_WORK       scratch directory                 (default ./mut next to this script)
The source of the product class must match the inetsoft-core jar in the repository (the control case below
checks that: an unmutated recompile must leave the spec green).
"""
import glob, hashlib, json, os, re, shutil, subprocess, sys, zipfile

M2 = os.environ.get("M2_REPO", "D:/m2-1")
JAR = M2 + "/com/inetsoft/stylebi/inetsoft-core/2.1.0-SNAPSHOT/inetsoft-core-2.1.0-SNAPSHOT.jar"
SRC = os.environ.get("PRODUCT_CORE", "E:/inetsoft/sr14_0/stylebi-main/community/core") + "/src/main/java"
REPO = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "..")).replace("\\", "/")
WORK = os.environ.get("MUT_WORK", os.path.dirname(os.path.abspath(__file__)) + "/mut")
CP = open(os.environ["CP_FILE"]).read().strip()
REPORTS = REPO + "/vsscript/target/surefire-reports"


def sha(p):
    h = hashlib.sha256()
    with open(p, "rb") as f:
        for b in iter(lambda: f.read(1 << 20), b""):
            h.update(b)
    return h.hexdigest()


def sub1(text, old, new, label):
    assert text.count(old) == 1, f"{label}: expected exactly 1 occurrence of target, found {text.count(old)}"
    return text.replace(old, new)


def m_none(t):
    return t


def m_submit_prop(t):
    return sub1(t, '''      addProperty("refreshAfterSubmit", "isRefresh", "setRefresh",
                  boolean.class, info.getClass(), info);
''', '', 'submit prop')


def m_text_autosize(t):
    return sub1(t, '''      addProperty("autoSize", "isAutoSize", "setAutoSize", boolean.class,
                  info.getClass(), info);
''', '', 'text autoSize')


def m_chart_color(t):
    return sub1(t, 'return getFieldName(bindable.getRTColorField());', 'return null;', 'chart colorField')


def m_skip_apply(t):
    return sub1(t, '''         hints[i] = applySelection(vsId, assemblyNames[i], selectedObjects[i],
                                   principal, dispatcher);''', '         hints[i] = 0;', 'skip apply')


def m_alert_info(t):
    return sub1(t, 'final UserMessage userMessage = new UserMessage(String.valueOf(msg), ((MessageCommand.Type) level).code());',
                'level = MessageCommand.Type.INFO; final UserMessage userMessage = new UserMessage(String.valueOf(msg), ((MessageCommand.Type) level).code());',
                'alert level')


def m_fix_80709(t):
    return sub1(t, '''         return ((VSDimensionRef) dim).getRankingOptionValue().equals(
            "" + XCondition.BOTTOM_N);''', '''         return ("" + XCondition.BOTTOM_N).equals(((VSDimensionRef) dim).getRankingOptionValue());''', 'fix 80709')


MUTATIONS = [
    # name, source file, mutate fn, spec, what should go red
    ("control: unmutated recompile of SubmitVSAScriptable", "inetsoft/report/script/viewsheet/SubmitVSAScriptable.java", m_none, "ScriptStateSubmit_Spec", "nothing"),
    ("M1 Submit: remove the refreshAfterSubmit property", "inetsoft/report/script/viewsheet/SubmitVSAScriptable.java", m_submit_prop, "ScriptStateSubmit_Spec", "Submit_refreshAfterSubmit_roundtrip"),
    ("M2 Text: remove the autoSize property", "inetsoft/report/script/viewsheet/TextVSAScriptable.java", m_text_autosize, "ScriptStateSpike_Spec", "C_roundtrip_autoSize, D_script_vs_assembly_info"),
    ("M3 Chart: colorField reads null", "inetsoft/report/script/viewsheet/ChartVSAScriptable.java", m_chart_color, "ScriptStateChartCrosstab_Spec", "Chart_binding_roundtrip"),
    ("M4 VSInputService: submit does not apply input values", "inetsoft/web/viewsheet/service/VSInputService.java", m_skip_apply, "ScriptStateSubmit_Spec", "Submit_applies_pending_input_values, Submit_script_runs_after_values_are_applied"),
    ("M5 alert: level forced to INFO", "inetsoft/util/script/JavaScriptEngine.java", m_alert_info, "ScriptStateSpike_Spec", "F_click_alert"),
    ("M6 fix #80709 (null-safe isTopNReverse)", "inetsoft/report/script/viewsheet/VSChartBindingScriptable.java", m_fix_80709, "ScriptStateKnownBugs_Spec", "#80709 [bug present] + pending #80709 unexpectedly passes"),
]


def build(rel, fn):
    shutil.rmtree(WORK, ignore_errors=True)
    os.makedirs(WORK + "/src/" + os.path.dirname(rel))
    os.makedirs(WORK + "/out")
    text = open(SRC + "/" + rel, encoding="utf-8").read()
    open(WORK + "/src/" + rel, "w", encoding="utf-8").write(fn(text))
    r = subprocess.run([os.environ.get("JAVAC", "javac"), "--release", "21", "-proc:none", "-nowarn", "-encoding", "UTF-8", "-cp", CP,
                        "-d", WORK + "/out", WORK + "/src/" + rel], capture_output=True, text=True)
    if r.returncode != 0:
        raise RuntimeError("javac failed:\n" + r.stderr[-1500:])
    return [os.path.relpath(p, WORK + "/out").replace("\\", "/") for p in glob.glob(WORK + "/out/**/*.class", recursive=True)]


def patch_jar(src_jar, dst_jar, classes):
    repl = {c: open(WORK + "/out/" + c, "rb").read() for c in classes}
    with zipfile.ZipFile(src_jar) as zin, zipfile.ZipFile(dst_jar, "w", zipfile.ZIP_DEFLATED) as zout:
        seen = set()
        for item in zin.infolist():
            if item.filename in repl:
                zout.writestr(item.filename, repl[item.filename]); seen.add(item.filename)
            else:
                zout.writestr(item, zin.read(item.filename))
        for c in classes:
            if c not in seen:
                zout.writestr(c, repl[c])  # new inner class


def run_spec(spec):
    for f in glob.glob(REPORTS + "/TEST-*" + spec + ".xml"):
        os.remove(f)
    subprocess.run([REPO + ("/mvnw.cmd" if os.name == "nt" else "/mvnw"), "-q", "-o", "test", "-pl", "vsscript", "-Dtest=" + spec, "-DfailIfNoTests=false",
                    "-Dsurefire.failIfNoSpecifiedTests=false"], cwd=REPO, capture_output=True, text=True)
    files = glob.glob(REPORTS + "/TEST-*" + spec + ".xml")
    if not files:
        return None
    s = open(files[0], encoding="utf-8").read()
    tot = re.search(r'tests="(\d+)" errors="(\d+)" skipped="(\d+)" failures="(\d+)"', s).groups()
    failed = []
    for m in re.finditer(r'<testcase name="([^"]*)"[^>]*?(?:/>|>(.*?)</testcase>)', s, re.S):
        body = m.group(2) or ""
        if "<failure" in body or "<error" in body:
            failed.append(m.group(1))
    return {"tests/errors/skipped/failures": tot, "red": failed}


def main():
    orig_sha = sha(JAR)
    backup = WORK + "_orig.jar"
    shutil.copyfile(JAR, backup)
    assert sha(backup) == orig_sha
    results = []
    try:
        for name, rel, fn, spec, expect in MUTATIONS:
            entry = {"mutation": name, "spec": spec, "expected_red": expect}
            try:
                classes = build(rel, fn)
                patch_jar(backup, WORK + "_patched.jar", classes)
                shutil.copyfile(WORK + "_patched.jar", JAR)
                entry["result"] = run_spec(spec)
            except Exception as e:
                entry["error"] = type(e).__name__ + ": " + str(e)[:800]
            finally:
                shutil.copyfile(backup, JAR)
            results.append(entry)
            print(json.dumps(entry, ensure_ascii=False), flush=True)
    finally:
        shutil.copyfile(backup, JAR)
        print("jar restored, sha256 matches original:", sha(JAR) == orig_sha, flush=True)
    json.dump(results, open(WORK + "_results.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)


main()
