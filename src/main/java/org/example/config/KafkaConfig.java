package org.example.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.vacancy-raw}")
    private String vacancyRawTopic;

    @Value("${kafka.topics.vacancy-processed}")
    private String vacancyProcessedTopic;

    @Value("${kafka.topics.vacancy-failed}")
    private String vacancyFailedTopic;

    @Bean
    public NewTopic vacancyRawTopic() {
        return TopicBuilder.name(vacancyRawTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic vacancyProcessedTopic() {
        return TopicBuilder.name(vacancyProcessedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic vacancyFailedTopic() {
        return TopicBuilder.name(vacancyFailedTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}