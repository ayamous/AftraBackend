package ma.itroad.aace.eth.core.security.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JWTToken implements Serializable {
    private static final long serialVersionUID = 4435093800576101089L;

    private String idToken;

    @JsonCreator
    public JWTToken(@JsonProperty("id_token") String idToken) {
        this.idToken = idToken;
    }

}

