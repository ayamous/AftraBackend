package ma.itroad.aace.eth.coref.service.impl;

import edu.emory.mathcs.backport.java.util.Arrays;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.exception.ItemNotFoundException;
import ma.itroad.aace.eth.coref.model.bean.AgreementBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.enums.AgreementStatus;
import ma.itroad.aace.eth.coref.model.enums.RegimType;
import ma.itroad.aace.eth.coref.model.mapper.AgreementMapper;
import ma.itroad.aace.eth.coref.model.mapper.CountryGroupRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.CountryRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.AgreementLangProjection;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.TradeAgreementFilterPayload;
import ma.itroad.aace.eth.coref.repository.*;
import ma.itroad.aace.eth.coref.service.IAgreementService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.service.helper.*;
import ma.itroad.aace.eth.coref.service.helper.detailed.AgreementLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.mappers.HelperMapper;
import ma.itroad.aace.eth.coref.service.util.Util;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AgreementServiceImpl extends BaseServiceImpl<Agreement, AgreementBean> implements IAgreementService {
    @Autowired
    AgreementRepository agreementRepository;

    @Autowired
    AgreementTypeRepository agreementTypeRepository;

    @Autowired
    AgreementMapper agreementMapper;

    @Autowired
    CountryGroupRefRepository countryGroupRefRepository;

    @Autowired
    CountryRefRepository countryRefRepository;

    @Autowired
    private Validator validator;

    @Autowired
    private CountryRefMapper countryRefMapper;

    @Autowired
    private CountryGroupRefMapper countryGroupRefMapper;

    @Autowired
    LangRepository langRepository;

    @Autowired
    EntityRefLangRepository entityRefLangRepository;

    @Autowired
    private InternationalizationHelper internationalizationHelper;

    @Autowired
    private HelperMapper helperMapper ;

    static String[] HEADERs = {"CODE AGREEMENT", "TITRE DE L'ACCORD", "DATE OF AGREMENT", "AGREMENT STATUS","DESCRIPTION DE L'ACCORD", "AGREMENT TYPE", "REFERENCE GROUPEMENT", "REFERENCE PAYS", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "Agreements";

    @Override
    public List<Agreement> saveAll(List<Agreement> agreements) {
        if (!agreements.isEmpty()) {
            return agreementRepository.saveAll(agreements);
        }
        return null;
    }

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.AGREEMENT, id);
            for (EntityRefLang entityRefLang : entityRefLangs) {
                entityRefLangRepository.delete(entityRefLang);
            }
            agreementRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return response;
    }

    @Override
    public ErrorResponse deleteList(ListOfObject listOfObject) {
        ErrorResponse response = new ErrorResponse();

        try {
            for(Long id : listOfObject.getListOfObject()){
                countryRefRepository.deleteById(id);
            }

            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return response;
    }

    @Override
    public ByteArrayInputStream agreementsToExcel(List<AgreementLang> agreementLangs) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET);
            // Header
            Row headerRow = sheet.createRow(0);
            if (HEADERs.length > 0) {
                for (int col = 0; col < HEADERs.length; col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(HEADERs[col]);
                }
                int rowIdx = 1;
                if (!agreementLangs.isEmpty()) {

                    for (AgreementLang agreementLang : agreementLangs) {
                        Row row = sheet.createRow(rowIdx++);

                        row.createCell(0).setCellValue(agreementLang.getCode());
                        row.createCell(1).setCellValue(agreementLang.getTitle());
                        row.createCell(2).setCellValue(agreementLang.getDateOfAgreement().toString()!= null ?agreementLang.getDateOfAgreement().toString():"");
                        row.createCell(3).setCellValue(agreementLang.getAgreementStatus().toString());
                        row.createCell(4).setCellValue(agreementLang.getDescription());

                        row.createCell(5).setCellValue(agreementLang.getAgreementType());
                        row.createCell(6).setCellValue(agreementLang.getCountryGroupRef());
                        row.createCell(7).setCellValue(agreementLang.getCountryRef());

                        row.createCell(8).setCellValue(agreementLang.getLabel());
                        row.createCell(9).setCellValue(agreementLang.getGeneralDescription());
                        row.createCell(10).setCellValue(agreementLang.getLang());
                    }
                }
                workbook.write(out);
                return new ByteArrayInputStream(out.toByteArray());
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }


    @Override
    public List<AgreementLang> excelToAgreements(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            List<AgreementLang> agreementLangs = new ArrayList<AgreementLang>();
            Row currentRow;
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                currentRow= sheet.getRow(rowNum);
                AgreementLang agreementLang = new AgreementLang();
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            agreementLang.setCode(Util.cellValue(currentCell));
                            break;
                        case 1:
                            agreementLang.setTitle(Util.cellValue(currentCell));
                            break;
                        case 2:
                            agreementLang.setDateOfAgreement(Util.cellValue(currentCell)!=""?LocalDate.parse(Util.cellValue(currentCell)):null);
                            break;
                        case 3:
                             agreementLang.setAgreementStatus(AgreementStatus.valueOf(Util.cellValue(currentCell)));
                            break;
                        case 4:
                            agreementLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 5:
                            agreementLang.setAgreementType(Util.cellValue(currentCell));
                            break;
                        case 6:
                            agreementLang.setCountryGroupRef(Util.cellValue(currentCell));
                            break;
                        case 7:
                            agreementLang.setCountryRef(Util.cellValue(currentCell));
                            break;
                        case 8:
                            agreementLang.setLabel(Util.cellValue(currentCell));
                        case 9:
                            agreementLang.setGeneralDescription(Util.cellValue(currentCell));
                        case 10:
                            agreementLang.setLang(Util.cellValue(currentCell));
                            break;
                        default:
                            break;
                    }
                }
                agreementLangs.add(agreementLang);
            }
            workbook.close();
            return agreementLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream load() {
        List<Agreement> agreements = agreementRepository.findAll();
        ByteArrayInputStream in = null;
        return in;
    }


    @Override
    public ByteArrayInputStream load(String codeLang) {

        Lang lang = langRepository.findByCode(codeLang);
        List<Agreement> agreements = agreementRepository.findAll();
        List<AgreementLang> agreementLangs = new ArrayList<>();
        return agreementsToExcel(
                agreements.stream().map(agreement -> {
                    EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, lang.getId(), agreement.getId());

                        AgreementLang agreementLang = new AgreementLang();
                        agreementLang.setCode(agreement.getCode()!=null?agreement.getCode():"");
                        agreementLang.setTitle(agreement.getTitle()!=null?agreement.getTitle():"");
                        agreementLang.setDateOfAgreement(agreement.getDateOfAgreement());
                        agreementLang.setAgreementStatus(agreement.getAgreementStatus());
                        agreementLang.setDescription(agreement.getDescription()!=null?agreement.getDescription():"");

                        agreementLang.setAgreementType(agreement.getAgreementType()!=null?agreement.getAgreementType().getCode():"");
                        agreementLang.setCountryGroupRef(agreement.getCountryGroupRef()!=null?agreement.getCountryGroupRef().getReference():"");
                        agreementLang.setCountryRef(agreement.getCountryRef()!=null?agreement.getCountryRef().getReference():"");

                        agreementLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
                        agreementLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");
                        agreementLang.setLang(codeLang);


                    return agreementLang;
                }).collect(Collectors.toList()));
        }

    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            List<AgreementLang> agreementLangs = excelToAgreements(file.getInputStream());
            if (!agreementLangs.isEmpty()) {
                for (AgreementLang l : agreementLangs) {

                    Agreement agreement = new Agreement();
                    agreement.setCode(l.getCode());
                    agreement.setTitle(l.getTitle());
                    agreement.setDateOfAgreement(l.getDateOfAgreement());
                    agreement.setAgreementStatus(l.getAgreementStatus());
                    agreement.setDescription(l.getDescription());
                    agreement.setAgreementType(agreementTypeRepository.findByCode(l.getAgreementType()));
                    agreement.setCountryGroupRef(countryGroupRefRepository.findByReference(l.getCountryGroupRef()));
                    agreement.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
                    Agreement ltemp = agreementRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (agreementRepository.save(agreement)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setGeneralDescription(l.getGeneralDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.AGREEMENT);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        Long id = ltemp.getId();
                        agreement.setId(id);
                        agreementRepository.save(agreement);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT,langRepository.findByCode(l.getLang()).getId(),id);
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.AGREEMENT);
                            entityRefLang.setRefId(agreement.getId());
                        }
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setGeneralDescription(l.getGeneralDescription());
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    }

                }
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    public Page<AgreementBean> getAll(Pageable pageable) {
        Page<Agreement> entities = agreementRepository.findAll(pageable);
        Page<AgreementBean> result = entities.map(agreementMapper::entityToBean);
        return result;

    }


    @Override
    public void addAgreement(AgreementLang agreementLang){
        Agreement agreement = new Agreement();

        agreement.setCode(agreementLang.getCode());
        agreement.setTitle(agreementLang.getTitle());
        agreement.setDateOfAgreement(agreementLang.getDateOfAgreement());
        agreement.setAgreementStatus(agreementLang.getAgreementStatus());
        agreement.setDescription(agreementLang.getDescription());
        agreement.setAgreementType(agreementTypeRepository.findByCode(agreementLang.getAgreementType()));
        agreement.setCountryGroupRef(countryGroupRefRepository.findByReference(agreementLang.getCountryGroupRef()));
        agreement.setCountryRef(countryRefRepository.findByReference(agreementLang.getCountryRef()));

        Agreement ltemp = agreementRepository.findByCode(agreementLang.getCode());

        if (ltemp == null) {
            Long id = (agreementRepository.save(agreement)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();

            entityRefLang.setLabel(agreementLang.getLabel());
            entityRefLang.setGeneralDescription(agreementLang.getGeneralDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.AGREEMENT);
            entityRefLang.setLang(langRepository.findByCode(agreementLang.getLang()));
            entityRefLangRepository.save(entityRefLang);

        } else {
            Long id = ltemp.getId();
            agreement.setId(id);
            agreementRepository.save(agreement);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, langRepository.findByCode(agreementLang.getLang()).getId() ,id);
            entityRefLang.setLabel(agreementLang.getLabel());
            entityRefLang.setGeneralDescription(agreementLang.getGeneralDescription());
            entityRefLang.setLang(  langRepository.findByCode(agreementLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.AGREEMENT);
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
        entityRefLang.setTableRef(TableRef.AGREEMENT);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }






    public Page<AgreementLang> filterForPortal(ProductInformationFinderFilterPayload productInformationFinderFilterPayload, int page, int size, String codeLang, String orderDirection) {

            List<String> countryReferences = Arrays
                    .asList(new String[] { productInformationFinderFilterPayload.getExportCountryCode(),
                            productInformationFinderFilterPayload.getImportCountryCode() });

            List<CountryRef> countries = countryReferences.stream().filter(cr -> cr != null)
                    .map(cr -> countryRefRepository.findByReference(cr)).collect(Collectors.toList());

            Lang lang = langRepository.findByCode(codeLang);
            List<AgreementLang> agreementLangs = new ArrayList<>();
            Page<Agreement> agreements = agreementRepository.findByFilter(productInformationFinderFilterPayload.getTarifBookReference(),countryReferences, PageRequest.of(page, size));

            if (productInformationFinderFilterPayload.getTarifBookReference() != null && !productInformationFinderFilterPayload.getTarifBookReference().equals("")){
                agreements =  agreementRepository
                        .findByFilter(productInformationFinderFilterPayload.getTarifBookReference(),countryReferences, PageRequest.of(page, size));

            }
            else if (productInformationFinderFilterPayload.getExportCountryCode().equals("") && productInformationFinderFilterPayload.getImportCountryCode().equals("")){
                return getAll(page, size, codeLang, orderDirection);
            }
            else {
                agreements = agreementRepository.findByCountryRefInList(countries, PageRequest.of(page, size));
            }
        return agreements.map(agreement -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, lang.getId(), agreement.getId());

            AgreementLang agreementLang = new AgreementLang();

            agreementLang.setId(agreement.getId());
            agreementLang.setCode(agreement.getCode()!=null ?agreement.getCode():"");
            agreementLang.setTitle(agreement.getTitle()!=null ?agreement.getTitle():"");
            agreementLang.setDescription(agreement.getDescription()!=null ?agreement.getDescription():"");

            agreementLang.setAgreementType(agreement.getAgreementType()!=null ?agreement.getAgreementType() .getCode() :"");
            agreementLang.setCountryGroupRef(agreement.getCountryGroupRef()!=null?agreement.getCountryGroupRef() .getReference() : "");
            agreementLang.setCountryRef(agreement.getCountryRef()!=null?agreement.getCountryRef().getReference():"");

            agreementLang.setCreatedBy(agreement.getCreatedBy());
            agreementLang.setCreatedOn(agreement.getCreatedOn());
            agreementLang.setUpdatedBy(agreement.getUpdatedBy());
            agreementLang.setUpdatedOn(agreement.getUpdatedOn());

            agreementLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            agreementLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");
            agreementLang.setLang(codeLang);

            agreementLangs.add(agreementLang);

            return  agreementLang ;
        });
    }

   @Override
   public Page<AgreementLang> subPortalProductInformationFinderfilter(final int page, final int size, SubportalProductInformationFinderFilterPayload payload, String codeLang, String orderDirection) {
        Page<Agreement> agreements = null;
        if(!payload.getCountryRefCode().equals("")) {
            agreements = agreementRepository.findAgreementByCountryRef_Reference(payload.getCountryRefCode(), PageRequest.of(page, size));
        }
        else
        {
            return getAll(page, size, codeLang, orderDirection);
        }

       Lang lang = langRepository.findByCode(codeLang);
       List<AgreementLang> agreementLangs = new ArrayList<>();

       return agreements.map(agreement -> {
           EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, lang.getId(), agreement.getId());

           AgreementLang agreementLang = new AgreementLang();

           agreementLang.setId(agreement.getId());
           agreementLang.setCode(agreement.getCode()!=null ?agreement.getCode():"");
           agreementLang.setTitle(agreement.getTitle()!=null ?agreement.getTitle():"");
           agreementLang.setDescription(agreement.getDescription()!=null ?agreement.getDescription():"");

           agreementLang.setAgreementType(agreement.getAgreementType()!=null ?agreement.getAgreementType() .getCode() :"");
           agreementLang.setCountryGroupRef(agreement.getCountryGroupRef()!=null?agreement.getCountryGroupRef() .getReference() : "");
           agreementLang.setCountryRef(agreement.getCountryRef()!=null?agreement.getCountryRef().getReference():"");

           agreementLang.setCreatedBy(agreement.getCreatedBy());
           agreementLang.setCreatedOn(agreement.getCreatedOn());
           agreementLang.setUpdatedBy(agreement.getUpdatedBy());
           agreementLang.setUpdatedOn(agreement.getUpdatedOn());

           agreementLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
           agreementLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");
           agreementLang.setLang(codeLang);

           agreementLangs.add(agreementLang);

           return  agreementLang ;
       });
   }

    public Page<AgreementLang> tradeAgreementFilter(TradeAgreementFilterPayload tradeAgreementFilterPayload, int page, int size, String codeLang) {
        if (tradeAgreementFilterPayload.getCountryRefCode() == "") tradeAgreementFilterPayload.setCountryRefCode(null);
        if (tradeAgreementFilterPayload.getAgreementType() == "") tradeAgreementFilterPayload.setAgreementType(null);
        Page<Agreement> agreements = agreementRepository.findByFilter(
                tradeAgreementFilterPayload.getCode(),
                tradeAgreementFilterPayload.getCountryRefCode(),
                tradeAgreementFilterPayload.getAgreementType(),
                tradeAgreementFilterPayload.getAgreementStatus(),
                tradeAgreementFilterPayload.getDateOfAgreement(),
                PageRequest.of(page, size)
        );
        Lang lang = langRepository.findByCode(codeLang);
        List<AgreementLang> agreementLangs = new ArrayList<>();

        return agreements.map(agreement -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, lang.getId(), agreement.getId());

            AgreementLang agreementLang = new AgreementLang();

            agreementLang.setId(agreement.getId());
            agreementLang.setCode(agreement.getCode()!=null ?agreement.getCode():"");
            agreementLang.setTitle(agreement.getTitle()!=null ?agreement.getTitle():"");
            agreementLang.setDescription(agreement.getDescription()!=null ?agreement.getDescription():"");

            agreementLang.setAgreementType(agreement.getAgreementType()!=null ?agreement.getAgreementType() .getCode() :null);
            agreementLang.setCountryGroupRef(agreement.getCountryGroupRef()!=null?agreement.getCountryGroupRef() .getReference() : null);
            agreementLang.setCountryRef(agreement.getCountryRef()!=null?agreement.getCountryRef().getReference():null);

            agreementLang.setCreatedBy(agreement.getCreatedBy());
            agreementLang.setCreatedOn(agreement.getCreatedOn());
            agreementLang.setUpdatedBy(agreement.getUpdatedBy());
            agreementLang.setUpdatedOn(agreement.getUpdatedOn());

            agreementLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            agreementLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");
            agreementLang.setLang(codeLang);

            agreementLangs.add(agreementLang);

            return  agreementLang ;
        });
    }

    @Override
    public Page<AgreementLangProjection> tradeAgreementFilterProjection(TradeAgreementFilterPayload tradeAgreementFilterPayload, int page, int size, String lang) {


        return agreementRepository.findByFilterProjection(tradeAgreementFilterPayload.getCode(),
                tradeAgreementFilterPayload.getCountryRefCode(),tradeAgreementFilterPayload.getAgreementType(),
                tradeAgreementFilterPayload.getAgreementStatus().ordinal(),tradeAgreementFilterPayload.getDateOfAgreement(),lang,PageRequest.of(page,size));
    }


    @Override
    public ErrorResponse deleteInternationalisation(String codeLang, Long agreementId) {

        ErrorResponse response = new ErrorResponse();
        try {

            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, lang.getId(), agreementId);

            entityRefLangRepository.delete(entityRefLang);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");


        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return response;
    }

    @Override
    public AgreementLang findAgreement(Long id, String lang){
        AgreementLang agreementLang = new AgreementLang();
        Agreement agreement = agreementRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT,langRepository.findByCode(lang).getId(),agreement.getId());
        agreementLang.setCode(agreement.getCode());
        agreementLang.setTitle(agreement.getTitle());
        agreementLang.setDateOfAgreement(agreement.getDateOfAgreement());
        agreementLang.setAgreementStatus(agreement.getAgreementStatus());
        agreementLang.setDescription(agreement.getDescription());
        agreementLang.setAgreementType(agreement.getAgreementType()!=null?agreement.getAgreementType().getCode():null);
        agreementLang.setCountryGroupRef(agreement.getCountryGroupRef()!=null?agreement.getCountryGroupRef().getReference():null);
        agreementLang.setCountryRef(agreement.getCountryRef()!=null?agreement.getCountryRef().getReference():null);
        agreementLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        agreementLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");

        agreementLang.setLang(lang);
        return agreementLang;
    }

    public AgreementLangDetailed findAgreementDetailed(AgreementLang agreementLang ){
        return  helperMapper.toDetailedAgreement(agreementLang);
    }


    public Page<AgreementLang> mapCityRefsToRefLangs(Page<Agreement> agreements, String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<AgreementLang> agreementLangs = new ArrayList<>();

        return agreements.map(agreement -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, lang.getId(), agreement.getId());

            AgreementLang agreementLang = new AgreementLang();

            agreementLang.setId(agreement.getId());
            agreementLang.setCode(agreement.getCode()!=null ?agreement.getCode():"");
            agreementLang.setTitle(agreement.getTitle()!=null ?agreement.getTitle():"");
            agreementLang.setDateOfAgreement(agreement.getDateOfAgreement());
            agreementLang.setAgreementStatus(agreement.getAgreementStatus());
            agreementLang.setDescription(agreement.getDescription()!=null ?agreement.getDescription():"");


            agreementLang.setAgreementType(agreement.getAgreementType()!=null ?agreement.getAgreementType() .getCode() :null);
            agreementLang.setCountryGroupRef(agreement.getCountryGroupRef()!=null?agreement.getCountryGroupRef() .getReference() : null);
            agreementLang.setCountryRef(agreement.getCountryRef()!=null?agreement.getCountryRef().getReference():null);

            agreementLang.setCreatedBy(agreement.getCreatedBy());
            agreementLang.setCreatedOn(agreement.getCreatedOn());
            agreementLang.setUpdatedBy(agreement.getUpdatedBy());
            agreementLang.setUpdatedOn(agreement.getUpdatedOn());

            agreementLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            agreementLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");

            agreementLang.setLang(codeLang);

            agreementLangs.add(agreementLang);

            return  agreementLang ;
        });
    }


    public Page<AgreementLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<Agreement> agreementLang = null;
        if(orderDirection.equals("DESC")){
            agreementLang = agreementRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            agreementLang = agreementRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }

        return mapCityRefsToRefLangs(agreementLang,codeLang);
    }

    public Page<AgreementLangProjection>  filterByCodeOrLabel(String value, Pageable pageable, String codeLang)  {
//             return mapCityRefsToRefLangs(agreementRepository.filterByCodeOrLabel(value ,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
        return  agreementRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(codeLang).getId(),pageable);
    }

    @Override
    public AgreementBean findById(Long id) {
        Agreement agreement= agreementRepository.findById(id).orElseThrow(
                ()->new ItemNotFoundException("id not found"));
        return agreementMapper.entityToBean(agreement);
    }

    public Set<ConstraintViolation<AgreementLang>> validateItems(AgreementLang agreementLang) {
        Set<ConstraintViolation<AgreementLang>> violations = validator.validate(agreementLang);
        return violations;
    }

    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<AgreementLang> itemsList = excelToAgreements(file.getInputStream());
            List<AgreementLang> invalidItems = new ArrayList<AgreementLang>();
            List<AgreementLang> validItems = new ArrayList<AgreementLang>();

            int lenght = itemsList.size();

            for (int i = 0; i < lenght; i++) {

                Set<ConstraintViolation<AgreementLang>> violations = validateItems(itemsList.get(i));
                if (violations.isEmpty())

                {
                    validItems.add(itemsList.get(i));
                } else {
                    invalidItems.add(itemsList.get(i));
                }
            }

            if (!invalidItems.isEmpty()) {

                ByteArrayInputStream out = agreementsToExcel(invalidItems);
                xls = new InputStreamResource(out);

            }

            if (!validItems.isEmpty()) {
                for (AgreementLang l : validItems) {

                    Agreement agreement = new Agreement();
                    agreement.setCode(l.getCode());
                    agreement.setTitle(l.getTitle());
                    agreement.setDateOfAgreement(l.getDateOfAgreement());
                    agreement.setAgreementStatus(l.getAgreementStatus());
                    agreement.setDescription(l.getDescription());
                    agreement.setAgreementType(agreementTypeRepository.findByCode(l.getAgreementType()));
                    agreement.setCountryGroupRef(countryGroupRefRepository.findByReference(l.getCountryGroupRef()));
                    agreement.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
                    Agreement ltemp = agreementRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (agreementRepository.save(agreement)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setGeneralDescription(l.getGeneralDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.AGREEMENT);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        Long id = ltemp.getId();
                        agreement.setId(id);
                        agreementRepository.save(agreement);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT,langRepository.findByCode(l.getLang()).getId(),id);
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.AGREEMENT);
                            entityRefLang.setRefId(agreement.getId());
                        }
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setGeneralDescription(l.getGeneralDescription());
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    }

                }

            }

            if (!invalidItems.isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(
                                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(xls);

            if (!validItems.isEmpty())
                return ResponseEntity.status(HttpStatus.OK).body(null);

            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (IOException e) {

            return ResponseEntity.status(HttpStatus.OK).body("fail to store excel data: " + e.getMessage());
        }
    }
}
