package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CountryGroupRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarif;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.CountryGroupRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.CountryRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.CountryGroupReferenceJoinProjection;
import ma.itroad.aace.eth.coref.model.view.CountryGroupReferenceVM;
import ma.itroad.aace.eth.coref.repository.CountryGroupRefRepository;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.service.ICountryGroupJoinService;
import ma.itroad.aace.eth.coref.service.helper.CountryGroupReferenceVMEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.PageHelper;
import ma.itroad.aace.eth.coref.service.helper.TariffBookRefLang;
import ma.itroad.aace.eth.coref.service.util.Util;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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
public class CountryGroupJoinServiceImpl implements ICountryGroupJoinService {

    static String[] HEADERs = {" COUNTRY REFERENCE ", "LABEL COUNTRY","DESCRIPTION COUNTRY","COUNTRY GROUP REFERENCE","LABEL COUNTRY GROUP","DESCRIPTION COUNTRY GROUP"};
    static String SHEET = "country-group-join";

    @Autowired
    CountryGroupRefRepository countryGroupRefRepository;

    @Autowired
    LangRepository langRepository;
    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    CountryRefRepository countryRefRepository;

    @Autowired
    CountryGroupRefMapper countryGroupRefMapper;

    @Autowired
    CountryRefMapper countryRefMapper;


    @Override
    public Page<CountryGroupReferenceVM> getAll(final int page, final int size, String orderDirection) {

        List<CountryGroupReferenceVM> list = getListOfCountryGroupReference();

        return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
    }

    private List<CountryGroupReferenceVM> getListOfCountryGroupReference() {

        List<CountryGroupRef> result = countryGroupRefRepository.findAll();
        List<CountryGroupReferenceVM> list = new ArrayList<>();
        result.forEach(countryGroupRef -> {
            countryGroupRef.getCountryRefs().forEach(countryRef -> {
                list.add(new CountryGroupReferenceVM(countryGroupRef.getReference(), countryRef.getReference(),
                 countryGroupRef.getId(), countryRef.getId()) );

            });
        });

        return list;

        }



    @Override
    public ByteArrayInputStream load() {

        List<CountryGroupReferenceVM> countryGroupReferenceVM = getListOfCountryGroupReference();
        ByteArrayInputStream in = null;
               // countryGroupJoinToExcel(countryGroupReferenceVM);
        return in;


    }

