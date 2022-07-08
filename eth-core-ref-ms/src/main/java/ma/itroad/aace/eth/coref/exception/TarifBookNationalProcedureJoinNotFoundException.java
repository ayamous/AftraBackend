package ma.itroad.aace.eth.coref.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TarifBookNationalProcedureJoinNotFoundException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 894243412116529096L;

    public TarifBookNationalProcedureJoinNotFoundException(String s) {
        super(s);
    }
}