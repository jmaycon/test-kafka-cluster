package com.example;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
class TopicsController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping("/{topic}/{partition}/{messageKey}")
    long produce(@PathVariable("topic") String topic,
                 @PathVariable("partition") int partition,
                 @PathVariable("messageKey") String messageKey,
                 @RequestBody String data) throws ExecutionException, InterruptedException {
        return kafkaTemplate.send(topic, partition, messageKey, data).get().getRecordMetadata().offset();
    }

    @PostMapping("/happy-topic/{partition}/{messageKey}")
    long produceHappyTopic(@PathVariable("partition") int partition,
                 @PathVariable("messageKey") String messageKey,
                 @RequestBody String data) throws ExecutionException, InterruptedException {
        return kafkaTemplate.send("happy-topic", partition, messageKey, data).get().getRecordMetadata().offset();
    }

    @GetMapping("/{topic}/{partition}/{offset}")
    String consume(@PathVariable("topic") String topic, @PathVariable("partition") int partition, @PathVariable("offset") int offset) {
        ConsumerRecord<String, String> received = kafkaTemplate.receive(topic, partition, offset, Duration.of(5, ChronoUnit.SECONDS));
        return received != null ? received.value() : null;
    }
}
