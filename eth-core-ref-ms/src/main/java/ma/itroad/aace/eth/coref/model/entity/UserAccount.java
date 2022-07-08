package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.coref.model.enums.StatusUserAccount;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.user_account_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(JsonInclude.Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------
@Data
@Entity
@Table(name = "user_account", schema = "eth")
@EqualsAndHashCode(callSuper = false)
public class UserAccount extends AuditEntity {

    @Column(unique = true, nullable = false)
    private String reference;

    @Column(nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    private String temporalPwd;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String email;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date temporalPwdExpDate;

    @Column(nullable = false)
    private StatusUserAccount status;

    @ManyToOne
    @JoinColumn(name = "profil_id")
    private Profil profil;

    @ManyToMany(mappedBy = "userAccounts")
    private Set<PersonalContact> personalContacts;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
}
