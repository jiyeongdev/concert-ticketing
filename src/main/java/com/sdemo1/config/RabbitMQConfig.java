package com.sdemo1.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${queue.concert.exchanges.waiting}")
    private String waitingExchangeName;

    @Value("${queue.concert.exchanges.processing}")
    private String processingExchangeName;

    @Value("${queue.concert.exchanges.dead-letter}")
    private String deadLetterExchangeName;

    @Value("${queue.concert.routing-keys.processing}")
    private String processingRoutingKey;

    @Value("${queue.concert.routing-keys.waiting}")
    private String waitingRoutingKey;

    @Value("${queue.concert.routing-keys.dead-letter}")
    private String deadLetterRoutingKey;

    @Value("${queue.concert.queues.waiting-default}")
    private String waitingDefaultQueueName;

    @Value("${queue.concert.queues.processing-default}")
    private String processingDefaultQueueName;

    @Value("${queue.concert.queues.dead-letter}")
    private String deadLetterQueueName;

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
        return new DirectExchange(waitingExchangeName);
    }

    // 처리 중 익스체인지
    @Bean
    public DirectExchange processingExchange() {
        return new DirectExchange(processingExchangeName);
    }

    // 데드레터 익스체인지
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchangeName);
    }

    // 기본 대기열 큐
    @Bean
    public Queue defaultWaitingQueue() {
        return QueueBuilder.durable(waitingDefaultQueueName)
                .withArgument("x-message-ttl", 7200000) // 2시간 TTL (대기열 대기 시간)
                .withArgument("x-expires", 14400000) // 4시간 후 자동 삭제 (콘서트 예매 기간)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    // 기본 처리 큐
    @Bean
    public Queue defaultProcessingQueue() {
        return QueueBuilder.durable(processingDefaultQueueName)
                .withArgument("x-message-ttl", 300000) // 5분 TTL (예매 입장 처리 시간)
                .withArgument("x-expires", 14400000) // 4시간 후 자동 삭제 (콘서트 예매 기간)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    // 데드레터 큐 (만료된 메시지 처리)
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueueName)
                .build();
    }

    // 기본 대기열 바인딩
    @Bean
    public Binding defaultWaitingBinding() {
        return BindingBuilder.bind(defaultWaitingQueue())
                .to(waitingExchange())
                .with(waitingRoutingKey); // 설정 파일의 라우팅 키 사용
    }

    // 기본 처리 바인딩
    @Bean
    public Binding defaultProcessingBinding() {
        return BindingBuilder.bind(defaultProcessingQueue())
                .to(processingExchange())
                .with(processingRoutingKey); // 설정 파일의 라우팅 키 사용
    }

    // 데드레터 바인딩
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(deadLetterRoutingKey);
    }

} 