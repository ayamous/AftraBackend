package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.RefTransportationTypeBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.RefTransportationTypeMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.TransportationTypeEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.RefTransportationTypeRepository;
import ma.itroad.aace.eth.coref.service.IRefTransportationTypeService;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.TransportationTypeEntityRefLang;
import ma.itroad.aace.eth.coref.service.util.Util;
import org.apache.poi.ss.usermodel.*;
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
public class IRefTransportationTypeServiceImpl extends BaseServiceImpl<RefTransportationType, RefTransportationTypeBean> implements IRefTransportationTypeService {

    static String[] HEADERs = { "CODE", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "RefTransportationTypesSHEET";

    @Autowired
    private RefTransportationTypeRepository refTransportationTypeRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    LangRepository langRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;

    @Autowired
    RefTransportationTypeMapper refTransportationTypeMapper;

    @Autowired
    private Validator validator;

    @Override
    public List<RefTransportationType> saveAll(List<RefTransportationType> refTransportationTypes) {
        if(!refTransportationTypes.isEmpty()){
            return refTransportationTypeRepository.saveAll(refTransportationTypes);
        }
        return null;
    }

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.TRANSPORTATION_TYPE_REF, id);
            for(EntityRefLang entityRefLang:entityRefLangs ){
                entityRefLangRepository.delete(entityRefLang);
            }
            refTransportationTypeRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return  response;
    }

    @Override
    public ErrorResponse deleteInternationalisation(String codeLang, Long refTransportationTypeId) {

        ErrorResponse response = new ErrorResponse();
        try {

            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TRANSPORTATION_TYPE_REF, lang.getId(), refTransportationTypeId);

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
    public ByteArrayInputStream refTransportationTypesToExcel(List<TransportationTypeEntityRefLang> transportationTypeEntityRefLangs) {
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
                if(!transportationTypeEntityRefLangs.isEmpty()){

                    for (TransportationTypeEntityRefLang transportationTypeEntityRefLang : transportationTypeEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(transportationTypeEntityRefLang.getCode());
                        row.createCell(1).setCellValue(transportationTypeEntityRefLang.getLabel());
                        row.createCell(2).setCellValue(transportationTypeEntityRefLang.getDescription());
                        row.createCell(3).setCellValue(transportationTypeEntityRefLang.getLang());


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
    public Set<ConstraintViolation<TransportationTypeEntityRefLang>> validateTransportationType(TransportationTypeEntityRefLang transportationTypeEntityRefLang) {
        Set<ConstraintViolation<TransportationTypeEntityRefLang>> violations = validator.validate(transportationTypeEntityRefLang);
        return violations;
    }

    @Override
    public List<TransportationTypeEntityRefLang> excelToRefTransportationTypes(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<RefTransportationType> transportationTypes = new ArrayList<RefTransportationType>();
            List<TransportationTypeEntityRefLang> transportationTypeEntityRefLangs = new ArrayList<TransportationTypeEntityRefLang>();
            Row currentRow = rows.next();
            while (rows.hasNext()) {
                currentRow = rows.next();

                TransportationTypeEntityRefLang transportationTypeEntityRefLang = new TransportationTypeEntityRefLang();
                int cellIdx = 0;
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);

                    switch (colNum) {
                        case 0:
                            // read  data from the cell
                           /* FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                            CellValue cellValue = evaluator.evaluate(currentCell);
                            String vale=Integer.valueOf((int) cellValue.getNumberValue()).toString();
                            transportationTypeEntityRefLang.setCode(vale);

                            */
                            // read data from the Formula
                           transportationTypeEntityRefLang.setCode(Util.cellValue(currentCell));
                            break;
                        case 1:
                            transportationTypeEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 2:
                            transportationTypeEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 3:
                            transportationTypeEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;

                        default:
                            break;
                    }
                    cellIdx++;
                }
                transportationTypeEntityRefLangs.add(transportationTypeEntityRefLang);
            }
            workbook.close();
            return transportationTypeEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<TransportationTypeEntityRefLang> transportationTypeEntityRefLangs = excelToRefTransportationTypes(file.getInputStream());
            List<TransportationTypeEntityRefLang> InvalidTransportationTypeEntityRefLangs = new ArrayList<TransportationTypeEntityRefLang>();
            List<TransportationTypeEntityRefLang> ValidTransportationTypeEntityRefLangs = new ArrayList<TransportationTypeEntityRefLang>();

            int lenght = transportationTypeEntityRefLangs.size();
            for (int i = 0; i < lenght; i++) {
                Set<ConstraintViolation<TransportationTypeEntityRefLang>> violations = validateTransportationType(transportationTypeEntityRefLangs.get(i));
                if (violations.isEmpty())
                {
                    ValidTransportationTypeEntityRefLangs.add(transportationTypeEntityRefLangs.get(i));
                } else {
                    InvalidTransportationTypeEntityRefLangs.add(transportationTypeEntityRefLangs.get(i));
                }
            }
            if (!InvalidTransportationTypeEntityRefLangs.isEmpty()) {

                ByteArrayInputStream out = refTransportationTypesToExcel(InvalidTransportationTypeEntityRefLangs);
                xls = new InputStreamResource(out);

            }

            if (!ValidTransportationTypeEntityRefLangs.isEmpty()) {
                for (TransportationTypeEntityRefLang l : ValidTransportationTypeEntityRefLangs) {
                    RefTransportationType refTransportationType = new RefTransportationType();

                    TransportationTypeEntityRefLang transportationTypeEntityRefLang = new TransportationTypeEntityRefLang();
                    refTransportationType.setCode(l.getCode());
                    RefTransportationType ltemp = refTransportationTypeRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (refTransportationTypeRepository.save(refTransportationType)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.TRANSPORTATION_TYPE_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        Long id = ltemp.getId();
                        refTransportationType.setId(id);
                        refTransportationTypeRepository.save(refTransportationType);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TRANSPORTATION_TYPE_REF, langRepository.findByCode(l.getLang()).getId() ,id);
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.TRANSPORTATION_TYPE_REF);
                            entityRefLang.setRefId(refTransportationType.getId());
                        }
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    }

                }
            }
            if (!InvalidTransportationTypeEntityRefLangs.isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(
                                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(xls);

            if (!ValidTransportationTypeEntityRefLangs.isEmpty())
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
        List<RefTransportationType> refTransportationTypes = refTransportationTypeRepository.findAll();
        ByteArrayInputStream in = null;
                //refTransportationTypesToExcel(refTransportationTypes);
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<RefTransportationType> transportationTypes = refTransportationTypeRepository.findAll();
        List<TransportationTypeEntityRefLang> transportationTypeEntityRefLangs = new ArrayList<TransportationTypeEntityRefLang>();

        for(RefTransportationType u : transportationTypes) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TRANSPORTATION_TYPE_REF, lang.getId(), u.getId());
            if (entityRefLang!=null) {
                TransportationTypeEntityRefLang transportationTypeEntityRefLang = new TransportationTypeEntityRefLang();
                transportationTypeEntityRefLang.setCode(u.getCode());
                transportationTypeEntityRefLang.setLabel(entityRefLang.getLabel());
                transportationTypeEntityRefLang.setDescription(entityRefLang.getDescription());
                transportationTypeEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                transportationTypeEntityRefLangs.add(transportationTypeEntityRefLang);
            }
        }
        ByteArrayInputStream in = refTransportationTypesToExcel(transportationTypeEntityRefLangs);
        return in;
    }

    @Override
    public void addTransportationTypeEntityRef(TransportationTypeEntityRefLang transportationTypeEntityRefLang){
        RefTransportationType refTransportationType = new RefTransportationType();
        refTransportationType.setCode(transportationTypeEntityRefLang.getCode());
        RefTransportationType ltemp = refTransportationTypeRepository.findByCode(transportationTypeEntityRefLang.getCode());
        if (ltemp == null) {
            Long id = (refTransportationTypeRepository.save(refTransportationType)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(transportationTypeEntityRefLang.getLabel());
            entityRefLang.setDescription(transportationTypeEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.TRANSPORTATION_TYPE_REF);
            entityRefLang.setLang(  langRepository.findByCode(transportationTypeEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            refTransportationType.setId(id);
            refTransportationTypeRepository.save(refTransportationType);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TRANSPORTATION_TYPE_REF, langRepository.findByCode(transportationTypeEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(transportationTypeEntityRefLang.getLabel());
            entityRefLang.setDescription(transportationTypeEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public TransportationTypeEntityRefLang findTransportationType(Long id, String lang){
        TransportationTypeEntityRefLang transportationTypeEntityRefLang = new TransportationTypeEntityRefLang();
        RefTransportationType refTransportationType = refTransportationTypeRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TRANSPORTATION_TYPE_REF,langRepository.findByCode(lang).getId(),refTransportationType.getId());
        transportationTypeEntityRefLang.setCode(refTransportationType.getCode());
        transportationTypeEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        transportationTypeEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        transportationTypeEntityRefLang.setLang(lang);
        return transportationTypeEntityRefLang;
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.TRANSPORTATION_TYPE_REF);
    }

    public Page<TransportationTypeEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<RefTransportationType> refTransportationTypes = null;
        if(orderDirection.equals("DESC")){
            refTransportationTypes = refTransportationTypeRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            refTransportationTypes = refTransportationTypeRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
        Lang lang = langRepository.findByCode(codeLang);
        List<TransportationTypeEntityRefLang> transportationTypeEntityRefLangs = new ArrayList<>();
        return refTransportationTypes.map(refTransportationType -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TRANSPORTATION_TYPE_REF, lang.getId(), refTransportationType.getId());

            TransportationTypeEntityRefLang transportationTypeEntityRefLang = new TransportationTypeEntityRefLang();

            transportationTypeEntityRefLang.setId(refTransportationType.getId());
            transportationTypeEntityRefLang.setCode(refTransportationType.getCode()!=null?refTransportationType.getCode():"");
            transportationTypeEntityRefLang.setCreatedBy(refTransportationType.getCreatedBy());
            transportationTypeEntityRefLang.setCreatedOn(refTransportationType.getCreatedOn());
            transportationTypeEntityRefLang.setUpdatedBy(refTransportationType.getUpdatedBy());
            transportationTypeEntityRefLang.setUpdatedOn(refTransportationType.getUpdatedOn());

            transportationTypeEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            transportationTypeEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            transportationTypeEntityRefLang.setLang(codeLang);
            transportationTypeEntityRefLangs.add(transportationTypeEntityRefLang);
            return  transportationTypeEntityRefLang ;
        });
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang,String lang){
        entityRefLang.setTableRef(TableRef.TRANSPORTATION_TYPE_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TRANSPORTATION_TYPE_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    public Page<TransportationTypeEntityRefLang> mapLangToRefLangs(Page<RefTransportationType> refTransportationTypes, String codeLang) {
        List<TransportationTypeEntityRefLang> transportationTypeEntityRefLangs = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        return refTransportationTypes.map(refTransportationType -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TRANSPORTATION_TYPE_REF, lang.getId(), refTransportationType.getId());
            TransportationTypeEntityRefLang transportationTypeEntityRefLang = new TransportationTypeEntityRefLang();
            transportationTypeEntityRefLang.setId(refTransportationType.getId());

            transportationTypeEntityRefLang.setCode(refTransportationType.getCode() != null ? refTransportationType.getCode() : "");
            transportationTypeEntityRefLang.setCreatedBy(refTransportationType.getCreatedBy());
            transportationTypeEntityRefLang.setCreatedOn(refTransportationType.getCreatedOn());
            transportationTypeEntityRefLang.setUpdatedBy(refTransportationType.getUpdatedBy());
            transportationTypeEntityRefLang.setUpdatedOn(refTransportationType.getUpdatedOn());
            transportationTypeEntityRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
            transportationTypeEntityRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
            transportationTypeEntityRefLang.setLang(codeLang);
            transportationTypeEntityRefLangs.add(transportationTypeEntityRefLang);
            return transportationTypeEntityRefLang;
        });
    }

    public Page<TransportationTypeEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
        return mapLangToRefLangs(refTransportationTypeRepository.filterByCodeOrLabel(value,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
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
    public RefTransportationTypeBean findById(Long id) {
        RefTransportationType result= refTransportationTypeRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return refTransportationTypeMapper.entityToBean(result);
    }

    @Override
    public Page<TransportationTypeEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
        return refTransportationTypeRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
    }
}
