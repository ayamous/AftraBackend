package ma.itroad.aace.eth.coref.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContactUserAccountVM implements Serializable {
    private Long contactId;
    private Long userId;
    private String contactReference;
    private String userAccountReference;
}
