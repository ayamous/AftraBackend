package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.exception.TarifBookTaxationJoinNotFoundException;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarif;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.bean.TariffBookRefBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.TarifBookAgreementVMProjection;
import ma.itroad.aace.eth.coref.model.mapper.TariffBookRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.TarifBookNationalProcedureVMProjection;
import ma.itroad.aace.eth.coref.model.view.RegulationTariffBookRefVM;
import ma.itroad.aace.eth.coref.model.view.TarifBookAgreementVM;
import ma.itroad.aace.eth.coref.model.view.TarifBookNationalProcedureVM;
import ma.itroad.aace.eth.coref.model.view.TarifBookTaxationVM;
import ma.itroad.aace.eth.coref.repository.*;
import ma.itroad.aace.eth.coref.service.ITariffBookRefAgreementJoinService;
import ma.itroad.aace.eth.coref.service.helper.PageHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Service
public class TarifBookRefAgreementJoinServiceImp implements ITariffBookRefAgreementJoinService {

    static String[] HEADERs = {"REFERENCE TARIF BOOK", "REFERENCE AGREEMENT"};
    static String SHEET = "tarif-book-agreement-join";
    
    @Autowired
    Validator validator;

    @Autowired
    private TarifBookRefRepository tarifBookRefRepository;


    @Autowired
    private AgreementRepository agreementRepository;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    private TariffBookRefMapper tariffBookRefMapper;

