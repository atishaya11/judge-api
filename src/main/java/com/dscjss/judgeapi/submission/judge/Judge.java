package com.dscjss.judgeapi.submission.judge;

import com.dscjss.judgeapi.submission.dto.TaskResult;
import com.dscjss.judgeapi.submission.model.TestCase;

public interface Judge {

    int judge(TestCase testCase, String output);
}
