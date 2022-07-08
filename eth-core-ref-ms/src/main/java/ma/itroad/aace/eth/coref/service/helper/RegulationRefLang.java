package ma.itroad.aace.eth.coref.service.helper;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;


@Setter
@Getter
public class RegulationRefLang extends CodeEntity {

    private String countryRef;
    private String organization;

    /*
    translate
     */
    @NotBlank
    private String label;
    @NotBlank
    private String generalDescription;
    @NotBlank
    @langConstraints
    private String lang;

}
