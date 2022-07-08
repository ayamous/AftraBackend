package ma.itroad.aace.eth.messaging.repository;

import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageStatus;
import ma.itroad.aace.eth.messaging.core.entity.AppMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppMessageJpaRepository extends JpaRepository<AppMessageEntity, Long> {
    List<AppMessageEntity> findAllByRecipientAndCreatedAtAfter(String recipient, LocalDateTime createdAt);

    List<AppMessageEntity> findAllByRecipient(String recipient);

    List<AppMessageEntity> findAllByStatusIn(MessageStatus... statuses);

    List<AppMessageEntity> findAllByRetryLessThanEqualAndStatusIn(int retry, MessageStatus... statuses);

    AppMessageEntity findTopByRecipientOrderByCreatedAtDesc(String recipient);

    AppMessageEntity findByMessageId(String messageId);

    long countAllByRecipientAndCreatedAtAfter(String recipient, LocalDateTime createdAt);

    long countAllByRecipientAndStatusAndCreatedAtAfter(String recipient, MessageStatus status, LocalDateTime createdAt);
}
