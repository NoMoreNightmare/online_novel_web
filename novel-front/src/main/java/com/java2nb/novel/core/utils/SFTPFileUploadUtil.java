package com.java2nb.novel.core.utils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import java.io.*;
import java.util.Properties;

/**
 * 通过SFTP上传文件到nginx服务器
 */
/*
public class SFTPFileUploadUtil {

    @Value("${nginx.address}")
    private String nginxAddress;
    @Value("${nginx.port}")
    private int nginxPort;
    @Value("${nginx.directoryPath}")
    private String nginxDirectoryPath;
    @Value("${nginx.server.username}")
    private String nginxServerUsername;
    @Value("${nginx.server.password}")
    private String nginxServerPassword;



    public boolean uploadFile(String from, String to) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(nginxServerUsername, nginxAddress, nginxPort);
        session.setPassword(nginxServerPassword);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        File file = new File(from);

        try (InputStream inputStream = new FileInputStream(file)) {
            channelSftp.put(inputStream, nginxDirectoryPath + to);
        }catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

        return true;
    }

    public boolean uploadFile() throws JSchException, FileNotFoundException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(nginxServerUsername, nginxAddress, nginxPort);
        session.setPassword(nginxServerPassword);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        File file = new File("index/index.html");

        try (InputStream inputStream = new FileInputStream(file)) {
            channelSftp.put(inputStream, nginxDirectoryPath + "index.html");
        }catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

        return true;
    }
}*/
