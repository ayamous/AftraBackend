package ma.itroad.aace.eth.coref.model.view;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MspAndBarriersFilterPayload {
    private String code;
    private String countryReference;
    private String customRegimCode;
    private String organizationReference ;

}
