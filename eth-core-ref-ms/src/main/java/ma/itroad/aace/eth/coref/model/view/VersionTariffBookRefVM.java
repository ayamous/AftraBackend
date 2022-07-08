package ma.itroad.aace.eth.coref.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.coref.service.validator.CountryConstraints;
import ma.itroad.aace.eth.coref.service.validator.TarifBookConstraint;
import ma.itroad.aace.eth.coref.service.validator.VersionConstraints;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VersionTariffBookRefVM implements Serializable {

    private Long id;
   
    @VersionConstraints
    private String versionRefReference;
    @TarifBookConstraint
    private String tarifBookReference;
    @CountryConstraints
    private String countryRefReference;
    private Long versionRefId;
    private Long tarifBookId;
    private long countryRefId;
    private String versionLabel;
    private String versionDescription;
    private String tarifBookLabel;
    private String tarifBookDescription;
    private String lang;
}
