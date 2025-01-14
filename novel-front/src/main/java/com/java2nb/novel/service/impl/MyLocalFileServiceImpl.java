package com.java2nb.novel.service.impl;

import com.java2nb.novel.core.utils.PictureUtil;
import com.java2nb.novel.service.MyFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service(value = "db")
public class MyLocalFileServiceImpl implements MyFileService {

    @Override
    public String savePic(MultipartFile file, String picSavePath) throws IOException {
        return PictureUtil.createPicFile(file, picSavePath);
    }

    @Override
    public void deletePic(String picPath) throws IOException {
        PictureUtil.deleteCurrPic(picPath);
    }
}
