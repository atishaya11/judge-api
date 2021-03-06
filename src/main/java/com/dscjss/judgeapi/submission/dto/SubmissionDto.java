package com.dscjss.judgeapi.submission.dto;

import com.dscjss.judgeapi.submission.model.Result;
import com.dscjss.judgeapi.submission.model.TestCase;
import com.dscjss.judgeapi.submission.model.Compiler;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

public class SubmissionDto {

    private int id;
    private boolean executing;
    private Compiler compiler;
    private Result result;
    private String source;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss a z")
    private Date date;
    private List<TestCaseResult> testCaseResultList;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isExecuting() {
        return executing;
    }

    public void setExecuting(boolean executing) {
        this.executing = executing;
    }

    public Compiler getCompiler() {
        return compiler;
    }

    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public List<TestCaseResult> getTestCaseResultList() {
        return testCaseResultList;
    }

    public void setTestCaseResultList(List<TestCaseResult> testCaseResultList) {
        this.testCaseResultList = testCaseResultList;
    }
}
