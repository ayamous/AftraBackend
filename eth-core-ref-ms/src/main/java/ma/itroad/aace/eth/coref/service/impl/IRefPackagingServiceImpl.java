package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.RefCurrencyBean;
import ma.itroad.aace.eth.coref.model.bean.RefPackagingBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.entity.RefCurrency;
import ma.itroad.aace.eth.coref.model.entity.RefPackaging;
import ma.itroad.aace.eth.coref.model.mapper.RefCurrencyMapper;
import ma.itroad.aace.eth.coref.model.mapper.RefPackagingMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.RefPackagingEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.RefPackagingRepository;
import ma.itroad.aace.eth.coref.service.IRefPackagingService;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.RefPackagingEntityRefLang;
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
public class IRefPackagingServiceImpl extends BaseServiceImpl<RefPackaging, RefPackagingBean> implements IRefPackagingService {

    static String[] HEADERs = {"CODE", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "PackagingSHEET";

    @Autowired
    private RefPackagingRepository refPackagingRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    LangRepository langRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;

    @Autowired
    private RefPackagingMapper refPackagingMapper;

    @Autowired
    private Validator validator;

    @Override
    public List<RefPackaging> saveAll(List<RefPackaging> refPackagings) {
        if(!refPackagings.isEmpty()){
            return refPackagingRepository.saveAll(refPackagings);
        }
        return null;
    }

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.PACKAGING_REF, id);
            for(EntityRefLang entityRefLang:entityRefLangs ){
                entityRefLangRepository.delete(entityRefLang);
            }
            refPackagingRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return  response;
    }

    @Override
    public ErrorResponse deleteInternationalisation(String codeLang, Long refPackagingId) {
        ErrorResponse response = new ErrorResponse();
        try {

            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PACKAGING_REF, lang.getId(), refPackagingId);
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
    public ByteArrayInputStream refPackagingsToExcel(List<RefPackagingEntityRefLang> packagingEntityRefLangs) {
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
                if(!packagingEntityRefLangs.isEmpty()){

                    for (RefPackagingEntityRefLang packagingEntityRefLang : packagingEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(packagingEntityRefLang.getCode());
                        row.createCell(1).setCellValue(packagingEntityRefLang.getLabel());
                        row.createCell(2).setCellValue(packagingEntityRefLang.getDescription());
                        row.createCell(3).setCellValue(packagingEntityRefLang.getLang());
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
    public Set<ConstraintViolation<RefPackagingEntityRefLang>> validatePackaging(RefPackagingEntityRefLang refPackagingEntityRefLang) {
        Set<ConstraintViolation<RefPackagingEntityRefLang>> violations = validator.validate(refPackagingEntityRefLang);
        return violations;
    }

    @Override
    public List<RefPackagingEntityRefLang> excelToRefPackaging(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<RefPackaging> packagings = new ArrayList<RefPackaging>();
            List<RefPackagingEntityRefLang> packagingEntityRefLangs = new ArrayList<RefPackagingEntityRefLang>();
            Row currentRow = rows.next();
            while (rows.hasNext()) {
                currentRow = rows.next();

                RefPackagingEntityRefLang packagingEntityRefLang = new RefPackagingEntityRefLang();
                int cellIdx = 0;
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);

                    switch (colNum) {
                        case 0:
                            packagingEntityRefLang.setCode(Util.cellValue(currentCell));
                            break;
                        case 1:
                            packagingEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 2:
                            packagingEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 3:
                            packagingEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;

                        default:
                            break;
                    }
                    cellIdx++;
                }
                packagingEntityRefLangs.add(packagingEntityRefLang);
            }
            workbook.close();
            return packagingEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<RefPackagingEntityRefLang> packagingEntityRefLangs = excelToRefPackaging(file.getInputStream());
            List<RefPackagingEntityRefLang> InvalidRefPackagingEntityRefLangs = new ArrayList<RefPackagingEntityRefLang>();
            List<RefPackagingEntityRefLang> ValidRefPackagingEntityRefLangs = new ArrayList<RefPackagingEntityRefLang>();

            int lenght = packagingEntityRefLangs.size();
            for (int i = 0; i < lenght; i++) {
                Set<ConstraintViolation<RefPackagingEntityRefLang>> violations = validatePackaging(packagingEntityRefLangs.get(i));
                if (violations.isEmpty())
                {
                    ValidRefPackagingEntityRefLangs.add(packagingEntityRefLangs.get(i));
                } else {
                    InvalidRefPackagingEntityRefLangs.add(packagingEntityRefLangs.get(i));
                }
            }
            if (!InvalidRefPackagingEntityRefLangs.isEmpty()) {

                ByteArrayInputStream out = refPackagingsToExcel(InvalidRefPackagingEntityRefLangs);
                xls = new InputStreamResource(out);

            }
            if (!ValidRefPackagingEntityRefLangs.isEmpty()) {
                for (RefPackagingEntityRefLang l : ValidRefPackagingEntityRefLangs) {
                    RefPackaging packaging = new RefPackaging();
                    packaging.setCode(l.getCode());
                    RefPackaging ltemp = refPackagingRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (refPackagingRepository.save(packaging)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.PACKAGING_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        Long id = ltemp.getId();
                        packaging.setId(id);
                        refPackagingRepository.save(packaging);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PACKAGING_REF, langRepository.findByCode(l.getLang()).getId() ,id);
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.PACKAGING_REF);
                            entityRefLang.setRefId(packaging.getId());
                        }
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    }

                }
            }
            if (!InvalidRefPackagingEntityRefLangs.isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(
                                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(xls);

            if (!ValidRefPackagingEntityRefLangs.isEmpty())
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
        List<RefPackaging> langs = refPackagingRepository.findAll();
        ByteArrayInputStream in = null;
                //refPackagingsToExcel(langs);
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<RefPackaging> packagings = refPackagingRepository.findAll();
        List<RefPackagingEntityRefLang> packagingEntityRefLangs = new ArrayList<RefPackagingEntityRefLang>();

        for(RefPackaging u : packagings) {


            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PACKAGING_REF, lang.getId(), u.getId());
            if (entityRefLang!=null) {
                RefPackagingEntityRefLang packagingEntityRefLang = new RefPackagingEntityRefLang();
                packagingEntityRefLang.setCode(u.getCode());
                packagingEntityRefLang.setLabel(entityRefLang.getLabel());
                packagingEntityRefLang.setDescription(entityRefLang.getDescription());
                packagingEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                packagingEntityRefLangs.add(packagingEntityRefLang);
            }
        }
        ByteArrayInputStream in = refPackagingsToExcel(packagingEntityRefLangs);
        return in;
    }

    @Override
    public void addRefPackaging(RefPackagingEntityRefLang refPackagingEntityRefLang){
        RefPackaging refPackaging = new RefPackaging();
        refPackaging.setCode(refPackagingEntityRefLang.getCode());
        RefPackaging ltemp = refPackagingRepository.findByCode(refPackagingEntityRefLang.getCode());
        if (ltemp == null) {
            Long id = (refPackagingRepository.save(refPackaging)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(refPackagingEntityRefLang.getLabel());
            entityRefLang.setDescription(refPackagingEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.PACKAGING_REF);
            entityRefLang.setLang(  langRepository.findByCode(refPackagingEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            refPackaging.setId(id);
            refPackagingRepository.save(refPackaging);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PACKAGING_REF, langRepository.findByCode(refPackagingEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(refPackagingEntityRefLang.getLabel());
            entityRefLang.setDescription(refPackagingEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public RefPackagingEntityRefLang findRefPackaging(Long id, String lang){
        RefPackagingEntityRefLang refPackagingEntityRefLang = new RefPackagingEntityRefLang();
        RefPackaging refPackaging = refPackagingRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PACKAGING_REF,langRepository.findByCode(lang).getId(),refPackaging.getId());
        refPackagingEntityRefLang.setCode(refPackaging.getCode());
        refPackagingEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        refPackagingEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        refPackagingEntityRefLang.setLang(lang);
        return refPackagingEntityRefLang;
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.PACKAGING_REF);
    }

    public Page<RefPackagingBean> getAll(Pageable pageable) {
        Page<RefPackaging> entities = refPackagingRepository.findAll(pageable);
        Page<RefPackagingBean> result = entities.map(refPackagingMapper::entityToBean);
        return result;
    }

    public Page<RefPackagingEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<RefPackaging> refPackagings = null;
        if(orderDirection.equals("DESC")){
            refPackagings = refPackagingRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            refPackagings = refPackagingRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }

        Lang lang = langRepository.findByCode(codeLang);
        List<RefPackagingEntityRefLang> refPackagingEntityRefLangs = new ArrayList<>();
        return refPackagings.map(refPackaging -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PACKAGING_REF, lang.getId(), refPackaging.getId());

            RefPackagingEntityRefLang refPackagingEntityRefLang = new RefPackagingEntityRefLang();

            refPackagingEntityRefLang.setId(refPackaging.getId());
            refPackagingEntityRefLang.setCode(refPackaging.getCode()!=null?refPackaging.getCode():"");
            refPackagingEntityRefLang.setCreatedBy(refPackaging.getCreatedBy());
            refPackagingEntityRefLang.setCreatedOn(refPackaging.getCreatedOn());
            refPackagingEntityRefLang.setUpdatedBy(refPackaging.getUpdatedBy());
            refPackagingEntityRefLang.setUpdatedOn(refPackaging.getUpdatedOn());

            refPackagingEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            refPackagingEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            refPackagingEntityRefLang.setLang(codeLang);
            refPackagingEntityRefLangs.add(refPackagingEntityRefLang);
            return  refPackagingEntityRefLang ;
        });
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang,String lang){
        entityRefLang.setTableRef(TableRef.PACKAGING_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PACKAGING_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    public Page<RefPackagingEntityRefLang> mapLangToRefLangs(Page<RefPackaging> refPackagings, String codeLang) {
        List<RefPackagingEntityRefLang> refPackagingEntityRefLangs = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        return refPackagings.map(refPackaging -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PACKAGING_REF, lang.getId(), refPackaging.getId());
            RefPackagingEntityRefLang refPackagingEntityRefLang = new RefPackagingEntityRefLang();
            refPackagingEntityRefLang.setId(refPackaging.getId());

            refPackagingEntityRefLang.setCode(refPackaging.getCode() != null ? refPackaging.getCode() : "");
            refPackagingEntityRefLang.setCreatedBy(refPackaging.getCreatedBy());
            refPackagingEntityRefLang.setCreatedOn(refPackaging.getCreatedOn());
            refPackagingEntityRefLang.setUpdatedBy(refPackaging.getUpdatedBy());
            refPackagingEntityRefLang.setUpdatedOn(refPackaging.getUpdatedOn());
            refPackagingEntityRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
            refPackagingEntityRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
            refPackagingEntityRefLang.setLang(codeLang);
            refPackagingEntityRefLangs.add(refPackagingEntityRefLang);
            return refPackagingEntityRefLang;
        });
    }

    public Page<RefPackagingEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
        return mapLangToRefLangs(refPackagingRepository.filterByCodeOrLabel(value,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
    }

    @Override
    public ErrorResponse deleteList(ListOfObject listOfObject) {
        ErrorResponse response = new ErrorResponse();

        try {
            for(Long id : listOfObject.getListOfObject()){
                delete(id);
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
    public RefPackagingBean findById(Long id) {
        RefPackaging result= refPackagingRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return refPackagingMapper.entityToBean(result);
    }

    @Override
    public Page<RefPackagingEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
        return refPackagingRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
    }

}
