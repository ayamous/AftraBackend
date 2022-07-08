package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ProfilBean;
import ma.itroad.aace.eth.coref.model.entity.Profil;
import ma.itroad.aace.eth.coref.repository.ProfilRepository;
import ma.itroad.aace.eth.coref.service.IProfilService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
public class ProfilServiceImpl extends BaseServiceImpl<Profil, ProfilBean> implements IProfilService {


    static String[] HEADERs = {"REFERENCE", "NOM", "MDP TEMPORAIRE", "DESCRIPTION"};
    static String SHEET = "ProfilesSheet";

    @Autowired
    private ProfilRepository profilRepository;

    @Override
    public Page<ProfilBean> getAll(int page, int size) {
        return null;
    }

    @Override
    public List<Profil> excelToProfils(InputStream is) {

        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<Profil> profils = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Profil profil = new Profil();
                for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++)
                    for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                        Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);

                        switch (colNum) {
                            case 0:
                                if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                    profil.setReference((int) currentCell.getNumericCellValue() + "");
                                else
                                    profil.setReference(currentCell.getStringCellValue());
                                break;
                            case 1:
                                if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                    profil.setName((int) currentCell.getNumericCellValue() + "");
                                else
                                    profil.setName(currentCell.getStringCellValue());
                                break;
                            case 2:
                                if (currentCell.getCellType() == Cell.CELL_TYPE_BOOLEAN)
                                    profil.setEnabled(currentCell.getBooleanCellValue());
                                break;
                            case 3:
                                if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                    profil.setDescription((int) currentCell.getNumericCellValue() + "");
                                else
                                    profil.setDescription(currentCell.getStringCellValue());
                                break;
                            default:
                                break;
                        }

                    }
                profils.add(profil);
            }
            workbook.close();
            return profils;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream profilsToExcel(List<Profil> profils) {

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
                if (!profils.isEmpty()) {
                    for (Profil profil : profils) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(profil.getReference());
                        row.createCell(1).setCellValue(profil.getName());
                        row.createCell(2).setCellValue(profil.isEnabled());
                        row.createCell(3).setCellValue(profil.getDescription() != null ? profil.getDescription() : "");
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
            profilRepository.deleteById(id);
            return new ErrorResponse(HttpStatus.OK, ErrorMessageType.DELETE_SUCESS.getMessagePattern(), null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(HttpStatus.CONFLICT, ErrorMessageType.DELETE_ERROR.getMessagePattern(), null);
        }

    }

    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            List<Profil> profils = excelToProfils(file.getInputStream());
            if (!profils.isEmpty())
                profils.stream().forEach(
                        prof -> {
                            Optional<Profil> ltemp =
                                    profilRepository.findByReference(prof.getReference());
                            if (!ltemp.isPresent()) {

                                profilRepository.save(prof);
                            } else {
                                prof.setId(ltemp.get().getId());
                                profilRepository.save(prof);
                            }
                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream load() {
        List<Profil> profils = profilRepository.findAll();
        ByteArrayInputStream in = profilsToExcel(profils);
        return in;
    }
}
