package inetsoft.test

import inetsoft.graph.data.DataSet
import inetsoft.report.ReportSheet
import inetsoft.report.StylePage
import inetsoft.report.TableLens
import inetsoft.report.composition.execution.AssetTableLens
import inetsoft.report.filter.DCMergeDatesCell
import inetsoft.report.internal.ReportGenerator
import inetsoft.uql.viewsheet.CompositeSelectionValue
import inetsoft.uql.viewsheet.SelectionList
import inetsoft.uql.viewsheet.SelectionValue
import inetsoft.graph.data.BoxDataSet

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat

class ExportUtil {

   /**
    * export vs data
    * @param fileName
    * @param data
    * @return
    */
   def exportVSObject(String fileName, def data) {
      exportVSObject(fileName, data, true)
   }

   /**
    * export vs data view component
    * @param fileName
    * @param data
    * @return
    */
   def exportVSObject(String fileName, def data, Boolean isFormat) {
      File file = new File(fileName)
      if(!file.getParentFile().exists()) {
         file.getParentFile().mkdirs()
      } else if(file.exists()){
         file.delete()
      }

      if(data == null || data == '') {
         data = ['null']
      }

      if(data instanceof DataSet || data instanceof BoxDataSet) {
         StringBuffer buffer = new StringBuffer()
         for(int max = 0; max < data.colCount; max++) {
            buffer.append(data.getHeader(max))
            if(data.colCount != (max+1)) {
               buffer.append(', ')
            }
         }
         buffer.append('\n')
         for(int row = 0 ; row < data.rowCount; row++) {
            for(int col = 0; col < data.colCount; col++) {
               buffer.append(format(data.getData(col, row)))
               if(data.colCount != (col+1)) {
                  buffer.append(', ')
               }
            }
            buffer.append('\n')
         }
         file.withPrintWriter { printWriter ->
            printWriter.println("The data size(row x col) is:(" + (data.rowCount + 1) + " x " + data.colCount + ")")
            printWriter.print(buffer.toString())
         }
      }
      else if(data instanceof TableLens) {
         //TableLens table = new SortFilter(data) // sort table
         TableLens table = wrapTable(data, isFormat)
         StringBuffer buffer = new StringBuffer()
         int row = 0
         while (table.moreRows(row)) {
            for(int col = 0; col < table.getColCount(); col++) {
               def obj = table.getObject(row, col)
               if(obj instanceof DCMergeDatesCell) {
                  obj = ((DCMergeDatesCell) obj).getFormatedOriginalDate();
               }
               buffer.append(obj)
               if(table.getColCount() != (col+1)) {
                  buffer.append(', ')
               }
            }
            buffer.append('\n')
            row++
         }
         file.withPrintWriter { printWriter ->
            printWriter.println("The data size(row x col) is:(" + row + " x " + table
                    .getColCount() + ")")
            printWriter.print(buffer.toString())
         }
      }
      else if(data instanceof SelectionList) {
         StringBuffer bufferAll = new StringBuffer()
         data.getSelectionValues().eachWithIndex{ SelectionValue entry, int i ->
            StringBuffer buffer = new StringBuffer()
            bufferAll.append(printSelectionValue(entry, buffer).toString())
         }

         file.withPrintWriter { printWriter ->
            printWriter.println("**level--[label, status, level, value], 0|8[unselectd], 9|1[selected], 2[include], 4[exclude]**" )
            printWriter.print(bufferAll.toString())
         }
      }
      else if(data instanceof StringBuffer) {
         file.withPrintWriter {printWriter ->
            printWriter.print(data.toString())
         }
      }
      else if(data instanceof BufferedImage) {
         ImageIO.write(data, 'png', file)
      }
      else if(data != null) {
         file.withPrintWriter {printWriter ->
            printWriter.print(data)
         }
      }
   }

