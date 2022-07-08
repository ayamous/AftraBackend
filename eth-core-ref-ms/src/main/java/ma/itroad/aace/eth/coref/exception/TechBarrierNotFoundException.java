package ma.itroad.aace.eth.coref.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TechBarrierNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4573016805108050227L;

	public TechBarrierNotFoundException(String message) {
        super(message);
    }
}
