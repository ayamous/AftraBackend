package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.VersionBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.enums.StatusVersion;
import ma.itroad.aace.eth.coref.model.mapper.VersionMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.VersionRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.VersionRefRepository;
import ma.itroad.aace.eth.coref.service.IVersionService;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.TariffBookRefLang;
import ma.itroad.aace.eth.coref.service.helper.TaxationRefLang;
import ma.itroad.aace.eth.coref.service.helper.VersionRefEntityRefLang;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class VersionServiceImpl extends BaseServiceImpl<VersionRef, VersionBean> implements IVersionService {

    static String SHEET = "VersionRefsSHEET";
    static String[] HEADERs = {"N°", "active", "status", "applicated on", "validated on", "archived on", "LABEL", "DESCRIPTION", "LANGUE"};

    @Autowired
    VersionRefRepository versionRefRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;

    @Autowired
    LangRepository langRepository;

    @Autowired
    VersionMapper versionMapper;

    @Autowired
    private Validator validator;

    @Override
    public List<VersionRef> saveAll(List<VersionRef> versionRefs) {
        if (!versionRefs.isEmpty()) {
            return versionRefRepository.saveAll(versionRefs);
        }
        return null;
    }

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.VERSION_REF, id);
            for(EntityRefLang entityRefLang:entityRefLangs ){
                entityRefLangRepository.delete(entityRefLang);
            }
            versionRefRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return  response;
    }

    @Override
    public ErrorResponse deleteInternationalisation(String codeLang, Long versionRefId) {

        ErrorResponse response = new ErrorResponse();
        try {

            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_REF, lang.getId(), versionRefId);

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
            List<VersionRefEntityRefLang> versionRefEntityRefLangs = excelToVersionRef(file.getInputStream());
            if (!versionRefEntityRefLangs.isEmpty()) {
                for (VersionRefEntityRefLang l : versionRefEntityRefLangs) {
                    VersionRef versionRef = new VersionRef();

                    versionRef.setVersion(l.getVersion());
                    versionRef.setEnabled(l.isEnabled());
                    versionRef.setStatusVersion(l.getStatusVersion());
                    versionRef.setApplicatedOn(l.getApplicatedOn());
                    versionRef.setValidatedOn(l.getValidatedOn());
                    versionRef.setArchivedOn(l.getArchivedOn());

                    VersionRef ltemp = versionRefRepository.findByVersion(l.getVersion());
                    if (ltemp == null) {
                        Long id = (versionRefRepository.save(versionRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.VERSION_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        versionRef.setId( ltemp.getId());
                        versionRefRepository.save(versionRef);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_REF, langRepository.findByCode(l.getLang()).getId() ,versionRef.getId());
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.VERSION_REF);
                            entityRefLang.setRefId(versionRef.getId());
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

    /*
    private LocalDate convertToLocalDateViaInstant(String dateToConvert) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = format.parse(dateToConvert);

        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
    */

    @Override
    public List<VersionRefEntityRefLang> excelToVersionRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            List<VersionRefEntityRefLang> versionRefEntityRefLangs = new ArrayList<VersionRefEntityRefLang>();
            Row currentRow;
            VersionRefEntityRefLang versionRefEntityRefLang ;
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                currentRow= sheet.getRow(rowNum);
                versionRefEntityRefLang = new VersionRefEntityRefLang();
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            versionRefEntityRefLang.setVersion((Util.cellValue(currentCell)));
                            break;
                        case 1:
                            versionRefEntityRefLang.setEnabled(currentCell.getBooleanCellValue());
                            break;
                        case 2:
                            StatusVersion statusVersion = StatusVersion.valueOf(Util.cellValue(currentCell));
                            versionRefEntityRefLang.setStatusVersion(statusVersion);
                            break;
                        case 3:
                            versionRefEntityRefLang.setApplicatedOn(Util.cellValue(currentCell) != null?LocalDate.parse(Util.cellValue(currentCell)):null);
                            break;
                        case 4:
                            versionRefEntityRefLang.setValidatedOn(Util.cellValue(currentCell) != null?LocalDate.parse(Util.cellValue(currentCell)):null);
                            break;
                        case 5:
                            versionRefEntityRefLang.setArchivedOn(Util.cellValue(currentCell) != null?LocalDate.parse(Util.cellValue(currentCell)):null);
                            break;
                        case 6:
                            versionRefEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 7:
                            versionRefEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 8:
                            versionRefEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;
                        default:
                            break;
                    }
                }
                versionRefEntityRefLangs.add(versionRefEntityRefLang);
            }
            workbook.close();
            return versionRefEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
/*
    @Override
    public List<> (InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<VersionRefEntityRefLang> versionRefEntityRefLangs = new ArrayList<VersionRefEntityRefLang>();
            Row currentRow = rows.next();
            int rowNumber = 0;
            while (rows.hasNext()) {
                currentRow = rows.next();

                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                VersionRefEntityRefLang versionRefEntityRefLang = new VersionRefEntityRefLang();
                int cellIdx = 0;
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);

                    switch (cellIdx) {
                        case 0:
                            String data = null;

                            if (currentCell.getCellType() == Cell.CELL_TYPE_STRING)
                                data = currentCell.getStringCellValue();
                            if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                data = String.valueOf(currentCell.getNumericCellValue());

                            versionRefEntityRefLang.setVersion(data);
                           break;
                        case 1:
                           versionRefEntityRefLang.setEnabled(currentCell.getBooleanCellValue());
                            break;
                        case 2:
                            StatusVersion statusVersion = StatusVersion.valueOf(currentCell.getStringCellValue());
                            versionRefEntityRefLang.setStatusVersion(statusVersion);
                            break;
                        case 3:
                            versionRefEntityRefLang.setApplicatedOn(LocalDate.parse(Util.cellValue(currentCell)));
                            break;
                        case 4:
                            versionRefEntityRefLang.setValidatedOn(LocalDate.parse(Util.cellValue(currentCell)));
                            break;
                        case 5:
                            versionRefEntityRefLang.setArchivedOn(LocalDate.parse(Util.cellValue(currentCell)));
                            break;
                        case 6:
                            versionRefEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 7:
                            versionRefEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 8:
                            versionRefEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                versionRefEntityRefLangs.add(versionRefEntityRefLang);
            }
            workbook.close();
            return versionRefEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
    */

    @Override
    public ByteArrayInputStream load() {
        List<VersionRef> versionRefs = versionRefRepository.findAll();
        ByteArrayInputStream in = null;
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<VersionRef> versionRefs = versionRefRepository.findAll();
        List<VersionRefEntityRefLang> versionRefEntityRefLangs = new ArrayList<VersionRefEntityRefLang>();

        for(VersionRef u : versionRefs) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_REF, lang.getId(), u.getId());
            if (entityRefLang!=null) {
                VersionRefEntityRefLang versionRefEntityRefLang = new VersionRefEntityRefLang();
                versionRefEntityRefLang.setVersion(u.getVersion());
                versionRefEntityRefLang.setEnabled(u.isEnabled());
                versionRefEntityRefLang.setStatusVersion(u.getStatusVersion());
                versionRefEntityRefLang.setApplicatedOn(u.getApplicatedOn());
                versionRefEntityRefLang.setValidatedOn(u.getValidatedOn());
                versionRefEntityRefLang.setArchivedOn(u.getArchivedOn());

                versionRefEntityRefLang.setLabel(entityRefLang.getLabel());
                versionRefEntityRefLang.setDescription(entityRefLang.getDescription());
                versionRefEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                versionRefEntityRefLangs.add(versionRefEntityRefLang);
            }
        }
        ByteArrayInputStream in = versionsToExcel(versionRefEntityRefLangs);
        return in;
    }

    @Override
    public ByteArrayInputStream versionsToExcel(List<VersionRefEntityRefLang> versionRefEntityRefLangs) {
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
                if(!versionRefEntityRefLangs.isEmpty()){

                    for (VersionRefEntityRefLang versionRefEntityRefLang : versionRefEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(versionRefEntityRefLang.getVersion());
                        row.createCell(1).setCellValue(versionRefEntityRefLang.isEnabled());
                        row.createCell(2).setCellValue(versionRefEntityRefLang.getStatusVersion().toString());
                        row.createCell(3).setCellValue(versionRefEntityRefLang.getApplicatedOn().toString());
                        row.createCell(4).setCellValue(versionRefEntityRefLang.getValidatedOn().toString());
                        row.createCell(5).setCellValue(versionRefEntityRefLang.getArchivedOn().toString());

                        row.createCell(6).setCellValue(versionRefEntityRefLang.getLabel());
                        row.createCell(7).setCellValue(versionRefEntityRefLang.getDescription());
                        row.createCell(8).setCellValue(versionRefEntityRefLang.getLang());
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
    public void addVersionRef(VersionRefEntityRefLang versionRefEntityRefLang){
        VersionRef versionRef = new VersionRef();
        versionRef.setVersion(versionRefEntityRefLang.getVersion());
        versionRef.setEnabled(versionRefEntityRefLang.isEnabled());
        versionRef.setStatusVersion(versionRefEntityRefLang.getStatusVersion());
        versionRef.setApplicatedOn(versionRefEntityRefLang.getApplicatedOn());
        versionRef.setValidatedOn(versionRefEntityRefLang.getValidatedOn());
        versionRef.setArchivedOn(versionRefEntityRefLang.getArchivedOn());
        VersionRef ltemp = versionRefRepository.findByVersion(versionRefEntityRefLang.getVersion());
        if (ltemp == null) {
            Long id = (versionRefRepository.save(versionRef)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(versionRefEntityRefLang.getLabel());
            entityRefLang.setDescription(versionRefEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.VERSION_REF);
            entityRefLang.setLang(  langRepository.findByCode(versionRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            versionRef.setId(id);
            versionRefRepository.save(versionRef);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_REF, langRepository.findByCode(versionRefEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(versionRefEntityRefLang.getLabel());
            entityRefLang.setDescription(versionRefEntityRefLang.getDescription());
            entityRefLang.setLang(langRepository.findByCode(versionRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        }
    }


    @Override
    public VersionRefEntityRefLang findVersionRef(Long id, String lang){
        VersionRefEntityRefLang versionRefEntityRefLang = new VersionRefEntityRefLang();
        VersionRef versionRef = versionRefRepository.findOneById(id);

        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_REF,langRepository.findByCode(lang).getId(),versionRef.getId());

        versionRefEntityRefLang.setVersion(versionRef.getVersion());
        versionRefEntityRefLang.setEnabled(versionRef.isEnabled());
        versionRefEntityRefLang.setStatusVersion(versionRef.getStatusVersion());
        versionRefEntityRefLang.setApplicatedOn(versionRef.getApplicatedOn());
        versionRefEntityRefLang.setValidatedOn(versionRef.getValidatedOn());
        versionRefEntityRefLang.setArchivedOn(versionRef.getArchivedOn());
        versionRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        versionRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        versionRefEntityRefLang.setLang(lang);
        return versionRefEntityRefLang;
    }

    public Page<VersionBean> getAll(Pageable pageable) {
        Page<VersionRef> entities = versionRefRepository.findAll(pageable);
        Page<VersionBean> result = entities.map(versionMapper::entityToBean);
        return result;

    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.VERSION_REF);
    }
/*
    public Page<VersionRefEntityRefLang> getAll(final int page, final int size, String codeLang) {
        Page<VersionRef> versionRefs = versionRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn")));
        Lang lang = langRepository.findByCode(codeLang);
        List<VersionRefEntityRefLang> versionRefEntityRefLangs = new ArrayList<>();
        for (VersionRef u : versionRefs) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_REF, lang.getId(), u.getId());

            VersionRefEntityRefLang versionRefEntityRefLang = new VersionRefEntityRefLang();

            versionRefEntityRefLang.setId(u.getId());
            versionRefEntityRefLang.setVersion(u.getVersion()!=null?u.getVersion():"");
            versionRefEntityRefLang.setEnabled(u.getEnabled()!=null?u.getEnabled():"");
            versionRefEntityRefLang.setStatus(u.getStatus());
            versionRefEntityRefLang.setApplicatedOn(u.getApplicatedOn());
            versionRefEntityRefLang.setValidatedOn(u.getValidatedOn());
            versionRefEntityRefLang.setArchivedOn(u.getArchivedOn());

            versionRefEntityRefLang.setCreatedBy(u.getCreatedBy());
            versionRefEntityRefLang.setCreatedOn(u.getCreatedOn());
            versionRefEntityRefLang.setUpdatedBy(u.getUpdatedBy());
            versionRefEntityRefLang.setUpdatedOn(u.getUpdatedOn());

            versionRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            versionRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            versionRefEntityRefLang.setLang(codeLang);
            versionRefEntityRefLangs.add(versionRefEntityRefLang);
        }
        final Page<VersionRefEntityRefLang> versionRefEntityRefLangPage = new PageImpl<>(versionRefEntityRefLangs);
        return versionRefEntityRefLangPage;
    }
    */

    public Page<VersionRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<VersionRef> versionRefs = null;
        if(orderDirection.equals("DESC")){
            versionRefs = versionRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            versionRefs = versionRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
        Lang lang = langRepository.findByCode(codeLang);
        List<VersionRefEntityRefLang> versionRefEntityRefLangs = new ArrayList<>();

        return versionRefs.map(versionRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_REF, lang.getId(), versionRef.getId());

            VersionRefEntityRefLang versionRefEntityRefLang = new VersionRefEntityRefLang();

            versionRefEntityRefLang.setId(versionRef.getId());
            versionRefEntityRefLang.setVersion(versionRef.getVersion()!=null ?versionRef.getVersion():"");
            versionRefEntityRefLang.setEnabled(versionRef.isEnabled());
            versionRefEntityRefLang.setStatusVersion(versionRef.getStatusVersion());
            versionRefEntityRefLang.setApplicatedOn(versionRef.getApplicatedOn());
            versionRefEntityRefLang.setValidatedOn(versionRef.getValidatedOn());
            versionRefEntityRefLang.setArchivedOn(versionRef.getArchivedOn());

            versionRefEntityRefLang.setCreatedBy(versionRef.getCreatedBy());
            versionRefEntityRefLang.setCreatedOn(versionRef.getCreatedOn());
            versionRefEntityRefLang.setUpdatedBy(versionRef.getUpdatedBy());
            versionRefEntityRefLang.setUpdatedOn(versionRef.getUpdatedOn());

            versionRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            versionRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

            versionRefEntityRefLang.setLang(codeLang);

            versionRefEntityRefLangs.add(versionRefEntityRefLang);

            return  versionRefEntityRefLang ;
        });
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang,String lang){
        entityRefLang.setTableRef(TableRef.VERSION_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    public Page<VersionRefEntityRefLang> mapToRefLangs(Page<VersionRef> versionRefs, String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<VersionRefEntityRefLang> versionRefEntityRefLangs = new ArrayList<>();

        return versionRefs.map(versionRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_REF, lang.getId(), versionRef.getId());

            VersionRefEntityRefLang versionRefEntityRefLang = new VersionRefEntityRefLang();

            versionRefEntityRefLang.setId(versionRef.getId());
            versionRefEntityRefLang.setVersion(versionRef.getVersion()!=null ?versionRef.getVersion():"");
            versionRefEntityRefLang.setEnabled(versionRef.isEnabled());
            versionRefEntityRefLang.setStatusVersion(versionRef.getStatusVersion());
            versionRefEntityRefLang.setApplicatedOn(versionRef.getApplicatedOn());
            versionRefEntityRefLang.setValidatedOn(versionRef.getValidatedOn());
            versionRefEntityRefLang.setArchivedOn(versionRef.getArchivedOn());

            versionRefEntityRefLang.setCreatedBy(versionRef.getCreatedBy());
            versionRefEntityRefLang.setCreatedOn(versionRef.getCreatedOn());
            versionRefEntityRefLang.setUpdatedBy(versionRef.getUpdatedBy());
            versionRefEntityRefLang.setUpdatedOn(versionRef.getUpdatedOn());

            versionRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            versionRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

            versionRefEntityRefLang.setLang(codeLang);

            versionRefEntityRefLangs.add(versionRefEntityRefLang);

            return  versionRefEntityRefLang;
        });
    }

    public Page<VersionRefEntityRefLang> filterByReferenceOrLabel(String value, Pageable pageable, String codeLang)  {
        return mapToRefLangs(versionRefRepository.filterByVersionOrLabel(value, langRepository.findByCode(codeLang).getId(), pageable), codeLang);
    }

    @Override
    public VersionBean findById(Long id) {
        VersionRef result= versionRefRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return versionMapper.entityToBean(result);
    }

    @Override
    public Page<VersionRefEntityRefLangProjection> filterByReferenceOrLabelProjection(String value, Pageable pageable, String lang) {
        return versionRefRepository.filterByVersionOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
    }

    public Set<ConstraintViolation<VersionRefEntityRefLang>> validateItems(VersionRefEntityRefLang versionRefEntityRefLang) {
        Set<ConstraintViolation<VersionRefEntityRefLang>> violations = validator.validate(versionRefEntityRefLang);
        return violations;
    }

    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<VersionRefEntityRefLang> itemsList = excelToVersionRef(file.getInputStream());
            List<VersionRefEntityRefLang> invalidItems = new ArrayList<VersionRefEntityRefLang>();
            List<VersionRefEntityRefLang> validItems = new ArrayList<VersionRefEntityRefLang>();

            int lenght = itemsList.size();

            for (int i = 0; i < lenght; i++) {

                Set<ConstraintViolation<VersionRefEntityRefLang>> violations = validateItems(itemsList.get(i));
                if (violations.isEmpty())

                {
                    validItems.add(itemsList.get(i));
                } else {
                    invalidItems.add(itemsList.get(i));
                }
            }

            if (!invalidItems.isEmpty()) {

                ByteArrayInputStream out = versionsToExcel(invalidItems);
                xls = new InputStreamResource(out);

            }

            if (!validItems.isEmpty()) {
                for (VersionRefEntityRefLang l : validItems) {
                    VersionRef versionRef = new VersionRef();

                    versionRef.setVersion(l.getVersion());
                    versionRef.setEnabled(l.isEnabled());
                    versionRef.setStatusVersion(l.getStatusVersion());
                    versionRef.setApplicatedOn(l.getApplicatedOn());
                    versionRef.setValidatedOn(l.getValidatedOn());
                    versionRef.setArchivedOn(l.getArchivedOn());

                    VersionRef ltemp = versionRefRepository.findByVersion(l.getVersion());
                    if (ltemp == null) {
                        Long id = (versionRefRepository.save(versionRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.VERSION_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        versionRef.setId( ltemp.getId());
                        versionRefRepository.save(versionRef);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_REF, langRepository.findByCode(l.getLang()).getId() ,versionRef.getId());
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.VERSION_REF);
                            entityRefLang.setRefId(versionRef.getId());
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
