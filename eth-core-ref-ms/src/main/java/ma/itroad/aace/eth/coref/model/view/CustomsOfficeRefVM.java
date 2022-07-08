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
public class CustomsOfficeRefVM implements Serializable {

    private String countryRef;
    private String customsOfficeCode;
    Long customsOfficeId;
}
