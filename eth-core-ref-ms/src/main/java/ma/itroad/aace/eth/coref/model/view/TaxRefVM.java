package ma.itroad.aace.eth.coref.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.service.validator.CountryConstraints;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaxRefVM extends CodeEntity  implements Serializable {

    private Long id;
    private String taxRefCode;
    private String countryRefReference;
    private long countryRefId;
}
