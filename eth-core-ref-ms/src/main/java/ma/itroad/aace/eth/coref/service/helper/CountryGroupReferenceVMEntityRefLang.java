package ma.itroad.aace.eth.coref.service.helper;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CountryGroupReferenceVMEntityRefLang {
    private String labelCountryGroup;
    private String descriptionCountryGroup;
    private String labelCountry;
    private String descriptionCountry;
    private String countryGroupReference;
    private String CountryReference;
    private Long countryGroupRefId;
    private Long countryRefId;
}
