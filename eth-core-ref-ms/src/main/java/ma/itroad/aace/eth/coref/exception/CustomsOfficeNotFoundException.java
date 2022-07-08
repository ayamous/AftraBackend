package ma.itroad.aace.eth.coref.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomsOfficeNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6953509507189568749L;

	public CustomsOfficeNotFoundException(String message) {
        super(message);
    }
}
