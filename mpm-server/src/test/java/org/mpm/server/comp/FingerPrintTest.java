package org.mpm.server.comp;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class FingerPrintTest {

    @Test
    public void testCompare() throws IOException {
        /*
        FingerPrint fp1 = new FingerPrint(ImageIO.read(new File("/Users/simon/Downloads/favicon.ico")));
        FingerPrint fp2 = new FingerPrint(ImageIO.read(new File("/Users/simon/Downloads/logo_副本.jpeg")));
         */
        FingerPrint fp1 = new FingerPrint(
                ImageIO.read(new File("/Users/simon/Downloads/2024 公司照片/公司25周年/4.jpeg")));
        FingerPrint fp2 = new FingerPrint(
                ImageIO.read(new File("/Users/simon/Downloads/2024 公司照片/公司25周年/5.jpeg")));

        System.out.println(fp1.toString(true));
        System.out.printf("sim=%f", fp1.compare(fp2));
    }
}