package ma.itroad.aace.eth.coref.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TranslationFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7777875251229147962L;

	public TranslationFoundException(String message) {
        super(message);
    }
}
