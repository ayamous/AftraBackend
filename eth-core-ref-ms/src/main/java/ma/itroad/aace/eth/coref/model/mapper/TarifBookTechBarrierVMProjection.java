package ma.itroad.aace.eth.coref.model.mapper;

public interface TarifBookTechBarrierVMProjection {
    Long getTarifBookId();
    Long getTechBarrierId();
    String getTarifBookReference();
    String getTechBarrierReference();
    String getTariffBookLabel();
    String getTariffBookDescription();
    String getTechBarrierLabel();
    String getTechBarrierDescription();
    String lang();
}
