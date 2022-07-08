package ma.itroad.aace.eth.messaging.inbound;

import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageStatus;
import ma.itroad.aace.eth.messaging.core.entity.AppMessageEntity;
import ma.itroad.aace.eth.messaging.inbound.base.CrudService;

import java.time.LocalDateTime;
import java.util.List;

public interface AppMessageService extends CrudService<AppMessageEntity, Long> {
    List<AppMessageEntity> findAllByRecipient(String recipient);

    List<AppMessageEntity> findAllByRecipientAndCreatedAtAfter(String recipient, LocalDateTime createdAt);

    AppMessageEntity findLastByRecipientAndCreatedAt(String recipient);

    long countAllByRecipientAndCreatedAtAfter(String recipient, LocalDateTime createdAt);

    long countAllByRecipientAndStatusAndCreatedAtAfter(String recipient, MessageStatus status, LocalDateTime createdAt);

    List<AppMessageEntity> findAllRetryable();

    AppMessageEntity sendMail(AppMessage appMessage);

    AppMessageEntity sendSms(AppMessage appMessage);
}
