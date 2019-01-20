package com.dscjss.judgeapi.submission.controller;


import com.dscjss.judgeapi.submission.dto.SubmissionRequest;
import com.dscjss.judgeapi.submission.dto.SubmissionDto;
import com.dscjss.judgeapi.submission.exception.SourceDownloadException;
import com.dscjss.judgeapi.submission.model.Submission;
import com.dscjss.judgeapi.submission.service.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    private Logger logger = LoggerFactory.getLogger(SubmissionController.class);
    private SubmissionService submissionService;

    @Autowired
    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/submission")
    public Integer createSubmission(@RequestBody SubmissionRequest submissionRequest) {
        Submission submission = submissionService.createSubmission(submissionRequest);
        submissionService.processSubmission(submission, submissionRequest.getTestCaseList());
        return submission.getId();
    }

    @GetMapping("/submission/{id}")
    public SubmissionDto getSubmission(@PathVariable int id, @RequestParam(required = false, defaultValue = "false") boolean withSource) {
        SubmissionDto submissionDto = submissionService.getSubmission(id);
        if(!submissionDto.isExecuting() && withSource){
            try {
                submissionDto.setSource(submissionService.getSource(submissionDto.getId()));
            } catch (SourceDownloadException e) {
                e.printStackTrace();
                logger.error("Unable to download source code for submission {}", submissionDto.getId());
            }
        }
        return submissionDto;
    }
}
