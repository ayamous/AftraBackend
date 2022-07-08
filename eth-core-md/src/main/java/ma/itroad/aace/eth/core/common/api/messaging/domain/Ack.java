package ma.itroad.aace.eth.core.common.api.messaging.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Ack<T> {

    private AckStatus.AckResponse ack;
    private int code;
    private String message;
    private T data;

    public Ack<T> ack(AckStatus.AckResponse ack) {
        this.ack = ack;
        return this;
    }

    public Ack<T> code(int code) {
        this.code = code;
        return this;
    }

    public Ack<T> message(String message) {
        this.message = message;
        return this;
    }

    public void of(AckStatus.AckResponse ack, int code, String message, T data) {
        this.ack = ack;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public void ok(String message, T data) {
        this.ack = AckStatus.AckResponse.OK;
        this.code = AckStatus.ACK_OK;
        this.message = message;
        this.data = data;
    }

    public Ack<T> of(Ack<?> item) {
        ack = item.getAck();
        code = item.getCode();
        message = item.getMessage();
        return this;
    }

    public Ack<T> data(T data) {
        this.data = data;
        return this;
    }

    @JsonIgnore
    public boolean isNotOk() {
        return AckStatus.NOK_RESPONSE.contains(ack) || AckStatus.NOK_STATUS.contains(code);
    }
    @JsonIgnore
    public boolean isOk() {
        return !isNotOk();
    }
}
