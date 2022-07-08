package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.UserAccountBean;
import ma.itroad.aace.eth.coref.model.entity.Profil;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;
import ma.itroad.aace.eth.coref.model.enums.StatusUserAccount;
import ma.itroad.aace.eth.coref.repository.ProfilRepository;
import ma.itroad.aace.eth.coref.repository.UserAccountRepository;
import ma.itroad.aace.eth.coref.security.keycloak.utils.SecurityUtils;
import ma.itroad.aace.eth.coref.service.IUserAccountService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserAccountServiceImpl extends BaseServiceImpl<UserAccount, UserAccountBean> implements IUserAccountService {

    static String[] HEADERs = {"REFERENCE UTILISATEUR", "LOGIN",
            "MOT DE PASSE", "DATE EXPIRATION", "STATUT", "REFERENCE PROFIL"};

    static String SHEET = "UtilisateurSHEET";

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            List<UserAccount> users = excelToUserAccounts(file.getInputStream());
            if (!users.isEmpty())
                users.stream().forEach(
                        user -> {
                            Optional<UserAccount> ltemp =
                                    userAccountRepository.findByReference(user.getReference());
                            if (!ltemp.isPresent()) {

                                userAccountRepository.save(user);
                            } else {
                                user.setId(ltemp.get().getId());
                                userAccountRepository.save(user);
                            }
                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    @Override
    public Page<UserAccountBean> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserAccount> userPage = userAccountRepository.findAll(pageable);

        Page<UserAccountBean> taxationBeanPage = userPage.map(user -> {
            return new UserAccountBean(
                    user.getId(),
                    user.getReference(), //
                    user.getLogin(), //
                    user.getPassword(),//
                    user.getTemporalPwdExpDate(),
                    user.getStatus(),
                    user.getProfil().getReference(),
                    user.getProfil().getId());

        });
        return taxationBeanPage;
    }

    @Override
    public List<UserAccount> excelToUserAccounts(InputStream is) {

        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<UserAccount> users = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                UserAccount user = new UserAccount();
                for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++)
                    for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                        Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);

                        switch (colNum) {
                            case 0:
                                if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                    user.setReference((int) currentCell.getNumericCellValue() + "");
                                else
                                    user.setReference(currentCell.getStringCellValue());
                                break;
                            case 1:
                                if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                    user.setLogin((int) currentCell.getNumericCellValue() + "");
                                else
                                    user.setLogin(currentCell.getStringCellValue());
                                break;
                            case 2:
                                if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                    user.setPassword((int) currentCell.getNumericCellValue() + "");
                                else
                                    user.setPassword(currentCell.getStringCellValue());
                                break;
                            case 3:
                                user.setTemporalPwdExpDate(currentCell.getDateCellValue());
                                break;
                            case 4:
                                user.setStatus(setStatusFromExcel(currentCell.getStringCellValue()));
                                break;
                            case 5:
                                String profilReference;
                                if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                    profilReference = (int) currentCell.getNumericCellValue() + "";
                                else
                                    profilReference = currentCell.getStringCellValue();
                                Optional<Profil> profilOpt = profilRepository.findByReference(profilReference);
                                if (profilOpt.isPresent()) {
                                    user.setProfil(profilOpt.get());
                                }
                                break;
                            default:
                                break;
                        }

                    }
                users.add(user);
            }
            workbook.close();
            return users;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public LocalDate formatStringToDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        return LocalDate.parse(date, formatter);
    }

    public StatusUserAccount setStatusFromExcel(String status) {
        if (status.equals("ENABLED"))
            return StatusUserAccount.ENABLED;
        if (status.equals("SUSPENDED"))
            return StatusUserAccount.SUSPENDED;
        if (status.equals("DISABLED"))
            return StatusUserAccount.DISABLED;
        return null;
    }

    @Override
    public ByteArrayInputStream userAccountsToExcel(List<UserAccount> userAccounts) {

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
                if (!userAccounts.isEmpty()) {
                    for (UserAccount user : userAccounts) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(user.getReference());
                        row.createCell(1).setCellValue(user.getLogin());
                        row.createCell(2).setCellValue(user.getPassword());
                        row.createCell(3).setCellValue(user.getTemporalPwdExpDate());
                        row.createCell(4).setCellValue(user.getStatus().toString());
                        row.createCell(5).setCellValue(user.getProfil().getReference());

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
        List<UserAccount> users = userAccountRepository.findAll();
        ByteArrayInputStream in = userAccountsToExcel(users);
        return in;
    }

    @Override
    public Optional<UserAccount> getCurrentUser() {
        Optional<UserAccount> currentUser = SecurityUtils.getCurrentUser();
        if(currentUser.isPresent()) {
            Optional<UserAccount> dbUser = userAccountRepository.findByReference(currentUser.get().getReference());
            if (dbUser.isPresent()) {
                currentUser.get().setId(dbUser.get().getId());
                return currentUser;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserAccount> findUserByReference(String reference) {
        return userAccountRepository.findByReference(reference);
    }

    @Override
    public Set<UserAccount> findAllByOrganizationId(Long organizationId) {
        return userAccountRepository.findAllByOrganization_Id(organizationId);
    }

    @Override
    public ErrorResponse delete(Long id) {
        try {
            userAccountRepository.deleteById(id);
            return new ErrorResponse(HttpStatus.OK, ErrorMessageType.DELETE_SUCESS.getMessagePattern(), null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(HttpStatus.CONFLICT, ErrorMessageType.DELETE_ERROR.getMessagePattern(), null);
        }

    }

}
