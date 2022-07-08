package ma.itroad.aace.eth.core.common.api.messaging.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MessageStatus {
    PENDING, FAILED, FAILED_DUPLICATED_RECIPIENT, FAILED_INVALID_RECIPIENT, DELIVERED, PROVIDER_EMPTY_RESPONSE;

    public static boolean isFailed(MessageStatus status) {
        return Arrays.asList(FAILED,
                FAILED_DUPLICATED_RECIPIENT,
                FAILED_INVALID_RECIPIENT).contains(status);
    }
    public static boolean isDelivered(MessageStatus status) {
        return DELIVERED.equals(status);
    }
}
