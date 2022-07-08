package ma.itroad.aace.eth.coref.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private HttpStatus status;
    private String errorMsg;

    @JsonIgnore
    private String developerMsg;


}
