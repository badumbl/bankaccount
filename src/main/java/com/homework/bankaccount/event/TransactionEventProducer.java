package com.homework.bankaccount.event;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionEventProducer {

    static final String TOPIC = "bank-transactions";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void publish(TransactionEvent event) {
        String payload = objectMapper.writeValueAsString(event);
    kafkaTemplate
        .send(TOPIC, event.accountId().toString(), payload)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.error(
                    "Failed to publish transaction event for account {}: {}",
                    event.accountId(),
                    ex.getMessage());
              } else {
                log.info(
                    "Published {} event for account {} to partition {}",
                    event.type(),
                    event.accountId(),
                    result.getRecordMetadata().partition());
              }
            });
    }
}