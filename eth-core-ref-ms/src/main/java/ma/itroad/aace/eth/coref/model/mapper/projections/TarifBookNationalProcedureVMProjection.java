package ma.itroad.aace.eth.coref.model.mapper.projections;

public interface TarifBookNationalProcedureVMProjection {

     Long getTarifBookId();
      Long getNationalProcedureRefId();
      String getTarifBookReference();
      String getNationalProcedureCode();
      String getTariffBookLabel();
      String getTariffBookDescription();
      String getNationalProcedureLabel();
      String getNationalProcedureDescription();
      String getLang();

}
