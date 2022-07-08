package ma.itroad.aace.eth.coref.service.impl;

import com.netflix.discovery.converters.Auto;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.bean.EconomicOperatorBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.EconomicOperatorMapper;
import ma.itroad.aace.eth.coref.repository.EconomicOperatorRepository;
import ma.itroad.aace.eth.coref.service.IEconomicOperatorService;
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

@Service
public class EconomicOperatorServiceImpl extends BaseServiceImpl<EconomicOperator, EconomicOperatorBean> implements IEconomicOperatorService {

    @Autowired
    private EconomicOperatorRepository economicOperatorRepository;

    @Autowired
    private EconomicOperatorMapper economicOperatorMapper;

    static String[] HEADERs = {" Code operateur economique", "Forme juridique", "Numero de registre du commerce", "numero d'agrement",
    "Numero d'identification fiscale", "Importer", "Exporter", "Agent de compensation"};
    static String SHEET = "EconomicOperators";


    @Override
    public void saveFromExcel(MultipartFile file) {
        try {
            List<EconomicOperator> economicOperators = excelToEconomicOperator(file.getInputStream());
            if (!economicOperators.isEmpty())
                economicOperators.stream().forEach(
                        l -> {
                            EconomicOperator temp = economicOperatorRepository.findByCode(l.getCode());

                            if (temp == null) {
                                economicOperatorRepository.save(l);
                            } else {
                                l.setId(temp.getId());
                                economicOperatorRepository.save(l);
                            }

                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }
    @Override
    public ByteArrayInputStream economicOperatorToExcel(List<EconomicOperator> economicOperators) {
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
                if (!economicOperators.isEmpty()) {

                    for (EconomicOperator economicOperator : economicOperators) {
                        Row row = sheet.createRow(rowIdx++);

                        row.createCell(0).setCellValue(economicOperator.getCode());
                        row.createCell(1).setCellValue(economicOperator.getLegalForm());
                        row.createCell(2).setCellValue(economicOperator.getTradeRegisterNumber());
                        row.createCell(3).setCellValue(economicOperator.getAgreementNumber());
                        row.createCell(4).setCellValue(economicOperator.getTaxIdentifierNumber());
                        row.createCell(5).setCellValue(economicOperator.getImporter());
                        row.createCell(6).setCellValue(economicOperator.getExporter());
                        row.createCell(6).setCellValue(economicOperator.getClearingAgent());
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
        List<EconomicOperator> economicOperatorList = economicOperatorRepository.findAll();
        ByteArrayInputStream in = economicOperatorToExcel(economicOperatorList);
        return in;
    }

    @Override
    public List<EconomicOperator> excelToEconomicOperator(InputStream is) {

        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<EconomicOperator> economicOperators = new ArrayList<EconomicOperator>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();

                EconomicOperator operator = new EconomicOperator();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    int type = currentCell.getCellType();
                    switch (cellIdx) {
                        case 0:
                            operator.setCode(currentCell.getStringCellValue());
                            break;

                        case 1:
                            operator.setLegalForm(currentCell.getStringCellValue());
                            break;

                        case 2:

                            if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                operator.setTradeRegisterNumber(null);

                            break;
                        case 3:

                            if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                operator.setAgreementNumber(null);

                            break;

                        case 4:
                                operator.setTaxIdentifierNumber(currentCell.getStringCellValue());
                            break;

                        case 5:
                            operator.setImporter(currentCell.getStringCellValue());
                            break;

                        case 6:
                            operator.setExporter(currentCell.getStringCellValue());
                            break;

                        case 7:
                            operator.setClearingAgent(currentCell.getStringCellValue());
                            break;

                        default:
                            break;
                    }
                    cellIdx++;
                }
                economicOperators.add(operator);

            }
            workbook.close();
            return economicOperators;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public Page<EconomicOperatorBean> getAll(int page, int size) {
        Page<EconomicOperator> economicOperatorEntities = economicOperatorRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn")));
        Page<EconomicOperatorBean> economicOperatorBeans = economicOperatorEntities.map(economicOperatorMapper::entityToBean);
        return economicOperatorBeans;
    }


    @Override
    public ErrorResponse delete(Long id) {
        try {
            economicOperatorRepository.deleteById(id);
            return new ErrorResponse(HttpStatus.OK, ErrorMessageType.DELETE_SUCESS.getMessagePattern(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(HttpStatus.CONFLICT, ErrorMessageType.DELETE_ERROR.getMessagePattern(), null);
        }

    }
}
