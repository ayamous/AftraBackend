package ma.itroad.aace.eth.coref.model.mapper.projections;

// Data JPA Projections
public interface TarifBookTaxationVMProjection  {

     Long getTarifBookId();
     Long getTaxationId();
     String getTarifBookReference();
     String getTaxationReference();
     String getTariffBookLabel();
     String getTariffBookDescription();
     String getTaxationLabel();
     String getTaxationDescription();
     String getLang();

}
