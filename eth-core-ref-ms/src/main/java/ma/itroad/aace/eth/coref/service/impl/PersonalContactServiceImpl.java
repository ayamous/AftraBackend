package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.PersonalContactBean;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.entity.Organization;
import ma.itroad.aace.eth.coref.model.entity.PersonalContact;

import ma.itroad.aace.eth.coref.model.entity.PortRef;
import ma.itroad.aace.eth.coref.model.enums.ContactType;
import ma.itroad.aace.eth.coref.model.enums.Occupation;
import ma.itroad.aace.eth.coref.model.view.PersonalContactVM;
import ma.itroad.aace.eth.coref.model.view.PortRefVM;
import ma.itroad.aace.eth.coref.repository.OrganizationRepository;
import ma.itroad.aace.eth.coref.repository.PersonalContactRepository;
import ma.itroad.aace.eth.coref.service.IPersonalContactService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class PersonalContactServiceImpl extends BaseServiceImpl<PersonalContact,PersonalContactBean> implements IPersonalContactService {


    static String[] HEADERs = {"Reference", "Nom", "Prenom", "GSM","Fix","Fax","Adresse","Email","Type", "Occupation","refOrganisation"};
    static String SHEET = "Sheet1";



    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PersonalContactRepository personalContactRepository;



    @Override
    public List<PersonalContact> excelToPersonalContact(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);

            Iterator<Row> rows = sheet.iterator();
            List<PersonalContact> personalContacts = new ArrayList<PersonalContact>();
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> cellsInRow = currentRow.iterator();
                PersonalContact personalContact = new PersonalContact();

                for(int rowNum=1; rowNum <= sheet.getLastRowNum(); rowNum++)
                    for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {

                        Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                        String cellValue = null;
                        switch (colNum) {
                            case 0:
                                if( currentCell.getStringCellValue().isEmpty())    break;
                                personalContact.setReference(currentCell.getStringCellValue());
                                break;

                            case 1:
                                if( currentCell.getStringCellValue().isEmpty())    break;
                                personalContact.setLastName(currentCell.getStringCellValue());
                                break;

                            case 2:
                                if( currentCell.getStringCellValue().isEmpty())    break;
                                personalContact.setFirstName(currentCell.getStringCellValue());
                                break;
                            case 3:
                                if( currentCell.getStringCellValue().isEmpty())    break;
                                personalContact.setMobileNumber(currentCell.getStringCellValue());
                                break;
                            case 4:
                                if( currentCell.getStringCellValue().isEmpty())    break;
                                personalContact.setPhoneNumber(currentCell.getStringCellValue());
                                break;
                            case 5:
                                if( currentCell.getStringCellValue().isEmpty())    break;
                                personalContact.setFaxNumber(currentCell.getStringCellValue());
                                break;

                            case 6:
                                if( currentCell.getStringCellValue().isEmpty())    break;
                                personalContact.setAdress(currentCell.getStringCellValue());
                                break;

                            case 7:
                                if( currentCell.getStringCellValue().isEmpty())    break;
                                personalContact.setEmail(currentCell.getStringCellValue());
                                break;
                            case 8:
                                if( currentCell.getStringCellValue().isEmpty())    break;
                                ContactType contactType = ContactType.valueOf(currentCell.getStringCellValue());
                                personalContact.setContactType(contactType);
                                break;
                            case 9:
                                if( currentCell.getStringCellValue().isEmpty())    break;
                                Occupation occupation = Occupation.valueOf(currentCell.getStringCellValue());
                                personalContact.setOccupation(occupation);
                                break;

                            case 10:
                                if( currentCell.getStringCellValue().isEmpty())    break;

                                Organization organization = organizationRepository.findByReference(currentCell.getStringCellValue());

                                personalContact.setOrganization(organization);

                                break;

                            default:
                                break;
                        }
                    }
                personalContacts.add(personalContact);
            }
            workbook.close();
            return personalContacts;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream personalContactToExcel(List<PersonalContact> personalContacts) {

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
                if (!personalContacts.isEmpty()) {

                    for (PersonalContact personalContact : personalContacts) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(personalContact.getReference());
                        row.createCell(1).setCellValue(personalContact.getLastName());
                        row.createCell(2).setCellValue(personalContact.getFirstName());
                        row.createCell(3).setCellValue(personalContact.getMobileNumber());
                        row.createCell(4).setCellValue(personalContact.getPhoneNumber());
                        row.createCell(5).setCellValue(personalContact.getFaxNumber());
                        row.createCell(6).setCellValue(personalContact.getAdress());
                        row.createCell(7).setCellValue(personalContact.getEmail());
                        ContactType contactType = personalContact.getContactType();
                        if (contactType != null) {
                            row.createCell(8).setCellValue(contactType.name());
                        }
                        Occupation occupation = personalContact.getOccupation();
                        if (occupation != null) {
                            row.createCell(9).setCellValue(occupation.name());
                        }
                        Organization organization = personalContact.getOrganization();
                        if (organization != null) {
                            row.createCell(10).setCellValue(organization.getReference());
                        }
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
    public Page<PersonalContactVM> getAll(int page, int size) {
        Page<PersonalContact> entities = personalContactRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn")));
        Page<PersonalContactVM> result = entities.map(this::convertToPersonalContactVM);
        return result;
    }


    @Override
    public ErrorResponse delete(Long id) {
        try {
            personalContactRepository.deleteById(id);
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

    private PersonalContactVM convertToPersonalContactVM(PersonalContact entity) {
        final PersonalContactVM item = new PersonalContactVM();
        item.setId(entity.getId());
        item.setReference(entity.getReference());
        item.setLastName(entity.getLastName());
        item.setFirstName(entity.getFirstName());
        item.setPhoneNumber(entity.getPhoneNumber());
        item.setMobileNumber(entity.getMobileNumber());
        item.setFaxNumber(entity.getFaxNumber());
        item.setAdress(entity.getAdress());
        item.setEmail(entity.getEmail());
        item.setContactType(entity.getContactType());
        item.setOccupation(entity.getOccupation());
        item.setOrganizationRef(entity.getOrganization()== null ?  null : entity.getOrganization().getReference() );
        item.setOrganizationId(entity.getOrganization()== null ?  null : entity.getOrganization().getId() );
        return item;
    }



    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            List<PersonalContact> personalContacts = excelToPersonalContact(file.getInputStream());
            if (!personalContacts.isEmpty())
                personalContacts.stream().forEach(
                        l -> {
                            Optional<PersonalContact> ltemp = personalContactRepository.findByReference(l.getReference());

                            if (!ltemp.isPresent()) {
                                personalContactRepository.save(l);
                            } else {
                                l.setId(ltemp.get().getId());
                                personalContactRepository.save(l);
                            }

                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }

    }

    @Override
    public ByteArrayInputStream load() {
        List<PersonalContact> personalContacts = personalContactRepository.findAll();
        ByteArrayInputStream in = personalContactToExcel(personalContacts);
        return in;
    }
}
