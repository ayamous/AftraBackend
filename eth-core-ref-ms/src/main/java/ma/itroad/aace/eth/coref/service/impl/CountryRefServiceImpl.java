package ma.itroad.aace.eth.coref.service.impl;

import com.google.inject.internal.cglib.core.$CollectionUtils;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.model.bean.ExtendedProcedureRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.CountryRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.CountryRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.service.ICountryRefService;
import ma.itroad.aace.eth.coref.service.helper.CountryRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.SectionRefEntityRefLang;
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
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class CountryRefServiceImpl extends BaseServiceImpl<CountryRef, CountryRefBean> implements ICountryRefService {

    static String[] HEADERs = {"ISO CODE", "COUNTRY REFERENCE", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "CountryRefsSHEET";

    @Autowired
    private CountryRefRepository countryRefRepository;

    @Autowired
    CountryRefMapper countryRefsMapper;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    LangRepository langRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;

    @Autowired
    private Validator validator;


    @Override
    public List<CountryRef> saveAll(List<CountryRef> countryRefs) {
        if (!countryRefs.isEmpty()) {
            return countryRefRepository.saveAll(countryRefs);
        }
        return null;
    }


    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.COUNTRY_REF, id);
            for (EntityRefLang entityRefLang : entityRefLangs) {
                entityRefLangRepository.delete(entityRefLang);
            }
            countryRefRepository.deleteById(id);
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
    public ErrorResponse deleteInternationalisation(String codeLang, Long countryRefId) {

        ErrorResponse response = new ErrorResponse();
        try {

            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_REF, lang.getId(), countryRefId);

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
    public ByteArrayInputStream countryRefsToExcel(List<CountryRefEntityRefLang> countryRefEntityRefLangs) {
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
                if (!countryRefEntityRefLangs.isEmpty()) {

                    for (CountryRefEntityRefLang countryRefEntityRefLang : countryRefEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(countryRefEntityRefLang.getCodeIso());
                        row.createCell(1).setCellValue(countryRefEntityRefLang.getReference());
                        row.createCell(2).setCellValue(countryRefEntityRefLang.getLabel());
                        row.createCell(3).setCellValue(countryRefEntityRefLang.getDescription());
                        row.createCell(4).setCellValue(countryRefEntityRefLang.getLang());
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
    public Set<ConstraintViolation<CountryRefEntityRefLang>> validateCountryRef(CountryRefEntityRefLang countryRefEntityRefLang) {
        Set<ConstraintViolation<CountryRefEntityRefLang>> violations = validator.validate(countryRefEntityRefLang);
        return violations;
    }


    @Override
    public List<CountryRefEntityRefLang> excelToCountryRefsRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<CountryRefEntityRefLang> countryRefEntityRefLangs = new ArrayList<CountryRefEntityRefLang>();
            Row currentRow = rows.next();
            while (rows.hasNext()) {
                currentRow = rows.next();
                CountryRefEntityRefLang countryRefEntityRefLang = new CountryRefEntityRefLang();
                int cellIdx = 0;
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            countryRefEntityRefLang.setCodeIso(currentCell.getStringCellValue());
                            break;
                        case 1:
                            countryRefEntityRefLang.setReference(currentCell.getStringCellValue());
                            break;
                        case 2:
                            countryRefEntityRefLang.setLabel(currentCell.getStringCellValue());
                            break;
                        case 3:
                            countryRefEntityRefLang.setDescription(currentCell.getStringCellValue());
                            break;
                        case 4:
                            countryRefEntityRefLang.setLang(currentCell.getStringCellValue());
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                countryRefEntityRefLangs.add(countryRefEntityRefLang);
            }
            workbook.close();
            return countryRefEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }



    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<CountryRefEntityRefLang> countryRefEntityRefLangs = excelToCountryRefsRef(file.getInputStream());
            List<CountryRefEntityRefLang> InvalidCountryRefEntityRefLangs = new ArrayList<CountryRefEntityRefLang>();
            List<CountryRefEntityRefLang> ValidCountryRefEntityRefLangs = new ArrayList<CountryRefEntityRefLang>();

            int lenght = countryRefEntityRefLangs.size();
            for (int i = 0; i < lenght; i++) {
                Set<ConstraintViolation<CountryRefEntityRefLang>> violations = validateCountryRef(countryRefEntityRefLangs.get(i));
                if (violations.isEmpty())
                {
                    ValidCountryRefEntityRefLangs.add(countryRefEntityRefLangs.get(i));
                } else {
                    InvalidCountryRefEntityRefLangs.add(countryRefEntityRefLangs.get(i));
                }
            }
            if (!InvalidCountryRefEntityRefLangs.isEmpty()) {

                ByteArrayInputStream out = countryRefsToExcel(InvalidCountryRefEntityRefLangs);
                xls = new InputStreamResource(out);

            }

            if (!ValidCountryRefEntityRefLangs.isEmpty()) {
                for (CountryRefEntityRefLang l : ValidCountryRefEntityRefLangs) {
                    CountryRef countryRef = new CountryRef();
                    countryRef.setCodeIso(l.getCodeIso());
                    countryRef.setReference(l.getReference());
                    CountryRef ltemp = countryRefRepository.findByReference(l.getCodeIso());
                    if (ltemp == null) {
                        Long id = (countryRefRepository.save(countryRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.COUNTRY_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        countryRef.setId(ltemp.getId());
                        countryRefRepository.save(countryRef);
                        EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_REF, langRepository.findByCode(l.getLang()).getId(), ltemp.getId());
                        if(entityRefLang==null){
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setRefId(countryRef.getId());
                            entityRefLang.setTableRef(TableRef.COUNTRY_REF);
                        }
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLangRepository.save(entityRefLang);
                    }
                }
            }
            if (!InvalidCountryRefEntityRefLangs.isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(
                                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(xls);

            if (!ValidCountryRefEntityRefLangs.isEmpty())
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
        List<CountryRef> countryRefs = countryRefRepository.findAll();
        ByteArrayInputStream in = null;
        return in;
    }


    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<CountryRef> countryRefs = countryRefRepository.findAll();
        List<CountryRefEntityRefLang> countryRefEntityRefLangs = new ArrayList<CountryRefEntityRefLang>();

        for (CountryRef u : countryRefs) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_REF, lang.getId(), u.getId());
            if (entityRefLang != null) {
                CountryRefEntityRefLang countryRefEntityRefLang = new CountryRefEntityRefLang();
                countryRefEntityRefLang.setCodeIso(u.getCodeIso());
                countryRefEntityRefLang.setReference(u.getReference());
                countryRefEntityRefLang.setLabel(entityRefLang.getLabel());
                countryRefEntityRefLang.setDescription(entityRefLang.getDescription());
                countryRefEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                countryRefEntityRefLangs.add(countryRefEntityRefLang);
            }
        }
        ByteArrayInputStream in = countryRefsToExcel(countryRefEntityRefLangs);
        return in;
    }

    public Page<CountryRefBean> getAll(Pageable pageable) {
        Page<CountryRef> entities = countryRefRepository.findAll(pageable);
        Page<CountryRefBean> result = entities.map(countryRefsMapper::entityToBean);
        return result;

    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.COUNTRY_REF);
    }

    @Override
    public void addCountryRef(CountryRefEntityRefLang countryRefEntityRefLang) {
        CountryRef countryRef = new CountryRef();
        countryRef.setCodeIso(countryRefEntityRefLang.getCodeIso());
        countryRef.setReference(countryRefEntityRefLang.getReference());
        CountryRef ltemp = countryRefRepository.findByCodeIso(countryRefEntityRefLang.getCodeIso());
        if (ltemp == null) {
            Long id = (countryRefRepository.save(countryRef)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(countryRefEntityRefLang.getLabel());
            entityRefLang.setDescription(countryRefEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.COUNTRY_REF);
            entityRefLang.setLang(langRepository.findByCode(countryRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            countryRef.setId(id);
            countryRefRepository.save(countryRef);
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_REF, langRepository.findByCode(countryRefEntityRefLang.getLang()).getId(), id);
            entityRefLang.setLabel(countryRefEntityRefLang.getLabel());
            entityRefLang.setDescription(countryRefEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public CountryRefEntityRefLang findCountry(Long id, String lang) {
        CountryRefEntityRefLang countryRefEntityRefLang = new CountryRefEntityRefLang();
        CountryRef countryRef = countryRefRepository.findOneById(id);
        EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_REF, langRepository.findByCode(lang).getId(), countryRef.getId());
        countryRefEntityRefLang.setCodeIso(countryRef.getCodeIso());
        countryRefEntityRefLang.setReference(countryRef.getReference());
        countryRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        countryRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        countryRefEntityRefLang.setLang(lang);
        return countryRefEntityRefLang;
    }


    @Override
    public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
        entityRefLang.setTableRef(TableRef.COUNTRY_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
            if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
                entityRefLangRepository.save(entityRefLang);
            else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }


    public Page<CountryRefEntityRefLang> mapCountryRefsToRefLangs(Page<CountryRef> countryRefs , String codeLang){
        List<CountryRefEntityRefLang> countryRefEntityRefLangs = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        return countryRefs.map(countryRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_REF, lang.getId(), countryRef.getId());
            CountryRefEntityRefLang countryRefEntityRefLang = new CountryRefEntityRefLang( ) ;
            countryRefEntityRefLang.setId(countryRef.getId());
            countryRefEntityRefLang.setReference(countryRef!=null ?countryRef.getReference():"");
            countryRefEntityRefLang.setCodeIso(countryRef!=null ?countryRef.getCodeIso():"");
            countryRefEntityRefLang.setCreatedBy(countryRef.getCreatedBy());
            countryRefEntityRefLang.setCreatedOn(countryRef.getCreatedOn());
            countryRefEntityRefLang.setUpdatedBy(countryRef.getUpdatedBy());
            countryRefEntityRefLang.setUpdatedOn(countryRef.getUpdatedOn());
            countryRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            countryRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            countryRefEntityRefLang.setLang(codeLang);
            countryRefEntityRefLangs.add(countryRefEntityRefLang);
            return  countryRefEntityRefLang ;
        });
    }

    public Page<CountryRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<CountryRef> countryRefs = null;
        if(orderDirection.equals("DESC")){
            countryRefs = countryRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            countryRefs = countryRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }

        return  mapCountryRefsToRefLangs(countryRefs, codeLang) ;

    }

    /*
    public  Page<CountryRefEntityRefLang> filterByReferenceOrLabel(String value, int page,int size,String codeLang) {

        if (value.equals(" ") || value == null)
            return getAll(page, size, codeLang, "ASC");
        else
            return countryRefRepository.filterByReferenceOrLabelProjection(value,codeLang,PageRequest.of(page,size)).map(this::mapCountryRefsToRefLangs);
    }*/

   public  Page<CountryRefEntityRefLang> filterByReferenceOrLabel(String value, int page,int size,String codeLang) {
           return mapCountryRefsToRefLangs(
                   countryRefRepository.filterByReferenceOrLabel(value, langRepository.findByCode(codeLang).getId(), PageRequest.of(page, size)), codeLang);
   }

    @Override
    public Page<CountryRefEntityRefLangProjection> filterByReferenceOrLabelProjection(String value, Pageable pageable, String lang) {
        return countryRefRepository.filterByReferenceOrLabelProjection(value.toLowerCase(), langRepository.findByCode(lang).getId(),pageable);
    }

    @Override
    public CountryRefBean findById(Long id) {
        CountryRef result= countryRefRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return countryRefsMapper.entityToBean(result);
    }


    // WE USE THIS temporarily AFTER THAT WE CAN REFACTOR THIS
    public CountryRefEntityRefLang mapCountryRefsToRefLangs(CountryRefEntityRefLangProjection item) {
        CountryRefEntityRefLang x = new CountryRefEntityRefLang();
        x.setCodeIso(item.getCodeIso());
        x.setLang(item.getLang());
        x.setLabel(item.getLabel());
        x.setId(item.getId());
        x.setReference(item.getReference());
        x.setDescription(item.getDescription());

        return x;
    }


}