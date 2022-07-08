package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.coref.model.entity.UnitRef;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class TariffBookRefBean  extends AuditEntityBean implements Serializable {
    private String reference;
    private String hsCode;
    private String productYear;
    private LocalDate productExpiryDate;
    private UnitRefBean unitRef;
    private ChapterRefBean referenceChapitre;
    private TariffBookRefBean referencePositionMere;
    private String label;
    private String description;
    private String lang;
}
