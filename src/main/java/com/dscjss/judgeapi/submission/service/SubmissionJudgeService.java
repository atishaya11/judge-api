package com.dscjss.judgeapi.submission.service;

import com.dscjss.judgeapi.submission.judge.DefaultMasterJudge;
import com.dscjss.judgeapi.submission.judge.MasterJudge;
import com.dscjss.judgeapi.submission.model.Result;
import com.dscjss.judgeapi.submission.model.Submission;
import com.dscjss.judgeapi.submission.repository.SubmissionRepository;
import com.dscjss.judgeapi.util.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionJudgeService {

    private final Logger logger = LoggerFactory.getLogger(SubmissionJudgeService.class);
    private final SubmissionRepository submissionRepository;

    @Autowired
    public SubmissionJudgeService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }


    public void judge(int id) {
        Submission submission = submissionRepository.getOne(id);
        Result result = getSubmissionResult(submission);
        Result submissionResult = submission.getResult();
        submissionResult.setStatus(result.getStatus());
        submissionResult.setMemory(result.getMemory());
        submissionResult.setTime(result.getTime());
        submissionResult.setScore(result.getScore());
        submission.setExecuting(false);
        submissionRepository.save(submission);
    }

    private Result getSubmissionResult(Submission submission){
        MasterJudge masterJudge = new DefaultMasterJudge();
        return masterJudge.judge(submission);
    }

    @Transactional
    public void handleError(int id) {
        Submission submission = submissionRepository.getOne(id);
        Result submissionResult = submission.getResult();
        submissionResult.setStatus(Status.INTERNAL_ERROR);
        submission.setExecuting(false);
        submissionRepository.save(submission);
    }
}
