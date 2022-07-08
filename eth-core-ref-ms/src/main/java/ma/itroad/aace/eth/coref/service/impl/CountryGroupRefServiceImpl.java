package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CountryGroupRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.CountryGroupRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.CountryGroupRefsEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.CountryGroupRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.service.ICountryGroupRefService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.service.helper.CountryGroupRefsEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.util.Util;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CountryGroupRefServiceImpl extends BaseServiceImpl<CountryGroupRef, CountryGroupRefBean> implements ICountryGroupRefService {

    static String[] HEADERs = {"CODE", "REFERENCE", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "CountryGroupRefsSHEET";

    @Autowired
    private CountryGroupRefRepository countryGroupRefRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    LangRepository langRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;

    @Autowired
    CountryGroupRefMapper countryGroupRefMapper;

    @Autowired
    private Validator validator;

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.COUNTRY_GROUP_REF, id);
            for(EntityRefLang entityRefLang:entityRefLangs ){
                entityRefLangRepository.delete(entityRefLang);
            }
            countryGroupRefRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return  response;
    }
    @Override
    public ErrorResponse deleteList(ListOfObject listOfObject) {
        ErrorResponse response = new ErrorResponse();

        try {
            for(Long id : listOfObject.getListOfObject()){
                countryGroupRefRepository.deleteById(id);
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
    public ErrorResponse deleteInternationalisation(String codeLang, Long countryGroupRefId) {
        ErrorResponse response = new ErrorResponse();
        try {
            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_GROUP_REF, lang.getId(), countryGroupRefId);

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
    public ByteArrayInputStream countryGroupRefsToExcel(List<CountryGroupRefsEntityRefLang> countryGroupRefsEntityRefLangs) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET);
            // Header
            Row headerRow = sheet.createRow(0);
            if(HEADERs.length>0){
                for (int col = 0; col < HEADERs.length; col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(HEADERs[col]);
                }
                int rowIdx = 1;
                if(!countryGroupRefsEntityRefLangs.isEmpty()){

                    for (CountryGroupRefsEntityRefLang unitRefEntityRefLang : countryGroupRefsEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(unitRefEntityRefLang.getCode());
                        row.createCell(1).setCellValue(unitRefEntityRefLang.getReference());
                        row.createCell(2).setCellValue(unitRefEntityRefLang.getLabel());
                        row.createCell(3).setCellValue(unitRefEntityRefLang.getDescription());
                        row.createCell(4).setCellValue(unitRefEntityRefLang.getLang());
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
    public Set<ConstraintViolation<CountryGroupRefsEntityRefLang>> validateCountryGroupRef(CountryGroupRefsEntityRefLang countryGroupRefsEntityRefLang) {
        Set<ConstraintViolation<CountryGroupRefsEntityRefLang>> violations = validator.validate(countryGroupRefsEntityRefLang);
        return violations;
    }

    @Override
    public List<CountryGroupRefsEntityRefLang> excelToCountryGroupRefsRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            List<CountryGroupRefsEntityRefLang> countryGroupRefsEntityRefLangs = new ArrayList<CountryGroupRefsEntityRefLang>();
            Row currentRow;
            CountryGroupRefsEntityRefLang countryGroupRefsEntityRefLang ;
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                currentRow= sheet.getRow(rowNum);
                countryGroupRefsEntityRefLang = new CountryGroupRefsEntityRefLang();
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            countryGroupRefsEntityRefLang.setCode(Util.cellValue(currentCell));
                            break;
                        case 1:
                            countryGroupRefsEntityRefLang.setReference(Util.cellValue(currentCell));
                            break;
                        case 2:
                            countryGroupRefsEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 3:
                            countryGroupRefsEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 4:
                            countryGroupRefsEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;
                        default:
                            break;
                    }
                }
                countryGroupRefsEntityRefLangs.add(countryGroupRefsEntityRefLang);
            }
            workbook.close();
            return countryGroupRefsEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<CountryGroupRefsEntityRefLang> countryGroupRefsEntityRefLangs = excelToCountryGroupRefsRef(file.getInputStream());
            List<CountryGroupRefsEntityRefLang> InvalidCountryGroupRefsEntityRefLangs = new ArrayList<CountryGroupRefsEntityRefLang>();
            List<CountryGroupRefsEntityRefLang> ValidCountryGroupRefsEntityRefLangs = new ArrayList<CountryGroupRefsEntityRefLang>();

            int lenght = countryGroupRefsEntityRefLangs.size();
            for (int i = 0; i < lenght; i++) {
                Set<ConstraintViolation<CountryGroupRefsEntityRefLang>> violations = validateCountryGroupRef(countryGroupRefsEntityRefLangs.get(i));
                if (violations.isEmpty())
                {
                    ValidCountryGroupRefsEntityRefLangs.add(countryGroupRefsEntityRefLangs.get(i));
                } else {
                    InvalidCountryGroupRefsEntityRefLangs.add(countryGroupRefsEntityRefLangs.get(i));
                }
            }
            if (!InvalidCountryGroupRefsEntityRefLangs.isEmpty()) {

                ByteArrayInputStream out = countryGroupRefsToExcel(InvalidCountryGroupRefsEntityRefLangs);
                xls = new InputStreamResource(out);

            }

            if (!ValidCountryGroupRefsEntityRefLangs.isEmpty()) {
                for (CountryGroupRefsEntityRefLang l : ValidCountryGroupRefsEntityRefLangs) {
                    CountryGroupRef countryGroupRef = new CountryGroupRef();
                    countryGroupRef.setCode(l.getCode());
                    countryGroupRef.setReference(l.getReference());

                    CountryGroupRef ltemp = countryGroupRefRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (countryGroupRefRepository.save(countryGroupRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.COUNTRY_GROUP_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        countryGroupRef.setId( ltemp.getId());
                        countryGroupRefRepository.save(countryGroupRef);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_GROUP_REF,langRepository.findByCode(l.getLang()).getId(),countryGroupRef.getId());
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.COUNTRY_GROUP_REF);
                            entityRefLang.setRefId(countryGroupRef.getId());
                        }
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLangRepository.save(entityRefLang);
                    }

                }
            }
            if (!InvalidCountryGroupRefsEntityRefLangs.isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(
                                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(xls);

            if (!ValidCountryGroupRefsEntityRefLangs.isEmpty())
                return ResponseEntity.status(HttpStatus.OK).body(null);

            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    @Override
    public void saveFromExcel(MultipartFile file) {
        // TODO Auto-generated method stub

    }

    @Override
    public ByteArrayInputStream load() {
        List<CountryGroupRef> countryGroupRefs = countryGroupRefRepository.findAll();
        ByteArrayInputStream in = null;
              //  countryGroupRefsToExcel(countryGroupRefs);
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<CountryGroupRef> countryGroupRef = countryGroupRefRepository.findAll();
        List<CountryGroupRefsEntityRefLang> countryGroupRefsEntityRefLangs = new ArrayList<CountryGroupRefsEntityRefLang>();

        for(CountryGroupRef u : countryGroupRef) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_GROUP_REF, lang.getId(), u.getId());
            if (entityRefLang!=null) {
                CountryGroupRefsEntityRefLang countryGroupRefsEntityRefLang = new CountryGroupRefsEntityRefLang();
                countryGroupRefsEntityRefLang.setCode(u.getCode());
                countryGroupRefsEntityRefLang.setReference(u.getReference());
                countryGroupRefsEntityRefLang.setLabel(entityRefLang.getLabel());
                countryGroupRefsEntityRefLang.setDescription(entityRefLang.getDescription());
                countryGroupRefsEntityRefLang.setLang(entityRefLang.getLang().getCode());
                countryGroupRefsEntityRefLangs.add(countryGroupRefsEntityRefLang);
            }
        }
        ByteArrayInputStream in = countryGroupRefsToExcel(countryGroupRefsEntityRefLangs);
        return in;
    }

    @Override
    public void addCountryGroup(CountryGroupRefsEntityRefLang countryGroupRefsEntityRefLang){
        CountryGroupRef countryGroupRef = new CountryGroupRef();
        countryGroupRef.setCode(countryGroupRefsEntityRefLang.getCode());
        countryGroupRef.setReference(countryGroupRefsEntityRefLang.getReference());
        CountryGroupRef ltemp = countryGroupRefRepository.findByCode(countryGroupRefsEntityRefLang.getCode());
        if (ltemp == null) {
            Long id = (countryGroupRefRepository.save(countryGroupRef)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(countryGroupRefsEntityRefLang.getLabel());
            entityRefLang.setDescription(countryGroupRefsEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.COUNTRY_GROUP_REF);
            entityRefLang.setLang(  langRepository.findByCode(countryGroupRefsEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            countryGroupRef.setId(id);
            countryGroupRefRepository.save(countryGroupRef);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_GROUP_REF, langRepository.findByCode(countryGroupRefsEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(countryGroupRefsEntityRefLang.getLabel());
            entityRefLang.setDescription(countryGroupRefsEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }


    @Override
    public CountryGroupRefsEntityRefLang findCountryGroup(Long id, String lang){
        CountryGroupRefsEntityRefLang countryGroupRefsEntityRefLang = new CountryGroupRefsEntityRefLang();
        CountryGroupRef countryRef = countryGroupRefRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_GROUP_REF,langRepository.findByCode(lang).getId(),countryRef.getId());
        countryGroupRefsEntityRefLang.setCode(countryRef.getCode());
        countryGroupRefsEntityRefLang.setReference(countryRef.getReference());
        countryGroupRefsEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        countryGroupRefsEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        countryGroupRefsEntityRefLang.setLang(lang);
        return countryGroupRefsEntityRefLang;
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang,String lang){
        entityRefLang.setTableRef(TableRef.COUNTRY_GROUP_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_GROUP_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.COUNTRY_GROUP_REF);
    }
    @Override
    public List<CountryGroupRef> saveAll(List<CountryGroupRef> countryGroupRefs) {
        if (!countryGroupRefs.isEmpty()) {
            return countryGroupRefRepository.saveAll(countryGroupRefs);
        }
        return null;
    }

    public Page<CountryGroupRefsEntityRefLang> mapCountryGroupRefsToRefLangs( Page<CountryGroupRef> countryGroupRefs, String codeLang ){
        Lang lang = langRepository.findByCode(codeLang);
        List<CountryGroupRefsEntityRefLang> countryGroupRefsEntityRefLangs = new ArrayList<>();
        return countryGroupRefs.map(countryGroupRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_GROUP_REF, lang.getId(), countryGroupRef.getId());

            CountryGroupRefsEntityRefLang countryGroupRefsEntityRefLang = new CountryGroupRefsEntityRefLang();

            countryGroupRefsEntityRefLang.setId(countryGroupRef.getId());
            countryGroupRefsEntityRefLang.setCode(countryGroupRef.getCode()!=null ?countryGroupRef.getCode():"");
            countryGroupRefsEntityRefLang.setReference(countryGroupRef.getReference()!=null ?countryGroupRef.getReference():"");
            countryGroupRefsEntityRefLang.setCreatedBy(countryGroupRef.getCreatedBy());
            countryGroupRefsEntityRefLang.setCreatedOn(countryGroupRef.getCreatedOn());
            countryGroupRefsEntityRefLang.setUpdatedBy(countryGroupRef.getUpdatedBy());
            countryGroupRefsEntityRefLang.setUpdatedOn(countryGroupRef.getUpdatedOn());

            countryGroupRefsEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            countryGroupRefsEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            countryGroupRefsEntityRefLang.setLang(codeLang);
            countryGroupRefsEntityRefLangs.add(countryGroupRefsEntityRefLang);

            return  countryGroupRefsEntityRefLang ;
        });
    }

    public Page<CountryGroupRefsEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<CountryGroupRef> countryGroupRefs = null;
        if(orderDirection.equals("DESC")){
            countryGroupRefs = countryGroupRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            countryGroupRefs = countryGroupRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }

        return  mapCountryGroupRefsToRefLangs(countryGroupRefs, codeLang) ;
    }

    public  Page<CountryGroupRefsEntityRefLang> filterByReferenceOrLabel(String value, Pageable pageable, String codeLang){
        return  mapCountryGroupRefsToRefLangs( countryGroupRefRepository.filterByReferenceOrLabel(value,langRepository.findByCode(codeLang).getId() ,pageable ), codeLang );

    }

    @Override
    public CountryGroupRefBean findById(Long id) {
        CountryGroupRef result= countryGroupRefRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return countryGroupRefMapper.entityToBean(result);
    }

    @Override
    public Page<CountryGroupRefsEntityRefLangProjection> filterByReferenceOrLabelProjection(String value, Pageable pageable, String lang) {
        return countryGroupRefRepository.filterByReferenceOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang),pageable);
    }

}