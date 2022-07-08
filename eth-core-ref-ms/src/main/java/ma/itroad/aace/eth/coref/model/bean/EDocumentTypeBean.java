package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.model.enums.DocumentType;

import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EDocumentTypeBean extends CodeEntityBean {

    Set<ESafeDocumentBean> eSafeDocumentSet ;
    private DocumentType documentType;


}
