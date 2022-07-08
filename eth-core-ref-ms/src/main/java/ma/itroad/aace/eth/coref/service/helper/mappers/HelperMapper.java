package ma.itroad.aace.eth.coref.service.helper.mappers;

import ma.itroad.aace.eth.coref.repository.CountryGroupRefRepository;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.repository.CustomsRegimRefRepository;
import ma.itroad.aace.eth.coref.repository.TaxRefRepository;
import ma.itroad.aace.eth.coref.repository.TaxationRepository;
import ma.itroad.aace.eth.coref.repository.UnitRefRepository;
import ma.itroad.aace.eth.coref.service.*;
import ma.itroad.aace.eth.coref.service.ICountryGroupRefService;
import ma.itroad.aace.eth.coref.service.helper.*;
import ma.itroad.aace.eth.coref.service.helper.detailed.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelperMapper {
    @Autowired
    ICountryRefService iCountryRefService;

    @Autowired
    CountryRefRepository countryRefRepository;

    @Autowired
    ICountryGroupRefService iCountryGroupRefService;

    @Autowired
    CountryGroupRefRepository countryGroupRefRepository;

    @Autowired
    ICustomsRegimRefService iCustomsRegimRefService;

    @Autowired
    CustomsRegimRefRepository customsRegimRefRepository;

    @Autowired
    IUnitRefService iUnitRefService;

    @Autowired
    UnitRefRepository unitRefRepository;

    @Autowired
    ITaxRefService iTaxRefService;

    @Autowired
    TaxRefRepository taxRefRepository;




    public NationalProcedureRefLangDetailed toDetailedNationalProcedure(NationalProcedureRefLang nationalProcedureRefLang) {
        NationalProcedureRefLangDetailed nationalProcedureRefLangDetailed = new NationalProcedureRefLangDetailed();
        nationalProcedureRefLangDetailed.setId(nationalProcedureRefLang.getId());
        nationalProcedureRefLangDetailed.setCode(nationalProcedureRefLang.getCode());
        nationalProcedureRefLangDetailed.setCreatedBy(nationalProcedureRefLang.getCreatedBy());
        nationalProcedureRefLangDetailed.setCreatedOn(nationalProcedureRefLang.getCreatedOn());
        nationalProcedureRefLangDetailed.setUpdatedBy(nationalProcedureRefLang.getUpdatedBy());
        nationalProcedureRefLangDetailed.setUpdatedOn(nationalProcedureRefLang.getUpdatedOn());
        nationalProcedureRefLangDetailed.setLang(nationalProcedureRefLang.getLang());
        nationalProcedureRefLangDetailed.setLabel(nationalProcedureRefLang.getLabel());
        nationalProcedureRefLangDetailed.setGeneralDescription(nationalProcedureRefLang.getGeneralDescription());
        if (nationalProcedureRefLang.getCountryRef() != null && countryRefRepository.findByReference(nationalProcedureRefLang.getCountryRef()) != null)
            nationalProcedureRefLangDetailed.setCountryRef(
                    iCountryRefService.findCountry(countryRefRepository.findByReference(nationalProcedureRefLang.getCountryRef()).getId(), nationalProcedureRefLang.getLang()));
        if (nationalProcedureRefLang.getCustomsRegimRef() != null && customsRegimRefRepository.findByCode(nationalProcedureRefLang.getCustomsRegimRef()) != null)
            nationalProcedureRefLangDetailed.setCustomsRegimRef(
                    iCustomsRegimRefService.findCustomsRegimRef(customsRegimRefRepository.findByCode(nationalProcedureRefLang.getCustomsRegimRef()).getId(), nationalProcedureRefLang.getLang()));
        return nationalProcedureRefLangDetailed;
    }

    public AgreementLangDetailed toDetailedAgreement(AgreementLang agreementLang) {
        AgreementLangDetailed agreementLangDetailed = new AgreementLangDetailed();

        agreementLangDetailed.setId(agreementLang.getId());
        agreementLangDetailed.setCode(agreementLang.getCode() != null ? agreementLang.getCode() : "");
        agreementLangDetailed.setTitle(agreementLang.getTitle() != null ? agreementLang.getTitle() : "");
        agreementLangDetailed.setDescription(agreementLang.getDescription() != null ? agreementLang.getDescription() : "");
        agreementLangDetailed.setLabel(agreementLang.getLabel() != null ? agreementLang.getLabel() : "");
        agreementLangDetailed.setLang(agreementLang.getLang());
        agreementLangDetailed.setCreatedBy(agreementLang.getCreatedBy());
        agreementLangDetailed.setCreatedOn(agreementLang.getCreatedOn());
        agreementLangDetailed.setUpdatedBy(agreementLang.getUpdatedBy());
        agreementLangDetailed.setUpdatedOn(agreementLang.getUpdatedOn());

        if (agreementLang.getCountryRef() != null && countryRefRepository.findByReference(agreementLang.getCountryRef()) != null)
            agreementLangDetailed.setCountryRef(
                    iCountryRefService.findCountry(countryRefRepository.findByReference(agreementLang.getCountryRef()).getId(), agreementLang.getLang()));

        if (agreementLang.getCountryGroupRef() != null && countryGroupRefRepository.findByReference(agreementLang.getCountryGroupRef()) != null)
            agreementLangDetailed.setCountryGroupRef(
                    iCountryGroupRefService.findCountryGroup(countryGroupRefRepository.findByReference(agreementLang.getCountryGroupRef()).getId(), agreementLang.getLang()));
        return agreementLangDetailed;
    }

    public SanitaryPhytosanitaryMeasuresRefLangDetailed toDetailedSanitaryPhytosanitaryMeasure(SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang){
      SanitaryPhytosanitaryMeasuresRefLangDetailed sanitaryPhytosanitaryMeasuresRefLangDetailed = new SanitaryPhytosanitaryMeasuresRefLangDetailed() ;

      sanitaryPhytosanitaryMeasuresRefLangDetailed.setId(sanitaryPhytosanitaryMeasuresRefLang.getId());
      sanitaryPhytosanitaryMeasuresRefLangDetailed.setCode(sanitaryPhytosanitaryMeasuresRefLang.getCode()!=null ?sanitaryPhytosanitaryMeasuresRefLang.getCode():"");
      sanitaryPhytosanitaryMeasuresRefLangDetailed.setCreatedBy(sanitaryPhytosanitaryMeasuresRefLang.getCreatedBy());
      sanitaryPhytosanitaryMeasuresRefLangDetailed.setCreatedOn(sanitaryPhytosanitaryMeasuresRefLang.getCreatedOn());
      sanitaryPhytosanitaryMeasuresRefLangDetailed.setUpdatedBy(sanitaryPhytosanitaryMeasuresRefLang.getUpdatedBy());
      sanitaryPhytosanitaryMeasuresRefLangDetailed.setUpdatedOn(sanitaryPhytosanitaryMeasuresRefLang.getUpdatedOn());
      sanitaryPhytosanitaryMeasuresRefLangDetailed.setLabel(sanitaryPhytosanitaryMeasuresRefLang.getLabel()!=null ?sanitaryPhytosanitaryMeasuresRefLang.getLabel():"");
      sanitaryPhytosanitaryMeasuresRefLangDetailed.setGeneralDescription(sanitaryPhytosanitaryMeasuresRefLang.getGeneralDescription()!=null ?sanitaryPhytosanitaryMeasuresRefLang.getGeneralDescription():"");
      if (sanitaryPhytosanitaryMeasuresRefLang.getCountryRef() != null && countryRefRepository.findByReference(sanitaryPhytosanitaryMeasuresRefLang.getCountryRef()) != null)
        sanitaryPhytosanitaryMeasuresRefLangDetailed.setCountryRef(
                iCountryRefService.findCountry(countryRefRepository.findByReference(sanitaryPhytosanitaryMeasuresRefLang.getCountryRef()).getId(), sanitaryPhytosanitaryMeasuresRefLang.getLang()));
      sanitaryPhytosanitaryMeasuresRefLangDetailed.setLang(sanitaryPhytosanitaryMeasuresRefLang.getLang());

      return sanitaryPhytosanitaryMeasuresRefLangDetailed ;
    }

    public RegulationRefLangDetailed toDetailedRegulation(RegulationRefLang regulationRefLang){
        RegulationRefLangDetailed regulationRefLangDetailed = new RegulationRefLangDetailed() ;

        regulationRefLangDetailed.setId(regulationRefLang.getId());
        regulationRefLangDetailed.setCode(regulationRefLang.getCode()!=null ?regulationRefLang.getCode():"");
        regulationRefLangDetailed.setCreatedBy(regulationRefLang.getCreatedBy());
        regulationRefLangDetailed.setCreatedOn(regulationRefLang.getCreatedOn());
        regulationRefLangDetailed.setUpdatedBy(regulationRefLang.getUpdatedBy());
        regulationRefLangDetailed.setUpdatedOn(regulationRefLang.getUpdatedOn());
        regulationRefLangDetailed.setLabel(regulationRefLang.getLabel()!=null ?regulationRefLang.getLabel():"");
        regulationRefLangDetailed.setGeneralDescription(regulationRefLang.getGeneralDescription()!=null ?regulationRefLang.getGeneralDescription():"");

        if (regulationRefLang.getCountryRef() != null && countryRefRepository.findByReference(regulationRefLang.getCountryRef()) != null)
            regulationRefLangDetailed.setCountryRef(
                    iCountryRefService.findCountry(countryRefRepository.findByReference(regulationRefLang.getCountryRef()).getId(), regulationRefLang.getLang()));


        regulationRefLangDetailed.setLang(regulationRefLang.getLang());

        return regulationRefLangDetailed ;
    }

    public TaxationRefLangDetailed toDetailedTaxation(TaxationRefLang taxationRefLang){
        TaxationRefLangDetailed taxationRefLangDetailed = new TaxationRefLangDetailed();

        taxationRefLangDetailed.setId(taxationRefLang.getId());
        taxationRefLangDetailed.setReference(taxationRefLang.getReference()!=null ?taxationRefLang.getReference():"");
        taxationRefLangDetailed.setRate(taxationRefLang.getRate()!=null ?taxationRefLang.getRate():"");
        taxationRefLangDetailed.setValue(taxationRefLang.getValue()!=null ?taxationRefLang.getValue():"");
        taxationRefLangDetailed.setGeneralDescription(taxationRefLang.getGeneralDescription());
        taxationRefLangDetailed.setTaxRef(taxationRefLang.getTaxRef()!=null ?taxationRefLang.getTaxRef():"");

        taxationRefLangDetailed.setCreatedBy(taxationRefLang.getCreatedBy());
        taxationRefLangDetailed.setCreatedOn(taxationRefLang.getCreatedOn());
        taxationRefLangDetailed.setUpdatedBy(taxationRefLang.getUpdatedBy());
        taxationRefLangDetailed.setUpdatedOn(taxationRefLang.getUpdatedOn());
        taxationRefLangDetailed.setLabel(taxationRefLang.getLabel()!=null ?taxationRefLang.getLabel():"");
        taxationRefLangDetailed.setGeneralDescription(taxationRefLang.getGeneralDescription()!=null ?taxationRefLang.getGeneralDescription():"");

        if (taxationRefLang.getCountryRef() != null && countryRefRepository.findByReference(taxationRefLang.getCountryRef()) != null)
            taxationRefLangDetailed.setCountryRef(
                    iCountryRefService.findCountry(countryRefRepository.findByReference(taxationRefLang.getCountryRef()).getId(), taxationRefLang.getLang()));

        if (taxationRefLang.getCustomsRegimRef() != null && customsRegimRefRepository.findByCode(taxationRefLang.getCustomsRegimRef()) != null)
            taxationRefLangDetailed.setCustomsRegimRef(
                    iCustomsRegimRefService.findCustomsRegimRef(customsRegimRefRepository.findByCode(taxationRefLang.getCustomsRegimRef()).getId(), taxationRefLang.getLang()));


        if (taxationRefLang.getUnitRef() != null && unitRefRepository.findByCode(taxationRefLang.getUnitRef()) != null)
            taxationRefLangDetailed.setUnitRef(
                    iUnitRefService.findUnitRef(unitRefRepository.findByCode(taxationRefLang.getUnitRef()).getId(), taxationRefLang.getLang()));

        taxationRefLangDetailed.setLang(taxationRefLang.getLang());

        return taxationRefLangDetailed ;
    }

    public TechnicalBarrierRefLangDetailed toDetailedTechnicalBarrier(TechnicalBarrierRefLang technicalBarrierRefLang){
        TechnicalBarrierRefLangDetailed technicalBarrierRefLangDetailed = new TechnicalBarrierRefLangDetailed() ;

        technicalBarrierRefLangDetailed.setId(technicalBarrierRefLang.getId());
        technicalBarrierRefLangDetailed.setCode(technicalBarrierRefLang.getCode()!=null ?technicalBarrierRefLang.getCode():"");
        technicalBarrierRefLangDetailed.setOrganization(technicalBarrierRefLang.getOrganization()!=null ?technicalBarrierRefLang.getOrganization():"");
        technicalBarrierRefLangDetailed.setGeneralDescription(technicalBarrierRefLang.getGeneralDescription());



        technicalBarrierRefLangDetailed.setCreatedBy(technicalBarrierRefLang.getCreatedBy());
        technicalBarrierRefLangDetailed.setCreatedOn(technicalBarrierRefLang.getCreatedOn());
        technicalBarrierRefLangDetailed.setUpdatedBy(technicalBarrierRefLang.getUpdatedBy());
        technicalBarrierRefLangDetailed.setUpdatedOn(technicalBarrierRefLang.getUpdatedOn());
        technicalBarrierRefLangDetailed.setLabel(technicalBarrierRefLang.getLabel()!=null ?technicalBarrierRefLang.getLabel():"");
        technicalBarrierRefLangDetailed.setGeneralDescription(technicalBarrierRefLang.getGeneralDescription()!=null ?technicalBarrierRefLang.getGeneralDescription():"");

        if (technicalBarrierRefLang.getCountryRef() != null && countryRefRepository.findByReference(technicalBarrierRefLang.getCountryRef()) != null)
            technicalBarrierRefLangDetailed.setCountryRef(
                    iCountryRefService.findCountry(countryRefRepository.findByReference(technicalBarrierRefLang.getCountryRef()).getId(), technicalBarrierRefLang.getLang()));

        if (technicalBarrierRefLang.getCustomsRegimRef() != null && customsRegimRefRepository.findByCode(technicalBarrierRefLang.getCustomsRegimRef()) != null)
            technicalBarrierRefLangDetailed.setCustomsRegimRef(
                    iCustomsRegimRefService.findCustomsRegimRef(customsRegimRefRepository.findByCode(technicalBarrierRefLang.getCustomsRegimRef()).getId(), technicalBarrierRefLang.getLang()));


        technicalBarrierRefLangDetailed.setLang(technicalBarrierRefLang.getLang());

        return technicalBarrierRefLangDetailed ;
    }
}
