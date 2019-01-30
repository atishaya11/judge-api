package com.dscjss.judgeapi.submission.service;


import com.dscjss.judgeapi.submission.dto.TaskResult;
import com.dscjss.judgeapi.submission.model.Result;
import com.dscjss.judgeapi.submission.model.Submission;
import com.dscjss.judgeapi.submission.model.TestCase;
import com.dscjss.judgeapi.util.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@RabbitListener(queues = {"${rabbitmq.receive-result}"})
public class TaskResultReceiver {

    private Logger logger = LoggerFactory.getLogger(TaskResultReceiver.class);

    private JudgeService judgeService;

    @Autowired
    public TaskResultReceiver(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @RabbitHandler
    public void receiveMessage(TaskResult taskResult) {
        logger.info("Received task result after execution. Task id : {}.", taskResult.getId());
        judgeService.processTaskResult(taskResult);

    }

}
