package ma.itroad.aace.eth.core.common.api.messaging;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import ma.itroad.aace.eth.core.common.api.messaging.domain.Ack;
import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RequestMapping("v1/messaging")
public interface MessagingAPI {

    @ApiOperation(value = "Send an sms to single or multiple recipients", response = Ack.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Sms message(s) was successfully processed"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 400, message = "Malformed request")
    })
    @PostMapping("sms")
    @ApiModelProperty
    ResponseEntity<Ack<List<Ack<AppMessage>>>> sendSms(@Valid @RequestBody Collection<AppMessage> messages);

    @ApiOperation(value = "Send an email to a single or multiple recipients", response = Ack.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Mail message(s) was successfully processed"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 400, message = "Malformed request")
    })
    @PostMapping("mail")
    @ApiModelProperty
    ResponseEntity<Ack<List<Ack<AppMessage>>>> sendMail(@Valid @RequestBody Collection<AppMessage> messages);


    @ApiOperation(value = "Send an email to a single or multiple recipients", response = Ack.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Mail message(s) was successfully processed"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 400, message = "Malformed request")
    })
    @PostMapping("async/mail")
    @ApiModelProperty
    ResponseEntity<Void> sendMailAsync(@Valid @RequestBody Collection<AppMessage> messages);

    @ApiOperation(value = "Send a notification to single or multiple recipients", response = Ack.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Notification message(s) was successfully processed"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 400, message = "Malformed request")
    })
    @PostMapping("notify")
    @ApiModelProperty
    ResponseEntity<AppMessage> sendNotification(@Valid @RequestBody AppMessage appMessage);
}
