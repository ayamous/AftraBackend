package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.TaxRefBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.TaxRefMapper;
import ma.itroad.aace.eth.coref.model.view.TaxRefVM;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.repository.TaxRefRepository;
import ma.itroad.aace.eth.coref.service.ITaxRefService;
import ma.itroad.aace.eth.coref.service.helper.TaxRefLang;
import ma.itroad.aace.eth.coref.service.helper.TaxationRefLang;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class TaxRefServiceImpl extends BaseServiceImpl<TaxRef, TaxRefBean> implements ITaxRefService {


    static String[] HEADERs = {"TAX CODE", "COUNTRY REFERENCE"};
    static String SHEET = "TaxRefSHEET";

    @Autowired
    private TaxRefRepository taxRefRepository;

    @Autowired
    private CountryRefRepository countryRefRepository;

    @Autowired
    TaxRefMapper taxRefMapper;

    @Autowired
    private Validator validator;


    @Override
    public void saveFromExcel(MultipartFile file) {
        try {
            List<TaxRef> taxRefs = excelToTaxRefs(file.getInputStream());
            if (!taxRefs.isEmpty())
                taxRefs.stream().forEach(
                        tax -> {
                            TaxRef ltemp = taxRefRepository.findByCode(tax.getCode());
                            if (ltemp == null) {
                                taxRefRepository.save(tax);
                            } else {
                                tax.setId(ltemp.getId());
                                taxRefRepository.save(tax);
                            }
                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }

    }


    @Override
    public List<TaxRef> excelToTaxRefs(InputStream is) {

        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<TaxRef> taxRefs = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();

                TaxRef taxRef = new TaxRef();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                     switch (cellIdx) {
                        case 0:
                            if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                taxRef.setCode((int) currentCell.getNumericCellValue() + "");
                            else
                                taxRef.setCode(currentCell.getStringCellValue());

                            break;
                        case 1:
                            CountryRef country;

                            if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                country = countryRefRepository.findByReference((int) currentCell.getNumericCellValue() + "");
                            else
                                country = countryRefRepository.findByReference(currentCell.getStringCellValue());

                            if (country != null)
                                taxRef.setCountryRef(country);
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                taxRefs.add(taxRef);
            }
            workbook.close();
            return taxRefs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }



    public List<TaxRefLang> excelToTaxRefsLang(InputStream is) {

        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<TaxRefLang> taxRefs = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();

                TaxRefLang taxRef = new TaxRefLang();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    switch (cellIdx) {
                        case 0:
                            if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                taxRef.setCode((int) currentCell.getNumericCellValue() + "");
                            else
                                taxRef.setCode(currentCell.getStringCellValue());

                            break;
                        case 1:
                            taxRef.setCountryRefReference(currentCell.getStringCellValue());
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                taxRefs.add(taxRef);
            }
            workbook.close();
            return taxRefs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public ByteArrayInputStream taxRefsToExcelLang(List<TaxRefLang> taxRefs) {

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
                if (!taxRefs.isEmpty()) {

                    for (TaxRefLang taxRef : taxRefs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(taxRef.getCode());
                        row.createCell(1).setCellValue(taxRef.getCountryRefReference());
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
    public Page<TaxRefVM> getAll(int page, int size, String orderDirection) {
        Page<TaxRef> entities = null;
        if(orderDirection.equals("DESC")){
            entities = taxRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            entities = taxRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }

        Page<TaxRefVM> result = entities.map(this::convertToTaxRefVM);
        return result;
    }
    private TaxRefVM convertToTaxRefVM(TaxRef entity) {
        final TaxRefVM item = new TaxRefVM();
        if (entity != null) {
            item.setId(entity.getId());
            item.setTaxRefCode(entity.getCode() != null ? entity.getCode() : null);
            item.setCountryRefReference(entity.getCountryRef() != null ? entity.getCountryRef().getReference() : null);
            item.setCode(entity.getCode());

            item.setCreatedBy(entity.getCreatedBy());
            item.setCreatedOn(entity.getCreatedOn());
            item.setUpdatedBy(entity.getUpdatedBy());
            item.setUpdatedOn(entity.getUpdatedOn());

            if (entity.getCountryRef()!= null) {
                item.setCountryRefId(entity.getCountryRef().getId() != null ? entity.getCountryRef().getId() : null);
            }
            item.setId(entity.getId());
        }
        return item;
    }
    @Override
    public ByteArrayInputStream taxRefsToExcel(List<TaxRef> taxRefs) {

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
                if (!taxRefs.isEmpty()) {

                    for (TaxRef taxRef : taxRefs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(taxRef.getCode());
                        row.createCell(1).setCellValue(taxRef.getCountryRef().getReference());
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
    public ByteArrayInputStream load() {
        List<TaxRef> taxRefs = taxRefRepository.findAll();
        ByteArrayInputStream in = taxRefsToExcel(taxRefs);
        return in;
    }

    @Override
    public ErrorResponse delete(Long id) {
        try {
            taxRefRepository.deleteById(id);
            return new ErrorResponse(HttpStatus.OK, ErrorMessageType.DELETE_SUCESS.getMessagePattern(), null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(HttpStatus.CONFLICT, ErrorMessageType.DELETE_ERROR.getMessagePattern(), null);
        }

    }

    @Override
    public ErrorResponse deleteList(ListOfObject listOfObject) {
        ErrorResponse response = new ErrorResponse();

        try {
            for(Long id : listOfObject.getListOfObject()){
                taxRefRepository.deleteById(id);
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
    public TaxRefBean findById(Long id) {
        TaxRef result= taxRefRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return taxRefMapper.entityToBean(result);
    }

    @Override
    public Page<TaxRefBean> findByCode(String code,Pageable pageable) {
        return taxRefRepository.findAllByCode(code.toLowerCase(),pageable)
                .map(taxRefMapper::entityToBean);
    }

    public Set<ConstraintViolation<TaxRefLang>> validateItems(TaxRefLang taxRefLang) {
        Set<ConstraintViolation<TaxRefLang>> violations = validator.validate(taxRefLang);
        return violations;
    }

    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<TaxRefLang> itemsList = excelToTaxRefsLang(file.getInputStream());
            List<TaxRefLang> invalidItems = new ArrayList<TaxRefLang>();
            List<TaxRefLang> validItems = new ArrayList<TaxRefLang>();

            int lenght = itemsList.size();


            for (int i = 0; i < lenght; i++) {

                Set<ConstraintViolation<TaxRefLang>> violations = validateItems(itemsList.get(i));
                if (violations.isEmpty())
                {
                    validItems.add(itemsList.get(i));
                } else {
                    invalidItems.add(itemsList.get(i));
                }
            }

            if (!invalidItems.isEmpty()) {

                ByteArrayInputStream out = taxRefsToExcelLang(invalidItems);
                xls = new InputStreamResource(out);

            }


            for (TaxRefLang l : validItems) {
                TaxRef taxRef = new TaxRef();
                taxRef.setCode(l.getCode());
                taxRef.setCountryRef(countryRefRepository.findByReference(l.getCountryRefReference()));
                TaxRef ltemp = taxRefRepository.findByCode(l.getCode());
                if (ltemp == null) {
                    Long id = (taxRefRepository.save(taxRef)).getId();
//                    EntityRefLang entityRefLang = new EntityRefLang();
//                    entityRefLang.setLabel(l.getLabel());
//                    entityRefLang.setDescription(l.getDescription());
//                    entityRefLang.setRefId(id);
//                    entityRefLang.setTableRef(TableRef.TAXATION_REF);
//                    entityRefLang.setLang(langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()) : langRepository.findAll().get(0));
//                    entityRefLangRepository.save(entityRefLang);
                } else {
                    Long id = ltemp.getId();
                    taxRef.setId(id);
                    taxRefRepository.save(taxRef);
//                    EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF,langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()).getId() : langRepository.findAll().get(0).getId(),id);
//                    if(entityRefLang == null )
//                    {
//                        entityRefLang =new EntityRefLang() ;
//                        entityRefLang.setLang(langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()) : langRepository.findAll().get(0));
//                        entityRefLang.setTableRef(TableRef.TAXATION_REF);
//                        entityRefLang.setRefId(taxation.getId());
//                    }
//                    entityRefLang.setLabel(l.getLabel());
//                    entityRefLang.setDescription(l.getDescription());
//                    entityRefLang.setLang(langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()) : langRepository.findAll().get(0));
//                    entityRefLangRepository.save(entityRefLang);
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
