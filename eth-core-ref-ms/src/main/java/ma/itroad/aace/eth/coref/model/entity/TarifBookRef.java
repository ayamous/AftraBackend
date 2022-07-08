package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;


@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.tariff_book_ref_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "tarif_book_ref", schema = "eth")
public class TarifBookRef extends AuditEntity {
    private static final long serialVersionUID = 3847205726858956918L;

    @Column(name = "reference", unique = true)
    private String reference;

    private String hsCode;

    private String productYear;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate productExpiryDate;

    @ManyToOne()
    @JoinColumn(name = "chapter_ref_id", nullable = true,
            foreignKey = @ForeignKey(name = "FK_TARIFF_CHAPTER_ID"))
    private ChapterRef chapterRef;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.ALL})
    private List<TarifBookRef> tarifBookRefList;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "parent_id")
    private TarifBookRef parent;

    @OneToMany(mappedBy = "tarifBookRef", cascade = {CascadeType.ALL})
    private List<VersionTariffBookRef> versionTariffBooks;

    @ManyToOne
    @JoinColumn(name = "unit_ref_id")
    private UnitRef unitRef;


    @ManyToMany(mappedBy = "tarifBookRefs")
    private Set<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs;


    @ManyToMany(mappedBy = "tarifBookRefs")
    private Set<RegulationRef> regulationRefs;

    @ManyToMany(mappedBy = "tarifBookRefs")
    private Set<NationalProcedureRef> nationalProcedureRefs;
 //  private Set<NationalProcedureRef> nationalProcedureRefs ;

    @ManyToMany(mappedBy = "tarifBookRefs")
    private Set<Taxation> taxations;

    @ManyToMany(mappedBy = "tariffBookRefs")
    @JsonIgnore
    private Set<TechBarrierRef> techBarrierRefs;

    @ManyToMany(mappedBy = "tarifBookRefs")
    private Set<Agreement> agreements;
    
    @Transient
    private String label;
    
    @Transient
    private String description;
    
    @Transient
    private String lang;


    public TarifBookRef reference(String reference) {
        this.setReference(reference);
        return this;
    }
}
