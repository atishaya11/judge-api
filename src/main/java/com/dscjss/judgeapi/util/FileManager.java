package com.dscjss.judgeapi.util;

import com.dscjss.judgeapi.s3.AmazonS3Config;
import com.dscjss.judgeapi.s3.S3Services;
import com.dscjss.judgeapi.submission.dto.TaskResult;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import static com.dscjss.judgeapi.util.Constants.*;

@Service
public class FileManager {

    private S3Services s3Services;

    @Autowired
    public FileManager(S3Services s3Services) {
        this.s3Services = s3Services;
    }

    public void uploadSubmissionSourceFile(String fileName, File file) throws InterruptedException {
        s3Services.uploadFile(fileName, file);

    }

    public void uploadSubmissionOutputFile(String fileName, File file) throws InterruptedException {
        s3Services.uploadFile(fileName, file);

    }

    public void uploadFile(String fileName, File file) throws InterruptedException {
        s3Services.uploadFile(fileName, file);

    }
    public File downloadSourceFile(String fileName) throws InterruptedException {
        String path = "/tmp/source/" + fileName;
        File dir = new File(path);
        boolean created = dir.mkdirs();
        File tempFile = new File(dir.getAbsolutePath() +"/" + System.currentTimeMillis());
        s3Services.downloadFile(fileName, tempFile.getAbsolutePath());
        return tempFile;
    }

    @Async
    public void uploadStreams(TaskResult taskResult, int submissionId, int testCaseId) {
        String baseLocation = submissionId + "/" + testCaseId + "/";
        try {
            String outputFileName = baseLocation + FILE_NAME_STD_OUTPUT;
            uploadOutputFile(outputFileName, taskResult.getStdOut());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        String compileErrorFileName = baseLocation + FILE_NAME_COMPILE_ERROR;
        try {
            uploadCompileError(compileErrorFileName, taskResult.getCompileErr());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        String stdErrorFileName = baseLocation + FILE_NAME_STD_ERROR;
        try {
            uploadStdError(stdErrorFileName, taskResult.getStdErr());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void uploadStdError(String stdErrorFileName, String stdErr) throws IOException, InterruptedException {
        uploadData(stdErrorFileName, stdErr);
    }

    private void uploadCompileError(String compileErrorFile, String stdOut) throws IOException, InterruptedException {
        uploadData(compileErrorFile, stdOut);
    }

    private void uploadOutputFile(String outputFileName, String output) throws IOException, InterruptedException {
        uploadData(outputFileName, output);
    }

    private void uploadData(String fileName, String data) throws IOException, InterruptedException {
        File tempFile = new File("/tmp" + "/out/" + fileName);
        FileUtils.writeStringToFile(tempFile, data);
        uploadFile(fileName, tempFile);
        boolean delete = tempFile.delete();
    }

}