    @Override
    public List<TarifBookAgreementVM> excelToElemetsRefs(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<TarifBookAgreementVM> documentCustomsRegimVMs = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> cellsInRow = currentRow.iterator();
                TarifBookAgreementVM tarifBookAgreementVM = new TarifBookAgreementVM();
                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    int type = currentCell.getCellType();
                    switch (cellIdx) {
                        case 0:
                            if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                tarifBookAgreementVM.setTarifBookReference(currentCell.getNumericCellValue() + "");
                            else
                                tarifBookAgreementVM.setTarifBookReference(currentCell.getStringCellValue());
                            TarifBookRef tarifBookRef = tarifBookRefRepository.findByReference(currentCell.getStringCellValue().replace("\"", ""));
                            System.out.println(currentCell.getStringCellValue().replace("\"", ""));
                            Long idCurentTarifBook = tarifBookRef != null ?
                                    tarifBookRef.getId() : -1;
                            tarifBookAgreementVM.setTarifBookId(idCurentTarifBook);
                            break;
                        case 1:

                            if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                tarifBookAgreementVM.setAgreementReference(currentCell.getNumericCellValue() + "");
                            else
                                tarifBookAgreementVM.setAgreementReference(currentCell.getStringCellValue());
                            Agreement agreement = agreementRepository.findByCode(currentCell.getStringCellValue().replace("\"", ""));
                            Long idTaxation = agreement != null ?
                                    agreement.getId() : -1;
                            tarifBookAgreementVM.setAgreementId(idTaxation);
                            break;

                        default:
                            break;
                    }
                    cellIdx++;
                }
                documentCustomsRegimVMs.add(tarifBookAgreementVM);
            }
            workbook.close();
            return documentCustomsRegimVMs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
    
	@Override
	public Set<ConstraintViolation<TarifBookAgreementVM>> validateTarifBookAgreementVM(TarifBookAgreementVM tarifBookAgreementVM) {

		Set<ConstraintViolation<TarifBookAgreementVM>> violations = validator.validate(tarifBookAgreementVM);

		return violations;
	}
    @Override
    public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
        try {
        	List<TarifBookAgreementVM> tarifBookAgreementVM = excelToElemetsRefs(file.getInputStream());
			 
			List<TarifBookAgreementVM> invalidTarifBookAgreementVM = new ArrayList<TarifBookAgreementVM>();
			List<TarifBookAgreementVM> validTarifBookAgreementVM = new ArrayList<TarifBookAgreementVM>();

			int lenght = tarifBookAgreementVM.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<TarifBookAgreementVM>> violations = validateTarifBookAgreementVM(
						tarifBookAgreementVM.get(i));
				if (violations.isEmpty())

				{
					validTarifBookAgreementVM.add(tarifBookAgreementVM.get(i));
				} else {
					
					
					invalidTarifBookAgreementVM.add(tarifBookAgreementVM.get(i));
				}
				
				if (!invalidTarifBookAgreementVM.isEmpty()) {

					ByteArrayInputStream out = tarifBookTaxationJoinToExcel(invalidTarifBookAgreementVM);
					xls = new InputStreamResource(out);}
        	
			}
        	
          
            //System.out.println(tarifBookAgreementVMs.toString());
            if (!validTarifBookAgreementVM.isEmpty())
            	validTarifBookAgreementVM.stream().forEach(
                        tbt -> {
                            if (tbt.getAgreementId() != -1 && tbt.getTarifBookId() != -1) {
                                this.save(tbt);
                            }
                        }
                );
            if (!invalidTarifBookAgreementVM.isEmpty())
 				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
 						.contentType(
 								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
 						.body(xls);

 			if (!validTarifBookAgreementVM.isEmpty())
 				return ResponseEntity.status(HttpStatus.OK).body(null);

 			return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }


    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            List<TarifBookAgreementVM> tarifBookAgreementVMs = excelToElemetsRefs(file.getInputStream());
            //System.out.println(tarifBookAgreementVMs.toString());
            if (!tarifBookAgreementVMs.isEmpty())
                tarifBookAgreementVMs.stream().forEach(
                        tbt -> {
                            if (tbt.getAgreementId() != -1 && tbt.getTarifBookId() != -1) {
                                this.save(tbt);
                            }
                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }


    public TarifBookAgreementVM save(TarifBookAgreementVM model) {

        Agreement agreement = agreementRepository.findOneById(model.getAgreementId());
        System.out.println(agreement);
        if (agreement != null) {
            Optional<TarifBookRef> optional = tarifBookRefRepository.findById(model.getTarifBookId());
            if (optional.isPresent()) {
                Set<TarifBookRef> tarifBookRefs = Stream.concat(agreement.getTarifBookRefs().stream(), Stream.of(optional.get())).collect(Collectors.toSet());
                System.out.println(tarifBookRefs);
                agreement.setTarifBookRefs(tarifBookRefs);
            }
        }

        Agreement entity = agreementRepository.save(agreement);

        if(entity != null ){
            return model;
        }

        return null;
    }

    @Override
    public Page<TarifBookAgreementVM> findTarifTaxation(int page, int size, String  reference) {
        List<TarifBookRef>  tarifBookRefs =  tarifBookRefRepository.findByReferenceIgnoreCaseContains(reference);
        List<TarifBookAgreementVM> list = new ArrayList<>();
        tarifBookRefs.forEach(tarifBookRef -> {
            tarifBookRef.getAgreements().forEach(agreement -> {
                list.add(

                        new TarifBookAgreementVM(tarifBookRef.getId(), agreement.getId(), tarifBookRef.getReference(), agreement.getCode(),"","","","","")

                );
            });
        });


        return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
    }

    @Override
    public ByteArrayInputStream load() {

        List<TarifBookAgreementVM> tarifBookAgreementVMs = getListOfTarifBookTaxationVMs();
        ByteArrayInputStream in = tarifBookTaxationJoinToExcel(tarifBookAgreementVMs);
        return in;


    }

    // get all data at first and separate it into smaller chunks is not good for the performance
    private List<TarifBookAgreementVM> getListOfTarifBookTaxationVMs() {

        List<TarifBookRef> result = tarifBookRefRepository.findAll();
        List<TarifBookAgreementVM> list = new ArrayList<>();
        result.forEach(tarifBookRef -> {
            tarifBookRef.getAgreements().forEach(agreement -> {
                list.add(

                        new TarifBookAgreementVM(tarifBookRef.getId(), agreement.getId(), tarifBookRef.getReference(), agreement.getCode(),"","","","","")

                );
            });
        });

        return list;
    }

    private Page<TarifBookAgreementVM> getListOfTarifBookTaxationVMs(int page, int size, String codeLang) {
        return mapTarifBookAgreementVMProjectionToTarifBookNationalProcedureVM(tarifBookRefRepository.getTarifBookAgreementData(PageRequest.of(page, size)), codeLang);
    }

    public Page<TarifBookAgreementVM> mapTarifBookAgreementVMProjectionToTarifBookNationalProcedureVM(Page<TarifBookAgreementVMProjection> tarifBookNationalProcedureVMProjections, String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<TarifBookAgreementVM> tarifBookNationalProcedureVMs = new ArrayList<>();

        return tarifBookNationalProcedureVMProjections.map(tarifBookNationalProcedureVMProjection -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, lang.getId(), tarifBookNationalProcedureVMProjection.getAgreementId());
            EntityRefLang entityRefLangTarif = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), tarifBookNationalProcedureVMProjection.getTarifBookId());
            TarifBookAgreementVM agreementLang = new TarifBookAgreementVM();

            agreementLang.setAgreementLabel(entityRefLang!=null?entityRefLang.getLabel():"");
            agreementLang.setAgreementId(tarifBookNationalProcedureVMProjection!=null ?
                    tarifBookNationalProcedureVMProjection.getAgreementId():null);
            agreementLang.setAgreementReference(tarifBookNationalProcedureVMProjection!=null?
                    tarifBookNationalProcedureVMProjection.getAgreementReference():"");

            agreementLang.setTariffBookLabel(entityRefLangTarif!=null?entityRefLangTarif.getLabel():"");
            agreementLang.setTarifBookId(tarifBookNationalProcedureVMProjection!=null ?tarifBookNationalProcedureVMProjection.getTarifBookId():null);
            agreementLang.setTarifBookReference(tarifBookNationalProcedureVMProjection!=null?tarifBookNationalProcedureVMProjection.getTarifBookReference():"");
            agreementLang.setLang(codeLang);

            tarifBookNationalProcedureVMs.add(agreementLang);

            return  agreementLang ;
        });
    }

    @Override
    public ByteArrayInputStream tarifBookTaxationJoinToExcel(List<TarifBookAgreementVM> tarifBookTaxationVMs) {

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
                if (!tarifBookTaxationVMs.isEmpty()) {

                    for (TarifBookAgreementVM model : tarifBookTaxationVMs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(model.getTarifBookReference());
                        row.createCell(1).setCellValue(model.getAgreementReference());

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
    public Page<TarifBookAgreementVM> getAll(int page, int size, String codeLang) {
        return getListOfTarifBookTaxationVMs( page, size, codeLang);
    }

    @Override
    public ByteArrayInputStream load(String codeLang, final int page, final int size) {
        List<TarifBookAgreementVM> listTarifBookTaxationVM = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        Page<TarifBookRef> tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size));

        for (TarifBookRef t : tarifBookRefs) {
            EntityRefLang tariffBookEntityRefLang = entityRefLangRepository
                    .findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), t.getId());
            if (tariffBookEntityRefLang != null) {
                t.getAgreements().forEach(taxation -> {
                    EntityRefLang taxationEntityRefLang = entityRefLangRepository
                            .findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, lang.getId(), !Objects.isNull(taxation) ? taxation.getId() : 0);
                    listTarifBookTaxationVM.add(new TarifBookAgreementVM(t.getId(),
                            taxation.getId(), t.getReference(), taxation.getCode(),
                            (!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getLabel())) ? tariffBookEntityRefLang.getLabel() : " ",
                            (!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getDescription())) ? tariffBookEntityRefLang.getDescription() : " ",
                            (!Objects.isNull(taxationEntityRefLang) && !Objects.isNull(taxationEntityRefLang.getLabel())) ? taxationEntityRefLang.getLabel() : " ",
                            (!Objects.isNull(taxationEntityRefLang) && !Objects.isNull(taxationEntityRefLang.getDescription())) ? taxationEntityRefLang.getDescription() : " ",
                            codeLang));
                });
            }
        }

        ByteArrayInputStream in = tarifBookTaxationJoinToExcel(listTarifBookTaxationVM);

        return in;

    }

    @Override
    public Page<TarifBookAgreementVM> findTarifTaxationJoin(Long idTarifBookRef, String codeLang, int page, int size) {
        TarifBookRef tarifBookRef = tarifBookRefRepository.findOneById(idTarifBookRef);
        List<TarifBookAgreementVM> listTarifBookTaxationVM = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);

        EntityRefLang entityRefLang = entityRefLangRepository
                .findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), !Objects.isNull(tarifBookRef) ? tarifBookRef.getId() : 0);

        if (Objects.isNull(tarifBookRef) || Objects.isNull(entityRefLang)) {
            throw new TarifBookTaxationJoinNotFoundException(
                    "TarifBookTaxationVM: " + idTarifBookRef);
        }

        if (!Objects.isNull(entityRefLang)) {
            tarifBookRef.getAgreements().forEach(taxation -> {
                listTarifBookTaxationVM.add(

                        new TarifBookAgreementVM(tarifBookRef.getId(), taxation.getId(), tarifBookRef.getReference(), taxation.getCode(),
                                " ", " ", " ", " ",	" ")

                );
            });
        }

        return PageHelper.listConvertToPage(listTarifBookTaxationVM, listTarifBookTaxationVM.size(), PageRequest.of(page, size));

    }

    @Override
    public Page<TarifBookAgreementVM> getAll(String codeLang, int page, int size, String orderDirection) {
        List<TarifBookAgreementVM> tarifBookTaxationVMs = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        Page<TarifBookRef> tarifBookRefs = null;
        if(orderDirection.equals("DESC")){
            tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }

        for (TarifBookRef t : tarifBookRefs) {
            EntityRefLang tariffBookEntityRefLang = entityRefLangRepository
                    .findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), t.getId());
            if (tariffBookEntityRefLang != null) {
                t.getAgreements().forEach(taxation -> {
                    EntityRefLang taxationEntityRefLang = entityRefLangRepository
                            .findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, lang.getId(), !Objects.isNull(taxation) ? taxation.getId() : 0);
                    tarifBookTaxationVMs.add(new TarifBookAgreementVM(t.getId(),
                            taxation.getId(), t.getReference(), taxation.getCode(),
                            (!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getLabel())) ? tariffBookEntityRefLang.getLabel() : " ",
                            (!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getDescription())) ? tariffBookEntityRefLang.getDescription() : " ",
                            (!Objects.isNull(taxationEntityRefLang) && !Objects.isNull(taxationEntityRefLang.getLabel())) ? taxationEntityRefLang.getLabel() : " ",
                            (!Objects.isNull(taxationEntityRefLang) && !Objects.isNull(taxationEntityRefLang.getDescription())) ? taxationEntityRefLang.getDescription() : " ",
                            codeLang));
                });
            }
        }

        return PageHelper.listConvertToPage(tarifBookTaxationVMs, tarifBookTaxationVMs.size(), PageRequest.of(page, size));
    }

    @Override
    public ErrorResponse delete(Long id, Long id_tarif) {
        try {
            Agreement agreement = agreementRepository.findOneById(id);
            System.out.println(agreement);
            if (agreement != null) {
                Optional<TarifBookRef> optional = tarifBookRefRepository.findById(id_tarif);
                if (optional.isPresent()) {
                    agreement.getTarifBookRefs().remove(optional.get());
                    System.out.println(agreement.getTarifBookRefs());
                }
            }

            agreementRepository.save(agreement);
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
