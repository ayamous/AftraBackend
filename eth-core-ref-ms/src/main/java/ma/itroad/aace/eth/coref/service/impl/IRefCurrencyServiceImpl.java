package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.RefCurrencyBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.ExtendedProcedureRef;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.entity.RefCurrency;
import ma.itroad.aace.eth.coref.model.mapper.RefCurrencyMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.RefCurrencyRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.RefCurrencyRepository;
import ma.itroad.aace.eth.coref.service.IRefCurrencyService;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.RefCurrencyRefEntityRefLang;
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
public class IRefCurrencyServiceImpl extends BaseServiceImpl<RefCurrency, RefCurrencyBean> implements IRefCurrencyService {

    static String[] HEADERs = { "CODE", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "CurrenciesSHEET";

    @Autowired
    private RefCurrencyRepository refCurrencyRepository;

    @Autowired
    LangRepository langRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;

    @Autowired
    private RefCurrencyMapper refCurrencyMapper;

    @Autowired
    private Validator validator;

    @Override
    public List<RefCurrency> saveAll(List<RefCurrency> refCurrencies) {
        if(!refCurrencies.isEmpty()){
            return refCurrencyRepository.saveAll(refCurrencies);
        }
        return null;
    }

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.CURRENCY_REF, id);
            for(EntityRefLang entityRefLang:entityRefLangs ){
                entityRefLangRepository.delete(entityRefLang);
            }
            refCurrencyRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return  response;
    }

    @Override
    public ErrorResponse deleteInternationalisation(String codeLang, Long refCurrencyId) {
        ErrorResponse response = new ErrorResponse();
        try {
            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CURRENCY_REF, lang.getId(), refCurrencyId);

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
    public ByteArrayInputStream currenciesToExcel(List<RefCurrencyRefEntityRefLang> currencyRefEntityRefLangs) {
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
                if(!currencyRefEntityRefLangs.isEmpty()){
                    for (RefCurrencyRefEntityRefLang currencyRefEntityRefLang : currencyRefEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(currencyRefEntityRefLang.getCode());
                        row.createCell(1).setCellValue(currencyRefEntityRefLang.getLabel());
                        row.createCell(2).setCellValue(currencyRefEntityRefLang.getDescription());
                        row.createCell(3).setCellValue(currencyRefEntityRefLang.getLang());
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
    public Set<ConstraintViolation<RefCurrencyRefEntityRefLang>> validateCurrency(RefCurrencyRefEntityRefLang refCurrencyRefEntityRefLang) {
        Set<ConstraintViolation<RefCurrencyRefEntityRefLang>> violations = validator.validate(refCurrencyRefEntityRefLang);
        return violations;
    }


    @Override
    public List<RefCurrencyRefEntityRefLang> excelToCurrencies(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<RefCurrency> currencys = new ArrayList<RefCurrency>();
            List<RefCurrencyRefEntityRefLang> currencyRefEntityRefLangs = new ArrayList<RefCurrencyRefEntityRefLang>();
            Row currentRow = rows.next();
            while (rows.hasNext()) {
                currentRow = rows.next();
                RefCurrencyRefEntityRefLang currencyRefEntityRefLang = new RefCurrencyRefEntityRefLang();
                int cellIdx = 0;
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            currencyRefEntityRefLang.setCode(Util.cellValue(currentCell));
                            break;
                        case 1:
                            currencyRefEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 2:
                            currencyRefEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 3:
                            currencyRefEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                currencyRefEntityRefLangs.add(currencyRefEntityRefLang);
            }
            workbook.close();
            return currencyRefEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<RefCurrencyRefEntityRefLang> refCurrencyRefEntityRefLangs = excelToCurrencies(file.getInputStream());
            List<RefCurrencyRefEntityRefLang> InvalidCurrencyRefEntityRefLangs = new ArrayList<RefCurrencyRefEntityRefLang>();
            List<RefCurrencyRefEntityRefLang> ValidCurrencyRefEntityRefLangs = new ArrayList<RefCurrencyRefEntityRefLang>();

            int lenght = refCurrencyRefEntityRefLangs.size();
            for (int i = 0; i < lenght; i++) {
                Set<ConstraintViolation<RefCurrencyRefEntityRefLang>> violations = validateCurrency(refCurrencyRefEntityRefLangs.get(i));
                if (violations.isEmpty())
                {
                    ValidCurrencyRefEntityRefLangs.add(refCurrencyRefEntityRefLangs.get(i));
                } else {
                    InvalidCurrencyRefEntityRefLangs.add(refCurrencyRefEntityRefLangs.get(i));
                }
            }
            if (!InvalidCurrencyRefEntityRefLangs.isEmpty()) {

                ByteArrayInputStream out = currenciesToExcel(InvalidCurrencyRefEntityRefLangs);
                xls = new InputStreamResource(out);

            }

            if (!ValidCurrencyRefEntityRefLangs.isEmpty()) {
                for (RefCurrencyRefEntityRefLang l : ValidCurrencyRefEntityRefLangs) {
                    RefCurrency currency = new RefCurrency();

                    RefCurrencyRefEntityRefLang currencyRefEntityRefLang = new RefCurrencyRefEntityRefLang();
                    currency.setCode(l.getCode());
                    RefCurrency ltemp = refCurrencyRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (refCurrencyRepository.save(currency)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.CURRENCY_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        Long id = ltemp.getId();
                        currency.setId(id);
                        refCurrencyRepository.save(currency);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CURRENCY_REF,langRepository.findByCode(l.getLang()).getId(),id);
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.CURRENCY_REF);
                            entityRefLang.setRefId(currency.getId());
                        }
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    }
                }
            }
            if (!InvalidCurrencyRefEntityRefLangs.isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(
                                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(xls);

            if (!ValidCurrencyRefEntityRefLangs.isEmpty())
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
        List<RefCurrency> currencies = refCurrencyRepository.findAll();
        ByteArrayInputStream in = null;
        // currenciesToExcel(currencies);
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<RefCurrency> currencys = refCurrencyRepository.findAll();
        List<RefCurrencyRefEntityRefLang> currencyRefEntityRefLangs = new ArrayList<RefCurrencyRefEntityRefLang>();

        for(RefCurrency u : currencys) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CURRENCY_REF, lang.getId(), u.getId());
            if (entityRefLang!=null) {
                RefCurrencyRefEntityRefLang currencyRefEntityRefLang = new RefCurrencyRefEntityRefLang();
                currencyRefEntityRefLang.setCode(u.getCode());
                currencyRefEntityRefLang.setLabel(entityRefLang.getLabel());
                currencyRefEntityRefLang.setDescription(entityRefLang.getDescription());
                currencyRefEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                currencyRefEntityRefLangs.add(currencyRefEntityRefLang);
            }
        }
        ByteArrayInputStream in = currenciesToExcel(currencyRefEntityRefLangs);
        return in;
    }

    @Override
    public void addRefCurrencyRef(RefCurrencyRefEntityRefLang refCurrencyRefEntityRefLang){
        RefCurrency refCurrency = new RefCurrency();
        refCurrency.setCode(refCurrencyRefEntityRefLang.getCode());
        RefCurrency ltemp = refCurrencyRepository.findByCode(refCurrencyRefEntityRefLang.getCode());
        if (ltemp == null) {
            Long id = (refCurrencyRepository.save(refCurrency)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(refCurrencyRefEntityRefLang.getLabel());
            entityRefLang.setDescription(refCurrencyRefEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.CURRENCY_REF);
            entityRefLang.setLang(  langRepository.findByCode(refCurrencyRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            refCurrency.setId(id);
            refCurrencyRepository.save(refCurrency);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CURRENCY_REF, langRepository.findByCode(refCurrencyRefEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(refCurrencyRefEntityRefLang.getLabel());
            entityRefLang.setDescription(refCurrencyRefEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public RefCurrencyRefEntityRefLang findCurrency  (Long id, String lang){
        RefCurrencyRefEntityRefLang currencyRefEntityRefLang = new RefCurrencyRefEntityRefLang();
        RefCurrency currency = refCurrencyRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CURRENCY_REF,langRepository.findByCode(lang).getId(),currency.getId());
        currencyRefEntityRefLang.setCode(currency.getCode());
        currencyRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        currencyRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        currencyRefEntityRefLang.setLang(lang);
        return currencyRefEntityRefLang;
    }


    public Page<RefCurrencyBean> getAll(Pageable pageable) {
        Page<RefCurrency> entities = refCurrencyRepository.findAll(pageable);
        Page<RefCurrencyBean> result = entities.map(refCurrencyMapper::entityToBean);
        return result;
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.CURRENCY_REF);
    }

    public Page<RefCurrencyRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<RefCurrency> refCurrencies = null;
        if(orderDirection.equals("DESC")){
            refCurrencies = refCurrencyRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            refCurrencies = refCurrencyRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }

        Lang lang = langRepository.findByCode(codeLang);
        List<RefCurrencyRefEntityRefLang> refCurrencyRefEntityRefLangs = new ArrayList<>();
        return refCurrencies.map(refCurrency -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CURRENCY_REF, lang.getId(), refCurrency.getId());

            RefCurrencyRefEntityRefLang refCurrencyRefEntityRefLang = new RefCurrencyRefEntityRefLang();

            refCurrencyRefEntityRefLang.setId(refCurrency.getId());
            refCurrencyRefEntityRefLang.setCode(refCurrency.getCode()!=null?refCurrency.getCode():"");
            refCurrencyRefEntityRefLang.setCreatedBy(refCurrency.getCreatedBy());
            refCurrencyRefEntityRefLang.setCreatedOn(refCurrency.getCreatedOn());
            refCurrencyRefEntityRefLang.setUpdatedBy(refCurrency.getUpdatedBy());
            refCurrencyRefEntityRefLang.setUpdatedOn(refCurrency.getUpdatedOn());

            refCurrencyRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            refCurrencyRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            refCurrencyRefEntityRefLang.setLang(codeLang);
            refCurrencyRefEntityRefLangs.add(refCurrencyRefEntityRefLang);
            return  refCurrencyRefEntityRefLang ;
        });
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang,String lang){
        entityRefLang.setTableRef(TableRef.CURRENCY_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CURRENCY_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    public Page<RefCurrencyRefEntityRefLang> mapLangToRefLangs(Page<RefCurrency> refCurrencies, String codeLang) {
        List<RefCurrencyRefEntityRefLang> refCurrencyRefEntityRefLangs = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        return refCurrencies.map(refCurrency -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CURRENCY_REF, lang.getId(), refCurrency.getId());
            RefCurrencyRefEntityRefLang refCurrencyRefEntityRefLang = new RefCurrencyRefEntityRefLang();
            refCurrencyRefEntityRefLang.setId(refCurrency.getId());

            refCurrencyRefEntityRefLang.setCode(refCurrency.getCode() != null ? refCurrency.getCode() : "");
            refCurrencyRefEntityRefLang.setCreatedBy(refCurrency.getCreatedBy());
            refCurrencyRefEntityRefLang.setCreatedOn(refCurrency.getCreatedOn());
            refCurrencyRefEntityRefLang.setUpdatedBy(refCurrency.getUpdatedBy());
            refCurrencyRefEntityRefLang.setUpdatedOn(refCurrency.getUpdatedOn());
            refCurrencyRefEntityRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
            refCurrencyRefEntityRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
            refCurrencyRefEntityRefLang.setLang(codeLang);
            refCurrencyRefEntityRefLangs.add(refCurrencyRefEntityRefLang);
            return refCurrencyRefEntityRefLang;
        });
    }

    public Page<RefCurrencyRefEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
        return mapLangToRefLangs(refCurrencyRepository.filterByCodeOrLabel(value,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
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
    public RefCurrencyBean findById(Long id) {
        RefCurrency result= refCurrencyRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return refCurrencyMapper.entityToBean(result);
    }

    @Override
    public Page<RefCurrencyRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
        return refCurrencyRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
    }

}
