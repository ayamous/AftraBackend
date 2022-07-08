package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@Data
public class CountryGroupRefBean extends CodeEntityBean implements Serializable {

    private String reference;
}
