package com.example;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class ConsumerListener {

    @Value("${spring.kafka.bootstrap-servers}")
    private String value;

    @PostConstruct
    public void postConst() {
        System.out.printf(value);
    }

    @KafkaListener(topics = "happy-topic", autoStartup = "true", concurrency = "1")
    void consumer1(ConsumerRecord<String, String> consumerRecord) {
        log.info("Consumer1 Partition {} Offset {} Received {} / {}", consumerRecord.partition(),
                consumerRecord.offset(), consumerRecord.key(), consumerRecord.value());
    }

    @KafkaListener(topics = "happy-topic", autoStartup = "true", concurrency = "1")
    void consumer2(ConsumerRecord<String, String> consumerRecord) {
        log.info("Consumer2 Partition {} Offset {} Received {} / {}", consumerRecord.partition(),
                consumerRecord.offset(), consumerRecord.key(), consumerRecord.value());
    }

    @KafkaListener(topics = "happy-topic", autoStartup = "true", concurrency = "1")
    void consumer3(ConsumerRecord<String, String> consumerRecord) {
        log.info("Consumer3 Partition {} Offset {} Received {} / {}", consumerRecord.partition(),
                consumerRecord.offset(), consumerRecord.key(), consumerRecord.value());
    }

    @KafkaListener(topics = "happy-topic", autoStartup = "true", concurrency = "1")
    void consumer4(ConsumerRecord<String, String> consumerRecord) {
        log.info("Consumer4 Partition {} Offset {} Received {} / {}", consumerRecord.partition(),
                consumerRecord.offset(), consumerRecord.key(), consumerRecord.value());
    }
}
