package com.dscjss.judgeapi.util;

import com.dscjss.judgeapi.s3.AmazonS3Config;
import com.dscjss.judgeapi.s3.S3Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileManager {

    private S3Services s3Services;

    @Autowired
    public FileManager(S3Services s3Services) {
        this.s3Services = s3Services;
    }

    public void uploadSubmissionSourceFile(String fileName, File file) throws InterruptedException {
        ;
        s3Services.uploadFile(fileName, file);

    }

    public void uploadSubmissionOutputFile(String fileName, File file) throws InterruptedException {
        ;
        s3Services.uploadFile(fileName, file);

    }

}
