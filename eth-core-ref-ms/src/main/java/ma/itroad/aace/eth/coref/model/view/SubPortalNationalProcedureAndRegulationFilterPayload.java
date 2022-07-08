package ma.itroad.aace.eth.coref.model.view;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubPortalNationalProcedureAndRegulationFilterPayload {

    private String code;
    private String impCountry;
    private String expCountry;
}
