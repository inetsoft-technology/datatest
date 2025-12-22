package inetsoft.test.core

import inetsoft.graph.data.DataSet
import inetsoft.report.TableLens
import inetsoft.report.composition.execution.AssetTableLens
import inetsoft.report.filter.DCMergeDatesCell
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

   // Thread-safe date formatter using ThreadLocal
   private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
      @Override
      protected SimpleDateFormat initialValue() {
         return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      }
   }

   private static final NumberFormat NUM_FORMAT = new DecimalFormat("#0.####")
   private static final String NULL_STRING = 'NULL'
   private static final long FILE_WRITE_DELAY_MS = 1000L

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
    * @param isFormat whether to format the data
    * @return
    */
   def exportVSObject(String fileName, def data, Boolean isFormat) {
      try {
         prepareFile(fileName)

         if(data == null || data == '') {
            data = ['null']
         }

         if(data instanceof DataSet || data instanceof BoxDataSet) {
            exportDataSet(fileName, data)
         }
         else if(data instanceof TableLens) {
            exportTableLens(fileName, data, isFormat)
         }
         else if(data instanceof SelectionList) {
            exportSelectionList(fileName, data)
         }
         else if(data instanceof StringBuffer) {
            writeToFile(fileName, data.toString())
         }
         else if(data instanceof BufferedImage) {
            exportBufferedImage(fileName, data)
         }
         else if(data != null) {
            writeToFile(fileName, data.toString())
         }
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to export VS object to file: ${fileName}", e)
      }
   }

   /**
    * export WS table data to text
    * @param fileName
    * @param data
    * @return
    */
   def exportWSObject(String fileName, def data) {
      try {
         prepareFile(fileName)

         if(data == null || data == '') {
            data = ['null']
         }

         if(data instanceof TableLens) {
            exportTableLens(fileName, data, true)
            // Note: Consider removing this sleep if not necessary for file system synchronization
            Thread.sleep(FILE_WRITE_DELAY_MS)
         }
         else if(data instanceof StringBuffer) {
            writeToFile(fileName, data.toString())
         }
         else if(data != null) {
            writeToFile(fileName, data.toString())
         }
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to export WS object to file: ${fileName}", e)
      }
   }

   /**
    * return export file path, store by asset path
    * @param asset_id
    * @param packageName
    * @return export folder path
    * @throws IllegalArgumentException if asset_id format is invalid
    */
   String getExportFolderPath(String asset_id, String packageName) {
      String resourcePath = getResourcePath()
      resourcePath = (packageName == null) ? (resourcePath + '/exportData') : (resourcePath + '/exportData/' + packageName)

      if(asset_id.startsWith('1^128^__')) {
         return resourcePath + File.separator + asset_id.substring(asset_id.lastIndexOf('^') + 1)
      }
      else if(asset_id.startsWith('1^2^__')) {
         return resourcePath + File.separator + '/WSExp' + File.separator +
                 asset_id.substring(asset_id.lastIndexOf('^') + 1)
      }
      else {
         throw new IllegalArgumentException("Invalid asset_id format: ${asset_id}")
      }
   }

   private StringBuffer printSelectionValue(SelectionValue value, StringBuffer buffer) {
      if(value instanceof CompositeSelectionValue) {
         String valueStr = value.toString()
         if(valueStr.contains('SelectionList')) {
            String str = value.getLevel() + '--' + valueStr.split("\\[SelectionList")[0].
                    toString().replaceAll('SelectionValue', '')
            value.getLevel().times {
               str = ' ' + str
            }
            buffer.append(str)
            buffer.append('\n')
         }
         value.getSelectionList().getSelectionValues().eachWithIndex { SelectionValue entry, int i ->
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
         buffer.append(value.toString().replaceAll('SelectionValue', ''))
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
            return isFormat ? format(lens.getObject(r, c)) : lens.getObject(r, c)
         }
      }
   }

   private def format(def val) {
      if(val == null || val == '') {
         return NULL_STRING
      }
      else if(val instanceof java.util.Date && !(val instanceof java.sql.Date) &&
              !(val instanceof java.sql.Time)) {
         return DATE_FORMAT.get().format(val)
      }
      else if(val instanceof Float || val instanceof Double) {
         double doubleVal = (val as Number).doubleValue()
         if(Double.isNaN(doubleVal)) {
            return 'NaN'
         }
         else {
            return NUM_FORMAT.format(doubleVal)
         }
      }
      return val
   }

   /**
    * Prepare file for writing: create parent directories and delete existing file
    */
   private void prepareFile(String fileName) {
      File file = new File(fileName)
      File parentFile = file.getParentFile()
      if(parentFile != null && !parentFile.exists()) {
         if(!parentFile.mkdirs()) {
            throw new RuntimeException("Failed to create directory: ${parentFile.absolutePath}")
         }
      }
      if(file.exists() && !file.delete()) {
         throw new RuntimeException("Failed to delete existing file: ${file.absolutePath}")
      }
   }

   /**
    * Export DataSet or BoxDataSet to file
    */
   private void exportDataSet(String fileName, def data) {
      StringBuilder buffer = new StringBuilder()
      int colCount = data.colCount

      // Write headers
      for(int col = 0; col < colCount; col++) {
         buffer.append(data.getHeader(col))
         if(col < colCount - 1) {
            buffer.append(', ')
         }
      }
      buffer.append('\n')

      // Write data rows
      int rowCount = data.rowCount
      for(int row = 0; row < rowCount; row++) {
         for(int col = 0; col < colCount; col++) {
            buffer.append(format(data.getData(col, row)))
            if(col < colCount - 1) {
               buffer.append(', ')
            }
         }
         buffer.append('\n')
      }

      String content = "The data size(row x col) is:(${rowCount + 1} x ${colCount})\n" + buffer.toString()
      writeToFile(fileName, content)
   }

   /**
    * Export TableLens to file
    */
   private void exportTableLens(String fileName, TableLens data, Boolean isFormat) {
      TableLens table = wrapTable(data, isFormat)
      StringBuilder buffer = new StringBuilder()
      int colCount = table.getColCount()
      int row = 0

      while(table.moreRows(row)) {
         for(int col = 0; col < colCount; col++) {
            def obj = table.getObject(row, col)
            if(obj instanceof DCMergeDatesCell) {
               obj = ((DCMergeDatesCell) obj).getFormatedOriginalDate()
            }
            buffer.append(obj)
            if(col < colCount - 1) {
               buffer.append(', ')
            }
         }
         buffer.append('\n')
         row++
      }

      String content = "The data size(row x col) is:(${row} x ${colCount})\n" + buffer.toString()
      writeToFile(fileName, content)
   }

   /**
    * Export SelectionList to file
    */
   private void exportSelectionList(String fileName, SelectionList data) {
      StringBuilder bufferAll = new StringBuilder()
      data.getSelectionValues().eachWithIndex { SelectionValue entry, int i ->
         StringBuilder buffer = new StringBuilder()
         bufferAll.append(printSelectionValue(entry, buffer).toString())
      }

      String content = "**level--[label, status, level, value], 0|8[unselectd], 9|1[selected], 2[include], 4[exclude]**\n" + bufferAll.toString()
      writeToFile(fileName, content)
   }

   /**
    * Export BufferedImage to PNG file
    */
   private void exportBufferedImage(String fileName, BufferedImage data) {
      File file = new File(fileName)
      if(!ImageIO.write(data, 'png', file)) {
         throw new RuntimeException("Failed to write image to file: ${fileName}")
      }
   }

   /**
    * Write string content to file
    */
   private void writeToFile(String fileName, String content) {
      File file = new File(fileName)
      file.withPrintWriter { printWriter ->
         printWriter.print(content)
      }
   }

   /**
    * Get resource path, cached for performance
    */
   private String getResourcePath() {
      try {
         return new File(this.class.getResource('/expectData').getPath()).getParent()
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to get resource path", e)
      }
   }
}
