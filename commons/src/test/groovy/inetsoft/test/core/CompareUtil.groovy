package inetsoft.test.core

import com.github.romankh3.image.comparison.ImageComparison
import com.github.romankh3.image.comparison.ImageComparisonUtil
import com.github.romankh3.image.comparison.model.ImageComparisonResult
import com.github.romankh3.image.comparison.model.ImageComparisonState
import inetsoft.report.internal.png.PNGEncoder

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage
import java.util.List
import java.io.*

class CompareUtil {

   /**
    * compare all text file by asset name
    */
   def compareData(String asset_id, String packageName) {
      compareData(asset_id, null, packageName)
   }

   /**
    * compare text file by asset name
    * @param fileNames txt file names
    */
   def compareData(String asset_id, String[] fileNames, String packageName) {
      def resArray = []
      String folderPath = exportUtil.getExportFolderPath(asset_id, packageName)

      try {
         if (fileNames != null) {
            fileNames.each {
               String resPath = folderPath + File.separator + it + '.txt'
               resArray.add(FileCompare(resPath))
            }
         } else {
            File folder = new File(folderPath)
            if (!folder.exists() || !folder.isDirectory()) {
               def status = [:]
               status.put('false', "Compare Failed: Folder does not exist or is not a directory: ${folderPath}")
               collectAndAssertFailures([status])
               return
            }

            File[] files = folder.listFiles()
            if (files == null) {
               def status = [:]
               status.put('false', "Compare Failed: Cannot list files in folder (may be a permission issue): ${folderPath}")
               collectAndAssertFailures([status])
               return
            }

            files.each { file ->
               if (file.isFile()) {
                  String resPath = file.absolutePath
                  if (resPath.endsWith('.txt') || resPath.endsWith('.html')) {
                     resArray.add(FileCompare(resPath))
                  }
               }
            }
         }

         collectAndAssertFailures(resArray)
      } catch (Exception e) {
         def status = [:]
         status.put('false', "Compare Failed with exception: ${e.message}\nFolder: ${folderPath}")
         collectAndAssertFailures([status])
      }
   }

   /**
    * compare all image file by asset name
    */
   void compareImage(String asset_id, String packageName) {
      compareImage(asset_id, null, packageName)
   }

   /**
    * compare the image files by asset name
    * @param fls image names
    */
   void compareImage(String asset_id, String[] fls, String packageName) {
      def resArray = []
      String folderPath = exportUtil.getExportFolderPath(asset_id, packageName)

      if (fls != null) {
         fls.each {
            String resPath = folderPath + File.separator + it + '.png'
            File resFile = new File(resPath)
            if (resFile.exists() && !resPath.endsWith('_diff.png')) {
               resArray.add(PNGCompare(resPath))
            }
         }
      } else {
         File folder = new File(folderPath)
         if (!folder.exists()) {
            assert false: "Exported Folder didn't create, \n" + folder.getPath()
         }
         folder.listFiles().each { file ->
            String resPath = file.absolutePath
            if (resPath.endsWith('.png') && !resPath.endsWith('_diff.png')) {
               resArray.add(PNGCompare(resPath))
            }
         }
      }

      collectAndAssertFailures(resArray)
   }

   /**
    *  https://github.com/romankh3/image-comparison
    *  use plugin to compare tow Image and print the diff image
    * @param resFile
    * @return
    */

   def PNGCompare(String resFile) {
      PNGCompare(resFile, 0.001)
   }

