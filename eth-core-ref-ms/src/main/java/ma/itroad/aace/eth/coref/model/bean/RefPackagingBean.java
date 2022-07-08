package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;

import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class RefPackagingBean extends CodeEntityBean implements Serializable {
    private List<InternationalizationVM> internationalizationVMList;

}