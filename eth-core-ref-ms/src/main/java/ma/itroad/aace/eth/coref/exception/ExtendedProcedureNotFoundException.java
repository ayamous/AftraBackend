package ma.itroad.aace.eth.coref.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExtendedProcedureNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4031954580355728498L;

	public ExtendedProcedureNotFoundException(String message) {
        super(message);
    }
}
