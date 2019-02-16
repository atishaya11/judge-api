package com.dscjss.judgeapi.submission.judge;

import com.dscjss.judgeapi.submission.model.Result;
import com.dscjss.judgeapi.submission.model.Submission;
import com.dscjss.judgeapi.submission.model.TestCase;
import com.dscjss.judgeapi.util.Status;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CILMasterJudge implements MasterJudge {

    @Override
    public Result judge(Submission submission) {
        DefaultMasterJudge defaultMasterJudge = new DefaultMasterJudge();
        return defaultMasterJudge.judge(submission);
    }
}