    public ByteArrayInputStream countryGroupJoinToExcel(List<CountryGroupReferenceVMEntityRefLang> countryGroupReferenceVMEntityRefLangs) {

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
                if (!countryGroupReferenceVMEntityRefLangs.isEmpty()) {

                    for (CountryGroupReferenceVMEntityRefLang model : countryGroupReferenceVMEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(model.getCountryReference());
                        row.createCell(1).setCellValue(model.getLabelCountry());
                        row.createCell(2).setCellValue(model.getDescriptionCountry());
                        row.createCell(3).setCellValue(model.getCountryGroupReference());
                        row.createCell(4).setCellValue(model.getLabelCountryGroup());
                        row.createCell(5).setCellValue(model.getDescriptionCountryGroup());
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
    public List<CountryGroupReferenceVM> excelToElemetsRefsRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<CountryGroupReferenceVM> countryGroupReferenceVMs = new ArrayList<CountryGroupReferenceVM>();
            Row currentRow;

            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                currentRow= sheet.getRow(rowNum);
                CountryGroupReferenceVM countryGroupReferenceVM = new CountryGroupReferenceVM();
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 3:
                            countryGroupReferenceVM.setCountryGroupReference(Util.cellValue(currentCell));
                            break;
                        case 0:
                            countryGroupReferenceVM.setCountryReference(Util.cellValue(currentCell));
                            break;

                        default:
                            break;
                    }
                }
                countryGroupReferenceVMs.add(countryGroupReferenceVM);
            }
            workbook.close();
            return countryGroupReferenceVMs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }


    @Override
    public void saveFromExcel(MultipartFile file) {
        try {
            List<CountryGroupReferenceVM> countryGroupReferenceVMs = excelToElemetsRefsRef(file.getInputStream());
            if (!countryGroupReferenceVMs.isEmpty())
                countryGroupReferenceVMs.stream().forEach(
                        l -> {
                            if (l.getCountryGroupReference() !=null && l.getCountryReference() != null) {
                                this.save(l);
                            }
                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    @Override
    public CountryGroupReferenceVM save(CountryGroupReferenceVM model) {

        CountryGroupRef countryGroupRef = countryGroupRefRepository.findByReference(model.getCountryGroupReference());
          if (countryGroupRef != null) {

             CountryRef entity = countryRefRepository.findByReference(model.getCountryReference());
             Set<CountryRef> countryRefs = Stream.concat(countryGroupRef.getCountryRefs().stream(), Stream.of(entity)).collect(Collectors.toSet());
             countryGroupRef.setCountryRefs(countryRefs);
        }
        CountryGroupRef entity = countryGroupRefRepository.save(countryGroupRef);
        return model;
    }


    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<CountryGroupRef> result = countryGroupRefRepository.findAll();
        List<CountryGroupReferenceVMEntityRefLang> list = new ArrayList<>();
        result.forEach(countryGroupRef -> {
            countryGroupRef.getCountryRefs().forEach(countryRef -> {
                EntityRefLang entityRefLangCountry = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_REF, lang.getId(), countryRef.getId());
                EntityRefLang entityRefLangCountryGroupRef = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_GROUP_REF, lang.getId(), countryGroupRef.getId());

                CountryGroupReferenceVMEntityRefLang countryGroupReferenceVMEntityRefLang = new CountryGroupReferenceVMEntityRefLang();
                countryGroupReferenceVMEntityRefLang.setCountryReference(countryRef.getReference());
                countryGroupReferenceVMEntityRefLang.setLabelCountry(entityRefLangCountry.getLabel());
                countryGroupReferenceVMEntityRefLang.setDescriptionCountry(entityRefLangCountry.getDescription());
                countryGroupReferenceVMEntityRefLang.setCountryGroupReference(countryGroupRef.getReference());
                countryGroupReferenceVMEntityRefLang.setLabelCountryGroup(entityRefLangCountryGroupRef.getLabel());
                countryGroupReferenceVMEntityRefLang.setDescriptionCountryGroup(entityRefLangCountryGroupRef.getDescription());
                list.add(countryGroupReferenceVMEntityRefLang);

            });
        });

        ByteArrayInputStream in = countryGroupJoinToExcel(list);

        return in;
    }

    @Override
    public CountryGroupReferenceVMEntityRefLang findCountryGroupReferenceVM(Long idCountry,Long idCountryGroup, String lang){
        CountryGroupReferenceVMEntityRefLang countryGroupReferenceVMEntityRefLang = new CountryGroupReferenceVMEntityRefLang();
        CountryRef countryRef = countryRefRepository.findOneById(idCountry);
        EntityRefLang entityRefLangCountry =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_REF,langRepository.findByCode(lang).getId(),countryRef.getId());
        countryGroupReferenceVMEntityRefLang.setCountryReference(countryRef.getReference());
        countryGroupReferenceVMEntityRefLang.setLabelCountry(entityRefLangCountry.getLabel());
        countryGroupReferenceVMEntityRefLang.setDescriptionCountry(entityRefLangCountry.getDescription());
        countryGroupReferenceVMEntityRefLang.setCountryRefId(entityRefLangCountry.getRefId());
        CountryGroupRef countryGroupRef = countryGroupRefRepository.findOneById(idCountry);
        EntityRefLang entityRefLangCountryGroup =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.COUNTRY_GROUP_REF,langRepository.findByCode(lang).getId(),countryGroupRef.getId());
        countryGroupReferenceVMEntityRefLang.setCountryGroupReference(countryGroupRef.getReference());
        countryGroupReferenceVMEntityRefLang.setLabelCountryGroup(entityRefLangCountryGroup.getLabel());
        countryGroupReferenceVMEntityRefLang.setDescriptionCountryGroup(entityRefLangCountryGroup.getDescription());
        countryGroupReferenceVMEntityRefLang.setCountryGroupRefId(entityRefLangCountryGroup.getRefId());
        return countryGroupReferenceVMEntityRefLang;
    }

    @Override
    public Page<CountryGroupReferenceJoinProjection> findByRefrence(int page, int size, String reference) {

        return countryGroupRefRepository.findByReferenceProjection(reference.toLowerCase(),PageRequest.of(page,size));
    }

    @Override
    public ErrorResponse delete(Long id, Long id_tarif) {
        try {
            CountryGroupRef countryGroupRef = countryGroupRefRepository.findOneById(id);
            if (countryGroupRef != null) {
                Optional<CountryRef> optional = countryRefRepository.findById(id_tarif);
                if (optional.isPresent()) {
                    countryGroupRef.getCountryRefs().remove(optional.get());
                }
            }

            countryGroupRefRepository.save(countryGroupRef);
            return new ErrorResponse(HttpStatus.OK, ErrorMessageType.DELETE_SUCESS.getMessagePattern(), null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(HttpStatus.CONFLICT, ErrorMessageType.DELETE_ERROR.getMessagePattern(), null);
        }
    }

    @Override
    public ErrorResponse deleteList(ListOfObjectTarifBook listOfObjectTarifBook) {
        try {
            for(ListOfObjectTarif m:listOfObjectTarifBook.getListOfObjectTarifBook()){
                delete(m.getEntitId(), m.getTarifBookId());
            }
            return new ErrorResponse(HttpStatus.OK, ErrorMessageType.DELETE_SUCESS.getMessagePattern(), null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(HttpStatus.CONFLICT, ErrorMessageType.DELETE_ERROR.getMessagePattern(), null);
        }
    }

}
