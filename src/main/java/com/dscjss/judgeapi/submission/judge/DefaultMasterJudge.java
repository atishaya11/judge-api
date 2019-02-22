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
        int maxScore = submission.getMaxScore();
        for(TestCase testCase : testCaseList){
            if(testCase.getStatus() == null){
                result.setStatus(Status.INTERNAL_ERROR);
                break;
            }else{
                switch (testCase.getStatus()){
                    case INTERNAL_ERROR:
                        result.setStatus(Status.INTERNAL_ERROR);
                        break;
                    case COMPILATION_ERROR:
                        result.setStatus(Status.COMPILATION_ERROR);
                        break;
                    case RUNTIME_ERROR:
                        result.setStatus(Status.RUNTIME_ERROR);
                        break;
                    case TIME_LIMIT_EXCEEDED:
                        result.setStatus(Status.TIME_LIMIT_EXCEEDED);
                        break;
                    case WRONG:
                        result.setStatus(Status.WRONG);
                        break;
                    case CORRECT:
                        result.setStatus(Status.CORRECT);
                        break;
                    default:
                        result.setStatus(Status.INTERNAL_ERROR);
                        break;

                }
                if(result.getStatus() != Status.CORRECT){
                    break;
                }
            }
            if(result.getStatus() == Status.CORRECT){
                result.setScore(maxScore);
            }


        }
        int maxTime = Integer.MIN_VALUE;
        double maxMemory = Double.MIN_VALUE;
        for(TestCase testCase : testCaseList){
            maxTime = Math.max(testCase.getTime(), maxTime);
            maxMemory = Math.max(testCase.getMemory(), maxMemory);
        }
        result.setTime(maxTime);
        result.setMemory(maxMemory);
        return result;
    }
}
