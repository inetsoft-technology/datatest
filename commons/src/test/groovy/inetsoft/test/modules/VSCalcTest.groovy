package inetsoft.test.modules

import inetsoft.report.composition.RuntimeViewsheet
import inetsoft.report.composition.execution.ViewsheetSandbox
import inetsoft.uql.asset.Assembly
import inetsoft.uql.viewsheet.CalcTableVSAssembly
import inetsoft.uql.viewsheet.CrosstabVSAssembly
import inetsoft.uql.viewsheet.EmbeddedTableVSAssembly
import inetsoft.uql.viewsheet.FileFormatInfo
import inetsoft.uql.viewsheet.TableVSAssembly
import inetsoft.web.viewsheet.service.ExportResponse

class VSCalcTest extends ViewsheetTest {
   VSCalcTest(String asset_id, String caseName) {
      super(asset_id, caseName)
   }

   /**
    * Convert every visible table/crosstab to a freehand table, export the page as PNG, and check
    * that the data of each converted component equals the data it had before the conversion.
    * @param params viewsheet parameters
    * @param allowedDiff component name -> reason, for the components that may differ from their
    *        source (known differences, e.g. the conversion clears date comparison, drill and
    *        calculators); any other component that differs fails the case. A listed component
    *        must keep its row count: with a plain reason the row count must not change, with
    *        [reason: '...', rows: '21 -> 108'] it must change exactly like that, so a different
    *        change (for example losing rows) still fails the case.
    */
   def checkConvert(Map<String, String[]> params, Map<String, Object> allowedDiff = [:]) {
      checkConvert0(params, allowedDiff)
   }

   /**
    * EXPLORATION (G2, not asserting): export the data of every visible table/crosstab before and
    * after convertToFreehand and compare the two text exports line by line. All sources are
    * exported first, then all are converted, so conversions cannot influence each other's
    * "before" data. Files: {name}_src.txt and {name}_calc.txt in the case export folder.
    * @return assembly name -> 'SAME' or a short description of the differences
    */
   Map<String, String> compareConvertData(Map<String, String[]> params, boolean confirmed = true) {
      initVS(params, false)
      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(principal)
      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox().get()
      sandbox.shrink()
      List<String> names = convertibleNames(rvs)
      Map<String, String> result = [:]
      Map<String, String> errors = [:]
      Map<String, String> before = dumpComponents(sandbox, names, '_src.txt', errors)
      errors.each { name, ex -> result[name] = 'SRC-EXPORT-FAILED ' + ex }

      names.findAll { before.containsKey(it) }.each { name ->
         try {
            viewsheetResource.convertToFreehand(principal, name, confirmed)
         }
         catch(Exception ex) {
            result[name] = 'CONVERT-FAILED ' + ex
         }
      }

      rvs = viewsheetResource.getRuntimeViewsheet(principal)
      sandbox = rvs.getViewsheetSandbox().get()

      List<String> todo = names.findAll { before.containsKey(it) && !result.containsKey(it) }

      todo.findAll { !(rvs.getViewsheet().getAssembly(it) instanceof CalcTableVSAssembly) }.each {
         result[it] = 'NOT-CONVERTED'
      }

      errors = [:]
      Map<String, String> after = dumpComponents(sandbox,
         todo.findAll { !result.containsKey(it) }, '_calc.txt', errors)
      errors.each { name, ex -> result[name] = 'CALC-EXPORT-FAILED ' + ex }
      after.each { name, text -> result[name] = diffText(before[name], text) }

      return result
   }

   /** the visible tables, crosstabs and embedded tables, the components that can be converted */
   private static List<String> convertibleNames(RuntimeViewsheet rvs) {
      return rvs.getViewsheet().getAssemblies().findAll {
         (it instanceof EmbeddedTableVSAssembly || it instanceof TableVSAssembly ||
            it instanceof CrosstabVSAssembly) && it.getVSAssemblyInfo().isVisible(true)
      }.collect { it.getName() }
   }

