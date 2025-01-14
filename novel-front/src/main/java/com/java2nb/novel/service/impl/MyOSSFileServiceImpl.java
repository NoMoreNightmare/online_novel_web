package com.java2nb.novel.service.impl;

import com.java2nb.novel.core.config.OssProperties;
import com.java2nb.novel.core.utils.PictureUtil;
import com.java2nb.novel.service.MyFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service(value = "oss")
public class MyOSSFileServiceImpl implements MyFileService {
    @Autowired
    private OssProperties ossProperties;

    @Override
    public String savePic(MultipartFile file, String picSavePath) throws IOException {
        return PictureUtil.createPicFile(file, picSavePath, ossProperties);
    }

    @Override
    public void deletePic(String picPath) throws IOException {
        PictureUtil.deleteCurrPic(picPath, ossProperties);
    }

    public String getDownloadUrl(String picPath) throws IOException {
        return PictureUtil.getFullDownloadUrl(picPath, ossProperties);
    }
}
