package com.wallet.service.wallet.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {
    
    @Value("${kafka.topics.transaction-events}")
    private String transactionTopic;
    
    @Value("${kafka.topics.wallet-events}")
    private String walletTopic;
    
    @Value("${kafka.topics.payment-events}")
    private String paymentTopic;
    
    @Bean
    public NewTopic transactionEventsTopic() {
        return TopicBuilder.name(transactionTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic walletEventsTopic() {
        return TopicBuilder.name(walletTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(paymentTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
