package com.homework.bankaccount.event;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

  private final ObjectMapper objectMapper;

  @SneakyThrows
  @KafkaListener(
      topics = TransactionEventProducer.TOPIC,
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "transactionKafkaListenerContainerFactory")
  public void consume(String payload) {
    TransactionEvent event = objectMapper.readValue(payload, TransactionEvent.class);
    log.info(
        "[KAFKA EVENT] type={} accountId={} amount={} {} -> {} {}",
        event.type(),
        event.accountId(),
        event.amount(),
        event.currency(),
        event.targetAmount() != null ? event.targetAmount() : "-",
        event.targetCurrency() != null ? event.targetCurrency() : "");
    // Process the event according to business logic
  }
}