   def PNGCompare(String resFile, def allowedPixelPercent) {
      String[] temp = resFile.split('exportData')
      def status = [:]

      BufferedImage expectImage = null
      BufferedImage resImage = null

      try {
         expectImage = ImageComparisonUtil.readImageFromResources('expectData' + temp[1])
         resImage = ImageComparisonUtil.readImageFromResources('exportData' + temp[1])

         ImageComparison imageComparison = new ImageComparison(expectImage, resImage)
         imageComparison.setAllowingPercentOfDifferentPixels(allowedPixelPercent)
         ImageComparisonResult comparisonResult = imageComparison.compareImages()

         println '===diff percent=====' + comparisonResult.getDifferencePercent()

         def compareState = comparisonResult.getImageComparisonState()

         if (compareState == ImageComparisonState.SIZE_MISMATCH) {
            status.put('false', 'FAILED! The PNG size is Different! See ' + resFile)
            return status
         } else if (compareState == ImageComparisonState.MISMATCH) {
            String diffPath = resFile.replaceAll('.png', '_diff.png')
            BufferedImage resultImage = comparisonResult.getResult()
            mergePNG(resultImage, expectImage, diffPath)
            status.put('false', 'FAILED! Please check diff file:\n' + diffPath)
            return status
         } else if (compareState == ImageComparisonState.MATCH) {
            status.put('true', 'PASSED!')
            return status
         } else {
            status.put('false', 'FAILED! Unknown reasons.')
            return status
         }
      } catch (Exception e) {
         status.put('false', "Compare Failed with exception: ${e.message}\nFile: ${resFile}")
         return status
      } finally {
         // 确保资源正确释放
         if (resImage != null) {
            resImage.flush()
         }
         if (expectImage != null) {
            expectImage.flush()
         }
      }
   }

   /**
    * merge PNG
    * @param resultImage diff result image
    * @param expImage expected image
    * @param diffPath path to save merged diff image
    * @return
    */
   private mergePNG(BufferedImage resultImage, BufferedImage expImage, String diffPath) {
      def tmpWidth, tmpHeight
      tmpHeight = resultImage.getHeight()
      tmpWidth = resultImage.getWidth()

      BufferedImage combined = new BufferedImage(tmpWidth, tmpHeight + expImage.getHeight() + 10, BufferedImage.TYPE_INT_ARGB)
      Graphics g = combined.getGraphics()
      try {
         g.drawImage(resultImage, 0, 0, null)
         g.drawImage(expImage, 0, tmpHeight + 5, null)
         resultImage.flush()
         expImage.flush()

         // 使用 writeImage 方法避免 ImageIO.write() 导致的 OOM 问题
         writeImage(combined, diffPath)
         combined.flush()
      } finally {
         g.dispose()
      }
   }

   /**
    * Because ImageIO.write() always caused outOfMemory, use other way to save image
    * @param bufferedImage
    * @param filePath
    */
   def writeImage(BufferedImage bufferedImage, String filePath) {
      ByteArrayOutputStream output = new ByteArrayOutputStream()
      try {
         PNGEncoder pngEncoder = new PNGEncoder(bufferedImage, true)
         pngEncoder.encode(output)

         new File(filePath).withOutputStream { fileOutputStream ->
            fileOutputStream.write(output.toByteArray())
         }
      } finally {
         output.close()
      }
   }

   /**
    * compare two txt file
    * @param expFile
    * @param resFile
    */
   def FileCompare(String resFile) {
      FileCompare(resFile, null, false)
   }

   def FileCompare(String resFile, Boolean isHTML) {
      FileCompare(resFile, null, isHTML)
   }

   def FileCompare(String resFile, String charset, Boolean isHTML) {
      def status = [:]

      String expFile = resFile.replace('exportData', 'expectData')
      File resFileObj = new File(resFile)
      File expFileObj = new File(expFile)

      if (!expFileObj.exists() || !resFileObj.exists()) {
         status.put('false', "Compare Failed, Expect or Result File [" + expFile +
                 "] not found!")
         return status
      }

      // 快速失败：文件大小检查
      if (resFileObj.length() != expFileObj.length()) {
         status.put('false', "Compare Failed, file size is different. " +
                 "Expect file size: ${expFileObj.length()}, Result file size: ${resFileObj.length()}")
         return status
      }

      // 根据文件类型和大小选择比较策略
      long fileSize = resFileObj.length()
      boolean isHTMLFile = isHTML || resFile.endsWith('.html')
      long threshold = 200 * 1024  // 200KB 阈值

      // HTML文件：一律使用流式处理（体积可能很大）
      if (isHTMLFile) {
         return streamCompare(resFile, expFile, charset, true)
      }

      // 文本文件：根据大小选择策略
      if (fileSize > threshold) {
         return streamCompare(resFile, expFile, charset, false)
      } else {
         return memoryCompare(resFile, expFile, charset, false)
      }
   }

