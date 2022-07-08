package ma.itroad.aace.eth.coref.model.bean;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;

import java.util.List;



@Getter
@Setter

public class VersionTariffBookBean extends AuditEntityBean {

    private TariffBookRefBean tariffBookRefBean;

    private VersionBean versionBean;

    private List<CountryRefBean> countryRefsRefBeans;
    
    private String label;
    private String description;
    private String lang;

}
