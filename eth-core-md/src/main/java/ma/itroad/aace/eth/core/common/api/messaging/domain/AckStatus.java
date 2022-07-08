package ma.itroad.aace.eth.core.common.api.messaging.domain;

import java.util.Arrays;
import java.util.List;

public abstract class AckStatus {
    public static final List<Integer> NOK_STATUS = Arrays.asList(AckStatus.ACK_NOK, AckStatus.ACK_NOK);
    public static final List<AckResponse> NOK_RESPONSE = Arrays.asList(AckResponse.NOK, AckResponse.NOT_AUTH);

    public static int ACK_OK = 200;
    public static int ACK_UNKNOWN = 300;
    public static int ACK_NO_CONTENT = 201;
    public static int ACK_NOK = 500;
    public static int ACK_EXTERNAL_NOK = 501;

    public enum AckResponse {
        OK, NOK, NO_CONTENT, OK_PLUS_NOTIFICATION_SMS, USER_ACTIVATED, USER_TO_BE_ENABLED, USER_VOLUNTARY,
        OK_PLUS_NOTIFICATION_MAIL,
        OK_PLUS_NOTIFICATION,
        NOT_AUTH,
        MAIL_SENT, MAIL_NOT_SENT, SMS_SENT, SMS_NOT_SENT, PHONE_NUMBER_ALREADY_EXIST, PHONE_NUMBER_UNIQUE
    }
}
