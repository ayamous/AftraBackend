package ma.itroad.aace.eth.coref.service.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.service.validator.CountryConstraints;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaxRefLang extends CodeEntity {

    private Long id;
    private String taxRefCode;
    @CountryConstraints
    private String countryRefReference;
    private long countryRefId;
}
