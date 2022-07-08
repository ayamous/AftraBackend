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
import ma.itroad.aace.eth.coref.model.mapper.projections.TarifBookTaxationVMProjection;
import ma.itroad.aace.eth.coref.model.mapper.TariffBookRefMapper;
import ma.itroad.aace.eth.coref.model.view.TarifBookAgreementVM;
import ma.itroad.aace.eth.coref.model.view.TarifBookTaxationVM;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.TarifBookRefRepository;
import ma.itroad.aace.eth.coref.repository.TaxationRepository;
import ma.itroad.aace.eth.coref.service.ITariffBookRefTaxationJoinService;
import ma.itroad.aace.eth.coref.service.helper.ExcelCellFormatter;
import ma.itroad.aace.eth.coref.service.helper.PageHelper;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
public class TariffBookRefTaxationJoinServiceImpl implements ITariffBookRefTaxationJoinService {

    static String[] HEADERs = {"REFERENCE TARIF BOOK", "REFERENCE TAXATION"};
    static String SHEET = "tarif-book-taxation-join";
    
    @Autowired
    Validator validator;
    
    @Autowired
    private TarifBookRefRepository tarifBookRefRepository;


    @Autowired
    private TaxationRepository taxationRepository;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    private TariffBookRefMapper tariffBookRefMapper;

