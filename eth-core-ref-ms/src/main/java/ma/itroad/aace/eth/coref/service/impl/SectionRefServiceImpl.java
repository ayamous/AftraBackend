package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.AgreementBean;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.SectionRefBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.SectionRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.SectionRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.SectionRefRepository;
import ma.itroad.aace.eth.coref.service.ISectionRefService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.service.helper.ChapterRefLang;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.SectionRefEntityRefLang;
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
import java.util.*;

@Service
public class SectionRefServiceImpl extends BaseServiceImpl<SectionRef, SectionRefBean> implements ISectionRefService {

    static String[] HEADERs = {"CODE", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "sectionSheet";

    @Autowired
    private SectionRefRepository sectionRefRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;

    @Autowired
    LangRepository langRepository;

    @Autowired
    SectionRefMapper sectionRefMapper;

    @Autowired
    private Validator validator;

    @Override
    public List<SectionRef> saveAll(List<SectionRef> sectionRefs) {
        if (!sectionRefs.isEmpty()) {
            return sectionRefRepository.saveAll(sectionRefs);
        }
        return null;
    }

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.SECTION_REF, id);
            for(EntityRefLang entityRefLang:entityRefLangs ){
                entityRefLangRepository.delete(entityRefLang);
            }
            sectionRefRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return  response;
    }

    public ErrorResponse deleteList(ListOfObject listOfObject) {
        ErrorResponse response = new ErrorResponse();

        try {
            for(Long id : listOfObject.getListOfObject()){
                /*List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.SECTION_REF, id);
                for(EntityRefLang entityRefLang:entityRefLangs ){
                    entityRefLangRepository.delete(entityRefLang);
                }*/
                sectionRefRepository.deleteById(id);
                response.setStatus(HttpStatus.OK);
                response.setErrorMsg("null");
            }

        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return response;
    }

    @Override
    public ErrorResponse deleteInternationalisation(String codeLang, Long sectionRefId) {

        ErrorResponse response = new ErrorResponse();
        try {

            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SECTION_REF, lang.getId(), sectionRefId);

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
    public void saveFromExcel(MultipartFile file) {
        try {
            List<SectionRefEntityRefLang> sectionRefEntityRefLangs = excelToSectionsRef(file.getInputStream());
            if (!sectionRefEntityRefLangs.isEmpty()) {
                for (SectionRefEntityRefLang l : sectionRefEntityRefLangs) {
                    SectionRef sectionRef = new SectionRef();

                    SectionRefEntityRefLang sectionRefEntityRefLang = new SectionRefEntityRefLang();
                    sectionRef.setCode(l.getCode());
                    SectionRef ltemp = sectionRefRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (sectionRefRepository.save(sectionRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.SECTION_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        Long id = ltemp.getId();
                        sectionRef.setId(id);
                        sectionRefRepository.save(sectionRef);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SECTION_REF, langRepository.findByCode(l.getLang()).getId() ,id);
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang();
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.SECTION_REF);
                            entityRefLang.setRefId(sectionRef.getId());
                        }
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream sectionsToExcel(List<SectionRefEntityRefLang> sectionRefEntityRefLangs) {
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
                if(!sectionRefEntityRefLangs.isEmpty()){

                    for (SectionRefEntityRefLang sectionRefEntityRefLang : sectionRefEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(sectionRefEntityRefLang.getCode());
                        row.createCell(1).setCellValue(sectionRefEntityRefLang.getLabel());
                        row.createCell(2).setCellValue(sectionRefEntityRefLang.getDescription());
                        row.createCell(3).setCellValue(sectionRefEntityRefLang.getLang());
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
    public boolean isCellValid(String s) {
        return s != null && s.matches("^[ a-zA-Z0-9]*$");
    }

    @Override
    public List<SectionRefEntityRefLang> excelToSectionsRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<SectionRefEntityRefLang> sectionRefEntityRefLangs = new ArrayList<SectionRefEntityRefLang>();
            Row currentRow = rows.next();
            while (rows.hasNext()) {
                currentRow = rows.next();
                SectionRefEntityRefLang sectionRefEntityRefLang = new SectionRefEntityRefLang();
                int cellIdx = 0;
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            sectionRefEntityRefLang.setCode(Util.cellValue(currentCell));
                            break;
                        case 1:
                            sectionRefEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 2:
                            sectionRefEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 3:
                            sectionRefEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                sectionRefEntityRefLangs.add(sectionRefEntityRefLang);
            }
            workbook.close();
            return sectionRefEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream load() {
        List<SectionRef> sectionRefs = sectionRefRepository.findAll();
        ByteArrayInputStream in = null;
        //sectionsToExcel(sectionRefs);
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<SectionRef> sectionRefs = sectionRefRepository.findAll();
        List<SectionRefEntityRefLang> sectionRefEntityRefLangs = new ArrayList<SectionRefEntityRefLang>();

        for(SectionRef u : sectionRefs) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SECTION_REF, lang.getId(), u.getId());
            if (entityRefLang!=null) {
                SectionRefEntityRefLang sectionRefEntityRefLang = new SectionRefEntityRefLang();
                sectionRefEntityRefLang.setCode(u.getCode());
                sectionRefEntityRefLang.setLabel(entityRefLang.getLabel());
                sectionRefEntityRefLang.setDescription(entityRefLang.getDescription());
                sectionRefEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                sectionRefEntityRefLangs.add(sectionRefEntityRefLang);
            }
        }
        ByteArrayInputStream in = sectionsToExcel(sectionRefEntityRefLangs);
        return in;
    }
    @Override
    public void addSectionRef(SectionRefEntityRefLang sectionRefEntityRefLang){
        SectionRef sectionRef = new SectionRef();
        sectionRef.setCode(sectionRefEntityRefLang.getCode());
        SectionRef ltemp = sectionRefRepository.findByCode(sectionRefEntityRefLang.getCode());
        if (ltemp == null) {
            Long id = (sectionRefRepository.save(sectionRef)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(sectionRefEntityRefLang.getLabel());
            entityRefLang.setDescription(sectionRefEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.SECTION_REF);
            entityRefLang.setLang(  langRepository.findByCode(sectionRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            sectionRef.setId(id);
            sectionRefRepository.save(sectionRef);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SECTION_REF, langRepository.findByCode(sectionRefEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(sectionRefEntityRefLang.getLabel());
            entityRefLang.setDescription(sectionRefEntityRefLang.getDescription());
            entityRefLang.setLang(langRepository.findByCode(sectionRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public SectionRefEntityRefLang findSection(Long id, String lang){
        SectionRefEntityRefLang sectionRefEntityRefLang = new SectionRefEntityRefLang();
        SectionRef sectionRef = sectionRefRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SECTION_REF,langRepository.findByCode(lang).getId(),sectionRef.getId());
        sectionRefEntityRefLang.setCode(sectionRef.getCode());
        sectionRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        sectionRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        sectionRefEntityRefLang.setLang(lang);
        return sectionRefEntityRefLang;
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.SECTION_REF);
    }

    /*
@Override
public void addInternationalisation(SectionRefEntityRefLang sectionRefEntityRefLang){
    SectionRef sectionRef = new SectionRef();
    sectionRef.setCode(sectionRefEntityRefLang.getCode());


    SectionRef ltemp = sectionRefRepository.findByCode(sectionRefEntityRefLang.getCode());
    if (ltemp == null) {

        Long id = (sectionRefRepository.save(sectionRef)).getId();
        EntityRefLang entityRefLang = new EntityRefLang();
        entityRefLang.setLabel(sectionRefEntityRefLang.getLabel());
        entityRefLang.setDescription(sectionRefEntityRefLang.getDescription());
        entityRefLang.setRefId(id);
        entityRefLang.setTableRef(TableRef.SECTION_REF);
        entityRefLang.setLang(  langRepository.findOneById(sectionRefEntityRefLang.getLang())                   );
        entityRefLangRepository.save(entityRefLang);


    } else {
        Long id = ltemp.getId();
        sectionRef.setId(id);
        sectionRefRepository.save(sectionRef);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SECTION_REF,sectionRefEntityRefLang.getLang(),id);
        entityRefLang.setLabel(sectionRefEntityRefLang.getLabel());
        entityRefLang.setDescription(sectionRefEntityRefLang.getDescription());
        entityRefLangRepository.save(entityRefLang);
    }


}
*/
    public Page<SectionRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<SectionRef> sectionRefs = null;
        if(orderDirection.equals("DESC")){
            sectionRefs = sectionRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            sectionRefs = sectionRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
        Lang lang = langRepository.findByCode(codeLang);
        List<SectionRefEntityRefLang> sectionRefEntityRefLangs = new ArrayList<>();
        return sectionRefs.map(sectionRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SECTION_REF, lang.getId(), sectionRef.getId());

            SectionRefEntityRefLang sectionRefEntityRefLang = new SectionRefEntityRefLang();

            sectionRefEntityRefLang.setId(sectionRef.getId());
            sectionRefEntityRefLang.setCode(sectionRef.getCode()!=null?sectionRef.getCode():"");
            sectionRefEntityRefLang.setCreatedBy(sectionRef.getCreatedBy());
            sectionRefEntityRefLang.setCreatedOn(sectionRef.getCreatedOn());
            sectionRefEntityRefLang.setUpdatedBy(sectionRef.getUpdatedBy());
            sectionRefEntityRefLang.setUpdatedOn(sectionRef.getUpdatedOn());

            sectionRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            sectionRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            sectionRefEntityRefLang.setLang(codeLang);
            sectionRefEntityRefLangs.add(sectionRefEntityRefLang);
            return  sectionRefEntityRefLang ;
        });
    }

    public Page<SectionRefBean> getAll(Pageable pageable) {
        Page<SectionRef> entities = sectionRefRepository.findAll(pageable);
        Page<SectionRefBean> result = entities.map(sectionRefMapper::entityToBean);
        return result;

    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang,String lang){
        entityRefLang.setTableRef(TableRef.SECTION_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SECTION_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    public Page<SectionRefEntityRefLang> mapLangToRefLangs(Page<SectionRef> sectionRefs, String codeLang) {
        List<SectionRefEntityRefLang> sectionRefEntityRefLangs = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        return sectionRefs.map(sectionRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SECTION_REF, lang.getId(), sectionRef.getId());

            SectionRefEntityRefLang sectionRefEntityRefLang = new SectionRefEntityRefLang();

            sectionRefEntityRefLang.setId(sectionRef.getId());
            sectionRefEntityRefLang.setCode(sectionRef.getCode()!=null?sectionRef.getCode():"");
            sectionRefEntityRefLang.setCreatedBy(sectionRef.getCreatedBy());
            sectionRefEntityRefLang.setCreatedOn(sectionRef.getCreatedOn());
            sectionRefEntityRefLang.setUpdatedBy(sectionRef.getUpdatedBy());
            sectionRefEntityRefLang.setUpdatedOn(sectionRef.getUpdatedOn());

            sectionRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            sectionRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            sectionRefEntityRefLang.setLang(codeLang);
            sectionRefEntityRefLangs.add(sectionRefEntityRefLang);
            return  sectionRefEntityRefLang ;
        });
    }

    public Page<SectionRefEntityRefLang> filterByCodeOrLabel(String value, int page,int size, String codeLang) {

        if (value.equals(" ") || value == null)
            return getAll(page, size, codeLang, "ASC");
        else
        return mapLangToRefLangs(sectionRefRepository.filterByCodeOrLabel(value,langRepository.findByCode(codeLang).getId(), PageRequest.of( page, size)), codeLang);
    }

    @Override
    public Page<SectionRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
        return sectionRefRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
    }

    @Override
    public SectionRefBean findById(Long id) {
        SectionRef result= sectionRefRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return sectionRefMapper.entityToBean(result);
    }

    public Set<ConstraintViolation<SectionRefEntityRefLang>> validateItems(SectionRefEntityRefLang sectionRefEntityRefLang) {
        Set<ConstraintViolation<SectionRefEntityRefLang>> violations = validator.validate(sectionRefEntityRefLang);
        return violations;
    }

    @Override
    public ResponseEntity saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";

        InputStreamResource xls = null;
        try {
            List<SectionRefEntityRefLang> itemsList = excelToSectionsRef(file.getInputStream());
            List<SectionRefEntityRefLang> invalidItems = new ArrayList<SectionRefEntityRefLang>();
            List<SectionRefEntityRefLang> validItems = new ArrayList<SectionRefEntityRefLang>();

            int lenght = itemsList.size();

            for (int i = 0; i < lenght; i++) {

                Set<ConstraintViolation<SectionRefEntityRefLang>> violations = validateItems(itemsList.get(i));
                if (violations.isEmpty())

                {
                    validItems.add(itemsList.get(i));
                } else {
                    invalidItems.add(itemsList.get(i));
                }
            }

            if (!invalidItems.isEmpty()) {

                ByteArrayInputStream out = sectionsToExcel(invalidItems);
                xls = new InputStreamResource(out);

            }

            if (!validItems.isEmpty()) {
                for (SectionRefEntityRefLang l : validItems) {
                    SectionRef sectionRef = new SectionRef();
                    sectionRef.setCode(l.getCode());
                    SectionRef ltemp = sectionRefRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (sectionRefRepository.save(sectionRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.SECTION_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        Long id = ltemp.getId();
                        sectionRef.setId(id);
                        sectionRefRepository.save(sectionRef);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SECTION_REF, langRepository.findByCode(l.getLang()).getId() ,id);
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang();
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.SECTION_REF);
                            entityRefLang.setRefId(sectionRef.getId());
                        }
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
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