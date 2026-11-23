package inetsoft.test.vsscript.support

import groovy.xml.XmlSlurper
import java.util.zip.ZipFile

/**
 * Reads the cell texts of an xlsx file without POI (POI is not on the vsscript test class path): sheet name -> list of rows, each row a
 * map "column letters" -> text. Shared strings and inline strings are resolved; numbers and dates are the raw text of the file.
 */
class XlsxText {
   /** @return sheet name -> (row number -> (column letters -> text)) */
   static Map<String, Map<Integer, Map<String, String>>> read(File file) {
      def zip = new ZipFile(file)

      try {
         def parse = { String name ->
            def e = zip.getEntry(name)
            e == null ? null : new XmlSlurper(false, false).parse(zip.getInputStream(e))
         }

         def strings = []
         def ss = parse('xl/sharedStrings.xml')

         if(ss != null) {
            ss.si.each { si -> strings << si.'**'.findAll { it.name() == 't' }*.text().join() }
         }

         def wb = parse('xl/workbook.xml')
         def rels = parse('xl/_rels/workbook.xml.rels')
         def targets = rels.Relationship.collectEntries { [(it.@Id.text()): it.@Target.text()] }
         def result = [:]

         wb.sheets.sheet.each { sh ->
            def rid = sh.attributes().find { k, v -> k.toString().endsWith(':id') || k.toString().endsWith('}id') }?.value
            def target = targets[rid]
            target = target.startsWith('/') ? target.substring(1) : 'xl/' + target
            def doc = parse(target)
            def rows = [:]

            doc.sheetData.row.each { r ->
               def cells = [:]

               r.c.each { c ->
                  def ref = c.@r.text()
                  def col = ref.replaceAll(/\d+/, '')
                  def t = c.@t.text()
                  def v = c.v.text()
                  def text = t == 's' ? strings[v as int] : (t == 'inlineStr' ? c.is.'**'.findAll { it.name() == 't' }*.text().join() : v)

                  if(text) {
                     cells[col] = text
                  }
               }

               if(cells) {
                  rows[r.@r.text() as int] = cells
               }
            }

            result[sh.@name.text()] = rows
         }

         return result
      }
      finally {
         zip.close()
      }
   }

   /** all cell texts of the workbook */
   static List<String> allTexts(File file) {
      read(file).values().collectMany { rows -> rows.values().collectMany { it.values() } }
   }

   /** the texts of the text boxes of the workbook (xl/drawings/*.xml): the Text components of a viewsheet are exported as text boxes, not as cells */
   static List<String> drawingTexts(File file) {
      def zip = new ZipFile(file)

      try {
         zip.entries().findAll { it.name ==~ /xl\/drawings\/[^\/]+\.xml/ }.collectMany { e ->
            def doc = new XmlSlurper(false, false).parse(zip.getInputStream(e))
            // the parser is not namespace aware: the text runs of a drawing are named "a:t", not "t"
            doc.'**'.findAll { it.name() == 't' || it.name().endsWith(':t') }*.text()
         }
      }
      finally {
         zip.close()
      }
   }
}
