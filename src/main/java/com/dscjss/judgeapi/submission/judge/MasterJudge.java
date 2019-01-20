package com.dscjss.judgeapi.submission.judge;

import com.dscjss.judgeapi.submission.model.Result;
import com.dscjss.judgeapi.submission.model.Submission;

public interface MasterJudge {

    Result judge(Submission submission);
}
