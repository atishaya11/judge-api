package com.dscjss.judgeapi.submission.service;


import com.dscjss.judgeapi.submission.SubmissionRequest;
import com.dscjss.judgeapi.submission.dto.SubmissionDto;
import com.dscjss.judgeapi.submission.dto.Task;
import com.dscjss.judgeapi.submission.dto.TestCaseDto;
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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Submission createSubmission(SubmissionRequest submissionRequest) {
        Submission submission = new Submission();
        submission.setCompiler(getCompiler(submissionRequest.getCompilerId(), submissionRequest.getCompilerVersionId()));
        submission.setSource(submissionRequest.getSource());
        submission.setExecuting(true);
        submission.setDate(new Date());
        submission.setJudgeId(submissionRequest.getJudgeId() == 0 ? Constants.DEFAULT_JUDGE_ID : submissionRequest.getJudgeId());
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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            logger.error("Source file upload failed. {}", submission.getId());
            Result result = new Result();
            result.setStatus(Status.INTERNAL_ERROR);
            submission.setResult(result);
            submissionRepository.save(submission);
            return;
        }
        List<TestCase> testCases = new ArrayList<>();
        if (testCaseDtoList != null && testCaseDtoList.size() > 0) {
            testCaseDtoList.forEach(testCaseDto -> {
                TestCase testCase = new TestCase();
                testCase.setFetchData(testCaseDto.isFetchData());
                testCase.setInput(testCaseDto.getInput());
                testCase.setOutput(testCaseDto.getOutput());
                testCase.setSubmission(submission);
                testCaseRepository.save(testCase);
                testCases.add(testCase);
            });
        } else {
            TestCase testCase = new TestCase();
            testCase.setFetchData(false);
            testCase.setSubmission(submission);
            testCaseRepository.save(testCase);
        }
        submission.setTestCases(testCases);
        submissionRepository.save(submission);

        logger.info("Test Cases for submission created. Ready to be queued. ");
        queueSubmission(submission);

    }

    private void queueSubmission(Submission submission) {
        submission.getTestCases().forEach(testCase -> {
            Task task = createTask(submission, testCase);
            taskSender.send(task);
        });
    }

    private Task createTask(Submission submission, TestCase testCase) {
        Task task = new Task();
        task.setId(testCase.getId());
        if (testCase.isFetchData()) {
            task.setInput(fetchInputData(testCase.getTestCaseId()));
        } else {
            task.setInput(testCase.getInput());
        }
        task.setSource(submission.getSource());
        task.setLang(submission.getCompiler().getSlug());
        return task;
    }

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

    private void uploadSourceSubmissionFile(String submissionSourceFileName, String source) throws IOException, InterruptedException {
        File tempFile = new File("/tmp" + "/src/" + System.currentTimeMillis());
        FileUtils.writeStringToFile(tempFile, source);
        fileManager.uploadSubmissionSourceFile(submissionSourceFileName, tempFile);
        boolean delete = tempFile.delete();
    }


    @Override
    public SubmissionDto getSubmission(int id) {
        return ObjectMapper.getSubmissionDto(submissionRepository.getOne(id));
    }
}
