package com.dscjss.judgeapi.util;

import com.dscjss.judgeapi.submission.dto.SubmissionDto;
import com.dscjss.judgeapi.submission.dto.TestCaseResult;
import com.dscjss.judgeapi.submission.model.Submission;
import com.dscjss.judgeapi.submission.model.TestCase;

import java.util.ArrayList;
import java.util.List;

public class ObjectMapper {

    public static SubmissionDto getSubmissionDto(Submission submission) {
        if (submission == null)
            return null;
        SubmissionDto submissionDto = new SubmissionDto();
        submissionDto.setId(submission.getId());
        submissionDto.setCompiler(submission.getCompiler());
        submissionDto.setDate(submission.getDate());
        submissionDto.setExecuting(submission.isExecuting());
        if(!submission.isExecuting()){
            submissionDto.setResult(submission.getResult());
            submissionDto.setTestCaseResultList(getTestCaseResultList(submission.getTestCases()));
        }
        return submissionDto;
    }

    public static List<TestCaseResult> getTestCaseResultList(List<TestCase> testCases) {
        if(testCases == null || testCases.size() < 1){
            return null;
        }
        List<TestCaseResult> testCaseResultList = new ArrayList<>();
        testCases.forEach(testCase -> testCaseResultList.add(getTestCaseResult(testCase)));
        return testCaseResultList;
    }

    public static TestCaseResult getTestCaseResult(TestCase testCase){
        if(testCase == null)
            return null;

        TestCaseResult testCaseResult = new TestCaseResult();
        testCaseResult.setMemory(testCase.getMemory());
        testCaseResult.setStatus(testCase.getStatus());
        testCaseResult.setTime(testCase.getTime());
        testCaseResult.setTestCaseId(testCase.getTestCaseId());
        testCaseResult.setScore(testCase.getScore());

        return testCaseResult;
    }

}
