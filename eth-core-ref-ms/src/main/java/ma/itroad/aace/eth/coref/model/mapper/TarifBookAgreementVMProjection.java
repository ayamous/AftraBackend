package ma.itroad.aace.eth.coref.model.mapper;

public interface TarifBookAgreementVMProjection {
     Long getTarifBookId();
     Long getAgreementId();
     String getTarifBookReference();
     String getAgreementReference();
     String getTariffBookLabel();
     String getTariffBookDescription();
     String getAgreementLabel();
     String getAgreementDescription();
     String lang();
}