   /**
    * export WS table data to text
    * @param fileName
    * @param data
    * @return
    */
   def exportWSObject(String fileName, def data) {
      File file = new File(fileName)
      if(!file.getParentFile().exists()) {
         file.getParentFile().mkdirs()
      } else if(file.exists()){
         file.delete()
      }

      if(data == null || data == '') {
         data = ['null']
      }

      if(data instanceof TableLens) {
         TableLens table = wrapTable(data, true)
         StringBuffer buffer = new StringBuffer()
         int row = 0
         while (table.moreRows(row)) {
            for(int col = 0; col < table.getColCount(); col++) {
               buffer.append(table.getObject(row, col))
               if(table.getColCount() != (col+1)) {
                  buffer.append(', ')
               }
            }
            buffer.append('\n')
            row++
         }
         file.withPrintWriter { printWriter ->
            printWriter.println("The table size(row x col) is:(" + row + " x " + data.getColCount() + ")")
            printWriter.print(buffer.toString())
         }
         Thread.sleep(1000)
      }
      else if(data instanceof StringBuffer) {
         file.withPrintWriter {printWriter ->
            printWriter.print(data.toString())
         }
      }
      else if(data != null) {
         file.withPrintWriter {printWriter ->
            printWriter.print(data)
         }
      }
   }

   /**
    * return export file path, store by asset path
    * @param asset_id
    * @param packageName
    * @return
    */
   String getExportFolderPath(String asset_id, String packageName) {
      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
      resourcePath = (packageName == null)? (resourcePath + '/exportData') : (resourcePath + '/exportData/' + packageName)

      if(asset_id.startsWith('1^128^__')) {
         return resourcePath +  File.separator + asset_id.substring(asset_id.lastIndexOf('^') + 1)
      }
      else if(asset_id.startsWith('1^2^__')) {
         return resourcePath + File.separator + '/WSExp' + File.separator +
                  asset_id.substring(asset_id.lastIndexOf('^') + 1)
      }
      else {
        new Exception('------the asset_id not right-------------').printStackTrace()
      }
   }

   private StringBuffer printSelectionValue(SelectionValue value, StringBuffer buffer) {
      if(value instanceof CompositeSelectionValue) {
         if(value.toString().split('SelectionList') != 0) {
            String str = value.getLevel() + '--' + value.toString().split("\\[SelectionList")[0].
                    toString().replaceAll('SelectionValue', '')
            value.getLevel().times {
               str = ' ' + str
            }
            buffer.append(str)
            buffer.append('\n')
         }
         value.getSelectionList().getSelectionValues().eachWithIndex{ SelectionValue entry, int i ->
            if(entry instanceof CompositeSelectionValue) {
               printSelectionValue(entry, buffer)
            }
            else {
               String str2 = entry.getLevel() + '--' + entry.toString().replaceAll('SelectionValue', '')
               entry.getLevel().times {
                  str2 = ' ' + str2
               }
               buffer.append(str2)
               buffer.append('\n')
            }
         }
      }
      else {
         buffer.append(value.toString().replaceAll('SelectionValue',''))
         buffer.append('\n')
      }
      return buffer
   }

   TableLens wrapTable(TableLens lens, Boolean isFormat) {
      return new AssetTableLens() {
         int getRowCount() {
            return lens.getRowCount()
         }

         int getHeaderRowCount() {
            return lens.getHeaderRowCount()
         }

         int getColCount() {
            return lens.getColCount()
         }

         boolean moreRows(int row) {
            return lens.moreRows(row)
         }

         Object getObject(int r, int c) {
            return isFormat? format(lens.getObject(r, c)) : lens.getObject(r, c)
         }
      }
   }

   private def format(def val) {
      if (val == null || val == '') {
         val = 'NULL'
      } else if (val instanceof java.util.Date && !(val instanceof java.sql.Date) &&
              !(val instanceof java.sql.Time)) {
         val = dateformat.format(val)
      } else if (val instanceof Float || val instanceof Double) {
         if (val == Float.NaN || val == Double.NaN) {
            val = 'NaN'
         } else {
            val = numformat.format((val as Number).doubleValue())
         }
      }

      return val
   }

   private final static NumberFormat numformat = new DecimalFormat("#0.####")
   private final static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd " +
           "HH:mm:ss")
}
