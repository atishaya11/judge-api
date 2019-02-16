package com.dscjss.judgeapi.submission.service;

import com.dscjss.judgeapi.submission.exception.SourceDownloadException;
import com.dscjss.judgeapi.submission.judge.CILMasterJudge;
import com.dscjss.judgeapi.submission.judge.DefaultMasterJudge;
import com.dscjss.judgeapi.submission.judge.MasterJudge;
import com.dscjss.judgeapi.submission.model.Result;
import com.dscjss.judgeapi.submission.model.Submission;
import com.dscjss.judgeapi.submission.repository.SubmissionRepository;
import com.dscjss.judgeapi.util.Constants;
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
    private final SubmissionService submissionService;
    @Autowired
    public SubmissionJudgeService(SubmissionRepository submissionRepository, SubmissionService submissionService) {
        this.submissionRepository = submissionRepository;
        this.submissionService = submissionService;
    }


    public void judge(int id) {
        Submission submission = submissionRepository.getOne(id);
        Result result = getSubmissionResult(submission);
        Result submissionResult = submission.getResult();
        submissionResult.setStatus(result.getStatus());
        submissionResult.setMemory(result.getMemory());
        submissionResult.setTime(result.getTime());
        submissionResult.setScore(result.getScore());
        submissionResult.setSourceCharCount(result.getSourceCharCount());
        submission.setExecuting(false);
        submissionRepository.save(submission);
    }

    private Result getSubmissionResult(Submission submission){
        MasterJudge masterJudge;
        Result result;
        if(submission.getMasterJudgeId() == Constants.MASTER_JUDGE_ID_DEFAULT){
            masterJudge = new DefaultMasterJudge();
            result = masterJudge.judge(submission);
        }else if(submission.getMasterJudgeId() == Constants.MASTER_JUDGE_ID_CODE_IN_LESS){
            masterJudge = new CILMasterJudge();
            result = masterJudge.judge(submission);
            try {
                result.setSourceCharCount(getCharCount(submissionService.getSource(submission.getId())));
            } catch (SourceDownloadException e) {
                e.printStackTrace();
                logger.error("Unable to fetch source code, aborting code in less judging for submission id {}", submission.getId());
                result.setStatus(Status.INTERNAL_ERROR);
            }
        }else{
            result = new Result();
            result.setStatus(Status.INTERNAL_ERROR);
        }
        return result;
    }

    @Transactional
    public void handleError(int id) {
        Submission submission = submissionRepository.getOne(id);
        Result submissionResult = submission.getResult();
        submissionResult.setStatus(Status.INTERNAL_ERROR);
        submission.setExecuting(false);
        submissionRepository.save(submission);
    }

    private int getCharCount(String source){
        int whiteSpaceCharacterCount = 0;
        //String str = source.replaceAll("//s+", "");
        for(int i = 0; i < source.length(); i++){
            char ch = source.charAt(i);
            if(Character.isWhitespace(ch)){
                whiteSpaceCharacterCount += 1;
            }
        }
        return source.length() - whiteSpaceCharacterCount;
    }
}
