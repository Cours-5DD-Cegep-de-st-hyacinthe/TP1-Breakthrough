package com.kleblanc.breakthrough_backend.config;

import com.kleblanc.breakthrough_backend.model.Move;
import com.kleblanc.breakthrough_backend.model.message.IAStatusMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value(value = "${spring.kafka.consumer.group-id}")
    private String groupId;

    // IA Status topic --------------------------------------------------------------------------

    @Bean
    public ConsumerFactory<String, IAStatusMessage> iaStatusConsumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(IAStatusMessage.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, IAStatusMessage>
    iaStatusKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, IAStatusMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(iaStatusConsumerFactory());
        return factory;
    }

    // MoveResponse topic --------------------------------------------------------------------------

    @Bean
    public ConsumerFactory<String, Move> moveResponseConsumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(Move.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Move>
    moveResponseKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Move> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(moveResponseConsumerFactory());
        return factory;
    }
}
