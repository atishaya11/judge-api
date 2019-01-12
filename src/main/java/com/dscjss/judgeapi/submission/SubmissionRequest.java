package com.dscjss.judgeapi.submission;

import com.dscjss.judgeapi.submission.dto.TestCaseDto;

import java.util.List;

public class SubmissionRequest {

    private String source;
    private int compilerId;
    private int compilerVersionId;
    private List<TestCaseDto> testCaseList;
    private int judgeId;


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getCompilerId() {
        return compilerId;
    }

    public void setCompilerId(int compilerId) {
        this.compilerId = compilerId;
    }

    public int getCompilerVersionId() {
        return compilerVersionId;
    }

    public void setCompilerVersionId(int compilerVersionId) {
        this.compilerVersionId = compilerVersionId;
    }

    public List<TestCaseDto> getTestCaseList() {
        return testCaseList;
    }

    public void setTestCaseList(List<TestCaseDto> testCaseDtoList) {
        this.testCaseList = testCaseDtoList;
    }

    public int getJudgeId() {
        return judgeId;
    }

    public void setJudgeId(int judgeId) {
        this.judgeId = judgeId;
    }
}
