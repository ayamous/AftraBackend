package ma.itroad.aace.eth.coref.model.mapper.projections;

public interface CountryRefEntityRefLangProjection {
    Long getId();
     String getCodeIso();
     String getReference();
     String getLabel();
     String getDescription();
     String getLang();
}
