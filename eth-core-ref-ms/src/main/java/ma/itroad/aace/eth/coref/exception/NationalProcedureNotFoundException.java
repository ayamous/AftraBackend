package ma.itroad.aace.eth.coref.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NationalProcedureNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8491699254541354911L;

	public NationalProcedureNotFoundException(String message) {
        super(message);
    }
}
