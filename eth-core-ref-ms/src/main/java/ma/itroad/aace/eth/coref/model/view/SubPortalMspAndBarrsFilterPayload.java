package ma.itroad.aace.eth.coref.model.view;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubPortalMspAndBarrsFilterPayload {
    private String code;
    private String expCountry;
    private String impCounty;
    private String organizationReference;
}
