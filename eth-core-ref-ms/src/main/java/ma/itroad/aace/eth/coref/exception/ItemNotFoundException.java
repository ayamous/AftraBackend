package ma.itroad.aace.eth.coref.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ItemNotFoundException extends  RuntimeException{

    /**
     *
     */
    private static final long serialVersionUID = -837047005289834376L;

    public ItemNotFoundException(String message) {
        super(message);
    }
}
