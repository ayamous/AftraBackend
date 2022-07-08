package ma.itroad.aace.eth.coref.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.coref.service.validator.CustomRegimsConstraints;
import ma.itroad.aace.eth.coref.service.validator.MspConstraints;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomsRegimMSPVM implements Serializable {

	@MspConstraints
    private String mspReference;
	@CustomRegimsConstraints
    private String customsRegimReference;
    private Long msptId;
    private Long customsRegimId;
    private String mspLabel;
    private String mspDescription;
    private String customsRegimLabel;
    private String customsRegimDescription;
    private String lang;

}
