package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.model.enums.ContactType;
import ma.itroad.aace.eth.coref.model.enums.Occupation;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.personal_contact_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "personal_contact", schema = "eth")

public class PersonalContact extends AuditEntity {
    @Column(unique = true)
    private String reference;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String phoneNumber;
    private String adress;
    private String email;
    private String faxNumber;

    private ContactType contactType;
    private Occupation occupation;

    @ManyToOne
    @JoinColumn(name = "ref_org", nullable = true)
    private Organization organization;

    @ManyToMany
    @JoinTable(name = "CONTACT_USER_ACCOUNT_JOIN", schema = "eth",
            joinColumns = @JoinColumn(name = "CONTACT_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_CONTACT_ID")),
            inverseJoinColumns = @JoinColumn(name = "USER_ACCOUNT_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_USER_ACCOUNT_ID")))
    private Set<UserAccount> userAccounts;
}
