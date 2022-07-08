package ma.itroad.aace.eth.coref.service.helper;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.service.validator.CountryConstraints;
import ma.itroad.aace.eth.coref.service.validator.OrganizationConstraints;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class SanitaryPhytosanitaryMeasuresRefLang extends CodeEntity {

    @CountryConstraints
    @NotBlank
    private String countryRef;

    @NotBlank
    @OrganizationConstraints
    private String organization;

    @NotBlank
    private String label;

    @NotBlank
    private String generalDescription;

    @NotBlank
    @langConstraints
    private String lang;
}
