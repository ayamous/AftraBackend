package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;


@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.profil_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(JsonInclude.Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------
@Getter
@Setter
@Entity
@Table(name = "profil", schema = "eth")
public class Profil extends AuditEntity {

    @Column(unique = true, nullable = false)
    private String reference;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int rank;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = true)
    private String description;

    @OneToMany(mappedBy = "profil")
    private List<UserAccount> userAccounts;
}
