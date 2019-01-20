package com.dscjss.judgeapi.submission.dto;

public class TestCaseDto {

    private int id;
    private String input;
    private String output;
    private boolean fetchData; // true if the data is to be fetched through id
    private int timeLimit;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isFetchData() {
        return fetchData;
    }

    public void setFetchData(boolean fetchData) {
        this.fetchData = fetchData;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
}
