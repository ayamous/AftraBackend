package ma.itroad.aace.eth.coref.model.view;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductInformationFinderFilterPayload {

    private String exportCountryCode;
    private String importCountryCode;
    private String tarifBookReference;

}
