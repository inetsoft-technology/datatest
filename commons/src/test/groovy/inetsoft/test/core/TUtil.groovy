package inetsoft.test.core

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.tools.imageio.ImageIOUtil
import java.awt.image.BufferedImage
import java.text.SimpleDateFormat

import inetsoft.sree.security.IdentityID
import inetsoft.sree.security.SRPrincipal
import inetsoft.util.Tool

class TUtil {
   /**
    * create a user principal
    * @param userName user name
    * @param roles  the roles of user
    * @param groups  the groups of user
    * @return
    */
   static SRPrincipal createPrincipal(String userName, String[] roles, String[] groups) {
      IdentityID identityUser = new IdentityID(userName, 'host-org')
      IdentityID[] identityRoles = new IdentityID[0]
      roles.each { role ->
         IdentityID newRole = role != 'Administrator' ? new IdentityID(role, 'host-org'): new IdentityID(role, null)
         newRole.setName(role)
         identityRoles += newRole
      }
      //return new SRPrincipal(identityUser, identityRoles, groups, 'host-org', Tool.getSecureRandom().nextLong())
      SRPrincipal principal = new SRPrincipal(identityUser, identityRoles, groups, 'host-org', Tool.getSecureRandom().nextLong())
      principal.setIgnoreLogin(true)

      return principal
   }

   /**
    * convert pdf to png, use pdfbox
    * https://pdfbox.apache.org/2.0/migration.html
    * @param pdf
    */
   def convertPDFToPNG(String pdf) {
      convertPDFToPNG(pdf, false)
   }

   def convertPDFToPNG(String pdf, Boolean isOnePage) {
      PDDocument pdDocument = null
      try {
         pdDocument = PDDocument.load(new File(pdf))
         PDFRenderer pdfRenderer = new PDFRenderer(pdDocument)
         String pdfFilename = pdf.minus('.pdf')

         def pageCounter = 0
         def pageTotals = pdDocument.getNumberOfPages()

         for (PDPage page : pdDocument.getPages()) {
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageCounter, 72, ImageType.RGB)

            def suffix = (isOnePage && pageTotals == 1) ? '.png' : '-' + (pageCounter++) + '.png';
            ImageIOUtil.writeImage(bufferedImage, pdfFilename + suffix, 300)
            Thread.sleep(500)
         }
      } finally {
         if (pdDocument != null) {
            pdDocument.close()
         }
      }
   }

   /**
    * modify system date to one date
    */
   def changeSystemDateToDay(def ndate) {
      String wDatecmd = 'runas /savecred /user:administrator "cmd /c date ' + ndate + '"'
      String osName = System.getProperty("os.name")

      if (osName.toLowerCase().contains('windows')) {
         Runtime.getRuntime().exec(wDatecmd)
         Runtime.getRuntime().exec('runas /savecred /user:administrator "cmd /c time 00:00:00"')
         Thread.sleep(500)
      } else if (osName.toLowerCase().contains('linux')) {
         /*  Runtime.getRuntime().exec('sudo date -s ' + ndate)
           Thread.sleep(500)*/
      } else {
         new Exception('------ modify system date failed: -----------' + ndate)
      }
   }

   /**
    * modify system date to web current date
    */
   def restoreSystemTime() {
      URL url = new URL('http://www.baidu.com')
      URLConnection urlConnection = url.openConnection()
      urlConnection.connect()
      long ld = urlConnection.getDate()
      SimpleDateFormat dateFormat = new SimpleDateFormat('yyyy-MM-dd')
      SimpleDateFormat timeFormat = new SimpleDateFormat('HH:mm:ss')
      String osName = System.getProperty("os.name")

      if (osName.toLowerCase().contains('windows')) {
         Runtime.getRuntime().exec('runas /savecred /user:administrator "cmd /c date ' + dateFormat.format(new Date(ld)) + '"')
         Runtime.getRuntime().exec('runas /savecred /user:administrator "cmd /c time ' + timeFormat.format(new Date(ld)) + '"')
         Thread.sleep(500)
      } else if (osName.toLowerCase().contains('linux')) {
         /* Runtime.getRuntime().exec('sudo date -s ' + lsimpleDateFormat.format(new Date(ld)))
          Thread.sleep(500)*/
      } else {
         new Exception('------restore date to current failed: -----------')
      }
   }
}
