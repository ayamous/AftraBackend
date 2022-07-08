package ma.itroad.aace.eth.coref.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ChapterNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4672272630238789023L;

	public ChapterNotFoundException(String message) {
        super(message);
    }
}
