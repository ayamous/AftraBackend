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
public class TechBarrierRefVM implements Serializable {

    private Long techBarrierRefId;
    private String techBarrierRefCode;
    private String countryReference;
    private String documentReference;
    
    private String lang;
    private String description;
    private String label;
}
