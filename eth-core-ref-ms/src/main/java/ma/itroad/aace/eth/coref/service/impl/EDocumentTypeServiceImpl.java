package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.EDocumentTypeBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.EDocumentTypeMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.EDocTypeRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.EDocumentTypeRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.service.IEDocumentTypeService;
import ma.itroad.aace.eth.coref.service.helper.EDocTypeRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.TaxationRefLang;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class EDocumentTypeServiceImpl extends BaseServiceImpl<EDocumentType, EDocumentTypeBean> implements IEDocumentTypeService   {

    static String[] HEADERs = {"CODE DU DOCUMENT", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "edocumentType";



    @Autowired
    private EDocumentTypeMapper eDocumentTypeMapper;

    @Autowired
    private EDocumentTypeRepository eDocumentTypeRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    LangRepository langRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;

    @Override
    public List<EDocumentType> saveAll(List<EDocumentType> eDocumentTypes) {
        if (!eDocumentTypes.isEmpty()) {
            return eDocumentTypeRepository.saveAll(eDocumentTypes);
        }
        return null;
    }

    @Override
    @Transactional
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.EDOCUMENT_TYPE, id);
            for (EntityRefLang entityRefLang : entityRefLangs) {
                entityRefLangRepository.delete(entityRefLang);
            }
            eDocumentTypeRepository.deleteById(id);
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
                eDocumentTypeRepository.deleteById(id);
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
    public ErrorResponse deleteInternationalisation(String codeLang, Long eDocumentTypeId) {
        ErrorResponse response = new ErrorResponse();
        try {

            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EDOCUMENT_TYPE, lang.getId(), eDocumentTypeId);

            entityRefLangRepository.delete(entityRefLang);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");

        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return response;
    }

    public Page<EDocumentTypeBean> getAll(Pageable pageable) {
        Page<EDocumentType> entities = eDocumentTypeRepository.findAll(pageable);
        return entities.map(eDocumentTypeMapper::entityToBean);
    }

    @Override
    public EDocTypeRefEntityRefLang findEDocumentTypeRefLang(Long id, String lang) {
        EDocTypeRefEntityRefLang eDocTypeRefEntityRefLang = new EDocTypeRefEntityRefLang();
        EDocumentType edocumentType = eDocumentTypeRepository.findOneById(id);
        EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EDOCUMENT_TYPE, langRepository.findByCode(lang).getId(), edocumentType.getId());
        eDocTypeRefEntityRefLang.setCode(edocumentType.getCode());
        eDocTypeRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        eDocTypeRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        eDocTypeRefEntityRefLang.setLang(lang);
        return eDocTypeRefEntityRefLang;
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
        entityRefLang.setTableRef(TableRef.EDOCUMENT_TYPE);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EDOCUMENT_TYPE, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    @Override
    public Page<EDocTypeRefEntityRefLang> getAll(int page, int size, String codeLang, String orderDirection) {
        Page<EDocumentType> eDocumentTypes = null;
        if(orderDirection.equals("DESC")){
            eDocumentTypes = eDocumentTypeRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            eDocumentTypes = eDocumentTypeRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
        return  mapCountryRefsToRefLangs(eDocumentTypes, codeLang);
    }



    public Page<EDocTypeRefEntityRefLang> mapCountryRefsToRefLangs(Page<EDocumentType> eDocumentTypes , String codeLang){
        Lang lang = langRepository.findByCode(codeLang);
        return eDocumentTypes.map(docType -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EDOCUMENT_TYPE, lang.getId(), docType.getId());
            EDocTypeRefEntityRefLang eDocTypeRefEntityRefLang = new EDocTypeRefEntityRefLang();
            eDocTypeRefEntityRefLang.setId(docType.getId());
            eDocTypeRefEntityRefLang.setCode(docType!=null ?docType.getCode():"");
            eDocTypeRefEntityRefLang.setCreatedBy(docType.getCreatedBy());
            eDocTypeRefEntityRefLang.setCreatedOn(docType.getCreatedOn());
            eDocTypeRefEntityRefLang.setUpdatedBy(docType.getUpdatedBy());
            eDocTypeRefEntityRefLang.setUpdatedOn(docType.getUpdatedOn());
            eDocTypeRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            eDocTypeRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            eDocTypeRefEntityRefLang.setLang(codeLang);
            return  eDocTypeRefEntityRefLang;
        });
    }



    @Override
    public ByteArrayInputStream eDocumentTypeRefsToExcel(List<EDocTypeRefEntityRefLang> eDocTypeRefEntityRefLangs) {
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
                if (!eDocTypeRefEntityRefLangs.isEmpty()) {

                    for (EDocTypeRefEntityRefLang eDocTypeRefEntityRefLang : eDocTypeRefEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(eDocTypeRefEntityRefLang.getCode());
                        row.createCell(1).setCellValue(eDocTypeRefEntityRefLang.getLabel());
                        row.createCell(2).setCellValue(eDocTypeRefEntityRefLang.getDescription());
                        row.createCell(3).setCellValue(eDocTypeRefEntityRefLang.getLang());
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



    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.EDOCUMENT_TYPE);
    }


    @Override
    public List<EDocTypeRefEntityRefLang> excelToDocTypeRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<EDocTypeRefEntityRefLang> eDocTypeRefEntityRefLangs = new ArrayList<EDocTypeRefEntityRefLang>();
            Row currentRow = rows.next();
            while (rows.hasNext()) {
                currentRow = rows.next();
                EDocTypeRefEntityRefLang eDocTypeRefEntityRefLang = new EDocTypeRefEntityRefLang();
                int cellIdx = 0;
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            eDocTypeRefEntityRefLang.setCode(currentCell.getStringCellValue());
                            break;
                        case 1:
                            eDocTypeRefEntityRefLang.setLabel(currentCell.getStringCellValue());
                            break;
                        case 2:
                            eDocTypeRefEntityRefLang.setDescription(currentCell.getStringCellValue());
                            break;
                        case 3:
                            eDocTypeRefEntityRefLang.setLang(currentCell.getStringCellValue());
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                eDocTypeRefEntityRefLangs.add(eDocTypeRefEntityRefLang);
            }
            workbook.close();
            return eDocTypeRefEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }


    @Override
    public void saveFromExcel(MultipartFile file) {
        try {
            List<EDocTypeRefEntityRefLang> eDocTypeRefEntityRefLangs = excelToDocTypeRef(file.getInputStream());
            if (!eDocTypeRefEntityRefLangs.isEmpty()) {
                for (EDocTypeRefEntityRefLang l : eDocTypeRefEntityRefLangs) {
                    EDocumentType documentType = new EDocumentType();
                    documentType.setCode(l.getCode());
                    EDocumentType ltemp = eDocumentTypeRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (eDocumentTypeRepository.save(documentType)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.EDOCUMENT_TYPE);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        documentType.setId(ltemp.getId());
                        eDocumentTypeRepository.save(documentType);
                        EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EDOCUMENT_TYPE,
                                langRepository.findByCode(l.getLang()).getId(), ltemp.getId());
                        if(entityRefLang==null){
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setRefId(documentType.getId());
                            entityRefLang.setTableRef(TableRef.EDOCUMENT_TYPE);
                        }
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLangRepository.save(entityRefLang);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }


    @Override
    public ByteArrayInputStream load() {
        List<EDocumentType> countryRefs = eDocumentTypeRepository.findAll();
        ByteArrayInputStream in = null;
        //countryRefsToExcel(countryRefs);
        return in;
    }


    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<EDocumentType> eDocumentTypes = eDocumentTypeRepository.findAll();
        List<EDocTypeRefEntityRefLang> eDocTypeRefEntityRefLangs = new ArrayList<EDocTypeRefEntityRefLang>();

        for (EDocumentType u : eDocumentTypes) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EDOCUMENT_TYPE, lang.getId(), u.getId());
            if (entityRefLang != null) {
                EDocTypeRefEntityRefLang eDocTypeRefEntityRefLang = new EDocTypeRefEntityRefLang();
                eDocTypeRefEntityRefLang.setCode(u.getCode());
                eDocTypeRefEntityRefLang.setLabel(entityRefLang.getLabel());
                eDocTypeRefEntityRefLang.setDescription(entityRefLang.getDescription());
                eDocTypeRefEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                eDocTypeRefEntityRefLangs.add(eDocTypeRefEntityRefLang);
            }
        }
        ByteArrayInputStream in = eDocumentTypeRefsToExcel(eDocTypeRefEntityRefLangs);
        return in;
    }

    @Override
    @Transactional
    public void addEDocType(EDocTypeRefEntityRefLang eDocTypeRefEntityRefLang) {
        EDocumentType eDocumentType = new EDocumentType();
        eDocumentType.setCode(eDocTypeRefEntityRefLang.getCode());
        eDocumentType.setDocType(eDocTypeRefEntityRefLang.getDocumentType());

        EDocumentType ltemp = eDocumentTypeRepository.findByCode(eDocTypeRefEntityRefLang.getCode());
        if (ltemp == null) {
            Long id = (eDocumentTypeRepository.save(eDocumentType)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(eDocTypeRefEntityRefLang.getLabel());
            entityRefLang.setDescription(eDocTypeRefEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.EDOCUMENT_TYPE);
            entityRefLang.setLang(langRepository.findByCode(eDocTypeRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            eDocumentType.setId(id);
            eDocumentTypeRepository.save(eDocumentType);
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EDOCUMENT_TYPE, langRepository.findByCode(eDocTypeRefEntityRefLang.getLang()).getId(), id);
            entityRefLang.setLabel(eDocTypeRefEntityRefLang.getLabel());
            entityRefLang.setDescription(eDocTypeRefEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }


    @Override
    public Page<EDocTypeRefEntityRefLangProjection> filterByReferenceOrLabel(String value, Pageable pageable, String codeLang) {
        return eDocumentTypeRepository.filterByReferenceOrLabelProjection( value, codeLang,  pageable);
    }

    @Override
    public EDocTypeRefEntityRefLang findeDocumentType(Long id, String lang){
        EDocTypeRefEntityRefLang eDocTypeRefEntityRefLang = new EDocTypeRefEntityRefLang();

        EDocumentType eDocumentType = eDocumentTypeRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EDOCUMENT_TYPE,langRepository.findByCode(lang).getId(),eDocumentType.getId());

        eDocTypeRefEntityRefLang.setCode(eDocumentType!=null && eDocumentType.getCode()!=null ? eDocumentType.getCode(): "");
        eDocTypeRefEntityRefLang.setDocumentType(eDocumentType!=null && eDocumentType.getDocType()!=null ? eDocumentType.getDocType(): null);
        eDocTypeRefEntityRefLang.setUpdatedOn(eDocumentType.getUpdatedOn());

        eDocTypeRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        eDocTypeRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        eDocTypeRefEntityRefLang.setLang(lang);
        return eDocTypeRefEntityRefLang;
    }
}
