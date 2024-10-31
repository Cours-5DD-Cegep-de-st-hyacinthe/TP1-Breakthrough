package com.kleblanc.breakthrough_backend.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Consumer topics
    @Value(value = "${spring.kafka.topic.iaStatusTopic}")
    private String iaStatusTopic;
    @Value(value = "${spring.kafka.topic.moveResponseWhiteTopic}")
    private String moveResponseWhiteTopic;
    @Value(value = "${spring.kafka.topic.moveResponseBlackTopic}")
    private String moveResponseBlackTopic;

    // Producer topics
    @Value(value = "${spring.kafka.topic.colorAssignationTopic}")
    private String colorAssignationTopic;
    @Value(value = "${spring.kafka.topic.moveRequestWhiteTopic}")
    private String moveRequestWhiteTopic;
    @Value(value = "${spring.kafka.topic.moveRequestBlackTopic}")
    private String moveRequestBlackTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    // Consumer topics ----------------------------------------------------------------------------------------------
    @Bean
    public NewTopic iaStatusTopic() {
        return new NewTopic(iaStatusTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic moveResponseWhiteTopic() {
        return new NewTopic(moveResponseWhiteTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic moveResponseBlackTopic() {
        return new NewTopic(moveResponseBlackTopic, 1, (short) 1);
    }

    // Producer topics ----------------------------------------------------------------------------------------------
    @Bean
    public NewTopic colorAssignationTopic() {
        return new NewTopic(colorAssignationTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic moveRequestWhiteTopic() {
        return new NewTopic(moveRequestWhiteTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic moveRequestBlackTopic() {
        return new NewTopic(moveRequestBlackTopic, 1, (short) 1);
    }
}
