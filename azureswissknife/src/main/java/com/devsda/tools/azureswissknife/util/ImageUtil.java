package com.devsda.tools.azureswissknife.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageUtil {


    public static ByteBuffer getByteBufferOfImage(String imageLocation) throws IOException {

        BufferedImage bImage = ImageIO.read(new File(imageLocation));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bImage, "jpg", bos );
        byte [] data = bos.toByteArray();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        return byteBuffer;
    }
}
