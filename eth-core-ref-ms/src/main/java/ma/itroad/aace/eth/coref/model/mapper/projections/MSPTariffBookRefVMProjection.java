package ma.itroad.aace.eth.coref.model.mapper.projections;

public interface MSPTariffBookRefVMProjection {

     String getMspReference();
     String getTarifBookReference();
     Long getMspId();
     Long getTarifBookId();
     String getMspLabel();
     String getMspDescription();
     String getTarifBookLabel();
     String getTarifBookDescription();
     String getLang();
}
