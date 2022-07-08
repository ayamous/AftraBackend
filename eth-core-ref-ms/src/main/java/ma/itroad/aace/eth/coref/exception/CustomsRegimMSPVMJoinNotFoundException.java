package ma.itroad.aace.eth.coref.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomsRegimMSPVMJoinNotFoundException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2159064905803205270L;

	public CustomsRegimMSPVMJoinNotFoundException(String message) {
        super(message);
    }
}
