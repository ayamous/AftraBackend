package ma.itroad.aace.eth.core.common.api.messaging.enums;

public enum MessageType {
    EMAIL, EMAIL_HTML, SMS, PUSH_NOTIFICATION;

    public static boolean isEmail(MessageType type) {
        return EMAIL_HTML.equals(type) || EMAIL.equals(type);
    }

    public static boolean isSms(MessageType type) {
        return SMS.equals(type);
    }

    public static boolean isNotification(MessageType type) {
        return PUSH_NOTIFICATION.equals(type);
    }
}
