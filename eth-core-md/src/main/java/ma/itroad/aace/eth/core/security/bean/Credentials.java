package ma.itroad.aace.eth.core.security.bean;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Credentials implements Serializable {
    private static final long serialVersionUID = 3990329256233566024L;

    private String username;
    private String password;

}
