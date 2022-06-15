package com.centradatabase.consumerapp.configs.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MQConfig {
    @Value("${queue.exchange}")
    private String QUEUE_EXCHANGE;
    @Value("${consumer.routing.key}")
    private String CONSUMER_ROUTING_KEY;
    @Value("${validator.routing.key}")
    private String VALIDATOR_ROUTING_KEY;
    @Value("${consumer.queue}")
    private String consumerQueue;
    @Value("${validator.queue}")
    private String validatorQueue;

    @Bean
    public Queue queueConsumer() {
        return new Queue(consumerQueue);
    }

    @Bean
    public Queue queueValidator() {
        return new Queue(validatorQueue);
    }

    @Bean
    @Primary
    public TopicExchange exchangeQueues() {
        return new TopicExchange(QUEUE_EXCHANGE);
    }

    @Bean
    @Primary
    public Binding bindingConsumer(Queue queueConsumer, TopicExchange exchangeQueues){
        return BindingBuilder
                .bind(queueConsumer)
                .to(exchangeQueues)
                .with(CONSUMER_ROUTING_KEY);
    }

    @Bean
    @Primary
    public Binding bindingStudentBulk(Queue queueValidator, TopicExchange exchangeQueues){
        return BindingBuilder
                .bind(queueValidator)
                .to(exchangeQueues)
                .with(VALIDATOR_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate (ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

}