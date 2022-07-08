package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CustomsRegimRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.CustomsRegimRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.enums.RegimType;
import ma.itroad.aace.eth.coref.model.mapper.CustomsRegimRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.CustomsRegimRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.CustomsRegimRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.service.ICustomsRegimRefService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.service.helper.CustomsRegimRefEntityRefLang;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class CustomsRegimRefServiceImpl extends BaseServiceImpl<CustomsRegimRef,CustomsRegimRefBean> implements ICustomsRegimRefService {

    static String[] HEADERs = { "CODE","TYPE REGIM", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "CustomsRegimRefSHEET";

    @Autowired
    private CustomsRegimRefRepository customsRegimRefRepository;

    @Autowired
    LangRepository langRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;

    @Autowired
    CustomsRegimRefMapper customsRegimRefMapper;

    @Autowired
    private Validator validator;

    @Override
    public List<CustomsRegimRef> saveAll(List<CustomsRegimRef> customsRegimRefs) {
        if(!customsRegimRefs.isEmpty()){
            return customsRegimRefRepository.saveAll(customsRegimRefs);
        }
        return null;
    }

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.CUSTOMSREGIM_REF, id);
            for(EntityRefLang entityRefLang:entityRefLangs ){
                entityRefLangRepository.delete(entityRefLang);
            }
            customsRegimRefRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return  response;
    }

    @Override
    public ErrorResponse deleteInternationalisation(String codeLang, Long customsRegimRefId) {

        ErrorResponse response = new ErrorResponse();
        try {

            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSREGIM_REF, lang.getId(), customsRegimRefId);

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
    public ByteArrayInputStream customsRegimRefToExcel(List<CustomsRegimRefEntityRefLang> customsRegimRefEntityRefLangs) {
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
                if(!customsRegimRefEntityRefLangs.isEmpty()){

                    for (CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang : customsRegimRefEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(customsRegimRefEntityRefLang.getCode());
                        row.createCell(1).setCellValue(customsRegimRefEntityRefLang.getRegimType().toString());
                        row.createCell(2).setCellValue(customsRegimRefEntityRefLang.getLabel());
                        row.createCell(3).setCellValue(customsRegimRefEntityRefLang.getDescription());
                        row.createCell(4).setCellValue(customsRegimRefEntityRefLang.getLang());

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
    public Set<ConstraintViolation<CustomsRegimRefEntityRefLang>> validateCustomsRegim(CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang) {
        Set<ConstraintViolation<CustomsRegimRefEntityRefLang>> violations = validator.validate(customsRegimRefEntityRefLang);
        return violations;
    }

    @Override
    public List<CustomsRegimRefEntityRefLang> excelToCustomsRegimRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<CustomsRegimRef> customsRegimRefs = new ArrayList<CustomsRegimRef>();
            List<CustomsRegimRefEntityRefLang> customsRegimRefEntityRefLangs = new ArrayList<CustomsRegimRefEntityRefLang>();
            Row currentRow = rows.next();
            while (rows.hasNext()) {
                currentRow = rows.next();

                CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang = new CustomsRegimRefEntityRefLang();
                int cellIdx = 0;
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);

                    switch (colNum) {
                        case 0:
                            customsRegimRefEntityRefLang.setCode(Util.cellValue(currentCell));
                            break;
                        case 1:
                            RegimType regimType = RegimType.valueOf(Util.cellValue(currentCell));
                            customsRegimRefEntityRefLang.setRegimType(regimType);
                            break;

                        case 2:
                            customsRegimRefEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 3:
                            customsRegimRefEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 4:
                            customsRegimRefEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                customsRegimRefEntityRefLangs.add(customsRegimRefEntityRefLang);
            }
            workbook.close();
            return customsRegimRefEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<CustomsRegimRefEntityRefLang> customsRegimRefEntityRefLangs = excelToCustomsRegimRef(file.getInputStream());
            List<CustomsRegimRefEntityRefLang> InvalidCustomsRegimRefEntityRefLangs = new ArrayList<CustomsRegimRefEntityRefLang>();
            List<CustomsRegimRefEntityRefLang> ValidCustomsRegimRefEntityRefLangs = new ArrayList<CustomsRegimRefEntityRefLang>();

            int lenght = customsRegimRefEntityRefLangs.size();
            for (int i = 0; i < lenght; i++) {
                Set<ConstraintViolation<CustomsRegimRefEntityRefLang>> violations = validateCustomsRegim(customsRegimRefEntityRefLangs.get(i));
                if (violations.isEmpty())
                {
                    ValidCustomsRegimRefEntityRefLangs.add(customsRegimRefEntityRefLangs.get(i));
                } else {
                    InvalidCustomsRegimRefEntityRefLangs.add(customsRegimRefEntityRefLangs.get(i));
                }
            }
            if (!InvalidCustomsRegimRefEntityRefLangs.isEmpty()) {

                ByteArrayInputStream out = customsRegimRefToExcel(InvalidCustomsRegimRefEntityRefLangs);
                xls = new InputStreamResource(out);

            }

            if (!ValidCustomsRegimRefEntityRefLangs.isEmpty()) {
                for (CustomsRegimRefEntityRefLang l : ValidCustomsRegimRefEntityRefLangs) {

                    CustomsRegimRef customsRegimRef = new CustomsRegimRef();
                    CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang = new CustomsRegimRefEntityRefLang();
                    customsRegimRef.setCode(l.getCode());
                    customsRegimRef.setRegimType(l.getRegimType());

                    CustomsRegimRef ltemp = customsRegimRefRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (customsRegimRefRepository.save(customsRegimRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.CUSTOMSREGIM_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        Long id = ltemp.getId();
                        customsRegimRef.setId(id);
                        customsRegimRefRepository.save(customsRegimRef);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSREGIM_REF, langRepository.findByCode(l.getLang()).getId() ,id);
                        if(entityRefLang==null){
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setRefId(customsRegimRef.getId());
                            entityRefLang.setTableRef(TableRef.CUSTOMSREGIM_REF);
                        }
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLangRepository.save(entityRefLang);
                    }

                }
            }
            if (!InvalidCustomsRegimRefEntityRefLangs.isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(
                                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(xls);

            if (!ValidCustomsRegimRefEntityRefLangs.isEmpty())
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
        List<CustomsRegimRef> customsRegimRefs = customsRegimRefRepository.findAll();
        ByteArrayInputStream in = null;
        //customsRegimRefToExcel(customsRegimRefs);
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<CustomsRegimRef> customsRegimRefs = customsRegimRefRepository.findAll();
        List<CustomsRegimRefEntityRefLang> customsRegimRefEntityRefLangs = new ArrayList<CustomsRegimRefEntityRefLang>();

        for(CustomsRegimRef u : customsRegimRefs) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSREGIM_REF, lang.getId(), u.getId());
            if (entityRefLang!=null) {
                CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang = new CustomsRegimRefEntityRefLang();
                customsRegimRefEntityRefLang.setCode(u.getCode());
                customsRegimRefEntityRefLang.setRegimType(u.getRegimType());
                customsRegimRefEntityRefLang.setLabel(entityRefLang.getLabel());
                customsRegimRefEntityRefLang.setDescription(entityRefLang.getDescription());
                customsRegimRefEntityRefLang.setLang(entityRefLang.getLang().getCode());
                customsRegimRefEntityRefLangs.add(customsRegimRefEntityRefLang);
            }
        }
        ByteArrayInputStream in = customsRegimRefToExcel(customsRegimRefEntityRefLangs);
        return in;
    }

    @Override
    public void addCustomsRegimRef(CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang){
        CustomsRegimRef customsRegimRef = new CustomsRegimRef();
        customsRegimRef.setCode(customsRegimRefEntityRefLang.getCode());
        customsRegimRef.setRegimType(customsRegimRefEntityRefLang.getRegimType());
        CustomsRegimRef ltemp = customsRegimRefRepository.findByCode(customsRegimRefEntityRefLang.getCode());
        if (ltemp == null) {
            Long id = (customsRegimRefRepository.save(customsRegimRef)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(customsRegimRefEntityRefLang.getLabel());
            entityRefLang.setDescription(customsRegimRefEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.CUSTOMSREGIM_REF);
            entityRefLang.setLang(  langRepository.findByCode(customsRegimRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            customsRegimRef.setId(id);
            customsRegimRefRepository.save(customsRegimRef);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSREGIM_REF, langRepository.findByCode(customsRegimRefEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(customsRegimRefEntityRefLang.getLabel());
            entityRefLang.setDescription(customsRegimRefEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public CustomsRegimRefEntityRefLang findCustomsRegimRef(Long id, String lang){
        CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang = new CustomsRegimRefEntityRefLang();
        CustomsRegimRef customsRegimRef = customsRegimRefRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSREGIM_REF,langRepository.findByCode(lang).getId(),customsRegimRef.getId());
        customsRegimRefEntityRefLang.setCode(customsRegimRef.getCode());
        customsRegimRefEntityRefLang.setRegimType(customsRegimRef.getRegimType());
        customsRegimRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        customsRegimRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        customsRegimRefEntityRefLang.setLang(lang);
        return customsRegimRefEntityRefLang;
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.CUSTOMSREGIM_REF);
    }

    public Page<CustomsRegimRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
         Page<CustomsRegimRef> customsRegimRefs = null;
        if(orderDirection.equals("DESC")){
            customsRegimRefs = customsRegimRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            customsRegimRefs = customsRegimRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
        Lang lang = langRepository.findByCode(codeLang);
        List<CustomsRegimRefEntityRefLang> customsRegimRefEntityRefLangs = new ArrayList<>();
        return customsRegimRefs.map(customsRegimRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSREGIM_REF, lang.getId(), customsRegimRef.getId());

            CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang = new CustomsRegimRefEntityRefLang();

            customsRegimRefEntityRefLang.setId(customsRegimRef.getId());
            customsRegimRefEntityRefLang.setCode(customsRegimRef.getCode()!=null?customsRegimRef.getCode():"");
            customsRegimRefEntityRefLang.setRegimType(customsRegimRef.getRegimType());
            customsRegimRefEntityRefLang.setCreatedBy(customsRegimRef.getCreatedBy());
            customsRegimRefEntityRefLang.setCreatedOn(customsRegimRef.getCreatedOn());
            customsRegimRefEntityRefLang.setUpdatedBy(customsRegimRef.getUpdatedBy());
            customsRegimRefEntityRefLang.setUpdatedOn(customsRegimRef.getUpdatedOn());

            customsRegimRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            customsRegimRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            customsRegimRefEntityRefLang.setLang(codeLang);
            customsRegimRefEntityRefLangs.add(customsRegimRefEntityRefLang);
            return  customsRegimRefEntityRefLang ;
        });
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang,String lang){
        entityRefLang.setTableRef(TableRef.CUSTOMSREGIM_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
        entityRefLangRepository.save(entityRefLang);
    }

    public Page<CustomsRegimRefEntityRefLang> mapLangToRefLangs(Page<CustomsRegimRef> customsRegimRefs, String codeLang) {
        List<CustomsRegimRefEntityRefLang> customsRegimRefEntityRefLangs = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        return customsRegimRefs.map(customsRegimRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSREGIM_REF, lang.getId(), customsRegimRef.getId());
            CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang = new CustomsRegimRefEntityRefLang();
            customsRegimRefEntityRefLang.setId(customsRegimRef.getId());

            customsRegimRefEntityRefLang.setCode(customsRegimRef.getCode() != null ? customsRegimRef.getCode() : "");
            customsRegimRefEntityRefLang.setRegimType(customsRegimRef.getRegimType());
            customsRegimRefEntityRefLang.setCreatedBy(customsRegimRef.getCreatedBy());
            customsRegimRefEntityRefLang.setCreatedOn(customsRegimRef.getCreatedOn());
            customsRegimRefEntityRefLang.setUpdatedBy(customsRegimRef.getUpdatedBy());
            customsRegimRefEntityRefLang.setUpdatedOn(customsRegimRef.getUpdatedOn());
            customsRegimRefEntityRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
            customsRegimRefEntityRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
            customsRegimRefEntityRefLang.setLang(codeLang);
            customsRegimRefEntityRefLangs.add(customsRegimRefEntityRefLang);
            return customsRegimRefEntityRefLang;
        });
    }

    public Page<CustomsRegimRefEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
        return mapLangToRefLangs(customsRegimRefRepository.filterByCodeOrLabel(value,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
    }

    @Override
    public ErrorResponse deleteList(ListOfObject listOfObject) {
        ErrorResponse response = new ErrorResponse();

        try {
            for(Long id : listOfObject.getListOfObject()){
                customsRegimRefRepository.deleteById(id);
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
    public CustomsRegimRefBean findById(Long id) {
        CustomsRegimRef result= customsRegimRefRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return customsRegimRefMapper.entityToBean(result);
    }

    @Override
    public Page<CustomsRegimRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
        return customsRegimRefRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
    }

    @Override
    public CustomsRegimRefBean updateItem(Long id,CustomsRegimRefBean bean) {
        CustomsRegimRef result= customsRegimRefRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        result.setCode(bean.getCode()!=null?bean.getCode():result.getCode());
        result.setRegimType(bean.getRegimType()!=null?bean.getRegimType():result.getRegimType());
        result= customsRegimRefRepository.save(result);
        return customsRegimRefMapper.entityToBean(result);
    }
}