   /**
    * Memory-based file comparison for small files
    * @param resFile result file path
    * @param expFile expect file path
    * @param charset file charset
    * @param isHTML whether to normalize HTML
    * @return comparison status
    */
   private def memoryCompare(String resFile, String expFile, String charset, Boolean isHTML) {
      def status = [:]

      List resList, expList
      if (charset != null) {
         resList = new File(resFile).readLines(charset)
         expList = new File(expFile).readLines(charset)
      } else {
         resList = new File(resFile).readLines()
         expList = new File(expFile).readLines()
      }

      if (isHTML) {
         def classPattern1 = ".c[0-9]* \\{"
         def classPattern2 = "class='c[0-9]*'"
         resList = resList.eachWithIndex { String it, int idx ->
            if (it =~ classPattern1 || it =~ classPattern2) {
               resList[idx] = it.replaceAll(classPattern1, " {").replaceAll(classPattern2, "class=''")
            }
         }
         expList = expList.eachWithIndex { String it, int idx ->
            if (it =~ classPattern1 || it =~ classPattern2) {
               expList[idx] = it.replaceAll(classPattern1, " {").replaceAll(classPattern2, "class=''")
            }
         }
      }

      if ((resList == null || resList.size() <= 0) && expList != null) {
         status.put('false', "Compare Failed, Result File[ \n" + resFile + "] " +
                 "is empty, but Expect File[\n" + expFile + "] is not empty!")
         return status
      } else if (expList == null || expList.size() <= 0) {
         status.put('false', "Compare Failed, Expect File [ \n" + expFile + "] is empty!")
         return status
      } else if (expList.size() != resList.size()) {
         status.put('false', "Compare Failed, row number is " +
                 "different in expect file[ \n" + expFile + "] and result file[\n" +
                 resFile + "]!\n [Expect Rows:" + expList.size() + "]; Result Rows: [" + resList.size() + "]")
         return status
      } else if (expList != resList) {
         for (i in 0..<expList.size()) {
            String diffFile = expFile.substring(expFile.indexOf("expectData") + 10)
            if (expList[i] != resList[i]) {
               status.put('false', "Compare Failed at file, [\n" +
                       diffFile + "] in row [" + i + "], \n expect:[" + expList[i] + "], \n" +
                       "result:[" + resList[i] + "]")
               return status
            }
         }
      } else {
         status.put('true', "PASSED!")
         return status
      }
   }

   /**
    * Stream-based file comparison for large files
    * Uses constant memory by reading files line by line
    * @param resFile result file path
    * @param expFile expect file path
    * @param charset file charset
    * @param isHTML whether to normalize HTML
    * @return comparison status
    */
   private def streamCompare(String resFile, String expFile, String charset, Boolean isHTML) {
      def status = [:]

      BufferedReader resReader = createReader(resFile, charset)
      BufferedReader expReader = createReader(expFile, charset)

      try {
         int lineNum = 0
         String resLine, expLine

         while ((resLine = resReader.readLine()) != null) {
            expLine = expReader.readLine()
            lineNum++

            // 检查期望文件是否提前结束
            if (expLine == null) {
               String diffFile = expFile.substring(expFile.indexOf("expectData") + 10)
               status.put('false', "Compare Failed at file [${diffFile}]: " +
                       "Result file has more lines. Mismatch at line ${lineNum}, " +
                       "result file has extra line: [${resLine}]")
               return status
            }

            // HTML处理（如果需要）
            if (isHTML) {
               resLine = normalizeHTML(resLine)
               expLine = normalizeHTML(expLine)
            }

            // 逐行比较
            if (resLine != expLine) {
               String diffFile = expFile.substring(expFile.indexOf("expectData") + 10)
               status.put('false', "Compare Failed at file [${diffFile}] in row [${lineNum}], " +
                       "\n expect:[${expLine}], \n result:[${resLine}]")
               return status
            }
         }

         // 检查期望文件是否还有剩余行
         if ((expLine = expReader.readLine()) != null) {
            String diffFile = expFile.substring(expFile.indexOf("expectData") + 10)
            status.put('false', "Compare Failed at file [${diffFile}]: " +
                    "Expect file has more lines. Mismatch at line ${lineNum + 1}, " +
                    "expect file has extra line: [${expLine}]")
            return status
         }

         status.put('true', "PASSED!")
         return status
      } finally {
         resReader.close()
         expReader.close()
      }
   }

