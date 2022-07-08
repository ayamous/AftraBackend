package ma.itroad.aace.eth.messaging.service;

import ma.itroad.aace.eth.core.common.api.messaging.domain.Ack;
import ma.itroad.aace.eth.core.common.api.messaging.domain.AckStatus;
import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageStatus;
import ma.itroad.aace.eth.core.common.utils.CollectionUtils;
import ma.itroad.aace.eth.core.common.utils.PatternUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface MessagingService {

    default Ack<List<Ack<AppMessage>>> send(Collection<AppMessage> messages) {
        final Logger LOGGER = LoggerFactory.getLogger(MessagingService.class);
        Ack<List<Ack<AppMessage>>> acks = new Ack<>();
        List<Ack<AppMessage>> ackList = new ArrayList<>();
        if (CollectionUtils.isNullOrEmpty(messages)) {
            LOGGER.info("No messages was provided to be handle for mailing");
            acks.code(500).message("No messages was provided to be handle");
        } else {
            Predicate<AppMessage> predicate = appMessage -> {
                long count = messages.stream().filter(message -> {
                    final String recipient = PatternUtils.isEmail(appMessage.getRecipient()) ? appMessage.getRecipient() : PatternUtils.recoverPhoneNumber(appMessage.getRecipient());
                    final String nextRecipient = PatternUtils.isEmail(message.getRecipient()) ? message.getRecipient() : PatternUtils.recoverPhoneNumber(message.getRecipient());
                    return StringUtils.equals(recipient, nextRecipient);
                }).count();
                return count > 1;
            };
            messages.parallelStream().filter(predicate).forEach(appMessage -> appMessage.setStatus(MessageStatus.FAILED_DUPLICATED_RECIPIENT));
            messages.parallelStream().filter(appMessage -> StringUtils.isEmpty(appMessage.getRecipient())).forEach(appMessage -> appMessage.setStatus(MessageStatus.FAILED_INVALID_RECIPIENT));
            long duplicatedMessages = messages.parallelStream().filter(predicate).count();
            long invalidMessages = messages.parallelStream().filter(predicate).count();
            if (duplicatedMessages > 0) {
                LOGGER.info("Bulk operation contains {} duplicated recipient(s)", duplicatedMessages);
            }
            if (invalidMessages > 0) {
                LOGGER.info("Bulk operation contains {} invalid message(s)", duplicatedMessages);
            }
            ackList = process(messages);
        }
        final long deliveredMessages = ackList.parallelStream().filter(appMessageAck -> appMessageAck.getData() != null && appMessageAck.getData().isStatusIn(MessageStatus.DELIVERED)).count();
        final int totalMessages = messages.size();
        if (deliveredMessages == totalMessages) {
            acks.message("All messages was successfully sent");
            acks.code(200);
        } else {
            acks.message(String.format("Only %s out of %s messages was successfully sent", deliveredMessages, totalMessages));
            acks.code(210);
        }
        acks.data(ackList);
        LOGGER.info("{} out of {} mail messages was successfully sent",
                deliveredMessages,
                totalMessages);
        return acks;
    }

    default Ack<AppMessage> send(AppMessage appMessage) {
        Ack<List<Ack<AppMessage>>> acks = send(Collections.singletonList(appMessage));
        if (acks != null) {
            if (CollectionUtils.isNullOrEmpty(acks.getData())) {
                return acks.getData().get(0);
            } else {
                return new Ack<AppMessage>().of(acks).data(appMessage);
            }
        }
        return new Ack<AppMessage>().code(500).data(appMessage).ack(AckStatus.AckResponse.NOK);
    }

    @Async
    default CompletableFuture<Ack<List<Ack<AppMessage>>>> sendAsync(Collection<AppMessage> messages) {
        CompletableFuture<Ack<List<Ack<AppMessage>>>> completableFuture = new CompletableFuture<>();
        completableFuture.complete(send(messages));
        return completableFuture;
    }

    @Async
    default CompletableFuture<Ack<AppMessage>> sendAsync(AppMessage message) {
        CompletableFuture<Ack<AppMessage>> completableFuture = new CompletableFuture<>();
        completableFuture.complete(send(message));
        return completableFuture;
    }

    List<Ack<AppMessage>> process(Collection<AppMessage> messages);
}
