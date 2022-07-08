package ma.itroad.aace.eth.coref.model.mapper;

public interface RegulationTariffBookRefVMProjection {

     String getRegulationReference();
     String getTarifBookReference();
     Long getRegulationId();
     Long getTarifBookId();
     String getRegulationLabel();
     String getRegulationDescription();
     String getTarifBookLabel();
     String getTarifBookDescription();
     String getLang();
}
