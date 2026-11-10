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

   def checkConvert(Map<String, String[]> params) {
      checkConvert0(params)
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
      List<String> names = rvs.getViewsheet().getAssemblies().findAll {
         (it instanceof EmbeddedTableVSAssembly || it instanceof TableVSAssembly ||
            it instanceof CrosstabVSAssembly) && it.getVSAssemblyInfo().isVisible(true)
      }.collect { it.getName() }
      Map<String, String> before = [:]
      Map<String, String> result = [:]

      names.each { name ->
         try {
            File f = createExportFileByCase(name, null, '_src.txt')
            f.text = dumpLens(sandbox.getVSTableLens(name, false))
            before[name] = f.text
         }
         catch(Exception ex) {
            result[name] = 'SRC-EXPORT-FAILED ' + ex
         }
      }

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

      names.findAll { before.containsKey(it) && !result.containsKey(it) }.each { name ->
         try {
            if(!(rvs.getViewsheet().getAssembly(name) instanceof CalcTableVSAssembly)) {
               result[name] = 'NOT-CONVERTED'
               return
            }

            File f = createExportFileByCase(name, null, '_calc.txt')
            f.text = dumpLens(sandbox.getVSTableLens(name, false))
            result[name] = diffText(before[name], f.text)
         }
         catch(Exception ex) {
            result[name] = 'CALC-EXPORT-FAILED ' + ex
         }
      }

      return result
   }

   /**
    * One line per row, cells joined by ' | '. Cells inside a merged span (not its top-left cell)
    * are written as '~', so a source that repeats the value over the span and a freehand table
    * that leaves it empty compare equal. Values are written with toString().
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
         out << ("rows ${a.size()} -> ${b.size()}".toString())
      }

      if(a.size() != b.size()) {
         // rows cannot be aligned, report the first rows of both sides only
         out << ("first src row: [${a.size() > 1 ? a[1] : ''}] first calc row: [${b.size() > 1 ? b[1] : ''}]".toString())
         return out.join(' | ')
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
    * convert table as freehand, then export as PNG
    * @param params
    * @return
    */
   def checkConvert0(Map<String, String[]> params) {
      initVS(params, false)
      RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(principal)
      ViewsheetSandbox sandbox = rvs.getViewsheetSandbox().get()
      sandbox.shrink()
      Assembly[] assemblies = rvs.getViewsheet().getAssemblies()
      String assemblyName
      List<String> convertedAssemblies = []
      File pngFile = createExportFileByCase(null, null, '_CALC.png')

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
   }
}
