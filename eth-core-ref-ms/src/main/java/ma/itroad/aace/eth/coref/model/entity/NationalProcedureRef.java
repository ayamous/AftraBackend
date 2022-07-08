package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.national_procedure_ref_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "national_procedure_ref", schema = "eth")
public class NationalProcedureRef extends CodeEntity {
    @OneToMany(mappedBy = "nationalProcedureRef")
    private Set<ExtendedProcedureRef> extendedProcedureRefs ;

    @ManyToOne
    private CountryRef countryRef ;

    @ManyToOne
    private CustomsRegimRef customsRegimRef;

    @ManyToOne
    private Organization organization;

    @OneToOne
    private ESafeDocument eSafeDocument ;


    @ManyToMany
    @JoinTable(name = "TARIF_BOOK_REF_National_Procedure_REF_JOIN", schema = "eth",
            joinColumns = @JoinColumn(name = "NATIONAL_PROCEDURE_REF_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_NATIONAL_PROCEDURE_REF_ID")),
            inverseJoinColumns = @JoinColumn(name = "FK_TARIF_BOOK_REF_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_TARIF_BOOK_REF_ID"))
    )
    private Set<TarifBookRef> tarifBookRefs;
/*
    @Column(columnDefinition="TEXT")
    private String generalDescription ;

 */

    @Transient
    private String label;

    @Transient
    private String generalDescription;

    @Transient
    private String lang;

}
