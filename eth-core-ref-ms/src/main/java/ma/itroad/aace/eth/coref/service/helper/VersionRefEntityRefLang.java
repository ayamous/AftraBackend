package ma.itroad.aace.eth.coref.service.helper;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.coref.model.enums.StatusVersion;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VersionRefEntityRefLang extends AuditEntityBean {

    private String version;
    private boolean enabled;
    private StatusVersion statusVersion;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applicatedOn;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validatedOn;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate archivedOn;


    @NotBlank
    private String label;
    @NotBlank
    private String description;
    @NotBlank
    @langConstraints
    private String lang;
}

