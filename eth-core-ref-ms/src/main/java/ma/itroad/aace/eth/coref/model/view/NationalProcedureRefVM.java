package ma.itroad.aace.eth.coref.model.view;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NationalProcedureRefVM {

    private String procedureCode;
    private String customRegimeCode;
    private String countryRef;
    private String organization;
}
