package ma.itroad.aace.eth.core.common.api.messaging.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageStatus;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageType;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppMessage {

    public static final String TEMPLATE_KEY_NAME = "template";
    public static final String TEMPLATE_PARAMS_KEY_NAME = "params";
    public static final String RECIPIENTS_SEPARATOR = ",";

    private long id;
    private String messageId;
    private String recipient;
    private String sender;
    private String subject;
    private String content;
    private MessageStatus status = MessageStatus.PENDING;
    private String statusDescription;
    private String providerStatus;
    private String providerCodeStatus;
    private MessageType type;
    private int retry;

    private Map<String, Object> additionalProperties;

    public boolean isStatusIn(MessageStatus... statuses) {
        if (status == null)
            return false;
        for (MessageStatus value :
                statuses) {
            if (status.equals(value))
                return true;
        }
        return false;
    }

    @JsonIgnore
    public boolean isEmail() {
        return MessageType.isEmail(type);
    }

    @JsonIgnore
    public boolean isSms() {
        return MessageType.isSms(type);
    }

    @JsonIgnore
    public void incrementRetry() {
        retry++;
    }

    @JsonIgnore
    public void addProperty(String key, Object value) {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }
        additionalProperties.put(key, value);
    }

    @JsonIgnore
    public boolean hasTemplateProperties() {
        return MapUtils.isNotEmpty(additionalProperties) && additionalProperties.containsKey(TEMPLATE_KEY_NAME) && additionalProperties.containsKey(TEMPLATE_PARAMS_KEY_NAME);
    }

    @JsonIgnore
    public Object getProperty(String key) {
        if (additionalProperties == null) {
            return null;
        }
        return additionalProperties.get(key);
    }

    @JsonIgnore
    public String[] getAllRecipients() {
        if (recipient == null) {
            return new String[0];
        }
        return recipient.split(RECIPIENTS_SEPARATOR);
    }

}
