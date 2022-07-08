package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;

import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class PersonalContactBean extends AuditEntityBean implements Serializable {
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String phoneNumber;
    private String faxNumber;
    private String adress;
    private String email;
    private String contactType;
    private String occupation;
    private List<OrganizationBean> organizationsBeans;
}
