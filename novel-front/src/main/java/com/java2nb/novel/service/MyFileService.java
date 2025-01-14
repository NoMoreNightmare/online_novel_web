package com.java2nb.novel.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MyFileService {
    public String savePic(MultipartFile file, String picSavePath) throws IOException;

    public void deletePic(String picPath) throws IOException;
}
