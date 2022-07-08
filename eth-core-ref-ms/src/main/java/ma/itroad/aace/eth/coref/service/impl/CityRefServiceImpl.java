package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CityRefBean;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.CityRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.CountryRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.CityRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.CityRefVM;
import ma.itroad.aace.eth.coref.repository.CityRefRepository;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.service.ICityRefService;
import ma.itroad.aace.eth.coref.service.helper.*;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CityRefServiceImpl extends BaseServiceImpl<CityRef, CityRefBean> implements ICityRefService {
    static String[] HEADERs = {"CITY REFERENCE", "COUNTRY REFERENCE","LABEL","DESCRIPTION", "LANGUE"};
    static String SHEET = "CityRefsSHEET";
    @Autowired
    private CityRefRepository cityRefRepository;

    @Autowired
    private CountryRefRepository countryRefRepository;

    @Autowired
    private CountryRefMapper countryRefMapper;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    private CityRefMapper mapper;
    @Autowired
    LangRepository langRepository;

    @Autowired
    private InternationalizationHelper internationalizationHelper;

    @Autowired
    private Validator validator;


    @Override
    public List<CityRef> saveAll(List<CityRef> cityRefs) {
        if (!cityRefs.isEmpty()) {
            return cityRefRepository.saveAll(cityRefs);
        }
        return null;
    }



    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.CITY_REF, id);
            for(EntityRefLang entityRefLang:entityRefLangs ){
                entityRefLangRepository.delete(entityRefLang);
            }
            cityRefRepository.deleteById(id);
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
                cityRefRepository.deleteById(id);
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
    public ErrorResponse deleteInternationalisation(String codeLang, Long cityRefId) {
        ErrorResponse response = new ErrorResponse();
        try {
            Lang lang = langRepository.findByCode(codeLang);
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CITY_REF, lang.getId(), cityRefId);
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
    public ByteArrayInputStream cityRefsToExcel(List<CityRefEntityRefLang> cityRefEntityRefLangs) {
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
                if (!cityRefEntityRefLangs.isEmpty()) {

                    for (CityRefEntityRefLang cityRefEntityRefLang : cityRefEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(cityRefEntityRefLang.getReference());
                       row.createCell(1).setCellValue(cityRefEntityRefLang.getCountryRef());
                       // cityRefEntityRefLang.setCountryRef(u.getCountryRef().getReference());

                        row.createCell(2).setCellValue(cityRefEntityRefLang.getLabel());
                        row.createCell(3).setCellValue(cityRefEntityRefLang.getDescription());
                        row.createCell(4).setCellValue(cityRefEntityRefLang.getLang());
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
    //row.createCell(1).setCellValue(cityRef.getCountryRef() != null ? cityRef.getCountryRef().getReference() : "");

    @Override
    public Set<ConstraintViolation<CityRefEntityRefLang>> validateCityRef(CityRefEntityRefLang cityRefEntityRefLang) {
        Set<ConstraintViolation<CityRefEntityRefLang>> violations = validator.validate(cityRefEntityRefLang);
        return violations;
    }


    @Override
    public List<CityRefEntityRefLang> excelToCityRefsRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
              List<CountryRef> countryRefs = new ArrayList<CountryRef>();
            List<CityRefEntityRefLang> cityRefEntityRefLangs = new ArrayList<CityRefEntityRefLang>();
            Row currentRow = rows.next();
            int rowNumber = 0;
            while (rows.hasNext()) {
                currentRow = rows.next();

                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                CityRefEntityRefLang cityRefEntityRefLang = new CityRefEntityRefLang();
                int cellIdx = 0;
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);

                    switch (cellIdx) {
                        case 0:
                            cityRefEntityRefLang.setReference(Util.cellValue(currentCell));
                            break;
                        case 1:
                        cityRefEntityRefLang.setCountryRef(Util.cellValue(currentCell));

                        break;
                        case 2:
                            cityRefEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 3:
                            cityRefEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 4:
                            cityRefEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;

                        default:
                            break;
                    }
                    cellIdx++;
                }
                cityRefEntityRefLangs.add(cityRefEntityRefLang);
            }
            workbook.close();
            return cityRefEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public void addCityRef(CityRefEntityRefLang cityRefEntityRefLang){
        CityRef cityRef = new CityRef();
        cityRef.setReference(cityRefEntityRefLang.getReference());
        cityRef.setCountryRef(countryRefRepository.findByReference(cityRefEntityRefLang.getCountryRef()));
        CityRef ltemp = cityRefRepository.findByReference(cityRefEntityRefLang.getReference());
        if (ltemp == null) {
            Long id = (cityRefRepository.save(cityRef)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(cityRefEntityRefLang.getLabel());
            entityRefLang.setDescription(cityRefEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.CITY_REF);
            entityRefLang.setLang(  langRepository.findByCode(cityRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            cityRef.setId(id);
            cityRefRepository.save(cityRef);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CITY_REF, langRepository.findByCode(cityRefEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(cityRefEntityRefLang.getLabel());
            entityRefLang.setDescription(cityRefEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }

    private CityRefVM convertToCityRefVM(CityRef entity) {
        final CityRefVM item = new CityRefVM();
        item.setReference(entity.getReference());
        item.setCityRefId(entity.getId());
        item.setCountryRef(entity.getCountryRef() == null ? null : entity.getCountryRef().getReference());
        List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.CITY_REF, entity.getId());
        List<InternationalizationVM> internationalizationVMList = entityRefLangs.stream().map(InternationalizationHelper::toInternationalizationVM).collect(Collectors.toList());
        item.setInternationalizationVMList(internationalizationVMList);
        return item;
    }


    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<CityRefEntityRefLang> cityRefEntityRefLangs = excelToCityRefsRef(file.getInputStream());
            List<CityRefEntityRefLang> InvalidCityRefEntityRefLangs = new ArrayList<CityRefEntityRefLang>();
            List<CityRefEntityRefLang> ValidCityRefEntityRefLangs = new ArrayList<CityRefEntityRefLang>();

            int lenght = cityRefEntityRefLangs.size();
            for (int i = 0; i < lenght; i++) {
                Set<ConstraintViolation<CityRefEntityRefLang>> violations = validateCityRef(cityRefEntityRefLangs.get(i));
                if (violations.isEmpty())
                {
                    ValidCityRefEntityRefLangs.add(cityRefEntityRefLangs.get(i));
                } else {
                    InvalidCityRefEntityRefLangs.add(cityRefEntityRefLangs.get(i));
                }
            }
            if (!InvalidCityRefEntityRefLangs.isEmpty()) {

                ByteArrayInputStream out = cityRefsToExcel(InvalidCityRefEntityRefLangs);
                xls = new InputStreamResource(out);

            }

            if (!ValidCityRefEntityRefLangs.isEmpty()) {
                for (CityRefEntityRefLang l : ValidCityRefEntityRefLangs) {
                    CityRef cityRef = new CityRef();

                    CityRefEntityRefLang cityRefEntityRefLang = new CityRefEntityRefLang();
                    cityRef.setReference(l.getReference());
                    // cityRef.setCountryRef(countryRefRepository.findByReference(l.getCountryRef().getReference()));
                    cityRef.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
                    CityRef ltemp = cityRefRepository.findByReference(l.getReference());
                    if (ltemp == null) {
                        Long id = (cityRefRepository.save(cityRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.CITY_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        cityRef.setId(ltemp.getId());
                        cityRefRepository.save(cityRef);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CITY_REF,langRepository.findByCode(l.getLang()).getId(),cityRef.getId());
                        if(entityRefLang==null){
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setRefId(cityRef.getId());
                            entityRefLang.setTableRef(TableRef.CITY_REF);
                        }
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLangRepository.save(entityRefLang);
                    }

                }
            }
            if (!InvalidCityRefEntityRefLangs.isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(
                                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(xls);

            if (!ValidCityRefEntityRefLangs.isEmpty())
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
        List<CityRef> cityRefs = cityRefRepository.findAll();
        ByteArrayInputStream in = null;
       // ByteArrayInputStream in =  cityRefsToExcel(cityRefs);
                //cityRefsToExcel(cityRefs);
        return in;
    }



    @Override
    public ByteArrayInputStream load(String codeLang, final int page, final int size, String orderDirection) {
        Lang lang = langRepository.findByCode(codeLang);
       // Page<CityRef> cityRefs = cityRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        Page<CityRef> cityRefs = null;
        if(orderDirection.equals("DESC")){
            cityRefs = cityRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            cityRefs = cityRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }

        List<CityRefEntityRefLang> cityRefEntityRefLangs = new ArrayList<CityRefEntityRefLang>();

        for(CityRef u : cityRefs) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CITY_REF, lang.getId(), u.getId());
            if (entityRefLang!=null) {
                CityRefEntityRefLang cityRefEntityRefLang = new CityRefEntityRefLang();
                cityRefEntityRefLang.setReference(u.getReference());
                //cityRefEntityRefLang.setCountryRef((countryRefMapper.entityToBean(u.getCountryRef()) ));
                cityRefEntityRefLang.setCountryRef(u.getCountryRef().getReference());
                cityRefEntityRefLang.setLabel(entityRefLang.getLabel() != null ? entityRefLang.getLabel() : null);
                cityRefEntityRefLang.setDescription(entityRefLang.getDescription() != null ? entityRefLang.getDescription() : null);
                cityRefEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                cityRefEntityRefLangs.add(cityRefEntityRefLang);
            }
        }
        ByteArrayInputStream in = cityRefsToExcel(cityRefEntityRefLangs);
        return in;
    }

    public Page<CityRefBean> getAll(Pageable pageable) {
        Page<CityRef> entities = cityRefRepository.findAll(pageable);
        Page<CityRefBean> result = entities.map(mapper::entityToBean);
        return result;
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.CITY_REF);
    }

    @Override
    public CityRefEntityRefLang findcity(Long id, String lang){
        CityRefEntityRefLang cityRefEntityRefLang = new CityRefEntityRefLang();
        CityRef cityRef = cityRefRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CITY_REF,langRepository.findByCode(lang).getId(),cityRef.getId());
      //  cityRefEntityRefLang.setCountryRef(countryRefMapper.entityToBean(cityRef.getCountryRef()));
        cityRefEntityRefLang.setCountryRef(cityRef.getCountryRef().getReference());
        cityRefEntityRefLang.setReference(cityRef.getReference());
        cityRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        cityRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        cityRefEntityRefLang.setLang(lang);
        return cityRefEntityRefLang;
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang,String lang){
        entityRefLang.setTableRef(TableRef.CITY_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CITY_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    public Page<CityRefEntityRefLang> mapCityRefsToRefLangs(Page<CityRef> cityRefs, String codeLang) {
        List<CityRefEntityRefLang> cityRefEntityRefLangs = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        return cityRefs.map(cityRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CITY_REF, lang.getId(), cityRef.getId());
            CityRefEntityRefLang cityRefEntityRefLang = new CityRefEntityRefLang();
            cityRefEntityRefLang.setId(cityRef.getId());

            cityRefEntityRefLang.setReference(cityRef.getReference() != null ? cityRef.getReference() : "");
            cityRefEntityRefLang.setCountryRef(cityRef.getCountryRef().getReference() != null ? cityRef.getCountryRef().getReference() : "");
            cityRefEntityRefLang.setCreatedBy(cityRef.getCreatedBy());
            cityRefEntityRefLang.setCreatedOn(cityRef.getCreatedOn());
            cityRefEntityRefLang.setUpdatedBy(cityRef.getUpdatedBy());
            cityRefEntityRefLang.setUpdatedOn(cityRef.getUpdatedOn());
            cityRefEntityRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
            cityRefEntityRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
            cityRefEntityRefLang.setLang(codeLang);
            cityRefEntityRefLangs.add(cityRefEntityRefLang);
            return cityRefEntityRefLang;
        });
    }

    public Page<CityRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<CityRef> cityRefs = null;
        if(orderDirection.equals("DESC")){
            cityRefs = cityRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            cityRefs = cityRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
        return mapCityRefsToRefLangs(cityRefs, codeLang);
    }

    public Page<CityRefEntityRefLang> filterByReferenceOrLabel(String value, Pageable pageable, String codeLang) {
        return mapCityRefsToRefLangs(cityRefRepository.filterByReferenceOrLabel(value ,langRepository.findByCode(codeLang).getId() , pageable), codeLang);
    }

    @Override
    public Page<CityRefEntityRefLangProjection> filterByReferenceOrLabel(String value, String lang, int page, int size) {
        return cityRefRepository.filCityRefsProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),PageRequest.of(page,size));
    }

    @Override
    public CityRefBean findById(Long id) {
        CityRef result= cityRefRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return mapper.entityToBean(result);
    }
}
