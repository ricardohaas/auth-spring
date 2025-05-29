package com.authproject.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;


@Configuration
public class RabbitMQConfig {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

    public static final String TWEET_QUEUE = "tweet-queue";
    public static final String TWEET_EXCHANGE = "tweet-exchange";
    public static final String TWEET_CREATED_ROUTING_KEY = "tweet.created";

    @Autowired
    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void initialize() {
        logger.info("Inicializando configuração do RabbitMQ");
        try {
            RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);

            // Declara fila
            Queue queue = new Queue(TWEET_QUEUE, true);
            rabbitAdmin.declareQueue(queue);
            logger.info("Fila '{}' declarada com sucesso", TWEET_QUEUE);

            // Declara exchange
            TopicExchange exchange = new TopicExchange(TWEET_EXCHANGE);
            rabbitAdmin.declareExchange(exchange);
            logger.info("Exchange '{}' declarada com sucesso", TWEET_EXCHANGE);

            // Declara binding
            Binding binding = BindingBuilder.bind(queue).to(exchange).with(TWEET_CREATED_ROUTING_KEY);
            rabbitAdmin.declareBinding(binding);
            logger.info("Binding entre '{}' e '{}' com routing key '{}' criado com sucesso",
                    TWEET_QUEUE, TWEET_EXCHANGE, TWEET_CREATED_ROUTING_KEY);
        } catch (Exception e) {
            logger.error("Erro ao configurar o RabbitMQ", e);
            throw e;
        }
    }

    @Bean
    public Queue tweetQueue() {
        logger.info("Criando bean da fila '{}'", TWEET_QUEUE);
        return new Queue(TWEET_QUEUE, true);
    }

    @Bean
    public TopicExchange tweetExchange() {
        logger.info("Criando bean da exchange '{}'", TWEET_EXCHANGE);
        return new TopicExchange(TWEET_EXCHANGE);
    }

    @Bean
    public Binding tweetBinding(Queue tweetQueue, TopicExchange tweetExchange) {
        logger.info("Criando binding entre '{}' e '{}' com routing key '{}'",
                TWEET_QUEUE, TWEET_EXCHANGE, TWEET_CREATED_ROUTING_KEY);
        return BindingBuilder.bind(tweetQueue).to(tweetExchange).with(TWEET_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        logger.info("Configurando Jackson2JsonMessageConverter");
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        logger.info("Configurando RabbitTemplate");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}