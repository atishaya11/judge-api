package com.dscjss.judgeapi.s3;


import java.io.File;

public interface S3Services {

    void uploadFile(String fileName, File file) throws InterruptedException;

    void downloadFile(String keyName, String downloadFilePath) throws InterruptedException;
}
