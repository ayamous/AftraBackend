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
public class CountryGroupReferenceVM implements Serializable {
    String countryGroupReference;
    String countryReference;
    Long countryGroupRefId;
    Long countryRefId;
}
