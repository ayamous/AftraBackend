package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;

import java.io.Serializable;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class CountryRefBean extends CodeEntityBean implements Serializable {
    private String reference;
    private String codeIso;

}
