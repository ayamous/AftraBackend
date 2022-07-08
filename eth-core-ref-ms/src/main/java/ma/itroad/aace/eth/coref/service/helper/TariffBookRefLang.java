/**
 * 
 */
package ma.itroad.aace.eth.coref.service.helper;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.coref.service.validator.ChapterRefConstraints;
import ma.itroad.aace.eth.coref.service.validator.UnitRefConstraints;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 *
 */
@Setter
@Getter
public class TariffBookRefLang extends AuditEntityBean {

    @Pattern(regexp = "^[ a-zA-Z0-9]*$")
    private String reference;
    private String hsCode;
    private String productYear;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate productExpiryDate;

    @ChapterRefConstraints
    private String chapterRef;
    private String parent;
    @UnitRefConstraints
    private String unitRef;

    /**
     * lang
     */

    @NotBlank
    private String label;
    @NotBlank
    private String description;
    @NotBlank
    @langConstraints
    private String lang;
}
