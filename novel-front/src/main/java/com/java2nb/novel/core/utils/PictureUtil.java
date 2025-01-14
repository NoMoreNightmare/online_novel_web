package com.java2nb.novel.core.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.java2nb.novel.core.config.OssProperties;
import com.java2nb.novel.core.exception.FileCreatedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;


public class PictureUtil {

    public static void deleteCurrPic(String picPath) {
        File file = new File(picPath);
        boolean delete = file.delete();
    }

    //TODO 这里可以把路径改成oss的路径
    public static String createPicFile(MultipartFile file, String picSavePath) {
        String directory = generatePicDirectoryPath(picSavePath, false);

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


    public static void deleteCurrPic(String picPath, OssProperties ossProperties) {
        OSS ossClient = new OSSClientBuilder().build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());

        ossClient.deleteObject(ossProperties.getBucketName(), picPath);
        ossClient.shutdown();
    }

    //TODO 这里可以把路径改成oss的路径
    public static String createPicFile(MultipartFile file, String picSavePath, OssProperties ossProperties) {
        String directory = generatePicDirectoryPath(picSavePath, true);

        String originalFilename = file.getOriginalFilename();
        String type = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if(PictureUtil.isImage(type)){
            String filename = UUID.randomUUID().toString() + "." + type;

            OSS ossClient = new OSSClientBuilder().build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
            try {
                ossClient.putObject(ossProperties.getBucketName(), picSavePath + directory + "/" + filename, file.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if(ossClient != null){
                    ossClient.shutdown();
                }
            }

//            File saveFile = new File(picSavePath + directory, filename);
//            try {
//                file.transferTo(saveFile);
//            } catch (IOException e) {
//                throw new FileCreatedException();
//            }
            return getFullDownloadUrl(picSavePath + directory + "/" + filename, ossProperties);


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

    private static String generatePicDirectoryPath(String picSavePath, boolean isRemote) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        String directory = "";
        if(!isRemote){
            directory = Constants.LOCAL_PIC_PREFIX + year + "/" + month;
        }else{
            directory = "/" + year + "/" + month;
        }

        File file = new File(picSavePath + directory);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if(!mkdirs){
                throw new FileCreatedException();
            }
        }

        return directory;
    }


    public static String getFullDownloadUrl(String picPath, OssProperties ossProperties) {
        StringBuffer sb = new StringBuffer();
        sb.append("https://");
        sb.append(ossProperties.getBucketName());
        sb.append(".");
        sb.append(ossProperties.getEndpoint());
        sb.append("/");
        sb.append(picPath);
        return sb.toString();
    }
}
