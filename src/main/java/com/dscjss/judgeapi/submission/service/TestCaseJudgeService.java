package com.dscjss.judgeapi.submission.service;


import com.dscjss.judgeapi.submission.dto.TaskResult;
import com.dscjss.judgeapi.submission.judge.Judge;
import com.dscjss.judgeapi.submission.judge.LineByLineJudge;
import com.dscjss.judgeapi.submission.model.Submission;
import com.dscjss.judgeapi.submission.model.TestCase;
import com.dscjss.judgeapi.submission.repository.TestCaseRepository;
import com.dscjss.judgeapi.util.Constants;
import com.dscjss.judgeapi.util.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;

@Service
public class TestCaseJudgeService {

    private final Logger logger = LoggerFactory.getLogger(TestCaseJudgeService.class);

    private final TestCaseRepository testCaseRepository;

    @Autowired
    public TestCaseJudgeService(TestCaseRepository testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
    }


    @Transactional
    public void judge(TaskResult taskResult, int testCaseId, CountDownLatch countDownLatch) {
        TestCase testCase = testCaseRepository.getOne(taskResult.getId());
        Submission submission = testCase.getSubmission();
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
        testCaseRepository.save(testCase);
        countDownLatch.countDown();
        logger.info("Task id : {} judged.", taskResult.getId());
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
