package ma.itroad.aace.eth.messaging.core.entity;

import lombok.*;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageStatus;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageType;
import ma.itroad.aace.eth.core.model.entity.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@Table(name = "app_messaging")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppMessageEntity extends BaseEntity {
    private String messageId;
    private String recipient;
    private String sender;
    private String subject;

    @Column(length = Integer.MAX_VALUE)
    private String content;

    @Column(name = "message_status", length = 64)
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    private String statusDescription;
    private String providerStatus;
    private String providerCodeStatus;

    @Column(length = 64)
    @Enumerated(EnumType.STRING)
    private MessageType type;

    private LocalDateTime createdAt;

    private int retry;

    @PrePersist
    public void init() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

}
