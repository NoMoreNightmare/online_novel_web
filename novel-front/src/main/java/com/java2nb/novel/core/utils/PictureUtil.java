package com.java2nb.novel.core.utils;

import com.java2nb.novel.core.exception.FileCreatedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class PictureUtil {

    public static void deleteCurrPic(String picPath) {
        File file = new File(picPath);
        boolean delete = file.delete();
    }

    public static String createFile(MultipartFile file,String picSavePath) {
        String directory = generateDirectoryPath(picSavePath);

        String originalFilename = file.getOriginalFilename();
        String type = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if(PictureUtil.isImage(type)){
            String filename = UUID.randomUUID().toString() + "." + type;
            File saveFile = new File(picSavePath + directory, filename);
            try {
                file.transferTo(saveFile);
            } catch (IOException e) {
                throw new FileCreatedException();
            }
            return directory + "/" + filename;
        }

        return null;

    }

    private static boolean isImage(String type) {
        if("jpg".equalsIgnoreCase(type) || "png".equalsIgnoreCase(type) || "gif".equalsIgnoreCase(type) || "bmp".equalsIgnoreCase(type)
                || "jpeg".equalsIgnoreCase(type) || "tiff".equalsIgnoreCase(type)
        || "tif".equalsIgnoreCase(type)) {
            return true;
        }

        return false;
    }

    private static String generateDirectoryPath(String picSavePath) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        String directory = Constants.LOCAL_PIC_PREFIX + year + "/" + month;
        File file = new File(picSavePath + directory);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if(!mkdirs){
                throw new FileCreatedException();
            }
        }

        return directory;


    }


}
