package com.dscjss.judgeapi.submission.service;

import com.dscjss.judgeapi.submission.dto.SubmissionRequest;
import com.dscjss.judgeapi.submission.dto.SubmissionDto;
import com.dscjss.judgeapi.submission.dto.TestCaseDto;
import com.dscjss.judgeapi.submission.exception.SourceDownloadException;
import com.dscjss.judgeapi.submission.model.Submission;

import java.util.List;

public interface SubmissionService {

    Submission createSubmission(SubmissionRequest submissionRequest);

    void processSubmission(Submission submission, List<TestCaseDto> testCaseDtoList);

    SubmissionDto getSubmission(int id);

    String getSource(int submissionId) throws SourceDownloadException;
}
