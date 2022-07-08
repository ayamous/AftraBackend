package ma.itroad.aace.eth.coref.service.helper;

import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LangRefEntityRefLang extends CodeEntityBean {

    private String name;
    private String def;
    private String label;
    private String description;
    private String lang;
}