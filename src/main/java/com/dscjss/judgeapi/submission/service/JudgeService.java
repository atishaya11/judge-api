package com.dscjss.judgeapi.submission.service;

import com.dscjss.judgeapi.submission.dto.TaskResult;

public interface JudgeService {
    void processTaskResult(TaskResult taskResult);
}
