package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.model.enums.AgreementStatus;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.agreement_seq") })

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "agreement" , schema = "eth")
public class Agreement extends CodeEntity {
    private String title;
    private String description;
    private LocalDate dateOfAgreement ;
    private AgreementStatus agreementStatus;

    @ManyToOne
    private AgreementType agreementType ;
    @ManyToOne
    private  CountryGroupRef countryGroupRef;
    @ManyToOne
    @JoinColumn(name = "country_id")
    private CountryRef countryRef;

    @ManyToMany
    @JoinTable(name = "TARIF_BOOK_REF_AGREEMENTS_REF_JOIN", schema = "eth",
            joinColumns = @JoinColumn(name = "AGREEMENTS_REF_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_AGREEMENTS_REF_ID")),
            inverseJoinColumns = @JoinColumn(name = "FK_TARIF_BOOK_REF_ID", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_TARIF_BOOK_REF_ID"))
    )
    private Set<TarifBookRef> tarifBookRefs;

    @OneToOne
    private ESafeDocument eSafeDocument;
/*
    @Column(columnDefinition="TEXT")
    private String generalDescription;

 */

    @Transient
    private String label;

    @Transient
    private String generalDescription;

    @Transient
    private String lang;
}
