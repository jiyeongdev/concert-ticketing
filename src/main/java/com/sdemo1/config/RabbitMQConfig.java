package com.sdemo1.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${queue.concert.waiting-prefix}")
    private String waitingPrefix;

    @Value("${queue.concert.processing-prefix}")
    private String processingPrefix;

    // 메시지 컨버터 설정
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate 설정
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // 대기열 익스체인지
    @Bean
    public DirectExchange waitingExchange() {
        return new DirectExchange("concert.waiting.exchange");
    }

    // 처리 중 익스체인지
    @Bean
    public DirectExchange processingExchange() {
        return new DirectExchange("concert.processing.exchange");
    }

    // 데드레터 익스체인지
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("concert.dead.letter.exchange");
    }

    // 기본 대기열 큐
    @Bean
    public Queue defaultWaitingQueue() {
        return QueueBuilder.durable("concert.waiting.default")
                .withArgument("x-message-ttl", 7200000) // 2시간 TTL
                .withArgument("x-expires", 86400000) // 24시간 후 자동 삭제
                .withArgument("x-dead-letter-exchange", "concert.dead.letter.exchange")
                .withArgument("x-dead-letter-routing-key", "dead.letter")
                .build();
    }

    // 기본 처리 큐
    @Bean
    public Queue defaultProcessingQueue() {
        return QueueBuilder.durable("concert.processing.default")
                .withArgument("x-message-ttl", 600000) // 10분 TTL
                .withArgument("x-expires", 3600000) // 1시간 후 자동 삭제
                .withArgument("x-dead-letter-exchange", "concert.dead.letter.exchange")
                .withArgument("x-dead-letter-routing-key", "dead.letter")
                .build();
    }

    // 데드레터 큐 (만료된 메시지 처리)
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("concert.dead.letter")
                .build();
    }

    // 기본 대기열 바인딩
    @Bean
    public Binding defaultWaitingBinding() {
        return BindingBuilder.bind(defaultWaitingQueue())
                .to(waitingExchange())
                .with("waiting"); //라우팅 키
    }

    // 기본 처리 바인딩
    @Bean
    public Binding defaultProcessingBinding() {
        return BindingBuilder.bind(defaultProcessingQueue())
                .to(processingExchange())
                .with("processing"); // 라우팅 키
    }

    // 데드레터 바인딩
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dead.letter");
    }

    /**
     * 동적으로 대기열 큐 생성
     */
    public Queue createWaitingQueue(String concertId) {
        String queueName = waitingPrefix + concertId; // 큐 이름
        return QueueBuilder.durable(queueName)
                .withArgument("x-message-ttl", 7200000) // 2시간 TTL
                .withArgument("x-expires", 86400000) // 24시간 후 자동 삭제
                .withArgument("x-dead-letter-exchange", "concert.dead.letter.exchange")
                .withArgument("x-dead-letter-routing-key", "dead.letter")
                .build();
    }

    /**
     * 동적으로 처리 큐 생성
     */
    public Queue createProcessingQueue(String concertId) {
        String queueName = processingPrefix + concertId;
        return QueueBuilder.durable(queueName)
                .withArgument("x-message-ttl", 600000) // 10분 TTL
                .withArgument("x-expires", 3600000) // 1시간 후 자동 삭제
                .withArgument("x-dead-letter-exchange", "concert.dead.letter.exchange")
                .withArgument("x-dead-letter-routing-key", "dead.letter")
                .build();
    }

    /**
     * 대기열 큐 바인딩 생성
     */
    public Binding createWaitingBinding(String concertId) {
        String queueName = waitingPrefix + concertId; 
        Queue queue = createWaitingQueue(concertId);
        return BindingBuilder.bind(queue)
                .to(waitingExchange())
                .with("waiting." + concertId);  // 콘서트별 라우팅 키
    }

    /**
     * 처리 큐 바인딩 생성
     */
    public Binding createProcessingBinding(String concertId) {
        String queueName = processingPrefix + concertId;
        Queue queue = createProcessingQueue(concertId);
        return BindingBuilder.bind(queue)
                .to(processingExchange())
                .with("processing." + concertId);  // 콘서트별 라우팅 키
    }
} 