   /**
    * Create BufferedReader for file reading with optional charset
    * @param filePath file path
    * @param charset file charset (null for default)
    * @return BufferedReader instance
    */
   private BufferedReader createReader(String filePath, String charset) {
      if (charset != null) {
         return new BufferedReader(new InputStreamReader(
                 new FileInputStream(filePath), charset))
      } else {
         return new BufferedReader(new FileReader(filePath))
      }
   }

   /**
    * Normalize HTML line by removing dynamic class names
    * @param line HTML line to normalize
    * @return normalized HTML line
    */
   private String normalizeHTML(String line) {
      def classPattern1 = ".c[0-9]* \\{"
      def classPattern2 = "class='c[0-9]*'"
      if (line =~ classPattern1 || line =~ classPattern2) {
         return line.replaceAll(classPattern1, " {")
                 .replaceAll(classPattern2, "class=''")
      }
      return line
   }

   /**
    * compile files(png,pdf,text) by Feature name
    * @param fls , the compared name. ['table1', 'table2'] as String[]
    * @param suiteName
    * @param suffix : '.pdf', '.png', '.txt'
    */

   def CompareFileByFeature(String[] fls, String suiteName, String fmt) {
      CompareFileByFeature(fls, suiteName, fmt, null)
   }

   def CompareFileByFeature(String[] fls, String suiteName, String fmt, def allowedPixelPercent) {
      String resourcePath = new File(this.class.getResource('/expectData').getPath()).getParent()
      String fileName = resourcePath + '/exportData' + suiteName
      def resArray = []

      if (fls != null) {
         fls.each {
            def resPath = fileName + File.separator + it + getSuffix(fmt)
            resArray.add(processFile(resPath, fmt, allowedPixelPercent))
         }
      } else {
         new File(fileName).listFiles().each { file ->
            resArray.add(processFile(file.absolutePath, fmt, allowedPixelPercent))
         }
      }

      collectAndAssertFailures(resArray)
   }

   private processFile(String resPath, String fmt, allowedPixelPercent) {
      if (fmt == 'PNG' && resPath.endsWith('.png') && !resPath.endsWith('_diff.png')) {
         return allowedPixelPercent != null ? PNGCompare(resPath, allowedPixelPercent) : PNGCompare(resPath)
      } else if (['TXT', 'CSV'].contains(fmt) && (resPath.endsWith('.csv') || resPath.endsWith('.txt'))) {
         return FileCompare(resPath)
      } else if ('HTML' == fmt && resPath.endsWith('.html')) {
         return FileCompare(resPath, true)
      }
   }

   /**
    * return the suffix of compile type
    * @param fmt
    * @return
    */
   def getSuffix(String fmt) {
      def suffix
      switch (fmt) {
         case 'PNG': suffix = ".png"; break
         case 'PDF': suffix = '.pdf'; break
         case 'TXT': suffix = '.txt'; break
         case 'HTML': suffix = '.html'; break
         case 'CSV': suffix = '.csv'; break
      }
      return suffix
   }

   /**
    * Collect failure messages from result array and assert if any failures found
    * @param resArray array of comparison results
    */
   private void collectAndAssertFailures(List resArray) {
      def failedMessages = resArray.findAll { it != null && it.getAt('false') != null }
              .collect { it.getAt('false') }
      if (failedMessages) {
         assert false: "\n" + failedMessages.join("\n") + "\n"
      }
   }

   ExportUtil exportUtil = new ExportUtil()
}