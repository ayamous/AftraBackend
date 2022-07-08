package ma.itroad.aace.eth.coref.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TarifBookNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 691112165757762911L;

	public TarifBookNotFoundException(String message) {
        super(message);
    }
}
