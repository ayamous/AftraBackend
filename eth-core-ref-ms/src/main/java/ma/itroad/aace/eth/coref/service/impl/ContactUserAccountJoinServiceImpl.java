package ma.itroad.aace.eth.coref.service.impl;

import com.netflix.discovery.converters.Auto;
import ma.itroad.aace.eth.coref.model.bean.PersonalContactBean;
import ma.itroad.aace.eth.coref.model.entity.PersonalContact;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;
import ma.itroad.aace.eth.coref.model.mapper.PersonalContactMapper;
import ma.itroad.aace.eth.coref.model.view.ContactUserAccountVM;
import ma.itroad.aace.eth.coref.repository.PersonalContactRepository;
import ma.itroad.aace.eth.coref.repository.UserAccountRepository;
import ma.itroad.aace.eth.coref.service.IContactUserAccountJoinService;
import ma.itroad.aace.eth.coref.service.helper.PageHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ContactUserAccountJoinServiceImpl implements IContactUserAccountJoinService {

    static String[] HEADERs = {"REFERENCE CONTACT", "REFERENCE UTILISATEUR"};
    static String SHEET = "relationContactSheet";

    @Autowired
    private PersonalContactRepository personalContactRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PersonalContactMapper personalContactMapper;

    @Override
    public Page<ContactUserAccountVM> getAll(int page, int size) {

        List<ContactUserAccountVM> list = getListOfDocumentContactUserAccountVMs();
        return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
    }

    private List<ContactUserAccountVM> getListOfDocumentContactUserAccountVMs() {
        List<PersonalContact> result = personalContactRepository.findAll();
        List<ContactUserAccountVM> list = new ArrayList<>();
        result.forEach(personalContact -> {
            personalContact.getUserAccounts().forEach(userAccount -> {
                list.add(new ContactUserAccountVM(personalContact.getId(), userAccount.getId(),
                        personalContact.getReference(), userAccount.getReference()));
            });
        });

        return list;
    }

    @Override
    public Collection<ContactUserAccountVM> excelToElementsRefs(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<ContactUserAccountVM> contactUserAccountVMs = new ArrayList<ContactUserAccountVM>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();

                ContactUserAccountVM contactUserAccountVM = new ContactUserAccountVM();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    int type = currentCell.getCellType();
                    switch (cellIdx) {
                        case 0:
                            contactUserAccountVM.setContactReference(currentCell.getStringCellValue());
                            Long idContact = personalContactRepository.findByReference(currentCell.getStringCellValue()).isPresent() ?
                                    personalContactRepository.findByReference(currentCell.getStringCellValue()).get().getId() : -1;
                            contactUserAccountVM.setContactId(idContact);
                            break;
                        case 1:
                            contactUserAccountVM.setUserAccountReference(currentCell.getStringCellValue());
                            Long idUserAccount = userAccountRepository.findByReference(currentCell.getStringCellValue()).isPresent() ?
                                    userAccountRepository.findByReference(currentCell.getStringCellValue()).get().getId() : -1;
                            contactUserAccountVM.setUserId(idUserAccount);
                            break;

                        default:
                            break;
                    }
                    cellIdx++;
                }
                contactUserAccountVMs.add(contactUserAccountVM);
            }
            workbook.close();
            return contactUserAccountVMs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            Collection<ContactUserAccountVM> contactUserAccountVMs = excelToElementsRefs(file.getInputStream());
            if (!contactUserAccountVMs.isEmpty())
                contactUserAccountVMs.stream().forEach(
                        contactUser -> {
                            if (contactUser.getContactId() != -1 && contactUser.getUserId() != -1) {
                                this.save(contactUser);
                            }
                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream load() {
        List<ContactUserAccountVM> contactUserAccountVM = getListOfContactUserAccountVMs();
        ByteArrayInputStream in = contactUserAccountJoinToExcel(contactUserAccountVM);
        return in;
    }

    private List<ContactUserAccountVM> getListOfContactUserAccountVMs() {

        List<PersonalContact> result = personalContactRepository.findAll();
        List<ContactUserAccountVM> list = new ArrayList<>();
        result.forEach(personalContact -> {
            personalContact.getUserAccounts().forEach(userAccount -> {
                list.add(new ContactUserAccountVM(personalContact.getId(), userAccount.getId(),
                        personalContact.getReference(), userAccount.getReference()));

            });
        });

        return list;
    }

    public ByteArrayInputStream contactUserAccountJoinToExcel(List<ContactUserAccountVM> contactUserAccountVMs) {

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
                if (!contactUserAccountVMs.isEmpty()) {

                    for (ContactUserAccountVM model : contactUserAccountVMs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(model.getContactReference());
                        row.createCell(1).setCellValue(model.getUserAccountReference());

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

    public PersonalContactBean save(ContactUserAccountVM model) {

        PersonalContact personalContact = personalContactRepository.findOneById(model.getContactId());

        if (personalContact != null) {

            Optional<UserAccount> optional = userAccountRepository.findById(model.getUserId());

            if (optional.isPresent()) {

                Set<UserAccount> userAccounts = Stream.concat(personalContact.getUserAccounts().stream(), Stream.of(optional.get())).collect(Collectors.toSet());
                personalContact.setUserAccounts(userAccounts);
            }
        }
        PersonalContact entity = personalContactRepository.save(personalContact);
        return personalContactMapper.entityToBean(entity);
    }
}

