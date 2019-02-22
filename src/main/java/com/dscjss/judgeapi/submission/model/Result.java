package com.dscjss.judgeapi.submission.model;


import com.dscjss.judgeapi.util.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "submission_result")
public class Result {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "DEFAULT ")
    @Enumerated(value = EnumType.ORDINAL)
    private Status status;
    private double score;
    private int time;
    private double memory;

    @Column(name = "source_char_count")
    private int sourceCharCount;

    @JsonIgnore
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getMemory() {
        return memory;
    }

    public void setMemory(double memory) {
        this.memory = memory;
    }

    public int getSourceCharCount() {
        return sourceCharCount;
    }

    public void setSourceCharCount(int sourceCharCount) {
        this.sourceCharCount = sourceCharCount;
    }
}
