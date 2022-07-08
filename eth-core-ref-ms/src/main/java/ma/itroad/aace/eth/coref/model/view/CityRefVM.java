package ma.itroad.aace.eth.coref.model.view;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CityRefVM {

    private String reference;
    private String countryRef;
    Long CityRefId;

    List<InternationalizationVM> internationalizationVMList;
}
