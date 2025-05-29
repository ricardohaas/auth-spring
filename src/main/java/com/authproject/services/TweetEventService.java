package com.authproject.services;

import com.authproject.config.RabbitMQConfig;
import com.authproject.events.TweetCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TweetEventService {

    private final RabbitTemplate rabbitTemplate;

    public TweetEventService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTweetCreatedEvent(TweetCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TWEET_EXCHANGE,
                RabbitMQConfig.TWEET_CREATED_ROUTING_KEY,
                event
        );
    }
}
