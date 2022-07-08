package ma.itroad.aace.eth.coref.service.helper;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.coref.model.enums.DocumentType;


@Getter
@Setter
public class EDocTypeRefEntityRefLang extends AuditEntityBean {

    private String code;
    private String label;
    private String description;
    private String lang;
    private DocumentType documentType;
}
