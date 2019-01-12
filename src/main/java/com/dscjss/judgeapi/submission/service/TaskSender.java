package com.dscjss.judgeapi.submission.service;


import com.dscjss.judgeapi.submission.dto.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TaskSender {


    private final RabbitTemplate rabbitTemplate;
    private final Logger logger = LoggerFactory.getLogger(TaskSender.class);

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;

    @Value("${rabbitmq.routing-key}")
    private String ROUTING_KEY;


    @Autowired
    public TaskSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    public void send(Task task) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, task, m -> {
            m.getMessageProperties().getHeaders().remove("__TypeId__");
            return m;
        });
        logger.info("Task {} queued.", task);
    }

}