   /**
    * Export the data of the components to {name}{suffix} in the case export folder.
    * @param errors filled with name -> failure for the components that could not be exported
    * @return name -> the exported text
    */
   private Map<String, String> dumpComponents(ViewsheetSandbox sandbox, List<String> names,
                                              String suffix, Map<String, String> errors)
   {
      Map<String, String> out = [:]

      names.each { name ->
         try {
            File f = createExportFileByCase(name, null, suffix)
            f.text = dumpLens(sandbox.getVSTableLens(name, false))
            out[name] = f.text
         }
         catch(Exception ex) {
            errors[name] = ex.toString()
         }
      }

      return out
   }

   /**
    * One line per row, cells joined by ' | ', null and empty values written as 'NULL', other
    * values with toString().
    */
   private static String dumpLens(inetsoft.report.TableLens lens) {
      StringBuilder buf = new StringBuilder()
      int cols = lens.getColCount()
      int row = 0

      while(lens.moreRows(row)) {
         List<String> cells = []

         for(int col = 0; col < cols; col++) {
            Object v = lens.getObject(row, col)
            cells << (v == null || v == '' ? 'NULL' : v.toString())
         }

         buf.append(cells.join(' | ')).append('\n')
         row++
      }

      return buf.toString()
   }

   private static String diffText(String src, String calc) {
      List<String> a = src.readLines()
      List<String> b = calc.readLines()
      List<String> out = []

      if(a.size() != b.size()) {
         // rows cannot be aligned, report the row counts and the first rows of both sides only
         return "rows ${a.size()} -> ${b.size()} | first src row: [${a.size() > 1 ? a[1] : ''}] " +
            "first calc row: [${b.size() > 1 ? b[1] : ''}]"
      }

      int bad = 0

      for(int i = 0; i < a.size(); i++) {
         List<String> sc = a[i].split(' \\| ', -1).toList()
         List<String> cc = b[i].split(' \\| ', -1).toList()
         List<String> prev = i > 0 ? a[i - 1].split(' \\| ', -1).toList() : null

         if(sc.size() != cc.size()) {
            bad++
            out << ("row ${i}: cols ${sc.size()} -> ${cc.size()}".toString())
            continue
         }

         List<String> cells = []

         for(int j = 0; j < sc.size(); j++) {
            String s = sc[j]
            String c = cc[j]

            if(sameCell(s, c)) {
               continue
            }

            // merged span: the source repeats its left/upper neighbor, the freehand cell is empty
            if(c == 'NULL' && s != 'NULL' &&
               ((j > 0 && sc[j - 1] == s) || (prev != null && j < prev.size() && prev[j] == s)))
            {
               continue
            }

            cells << ("c${j}: '${s}' -> '${c}'".toString())
         }

         if(!cells.isEmpty()) {
            bad++

            if(bad <= 4) {
               out << ("row ${i}: " + cells.take(3).join(', ')).toString()
            }
         }
      }

      if(bad > 0) {
         out << ("${bad} of ${a.size()} rows differ".toString())
      }

      return out.isEmpty() ? 'SAME' : out.join(' | ')
   }

   /** equal as text, as numbers (22 vs 22.0), or as time with/without trailing .0 */
   private static boolean sameCell(String s, String c) {
      if(s == c) {
         return true
      }

      try {
         if(Double.parseDouble(s) == Double.parseDouble(c)) {
            return true
         }
      }
      catch(NumberFormatException ignore) {
      }

      return s.replaceAll('\\.0$', '') == c.replaceAll('\\.0$', '')
   }

