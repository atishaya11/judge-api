package com.dscjss.judgeapi.submission.service;


import com.dscjss.judgeapi.submission.dto.TaskResult;
import com.dscjss.judgeapi.submission.judge.DefaultMasterJudge;
import com.dscjss.judgeapi.submission.judge.Judge;
import com.dscjss.judgeapi.submission.judge.LineByLineJudge;
import com.dscjss.judgeapi.submission.judge.MasterJudge;
import com.dscjss.judgeapi.submission.model.Result;
import com.dscjss.judgeapi.submission.model.Submission;
import com.dscjss.judgeapi.submission.model.TestCase;
import com.dscjss.judgeapi.submission.repository.SubmissionRepository;
import com.dscjss.judgeapi.submission.repository.TestCaseRepository;
import com.dscjss.judgeapi.util.Constants;
import com.dscjss.judgeapi.util.FileManager;
import com.dscjss.judgeapi.util.Status;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

import static com.dscjss.judgeapi.util.Constants.*;

@Service
public class JudgeServiceImpl implements JudgeService {

    private Logger logger = LoggerFactory.getLogger(JudgeServiceImpl.class);

    private final TestCaseRepository testCaseRepository;
    private final FileManager fileManager;
    private final SubmissionRepository submissionRepository;


    @Autowired
    public JudgeServiceImpl(TestCaseRepository testCaseRepository, FileManager fileManager, SubmissionRepository submissionRepository) {
        this.testCaseRepository = testCaseRepository;
        this.fileManager = fileManager;
        this.submissionRepository = submissionRepository;
    }


    @Async
    @Override
    @Transactional
    public void judgeResult(TaskResult taskResult) {

        TestCase testCase = testCaseRepository.getOne(taskResult.getId());
        Submission submission = testCase.getSubmission();
        uploadStreams(taskResult, submission.getId(), testCase.getTestCaseId());
        if (taskResult.getStatus() == Status.EXECUTED) {
            if (testCase.isFetchData()) {
                testCase.setOutput(fetchOutputData(testCase.getTestCaseId()));
            }
            int judgeId = submission.getJudgeId();
            if (judgeId == Constants.DEFAULT_JUDGE_ID) {
                Judge judge = new LineByLineJudge();
                int result = judge.judge(testCase, taskResult.getStdOut());
                if (testCase.isFetchData()) {
                    testCase.setOutput(null);
                }
                if (result == 1) {
                    testCase.setStatus(Status.CORRECT);
                } else if (result == 0) {
                    testCase.setStatus(Status.WRONG);
                }
            }
        } else {
            testCase.setStatus(taskResult.getStatus());
        }
        testCase.setTime(taskResult.getTime());
        testCase.setMemory(taskResult.getMemory());
        testCaseRepository.saveAndFlush(testCase);
        boolean allTestCasesJudged = true;
        for(TestCase tc : submission.getTestCases()){
            if(tc.getStatus() == Status.QUEUED){
                allTestCasesJudged = false;
                break;
            }
        }
        if(allTestCasesJudged){
            MasterJudge masterJudge = new DefaultMasterJudge();
            Result result = masterJudge.judge(submission);
            submission.setResult(result);
            submission.setExecuting(false);
            submissionRepository.save(submission);
        }
    }

    private void uploadStreams(TaskResult taskResult, int submissionId, int testCaseId) {
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
        File tempFile = new File("/tmp" + "/out" + System.currentTimeMillis());
        FileUtils.writeStringToFile(tempFile, data);
        fileManager.uploadSubmissionOutputFile(fileName, tempFile);
        boolean delete = tempFile.delete();
    }

    private String fetchOutputData(int testCaseId) {
        String url = Constants.FETCH_TEST_DATA_URL + testCaseId + "/output";
        return fetchData(url);
    }

    //TODO Implement authentication token functionality
    private String fetchData(String url) {
        RestTemplate restTemplate = new RestTemplate();
        String data = restTemplate.getForObject(url, String.class);
        return data;
    }

}
