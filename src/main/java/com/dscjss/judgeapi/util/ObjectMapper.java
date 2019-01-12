package com.dscjss.judgeapi.util;

import com.dscjss.judgeapi.submission.dto.SubmissionDto;
import com.dscjss.judgeapi.submission.model.Submission;

public class ObjectMapper {

    public static SubmissionDto getSubmissionDto(Submission submission) {
        if (submission == null)
            return null;
        SubmissionDto submissionDto = new SubmissionDto();
        submissionDto.setId(submission.getId());
        submissionDto.setCompiler(submission.getCompiler());
        submissionDto.setDate(submission.getDate());
        submissionDto.setResult(submission.getResult());
        submissionDto.setExecuting(submission.isExecuting());
        return submissionDto;
    }

}
