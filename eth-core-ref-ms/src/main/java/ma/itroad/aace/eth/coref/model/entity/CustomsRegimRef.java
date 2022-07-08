package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.model.enums.RegimType;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.customs_regim_ref_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "customs_regim_ref", schema = "eth")
public class CustomsRegimRef extends CodeEntity {
    @Column(name = "regim_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RegimType regimType;

    @OneToMany(mappedBy = "customsRegimRef")
    private Set <NationalProcedureRef> nationalProcedureRef;


    @OneToMany(mappedBy = "customsRegimRef")
    private List<Taxation> taxations;

    @OneToMany(mappedBy = "customsRegimRef")
    private List<TechBarrierRef> techBarrierRefs;

    @ManyToMany
    @JoinTable(name = "customs_regim_REF_sanitary_phytosanitary_measures_JOIN",
            schema = "eth",
            joinColumns = @JoinColumn(name = "customs_regim_REF_ID", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_customs_regim_REF_ID")),
            inverseJoinColumns = @JoinColumn(name = "sanitary_phytosanitary_measures_ID", referencedColumnName = "id",  foreignKey = @ForeignKey(name = "FK_sanitary_phytosanitary_measures_ID")))
    private Set<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRef;

    @ManyToMany
    @JoinTable(name = "customs_regim_REF_regulation_JOIN",
            schema = "eth",
            joinColumns = @JoinColumn(name = "customs_regim_REF_ID", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_customs_regim_REF_ID")),
            inverseJoinColumns = @JoinColumn(name = "regulation_ID", referencedColumnName = "id",  foreignKey = @ForeignKey(name = "FK_regulation_ID")))
    private Set <RegulationRef> regulationRef;
}
