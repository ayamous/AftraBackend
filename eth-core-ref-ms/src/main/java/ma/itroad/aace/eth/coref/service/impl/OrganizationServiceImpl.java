package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.OrganizationBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.OrganizationMapper;
import ma.itroad.aace.eth.coref.repository.CategoryRefRepository;
import ma.itroad.aace.eth.coref.repository.CityRefRepository;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.repository.OrganizationRepository;
import ma.itroad.aace.eth.coref.service.IOrganizationService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.service.helper.*;
import ma.itroad.aace.eth.coref.service.util.Util;
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
public class OrganizationServiceImpl extends BaseServiceImpl<Organization, OrganizationBean> implements IOrganizationService {


    static String[] HEADERs = {"REFERENCE ORGANISATION", "ACRONYME", "NOM", "REFERENCE PARENT", "REFERENCE VILLE","REFERENCE COUNTRY", "CODE CATEGORIE",
            "ADRESSE","EMAIL","NUMERO TELEP"};
    static String SHEET = "OrganisationSheet";

    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private OrganizationMapper organizationMapper;
    @Autowired
    private CityRefRepository cityRefRepository;

    @Autowired
    private CountryRefRepository countryRefRepository;
    @Autowired
    private Validator validator;
    @Autowired
    private CategoryRefRepository categoryRefRepository;

    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            List<Organization> organizations = excelToOrganizations(file.getInputStream());
            if (!organizations.isEmpty())
                organizations.stream().forEach(
                        org -> {
                            if (org.getParent() != null) {
                                Organization orgOpt = organizationRepository.findByReference(org.getParent().getReference());
                                if (orgOpt == null) {
                                    org.setParent(orgOpt);
                                } else org.setParent(null);
                            }
                            Organization ltemp = organizationRepository.findByReference(org.getReference());
                            if (ltemp == null) {
                                organizationRepository.save(org);
                            } else {
                                org.setId(ltemp.getId());
                                organizationRepository.save(org);
                            }
                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }


    @Override
    public List<Organization> excelToOrganizations(InputStream is) {

        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            List<Organization> organizations = new ArrayList<>();
            Row currentRow;
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                currentRow= sheet.getRow(rowNum);
                Organization organization = new Organization();
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    String cellValue = Util.cellValue(currentCell);
                    switch (colNum) {
                        case 0:
                            organization.setReference(cellValue);
                            break;
                        case 1:
                            organization.setAcronym(cellValue);

                            break;
                        case 2:
                            organization.setName(cellValue);

                            break;
                        case 3:
                            String parentReference= Util.cellValue(currentCell);
                            if (parentReference == null) break;
                            organization.setParent(new Organization().reference(parentReference));
                            break;

                        case 4:
                            String cityRefReference = Util.cellValue(currentCell);
                            CityRef cityRef = cityRefRepository.findByReference(cityRefReference);
                            organization.setCityRef(cityRef);

                            break;
                        case 5:
                            String country =Util.cellValue(currentCell);
                            CountryRef countryRef = countryRefRepository.findByReference(country);
                            organization.setCountryRef(countryRef);
                            break;
                        case 6:
                            String categoryCode =Util.cellValue(currentCell);
                            CategoryRef categoryRef = categoryRefRepository.findByCode(categoryCode);
                            organization.setCategoryRef(categoryRef);
                            break;
                        case 7:
                            organization.setAdresse(Util.cellValue(currentCell));
                            break;
                        case 8:
                            organization.setEmail(Util.cellValue(currentCell));
                            break;
                        case 9:
                            organization.setTel(cellValue);
                            break;
                        default:
                            break;
                        }

                    }
                organizations.add(organization);
            }
            workbook.close();
            return organizations;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }

    }

    @Override
    public ByteArrayInputStream organizationsToExcel(List<Organization> organizations) {

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
                if (!organizations.isEmpty()) {
                    for (Organization organization : organizations) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(organization.getReference());
                        row.createCell(1).setCellValue(organization.getAcronym());
                        row.createCell(2).setCellValue(organization.getName());
                        row.createCell(3).setCellValue(organization.getParent() != null ? organization.getParent().getReference() : "");
                        row.createCell(4).setCellValue(organization.getCityRef() != null ? organization.getCityRef().getReference() : "");
                        row.createCell(5).setCellValue(organization.getCountryRef() != null ? organization.getCountryRef().getReference():"");
                        row.createCell(6).setCellValue(organization.getCategoryRef() != null ? organization.getCategoryRef().getCode() : "");
                        row.createCell(7).setCellValue(organization.getAdresse());
                        row.createCell(8).setCellValue(organization.getEmail());
                        row.createCell(9).setCellValue(organization.getTel());
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
    public ErrorResponse delete(Long id) {
        try {
            organizationRepository.deleteById(id);
            return new ErrorResponse(HttpStatus.OK, ErrorMessageType.DELETE_SUCESS.getMessagePattern(), null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(HttpStatus.CONFLICT, ErrorMessageType.DELETE_ERROR.getMessagePattern(), null);
        }

    }

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
    public Page<OrganizationHelper> getAll(int page, int size, String orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Organization> organizations = null;
        if(orderDirection.equals("DESC")){
            organizations = organizationRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            organizations = organizationRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
        return mapOrganizationToOrganizationHelper(organizations);
    }

    @Override
    public ByteArrayInputStream load() {
        List<Organization> organizations = organizationRepository.findAll();
        ByteArrayInputStream in = organizationsToExcel(organizations);
        return in;
    }

    private Page<OrganizationHelper> mapOrganizationToOrganizationHelper(Page<Organization> organizations){
        List<OrganizationHelper> organizationHelpers = new ArrayList<OrganizationHelper>();
        return organizations.map(organization -> {
            OrganizationHelper organizationHelper = new OrganizationHelper( );
            organizationHelper.setId(organization.getId());

            organizationHelper.setReference(organization.getReference()!=null ?organization.getReference():"");
            organizationHelper.setAcronym(organization.getAcronym()!=null ?organization.getAcronym():"");
            organizationHelper.setName(organization.getName()!=null ?organization.getName():"");

            organizationHelper.setCountryRef(organization.getCountryRef() != null ? organization.getCountryRef().getReference():"");
            organizationHelper.setParent(organization.getParent()!=null ?organization.getParent().getReference():"");
            organizationHelper.setCityRef(organization.getCityRef()!=null ?organization.getCityRef().getReference():"");
            organizationHelper.setCategoryRef(organization.getCategoryRef()!=null ?organization.getCategoryRef().getCode():"");
            organizationHelper.setAdresse(organization.getAdresse()!=null ?organization.getAdresse():"");
            organizationHelper.setTel(organization.getTel()!=null ?organization.getTel():"");
            organizationHelper.setEmail(organization.getEmail()!=null ?organization.getEmail():"");

            organizationHelper.setCreatedBy(organization.getCreatedBy());
            organizationHelper.setCreatedOn(organization.getCreatedOn());
            organizationHelper.setUpdatedBy(organization.getUpdatedBy());
            organizationHelper.setUpdatedOn(organization.getUpdatedOn());

            organizationHelpers.add(organizationHelper);
            return  organizationHelper;
        });
    }

    public ByteArrayInputStream organizationsHelpsToExcel(List<OrganizationHelper> organizations) {

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
                if (!organizations.isEmpty()) {
                    for (OrganizationHelper organization : organizations) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(organization.getReference());
                        row.createCell(1).setCellValue(organization.getAcronym());
                        row.createCell(2).setCellValue(organization.getName());
                        row.createCell(3).setCellValue(organization.getParent());
                        row.createCell(4).setCellValue(organization.getCityRef());
                        row.createCell(5).setCellValue(organization.getCountryRef());
                        row.createCell(6).setCellValue(organization.getCategoryRef());
                        row.createCell(7).setCellValue(organization.getAdresse());
                        row.createCell(8).setCellValue(organization.getEmail());
                        row.createCell(9).setCellValue(organization.getTel());
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
    public Page<OrganizationHelper> searchOrganizationByKeyWord(int page, int size, String keyWord, String orderDirection){
        Page<Organization> organizations = null;
        if(orderDirection.equals("DESC")){
            organizations = organizationRepository.findByReferenceIgnoreCaseContains(keyWord, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            organizations = organizationRepository.findByReferenceIgnoreCaseContains(keyWord, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
        return mapOrganizationToOrganizationHelper(organizations);
    }

    public Page<OrganizationHelper> filterByReferenceOrName(String value, Pageable pageable)  {
        return mapOrganizationToOrganizationHelper(organizationRepository.filterByReferenceOrName(value , pageable));
    }

    @Override
    public OrganizationBean findById(Long id) {
        Organization result= organizationRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return organizationMapper.entityToBean(result);
    }

    public Set<ConstraintViolation<OrganizationHelper>> validateItems(OrganizationHelper organizationHelper) {
        Set<ConstraintViolation<OrganizationHelper>> violations = validator.validate(organizationHelper);
        return violations;
    }

    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<Organization> itemsList = excelToOrganizations(file.getInputStream());
            List<OrganizationHelper> invalidItems = new ArrayList<OrganizationHelper>();
            List<OrganizationHelper> validItems = new ArrayList<OrganizationHelper>();

            int lenght = itemsList.size();

            for (int i = 0; i < lenght; i++) {

                OrganizationHelper organizationHelper = new OrganizationHelper();
                organizationHelper.setReference(itemsList.get(i).getReference());
                organizationHelper.setAcronym(itemsList.get(i).getAcronym());
                organizationHelper.setName(itemsList.get(i).getName());
                organizationHelper.setAdresse(itemsList.get(i).getAdresse());
                organizationHelper.setEmail(itemsList.get(i).getEmail());
                organizationHelper.setTel(itemsList.get(i).getTel());
                organizationHelper.setParent(itemsList.get(i).getParent() != null ? itemsList.get(i).getParent().getReference() : "");
                organizationHelper.setCityRef(itemsList.get(i).getCityRef() != null ? itemsList.get(i).getCityRef().getReference() : "");
                organizationHelper.setCategoryRef(itemsList.get(i).getCategoryRef() != null ? itemsList.get(i).getCategoryRef().getCode() : "");
                organizationHelper.setCountryRef(itemsList.get(i).getCountryRef() != null ? itemsList.get(i).getCountryRef().getReference() : "");

                Set<ConstraintViolation<OrganizationHelper>> violations = validateItems(organizationHelper);
                if (violations.isEmpty())
                {
                    validItems.add(organizationHelper);
                } else {
                    invalidItems.add(organizationHelper);
                }
            }

            if (!invalidItems.isEmpty()) {

                ByteArrayInputStream out = organizationsHelpsToExcel(invalidItems);
                xls = new InputStreamResource(out);

            }

            if (!validItems.isEmpty()) {
                for (OrganizationHelper l : validItems) {
                    Organization organization = new Organization();
                    organization.setReference(l.getReference());
                    organization.setReference(l.getReference());
                    organization.setAcronym(l.getAcronym());
                    organization.setName(l.getName());
                    organization.setAdresse(l.getAdresse());
                    organization.setEmail(l.getEmail());
                    organization.setTel(l.getTel());
                    organization.setParent(organizationRepository.findByReference(l.getParent()));
                    organization.setCityRef(cityRefRepository.findByReference(l.getCityRef()));
                    organization.setCategoryRef(categoryRefRepository.findByCode(l.getCategoryRef()));
                    organization.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));

                    Organization ltemp = organizationRepository.findByReference(l.getReference());
                    if (ltemp == null) {
                        organizationRepository.save(ltemp);
                    }
                    else {
                        organization.setId( ltemp.getId());
                        organizationRepository.save(organization);
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
