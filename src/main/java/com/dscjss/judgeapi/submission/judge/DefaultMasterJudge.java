package com.dscjss.judgeapi.submission.judge;

import com.dscjss.judgeapi.submission.model.Result;
import com.dscjss.judgeapi.submission.model.Submission;
import com.dscjss.judgeapi.submission.model.TestCase;
import com.dscjss.judgeapi.util.Status;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultMasterJudge implements MasterJudge {


    @Override
    public Result judge(Submission submission) {
        List<TestCase> testCaseList = submission.getTestCases();
        Result result = new Result();
        Set<Status> statusSet = testCaseList.stream().map(TestCase::getStatus).collect(Collectors.toSet());
        if(statusSet.contains(Status.INTERNAL_ERROR)){
            result.setStatus(Status.INTERNAL_ERROR);
        }else if(statusSet.contains(Status.COMPILATION_ERROR)){
            result.setStatus(Status.COMPILATION_ERROR);
        }else if(statusSet.contains(Status.RUNTIME_ERROR)){
            result.setStatus(Status.RUNTIME_ERROR);
        }else if(statusSet.contains(Status.TIME_LIMIT_EXCEEDED)){
            result.setStatus(Status.TIME_LIMIT_EXCEEDED);
        }else if(statusSet.contains(Status.WRONG)){
            result.setStatus(Status.WRONG);
        }else {
            result.setStatus(Status.CORRECT);
        }
        result.setTime(Collections.max(testCaseList, Comparator.comparingInt(TestCase::getTime)).getTime());

        result.setMemory(Collections.max(testCaseList, Comparator.comparingDouble(TestCase::getMemory)).getMemory());
        return result;
    }
}
