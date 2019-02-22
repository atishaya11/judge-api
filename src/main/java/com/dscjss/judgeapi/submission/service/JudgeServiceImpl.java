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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static com.dscjss.judgeapi.util.Constants.*;

@Service
public class JudgeServiceImpl implements JudgeService {

    private Logger logger = LoggerFactory.getLogger(JudgeServiceImpl.class);

    private final TestCaseRepository testCaseRepository;
    private final FileManager fileManager;

    private Map<Integer, CountDownLatch> countDownLatchMap = new ConcurrentHashMap<>();

    private final SubmissionJudgeService submissionJudgeService;

    private final TestCaseJudgeService testCaseJudgeService;


    @Autowired
    public JudgeServiceImpl(TestCaseRepository testCaseRepository, FileManager fileManager, SubmissionRepository submissionRepository, SubmissionJudgeService submissionJudgeService, TestCaseJudgeService testCaseJudgeService) {
        this.testCaseRepository = testCaseRepository;
        this.fileManager = fileManager;
        this.submissionJudgeService = submissionJudgeService;
        this.testCaseJudgeService = testCaseJudgeService;
    }

    @Async
    @Override
    public void processTaskResult(TaskResult taskResult) {
        TestCase testCase = testCaseRepository.getOne(taskResult.getId());
        Submission submission = testCase.getSubmission();
        fileManager.uploadStreams(taskResult, submission.getId(), testCase.getTestCaseId());
        CountDownLatch countDownLatch = countDownLatchMap.putIfAbsent(submission.getId(), new CountDownLatch(submission.getTestCases().size()));
        if(countDownLatch == null){
            countDownLatch = countDownLatchMap.get(submission.getId());
            run(taskResult, countDownLatch);
            try {
                countDownLatch.await();
                countDownLatchMap.remove(submission.getId());
                submissionJudgeService.judge(submission.getId());
            } catch (InterruptedException e) {
                logger.error("Unable to judge submission, internal error.");
                submissionJudgeService.handleError(submission.getId());
                e.printStackTrace();
            }
        }else{
            run(taskResult, countDownLatch);
        }

    }

    private void run(TaskResult taskResult, CountDownLatch countDownLatch) {
        Runnable runnable = () -> {
            testCaseJudgeService.judge(taskResult);
            countDownLatch.countDown();
        };
        new Thread(runnable).start();
    }


}
