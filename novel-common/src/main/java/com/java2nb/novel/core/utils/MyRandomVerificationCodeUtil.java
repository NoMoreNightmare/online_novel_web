package com.java2nb.novel.core.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Random;

@Slf4j
public class MyRandomVerificationCodeUtil {
    /**
     * redis缓存验证码的key前缀
     */
    public static final String VERIFICATION_CODE = "VerificationCodeForRegister";

    private String possibleCharacter = "01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 图片的宽度
     */
    private int width = 100;
    /**
     * 图片的高度
     */
    private int height = 38;

    private int linesLength = 10;

    /**
     * 验证码的长度
     */
    private int stringNum = 4;

    private Random random = new Random();

    /**
     * 获得字体
     */
    private Font getFont() {
        return new Font("Fixedsys", Font.PLAIN, 23);
    }

    @SneakyThrows
    public String genRandCodeImage(OutputStream out) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics graphics = image.getGraphics();
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(getRandColor());
        for (int i = 0; i < linesLength; i++) {
            drawLine(graphics);
        }

        String verificationCode = "";
        for (int i = 0; i < stringNum; i++) {
            verificationCode = drawCharacter(graphics, i, verificationCode);
        }

        graphics.dispose();

        ImageIO.write(image, "JPEG", out);

        return verificationCode;
    }

    private Color getRandColor() {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    /**
     * 绘制干扰线
     * @param g
     */
    private void drawLine(Graphics g) {
        int x = random.nextInt(width / 2);
        int y = random.nextInt(height / 2);
        int xl = random.nextInt(width / 2);
        int yl = random.nextInt(height / 2);
        g.drawLine(x, y, x + xl, y + yl);
    }

    /**
     * 绘制字符
     */
    private String drawCharacter(Graphics g, int pos, String currString) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.translate(random.nextInt(3), random.nextInt(3));

        AffineTransform original = g2d.getTransform();

        g2d.setFont(getFont());

        g2d.shear(random.nextDouble() / 4, random.nextDouble() / 4);

        g2d.setColor(getRandColor());

        String charToDraw = String.valueOf(possibleCharacter.charAt(random.nextInt(possibleCharacter.length())));



        g2d.drawString(charToDraw, 13 * pos, 23);

        g2d.setTransform(original);

        currString += charToDraw;

        return currString;
    }

}
