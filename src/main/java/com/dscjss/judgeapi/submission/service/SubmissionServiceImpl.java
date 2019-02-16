package com.dscjss.judgeapi.submission.service;


import com.amazonaws.AmazonClientException;
import com.dscjss.judgeapi.submission.dto.SubmissionRequest;
import com.dscjss.judgeapi.submission.dto.SubmissionDto;
import com.dscjss.judgeapi.submission.dto.Task;
import com.dscjss.judgeapi.submission.dto.TestCaseDto;
import com.dscjss.judgeapi.submission.exception.SourceDownloadException;
import com.dscjss.judgeapi.submission.exception.TaskFailedException;
import com.dscjss.judgeapi.submission.model.Compiler;
import com.dscjss.judgeapi.submission.model.Result;
import com.dscjss.judgeapi.submission.model.Submission;
import com.dscjss.judgeapi.submission.model.TestCase;
import com.dscjss.judgeapi.submission.repository.CompilerRepository;
import com.dscjss.judgeapi.submission.repository.SubmissionRepository;
import com.dscjss.judgeapi.submission.repository.TestCaseRepository;
import com.dscjss.judgeapi.util.Constants;
import com.dscjss.judgeapi.util.FileManager;
import com.dscjss.judgeapi.util.ObjectMapper;
import com.dscjss.judgeapi.util.Status;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class SubmissionServiceImpl implements SubmissionService {


    private Logger logger = LoggerFactory.getLogger(SubmissionServiceImpl.class);

    private CompilerRepository compilerRepository;
    private SubmissionRepository submissionRepository;
    private TestCaseRepository testCaseRepository;
    private FileManager fileManager;
    private TaskSender taskSender;

    @Autowired
    public SubmissionServiceImpl(CompilerRepository compilerRepository, SubmissionRepository submissionRepository,
                                 TestCaseRepository testCaseRepository, FileManager fileManager, TaskSender taskSender) {
        this.compilerRepository = compilerRepository;
        this.submissionRepository = submissionRepository;
        this.testCaseRepository = testCaseRepository;
        this.fileManager = fileManager;
        this.taskSender = taskSender;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Submission createSubmission(final SubmissionRequest submissionRequest) {
        Submission submission = new Submission();
        submission.setCompiler(getCompiler(submissionRequest.getCompilerId(), submissionRequest.getCompilerVersionId()));
        submission.setSource(submissionRequest.getSource());
        submission.setExecuting(true);
        submission.setDate(new Date());
        submission.setMaxScore(submissionRequest.getMaxScore());
        Result result = new Result();
        result.setStatus(Status.RUNNING);
        submission.setResult(result);
        submission.setJudgeId(submissionRequest.getJudgeId() == 0 ? Constants.JUDGE_ID_DEFAULT : submissionRequest.getJudgeId());
        submission.setMasterJudgeId(submissionRequest.getMasterJudgeId()  == 0 ? Constants.MASTER_JUDGE_ID_DEFAULT : submissionRequest.getMasterJudgeId());
        List<TestCase> testCases = new ArrayList<>();
        if (submissionRequest.getTestCaseList() != null && submissionRequest.getTestCaseList().size() > 0) {
            submissionRequest.getTestCaseList().forEach(testCaseDto -> {
                TestCase testCase = new TestCase();
                testCase.setTestCaseId(testCaseDto.getId());
                testCase.setFetchData(testCaseDto.isFetchData());
                testCase.setInput(testCaseDto.getInput());
                testCase.setOutput(testCaseDto.getOutput());
                testCase.setSubmission(submission);
                testCase.setTimeLimit(testCaseDto.getTimeLimit());
                testCase.setStatus(Status.QUEUED);
                testCaseRepository.save(testCase);
                testCases.add(testCase);
            });
        } else {
            TestCase testCase = new TestCase();
            testCase.setInput("");
            testCase.setOutput("");
            testCase.setFetchData(false);
            testCase.setSubmission(submission);
            testCaseRepository.save(testCase);
        }
        submission.setTestCases(testCases);
        logger.info("Test Cases for submission created");
        submissionRepository.save(submission);
        return submission;
    }

    private Compiler getCompiler(int compilerId, int compilerVersionId) {
        Compiler compiler = compilerRepository.getOne(compilerId);
        return compiler;
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSubmission(Submission submission, List<TestCaseDto> testCaseDtoList) {

        String submissionSourceFileName = submission.getId() + "/" + "source.txt";
        try {
            uploadSourceSubmissionFile(submissionSourceFileName, submission.getSource());
        } catch (IOException | InterruptedException |AmazonClientException e) {
            e.printStackTrace();
            logger.error("Source file upload failed. {}", submission.getId());
            submission.setExecuting(false);
            submission.getResult().setStatus(Status.INTERNAL_ERROR);
            submissionRepository.save(submission);
            return;
        }

        try {
            queueSubmission(submission);
            logger.info("Submission queued.");
        } catch (TaskFailedException e) {
            logger.error("Submission failed due to an internal error.");
            submission.setExecuting(false);
            submission.getResult().setStatus(Status.INTERNAL_ERROR);
        }
        submissionRepository.save(submission);
    }

    private void queueSubmission(Submission submission) throws TaskFailedException {
        for (TestCase testCase : submission.getTestCases()) {
            Task task = createTask(submission, testCase);
            taskSender.send(task);
        }
    }


    private Task createTask(Submission submission, TestCase testCase) {
        Task task = new Task();
        task.setId(testCase.getId());
        task.setTimeLimit(testCase.getTimeLimit());
        if (testCase.isFetchData()) {
            task.setInput(fetchInputData(testCase.getTestCaseId()));
        } else {
            task.setInput(testCase.getInput());
        }
        task.setSource(submission.getSource());
        task.setLang(submission.getCompiler().getSlug());
        return task;
    }
    //TODO Handle exceptions thrown when the test cases cannot be fetched
    private String fetchInputData(int testCaseId) {
        String url = Constants.FETCH_TEST_DATA_URL + testCaseId + "/input";
        return fetchData(url);
    }

    //TODO Implement authentication token functionality
    private String fetchData(String url) {
        RestTemplate restTemplate = new RestTemplate();
        String data = restTemplate.getForObject(url, String.class);
        return data;
    }

    private void uploadSourceSubmissionFile(String submissionSourceFileName, String source) throws IOException, InterruptedException, AmazonClientException {
        File tempFile = new File("/tmp" + "/src/" + System.currentTimeMillis());
        FileUtils.writeStringToFile(tempFile, source);
        fileManager.uploadSubmissionSourceFile(submissionSourceFileName, tempFile);
        boolean delete = tempFile.delete();
    }


    @Override
    public SubmissionDto getSubmission(int id) {
        return ObjectMapper.getSubmissionDto(submissionRepository.getOne(id));
    }

    @Override
    @Cacheable(value = "submission_source", key = "#submissionId", unless = "#result != null && #result.length() > 0")
    public String getSource(int submissionId) throws SourceDownloadException {
        try {
            String fileName = submissionId + "/source.txt";
            File file = fileManager.downloadSourceFile(fileName);
            return FileUtils.readFileToString(file, Charsets.UTF_8.name());
        }catch (InterruptedException | IOException e){
            throw new SourceDownloadException("Source code download failed.");
        }
    }
}
