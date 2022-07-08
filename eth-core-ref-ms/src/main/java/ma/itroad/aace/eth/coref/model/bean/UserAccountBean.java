package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.coref.model.enums.StatusUserAccount;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAccountBean extends AuditEntityBean implements Serializable {
    private Long id;

    private String reference;

    private String login;

    private String password;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date temporalPwdExpDate;

    private StatusUserAccount status;

    private String profilReference;

    private Long profilId;

}
