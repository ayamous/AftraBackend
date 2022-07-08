package ma.itroad.aace.eth.coref.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SanitaryPhytosanitaryMeasuresNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5071345013343002699L;

	public SanitaryPhytosanitaryMeasuresNotFoundException(String message) {
        super(message);
    }
}