   /**
    * convert table as freehand, then export as PNG, then compare the converted data with the
    * data before the conversion
    * @param params
    * @param allowedDiff component name -> reason or [reason: '...', rows: 'a -> b'], see checkConvert
    * @return
    */
   def checkConvert0(Map<String, String[]> params, Map<String, Object> allowedDiff = [:]) {
      initVS(params, false)
      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(principal)
      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox().get()
      sandbox.shrink()
      Assembly[] assemblies = rvs.getViewsheet().getAssemblies()
      String assemblyName
      List<String> convertedAssemblies = []
      File pngFile = createExportFileByCase(null, null, '_CALC.png')

      // the data before the conversion: all sources are exported first, so that converting one
      // component cannot change the "before" data of another (a freehand table can be bound to
      // another component of the same viewsheet)
      Map<String, String> errors = [:]
      Map<String, String> before = dumpComponents(sandbox, convertibleNames(rvs), '_src.txt', errors)

      if(!errors.isEmpty()) {
         throw new RuntimeException("export of the source data failed: " + errors)
      }

      try {
         assemblies.each {
            assemblyName = it.getName()

            if((it instanceof EmbeddedTableVSAssembly || it instanceof TableVSAssembly
                    || it instanceof CrosstabVSAssembly)
                    && it.getVSAssemblyInfo().isVisible(true)) {
               viewsheetResource.convertToFreehand(principal, assemblyName)
               convertedAssemblies.add(assemblyName)
            }
         }
      }
      catch(Exception e) {
         throw new RuntimeException("convert to freehand failed: " + assemblyName, e)
      }

      rvs = viewsheetResource.getRuntimeViewsheet(principal)

      if(convertedAssemblies.isEmpty()) {
         throw new RuntimeException("no visible table or crosstab assembly was converted")
      }

      convertedAssemblies.each {
         Assembly assembly = rvs.getViewsheet().getAssembly(it)

         if(!(assembly instanceof CalcTableVSAssembly)) {
            throw new RuntimeException("assembly was not converted to freehand: " + it)
         }
      }

      OutputStream out = new FileOutputStream(pngFile)
      try {
         viewsheetResource.exportVS(FileFormatInfo.EXPORT_TYPE_PNG, true,
                 false, true, false, false,
                 [] as String[], false, false, null, new ExportResponse(out), principal)
      }
      catch(Exception ex) {
         throw new RuntimeException("export converted viewsheet failed", ex)
      }
      finally {
         out.close()
      }

      if(pngFile.length() == 0L) {
         throw new RuntimeException("converted viewsheet export is empty: " + pngFile)
      }

      // the data after the conversion
      Map<String, String> failures = new LinkedHashMap<>()
      errors = [:]
      Map<String, String> after = dumpComponents(rvs.getViewsheetSandbox().get(),
         convertedAssemblies, '_calc.txt', errors)
      errors.each { name, ex -> failures.put(name, 'export of the converted data failed: ' + ex) }

      after.each { name, text ->
         String diff = diffText(before[name], text)
         Object allowed = allowedDiff.get(name)

         if(allowed == null) {
            if(diff != 'SAME') {
               failures.put(name, diff)
            }

            return
         }

         // a listed component may differ, but its row count must change exactly as declared
         // (unchanged when nothing is declared), losing rows is never accepted by accident
         String reason = allowed instanceof Map ? allowed.get('reason') : allowed
         String expectedRows = allowed instanceof Map ? allowed.get('rows') : null
         int rowsBefore = before[name].readLines().size()
         int rowsAfter = text.readLines().size()
         String rowChange = rowsBefore == rowsAfter ? null : rowsBefore + ' -> ' + rowsAfter

         if(rowChange != expectedRows) {
            failures.put(name, 'allowed to differ (' + reason + '), but the rows changed ' +
               (rowChange ?: 'not at all') + ' where ' + (expectedRows ?: 'no change') +
               ' was declared')
         }
         else if(diff == 'SAME') {
            System.err.println("checkConvert: " + name + " no longer differs from its source, " +
               "remove it from allowedDiff (" + reason + ")")
         }
      }

      if(!failures.isEmpty()) {
         violation("converted data differs from the data before the conversion:\n" +
            failures.collect { k, v -> "  " + k + ": " + v }.join("\n") + "\n")
      }
   }
}
