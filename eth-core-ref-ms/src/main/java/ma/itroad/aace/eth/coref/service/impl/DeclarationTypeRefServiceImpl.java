package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CityRefBean;
import ma.itroad.aace.eth.coref.model.bean.DeclarationTypeRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.CityRef;
import ma.itroad.aace.eth.coref.model.entity.DeclarationTypeRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.mapper.DeclarationTypeRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.DeclarationTypeRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.DeclarationTypeRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.service.IDeclarationTypeRefService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.service.helper.DeclarationTypeRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
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
public class DeclarationTypeRefServiceImpl extends BaseServiceImpl<DeclarationTypeRef,DeclarationTypeRefBean> implements IDeclarationTypeRefService {

    static String[] HEADERs = { "CODE", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "DeclarationTypeRefSHEET";

    @Autowired
    private DeclarationTypeRefRepository declarationTypeRefRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    LangRepository langRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;

    @Autowired
    DeclarationTypeRefMapper declarationTypeRefMapper;

    @Autowired
    private Validator validator;

    @Override
    public List<DeclarationTypeRef> saveAll(List<DeclarationTypeRef> declarationTypeRefs) {
        if(!declarationTypeRefs.isEmpty()){
            return declarationTypeRefRepository.saveAll(declarationTypeRefs);
        }
        return null;
    }
    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.DECLARATIONTYPE_REF, id);
            for(EntityRefLang entityRefLang:entityRefLangs ){
                entityRefLangRepository.delete(entityRefLang);
            }
            declarationTypeRefRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return  response;
    }

    @Override
    public ErrorResponse deleteInternationalisation(String codeLang, Long declarationTypeRefId) {

        ErrorResponse response = new ErrorResponse();
        try {
            Lang lang = langRepository.findByCode(codeLang);
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.DECLARATIONTYPE_REF, lang.getId(), declarationTypeRefId);
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
    public ByteArrayInputStream declarationTypeRefToExcel(List<DeclarationTypeRefEntityRefLang> declarationTypeRefEntityRefLangs) {
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
                if(!declarationTypeRefEntityRefLangs.isEmpty()){

                    for (DeclarationTypeRefEntityRefLang declarationTypeRefEntityRefLang : declarationTypeRefEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(declarationTypeRefEntityRefLang.getCode());
                        row.createCell(1).setCellValue(declarationTypeRefEntityRefLang.getLabel());
                        row.createCell(2).setCellValue(declarationTypeRefEntityRefLang.getDescription());

                        row.createCell(3).setCellValue(declarationTypeRefEntityRefLang.getLang());
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
    public Set<ConstraintViolation<DeclarationTypeRefEntityRefLang>> validateDeclarationType(DeclarationTypeRefEntityRefLang declarationTypeRefEntityRefLang) {
        Set<ConstraintViolation<DeclarationTypeRefEntityRefLang>> violations = validator.validate(declarationTypeRefEntityRefLang);
        return violations;
    }


    @Override
    public List<DeclarationTypeRefEntityRefLang> excelToDeclarationTypeRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<DeclarationTypeRef> declarationTypeRefs = new ArrayList<DeclarationTypeRef>();
            List<DeclarationTypeRefEntityRefLang> declarationTypeRefEntityRefLangs = new ArrayList<DeclarationTypeRefEntityRefLang>();
            Row currentRow = rows.next();
            while (rows.hasNext()) {
                currentRow = rows.next();

                DeclarationTypeRefEntityRefLang declarationTypeRefEntityRefLang = new DeclarationTypeRefEntityRefLang();
                int cellIdx = 0;
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);

                    switch (colNum) {
                        case 0:
                            declarationTypeRefEntityRefLang.setCode(currentCell.getStringCellValue());
                            break;
                        case 1:
                            declarationTypeRefEntityRefLang.setLabel(currentCell.getStringCellValue());
                            break;
                        case 2:
                            declarationTypeRefEntityRefLang.setDescription(currentCell.getStringCellValue());
                            break;
                        case 3:
                            declarationTypeRefEntityRefLang.setLang(currentCell.getStringCellValue());
                            break;

                        default:
                            break;
                    }
                    cellIdx++;
                }
                declarationTypeRefEntityRefLangs.add(declarationTypeRefEntityRefLang);
            }
            workbook.close();
            return declarationTypeRefEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<DeclarationTypeRefEntityRefLang> declarationTypeRefEntityRefLangs = excelToDeclarationTypeRef(file.getInputStream());
            List<DeclarationTypeRefEntityRefLang> InvalidDeclarationTypeRefEntityRefLangs = new ArrayList<DeclarationTypeRefEntityRefLang>();
            List<DeclarationTypeRefEntityRefLang> ValidDeclarationTypeRefEntityRefLangs = new ArrayList<DeclarationTypeRefEntityRefLang>();

            int lenght = declarationTypeRefEntityRefLangs.size();
            for (int i = 0; i < lenght; i++) {
                Set<ConstraintViolation<DeclarationTypeRefEntityRefLang>> violations = validateDeclarationType(declarationTypeRefEntityRefLangs.get(i));
                if (violations.isEmpty())
                {
                    ValidDeclarationTypeRefEntityRefLangs.add(declarationTypeRefEntityRefLangs.get(i));
                } else {
                    InvalidDeclarationTypeRefEntityRefLangs.add(declarationTypeRefEntityRefLangs.get(i));
                }
            }
            if (!InvalidDeclarationTypeRefEntityRefLangs.isEmpty()) {

                ByteArrayInputStream out = declarationTypeRefToExcel(InvalidDeclarationTypeRefEntityRefLangs);
                xls = new InputStreamResource(out);

            }

            if (!ValidDeclarationTypeRefEntityRefLangs.isEmpty()) {
                for (DeclarationTypeRefEntityRefLang l : ValidDeclarationTypeRefEntityRefLangs) {
                    DeclarationTypeRef declarationTypeRef = new DeclarationTypeRef();

                    DeclarationTypeRefEntityRefLang declarationTypeRefEntityRefLang = new DeclarationTypeRefEntityRefLang();
                    declarationTypeRef.setCode(l.getCode());
                    DeclarationTypeRef ltemp = declarationTypeRefRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (declarationTypeRefRepository.save(declarationTypeRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.DECLARATIONTYPE_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);

                    } else {
                        Long id = ltemp.getId();
                        declarationTypeRef.setId(id);
                        declarationTypeRefRepository.save(declarationTypeRef);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.DECLARATIONTYPE_REF, langRepository.findByCode(l.getLang()).getId() ,id);
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLangRepository.save(entityRefLang);
                    }

                }
            }
            if (!InvalidDeclarationTypeRefEntityRefLangs.isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(
                                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(xls);

            if (!ValidDeclarationTypeRefEntityRefLangs.isEmpty())
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
        List<DeclarationTypeRef> declarationTypeRefs = declarationTypeRefRepository.findAll();
        ByteArrayInputStream in = null;
                //declarationTypeRefToExcel(declarationTypeRefs);
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<DeclarationTypeRef> declarationTypeRefs = declarationTypeRefRepository.findAll();
        List<DeclarationTypeRefEntityRefLang> declarationTypeRefEntityRefLangs = new ArrayList<DeclarationTypeRefEntityRefLang>();
        for(DeclarationTypeRef u : declarationTypeRefs) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.DECLARATIONTYPE_REF, lang.getId(), u.getId());
            if (entityRefLang!=null) {
                DeclarationTypeRefEntityRefLang declarationTypeRefEntityRefLang = new DeclarationTypeRefEntityRefLang();
                declarationTypeRefEntityRefLang.setCode(u.getCode());
                declarationTypeRefEntityRefLang.setLabel(entityRefLang.getLabel());
                declarationTypeRefEntityRefLang.setDescription(entityRefLang.getDescription());
                declarationTypeRefEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                declarationTypeRefEntityRefLangs.add(declarationTypeRefEntityRefLang);
            }
        }
        ByteArrayInputStream in = declarationTypeRefToExcel(declarationTypeRefEntityRefLangs);
        return in;
    }

    @Override
    public void addDeclarationType(DeclarationTypeRefEntityRefLang declarationTypeRefEntityRefLang){
        DeclarationTypeRef declarationTypeRef = new DeclarationTypeRef();
        declarationTypeRef.setCode(declarationTypeRefEntityRefLang.getCode());
        DeclarationTypeRef ltemp = declarationTypeRefRepository.findByCode(declarationTypeRefEntityRefLang.getCode());
        if (ltemp == null) {
            Long id = (declarationTypeRefRepository.save(declarationTypeRef)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(declarationTypeRefEntityRefLang.getLabel());
            entityRefLang.setDescription(declarationTypeRefEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.DECLARATIONTYPE_REF);
            entityRefLang.setLang(  langRepository.findByCode(declarationTypeRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            declarationTypeRef.setId(id);
            declarationTypeRefRepository.save(declarationTypeRef);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.DECLARATIONTYPE_REF, langRepository.findByCode(declarationTypeRefEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(declarationTypeRefEntityRefLang.getLabel());
            entityRefLang.setDescription(declarationTypeRefEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public DeclarationTypeRefEntityRefLang findDeclarationtype(Long id, String lang){
        DeclarationTypeRefEntityRefLang declarationTypeRefEntityRefLang = new DeclarationTypeRefEntityRefLang();
        DeclarationTypeRef declarationTypeRef = declarationTypeRefRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.DECLARATIONTYPE_REF,langRepository.findByCode(lang).getId(),declarationTypeRef.getId());
        declarationTypeRefEntityRefLang.setCode(declarationTypeRef.getCode());
        declarationTypeRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        declarationTypeRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        declarationTypeRefEntityRefLang.setLang(lang);
        return declarationTypeRefEntityRefLang;
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.DECLARATIONTYPE_REF);
    }

    public Page<DeclarationTypeRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<DeclarationTypeRef> declarationTypeRefs = null;
        if(orderDirection.equals("DESC")){
            declarationTypeRefs = declarationTypeRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            declarationTypeRefs = declarationTypeRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
        Lang lang = langRepository.findByCode(codeLang);
        List<DeclarationTypeRefEntityRefLang> declarationTypeRefEntityRefLangs = new ArrayList<>();
        return declarationTypeRefs.map(declarationTypeRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.DECLARATIONTYPE_REF, lang.getId(), declarationTypeRef.getId());

            DeclarationTypeRefEntityRefLang declarationTypeRefEntityRefLang = new DeclarationTypeRefEntityRefLang();

            declarationTypeRefEntityRefLang.setId(declarationTypeRef.getId());
            declarationTypeRefEntityRefLang.setCode(declarationTypeRef.getCode()!=null?declarationTypeRef.getCode():"");
            declarationTypeRefEntityRefLang.setCreatedBy(declarationTypeRef.getCreatedBy());
            declarationTypeRefEntityRefLang.setCreatedOn(declarationTypeRef.getCreatedOn());
            declarationTypeRefEntityRefLang.setUpdatedBy(declarationTypeRef.getUpdatedBy());
            declarationTypeRefEntityRefLang.setUpdatedOn(declarationTypeRef.getUpdatedOn());

            declarationTypeRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            declarationTypeRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            declarationTypeRefEntityRefLang.setLang(codeLang);
            declarationTypeRefEntityRefLangs.add(declarationTypeRefEntityRefLang);
            return  declarationTypeRefEntityRefLang ;
        });
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang,String lang){
        entityRefLang.setTableRef(TableRef.DECLARATIONTYPE_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.DECLARATIONTYPE_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    public Page<DeclarationTypeRefEntityRefLang> mapLangToRefLangs(Page<DeclarationTypeRef> declarationTypeRefs, String codeLang) {
        List<DeclarationTypeRefEntityRefLang> declarationTypeRefEntityRefLangs = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        return declarationTypeRefs.map(declarationTypeRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.DECLARATIONTYPE_REF, lang.getId(), declarationTypeRef.getId());
            DeclarationTypeRefEntityRefLang declarationTypeRefEntityRefLang = new DeclarationTypeRefEntityRefLang();
            declarationTypeRefEntityRefLang.setId(declarationTypeRef.getId());

            declarationTypeRefEntityRefLang.setCode(declarationTypeRef.getCode() != null ? declarationTypeRef.getCode():"");
            declarationTypeRefEntityRefLang.setCreatedBy(declarationTypeRef.getCreatedBy());
            declarationTypeRefEntityRefLang.setCreatedOn(declarationTypeRef.getCreatedOn());
            declarationTypeRefEntityRefLang.setUpdatedBy(declarationTypeRef.getUpdatedBy());
            declarationTypeRefEntityRefLang.setUpdatedOn(declarationTypeRef.getUpdatedOn());
            declarationTypeRefEntityRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
            declarationTypeRefEntityRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
            declarationTypeRefEntityRefLang.setLang(codeLang);
            declarationTypeRefEntityRefLangs.add(declarationTypeRefEntityRefLang);
            return declarationTypeRefEntityRefLang;
        });
    }

    public Page<DeclarationTypeRefEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
        return mapLangToRefLangs(declarationTypeRefRepository.filterByCodeOrLabel(value,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
    }

    @Override
    public ErrorResponse deleteList(ListOfObject listOfObject) {
        ErrorResponse response = new ErrorResponse();

        try {
            for(Long id : listOfObject.getListOfObject()){
                declarationTypeRefRepository.deleteById(id);
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
    public DeclarationTypeRefBean findById(Long id) {
        DeclarationTypeRef result= declarationTypeRefRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return declarationTypeRefMapper.entityToBean(result);
    }

    @Override
    public Page<DeclarationTypeRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
        return declarationTypeRefRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
    }

    public Page<DeclarationTypeRefBean> getAll(Pageable pageable) {
        Page<DeclarationTypeRef> entities = declarationTypeRefRepository.findAll(pageable);
        Page<DeclarationTypeRefBean> result = entities.map(declarationTypeRefMapper::entityToBean);
        return result;
    }

}