    @Override
    public List<TarifBookTaxationVM> excelToElemetsRefs(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<TarifBookTaxationVM> documentCustomsRegimVMs = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> cellsInRow = currentRow.iterator();
                TarifBookTaxationVM tarifBookTaxationVM = new TarifBookTaxationVM();
                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    int type = currentCell.getCellType();
                    String cellValue = ExcelCellFormatter.getCellStringValue(currentCell);
                    switch (cellIdx) {
                        case 0:
                       
                                tarifBookTaxationVM.setTarifBookReference(cellValue);
                          
                            break;
                        case 1:

                        
                                tarifBookTaxationVM.setTaxationReference(Util.cellValue(currentCell));
                         
                            break;

                        default:
                            break;
                    }
                    cellIdx++;
                }
                documentCustomsRegimVMs.add(tarifBookTaxationVM);
            }
            workbook.close();
            return documentCustomsRegimVMs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    
    @Override
	public Set<ConstraintViolation<TarifBookTaxationVM>> validateTarifBookTaxationVM(TarifBookTaxationVM tarifBookTaxationVM) {

		Set<ConstraintViolation<TarifBookTaxationVM>> violations = validator.validate(tarifBookTaxationVM);

		return violations;
	}
    
    @Override
    public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
    	String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
        try {

			
			List<TarifBookTaxationVM> versionTariffBookRefVM = excelToElemetsRefs(file.getInputStream());
			 
			List<TarifBookTaxationVM> invalidTariffBookRefVM = new ArrayList<TarifBookTaxationVM>();
			List<TarifBookTaxationVM> validTariffBookRefVM = new ArrayList<TarifBookTaxationVM>();

			int lenght = versionTariffBookRefVM.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<TarifBookTaxationVM>> violations = validateTarifBookTaxationVM(
						versionTariffBookRefVM.get(i));
				if (violations.isEmpty())

				{
					validTariffBookRefVM.add(versionTariffBookRefVM.get(i));
				} else {
					
					
					invalidTariffBookRefVM.add(versionTariffBookRefVM.get(i));
				}
				
				if (!invalidTariffBookRefVM.isEmpty()) {

					ByteArrayInputStream out = tarifBookTaxationJoinToExcel(invalidTariffBookRefVM);
					xls = new InputStreamResource(out);}
        	
			}
        	
        	
        	
         
            if (!validTariffBookRefVM.isEmpty())
            	validTariffBookRefVM.forEach(
                        tbt -> {
                            if (tbt.getTaxationId() != -1 && tbt.getTarifBookId() != -1) {
                                this.save(tbt);
                            }
                        }
                );
            
            if (!invalidTariffBookRefVM.isEmpty())
				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);

			if (!validTariffBookRefVM.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(null);

			return ResponseEntity.status(HttpStatus.OK).body(null);
            
            
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }



    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            List<TarifBookTaxationVM> tarifBookTaxationVMs = excelToElemetsRefs(file.getInputStream());
            if (!tarifBookTaxationVMs.isEmpty())
                tarifBookTaxationVMs.stream().forEach(
                        tbt -> {
                            if (tbt.getTaxationId() != -1 && tbt.getTarifBookId() != -1) {
                                this.save(tbt);
                            }
                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }


    public TariffBookRefBean save(TarifBookTaxationVM model) {

        TarifBookRef tarifBookRef = tarifBookRefRepository.findOneById(model.getTarifBookId());

        if (tarifBookRef != null) {

            Optional<Taxation> optional = taxationRepository.findById(model.getTaxationId());


            if (optional.isPresent()) {

                Set<Taxation> taxations = Stream.concat(tarifBookRef.getTaxations().stream(), Stream.of(optional.get())).collect(Collectors.toSet());
                tarifBookRef.setTaxations(taxations);
            }
        }
        TarifBookRef entity = tarifBookRefRepository.save(tarifBookRef);
        return tariffBookRefMapper.entityToBean(entity);
    }

    @Override
    public Page<TarifBookTaxationVMProjection> findTarifTaxation(int page, int size, String  reference) {
        return tarifBookRefRepository.findByReferenceProjection(reference,PageRequest.of(page,size));
    }

//    @Override
//    public Page<TarifBookTaxationVM> findTarifTaxation(int page, int size, String  reference) {
//        List<TarifBookRef>  tarifBookRefs =  tarifBookRefRepository.findByReferenceIgnoreCaseContains(reference);
//        List<TarifBookTaxationVM> list = new ArrayList<>();
//        tarifBookRefs.forEach(tarifBookRef -> {
//            tarifBookRef.getTaxations().forEach(taxation -> {
//                list.add(
//
//                        new TarifBookTaxationVM(tarifBookRef.getId(), taxation.getId(), tarifBookRef.getReference(), taxation.getReference(),
//                                " ", " ", " ", " ", " ")
//
//                );
//            });
//        });
//        return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
//    }

    @Override
    public ByteArrayInputStream load() {

        List<TarifBookTaxationVM> tarifBookTaxationVMs = getListOfTarifBookTaxationVMs();
        ByteArrayInputStream in = tarifBookTaxationJoinToExcel(tarifBookTaxationVMs);
        return in;


    }


    // get all data at first and separate it into smaller chunks is not good for the performance
    private List<TarifBookTaxationVM> getListOfTarifBookTaxationVMs() {

        List<TarifBookRef> result = tarifBookRefRepository.findAll();
        List<TarifBookTaxationVM> list = new ArrayList<>();
        result.forEach(tarifBookRef -> {
            tarifBookRef.getTaxations().forEach(taxation -> {
                list.add(

                        new TarifBookTaxationVM(tarifBookRef.getId(), taxation.getId(), tarifBookRef.getReference(), taxation.getReference(),
                                " ", " ", " ", " ", " ")

                );
            });
        });

        return list;
    }

    private Page<TarifBookTaxationVM> getListOfTarifBookTaxationVMsa(int page, int size, String codeLang) {
        return  mapTarifBookTaxationVMProjectionToTarifBookNationalProcedureVM(tarifBookRefRepository.getTarifBookTaxationData(PageRequest.of(page, size)), codeLang);
    }
    public Page<TarifBookTaxationVM> mapTarifBookTaxationVMProjectionToTarifBookNationalProcedureVM(Page<TarifBookTaxationVMProjection> tarifBookNationalProcedureVMProjections, String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<TarifBookTaxationVM> tarifBookNationalProcedureVMs = new ArrayList<>();

        return tarifBookNationalProcedureVMProjections.map(tarifBookNationalProcedureVMProjection -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF, lang.getId(), tarifBookNationalProcedureVMProjection.getTaxationId());
            EntityRefLang entityRefLangTarif = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), tarifBookNationalProcedureVMProjection.getTarifBookId());
            TarifBookTaxationVM agreementLang = new TarifBookTaxationVM();

            agreementLang.setTaxationLabel(entityRefLang!=null?entityRefLang.getLabel():"");
            agreementLang.setTaxationId(tarifBookNationalProcedureVMProjection!=null ?
                    tarifBookNationalProcedureVMProjection.getTaxationId():null);
            agreementLang.setTaxationReference(tarifBookNationalProcedureVMProjection!=null?
                    tarifBookNationalProcedureVMProjection.getTaxationReference():"");

            agreementLang.setTariffBookLabel(entityRefLangTarif!=null?entityRefLangTarif.getLabel():"");
            agreementLang.setTarifBookId(tarifBookNationalProcedureVMProjection!=null ?tarifBookNationalProcedureVMProjection.getTarifBookId():null);
            agreementLang.setTarifBookReference(tarifBookNationalProcedureVMProjection!=null?tarifBookNationalProcedureVMProjection.getTarifBookReference():"");
            agreementLang.setLang(codeLang);

            tarifBookNationalProcedureVMs.add(agreementLang);

            return  agreementLang ;
        });
    }

    @Override
    public ByteArrayInputStream tarifBookTaxationJoinToExcel(List<TarifBookTaxationVM> tarifBookTaxationVMs) {

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

                    for (TarifBookTaxationVM model : tarifBookTaxationVMs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(model.getTarifBookReference());
                        row.createCell(1).setCellValue(model.getTaxationReference());

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
    public Page<TarifBookTaxationVM> getAll(int page, int size, String codeLang) {
        return getListOfTarifBookTaxationVMsa( page, size, codeLang);

    }

    @Override
    public ByteArrayInputStream load(String codeLang, final int page, final int size) {
        List<TarifBookTaxationVM> listTarifBookTaxationVM = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        Page<TarifBookRef> tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size));

        for (TarifBookRef t : tarifBookRefs) {
            EntityRefLang tariffBookEntityRefLang = entityRefLangRepository
                    .findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), t.getId());
            if (tariffBookEntityRefLang != null) {
                t.getTaxations().forEach(taxation -> {
                    EntityRefLang taxationEntityRefLang = entityRefLangRepository
                            .findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF, lang.getId(), !Objects.isNull(taxation) ? taxation.getId() : 0);
                    listTarifBookTaxationVM.add(new TarifBookTaxationVM(t.getId(),
                            taxation.getId(), t.getReference(), taxation.getReference(),
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
    public Page<TarifBookTaxationVM> findTarifTaxationJoin(Long idTarifBookRef, String codeLang, int page, int size) {
        TarifBookRef tarifBookRef = tarifBookRefRepository.findOneById(idTarifBookRef);
        List<TarifBookTaxationVM> listTarifBookTaxationVM = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);

        EntityRefLang entityRefLang = entityRefLangRepository
                .findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), !Objects.isNull(tarifBookRef) ? tarifBookRef.getId() : 0);

        if (Objects.isNull(tarifBookRef) || Objects.isNull(entityRefLang)) {
            throw new TarifBookTaxationJoinNotFoundException(
                    "TarifBookTaxationVM: " + idTarifBookRef);
        }

        if (!Objects.isNull(entityRefLang)) {
            tarifBookRef.getTaxations().forEach(taxation -> {
                listTarifBookTaxationVM.add(

                        new TarifBookTaxationVM(tarifBookRef.getId(), taxation.getId(), tarifBookRef.getReference(), taxation.getReference(),
                                " ", " ", " ", " ",	" ")

                );
            });
        }

        return PageHelper.listConvertToPage(listTarifBookTaxationVM, listTarifBookTaxationVM.size(), PageRequest.of(page, size));

    }

    @Override
    public Page<TarifBookTaxationVM> getAll(String codeLang, int page, int size, String orderDirection) {
        List<TarifBookTaxationVM> tarifBookTaxationVMs = new ArrayList<>();
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
                t.getTaxations().forEach(taxation -> {
                    EntityRefLang taxationEntityRefLang = entityRefLangRepository
                            .findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT, lang.getId(), !Objects.isNull(taxation) ? taxation.getId() : 0);
                    tarifBookTaxationVMs.add(new TarifBookTaxationVM(t.getId(),
                            taxation.getId(), t.getReference(), taxation.getReference(),
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
            Taxation taxation = taxationRepository.findOneById(id);
            if (taxation != null) {
                Optional<TarifBookRef> optional = tarifBookRefRepository.findById(id_tarif);
                if (optional.isPresent()) {
                    taxation.getTarifBookRefs().remove(optional.get());
                }
            }

            taxationRepository.save(taxation);
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
