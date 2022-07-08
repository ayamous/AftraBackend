package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.exception.TarifBookNationalProcedureJoinNotFoundException;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarif;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.bean.TariffBookRefBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.projections.TarifBookNationalProcedureVMProjection;
import ma.itroad.aace.eth.coref.model.mapper.TariffBookRefMapper;
import ma.itroad.aace.eth.coref.model.view.TarifBookNationalProcedureVM;
import ma.itroad.aace.eth.coref.repository.*;
import ma.itroad.aace.eth.coref.service.ITarifBookNationalProcedureJoinService;
import ma.itroad.aace.eth.coref.service.helper.AgreementLang;
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
public class TarifBookNationalProcedureJoinServiceImpl implements ITarifBookNationalProcedureJoinService {

    static String[] HEADERs = {"REFERENCE TARIF BOOK", "REFERENCE NATIONAL PROCEDURE"};
    static String SHEET = "tarif-book-national-procedure";
    
    @Autowired
    Validator validator;

    @Autowired
    private TarifBookRefRepository tarifBookRefRepository;

    @Autowired
    private NationalProcedureRefRepository nationalProcedureRefRepository;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    private TariffBookRefMapper tariffBookRefMapper;

    @Override
    public List<TarifBookNationalProcedureVM> excelToElemetsRefs(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            List<TarifBookNationalProcedureVM> tarifBookNationalProcedureVMS = new ArrayList<>();
            Row currentRow;
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                currentRow= sheet.getRow(rowNum);
                TarifBookNationalProcedureVM tarifBookNationalProcedureVM = new TarifBookNationalProcedureVM();
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            tarifBookNationalProcedureVM.setTarifBookReference(Util.cellValue(currentCell));
                            TarifBookRef tarifBookRef = tarifBookRefRepository.findByReference(tarifBookNationalProcedureVM.getTarifBookReference());
                            Long idCurentTarifBook = tarifBookRef != null ?  tarifBookRef.getId() : -1;
                            tarifBookNationalProcedureVM.setTarifBookId(idCurentTarifBook);
                            break;
                        case 1:
                            tarifBookNationalProcedureVM.setNationalProcedureCode(Util.cellValue(currentCell));
                            NationalProcedureRef nationalProcedureRef = nationalProcedureRefRepository.findByCode(tarifBookNationalProcedureVM.getNationalProcedureCode());
                            Long idNationalProcedureRef = nationalProcedureRef != null ?nationalProcedureRef.getId() : -1;
                            tarifBookNationalProcedureVM.setNationalProcedureRefId(idNationalProcedureRef);
                            break;

                        default:
                            break;
                    }
                }
                tarifBookNationalProcedureVMS.add(tarifBookNationalProcedureVM);
            }
            workbook.close();
            return tarifBookNationalProcedureVMS;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
    
	@Override
	public Set<ConstraintViolation<TarifBookNationalProcedureVM>> validateTarifBookNationalProcedureVM(TarifBookNationalProcedureVM tarifBookNationalProcedureVM) {

		Set<ConstraintViolation<TarifBookNationalProcedureVM>> violations = validator.validate(tarifBookNationalProcedureVM);

		return violations;
	}
    
    
    @Override
    public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
        try {
        	
        	List<TarifBookNationalProcedureVM> tarifBookNationalProcedureVM = excelToElemetsRefs(file.getInputStream());
			 
			List<TarifBookNationalProcedureVM> invalidTarifBookNationalProcedureVM = new ArrayList<TarifBookNationalProcedureVM>();
			List<TarifBookNationalProcedureVM> validTarifBookNationalProcedureVM = new ArrayList<TarifBookNationalProcedureVM>();

			int lenght = tarifBookNationalProcedureVM.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<TarifBookNationalProcedureVM>> violations = validateTarifBookNationalProcedureVM(
						tarifBookNationalProcedureVM.get(i));
				if (violations.isEmpty())

				{
					validTarifBookNationalProcedureVM.add(tarifBookNationalProcedureVM.get(i));
				} else {
					
					
					invalidTarifBookNationalProcedureVM.add(tarifBookNationalProcedureVM.get(i));
				}
				
				if (!invalidTarifBookNationalProcedureVM.isEmpty()) {

					ByteArrayInputStream out = tarifBookNationalProcedureJoinToExcel(invalidTarifBookNationalProcedureVM);
					xls = new InputStreamResource(out);}
        	
			}
        	

            if (!validTarifBookNationalProcedureVM.isEmpty())
            	validTarifBookNationalProcedureVM.stream().forEach(
                        tbt -> {
                            if (tbt.getNationalProcedureRefId() != -1 && tbt.getTarifBookId() != -1) {
                                this.save(tbt);
                            }
                        }
                );
            
            if (!invalidTarifBookNationalProcedureVM.isEmpty())
    				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
    						.contentType(
    								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    						.body(xls);

    			if (!validTarifBookNationalProcedureVM.isEmpty())
    				return ResponseEntity.status(HttpStatus.OK).body(null);

    			return ResponseEntity.status(HttpStatus.OK).body(null);
            
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }


    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            List<TarifBookNationalProcedureVM> tarifBookNationalProcedureVMs = excelToElemetsRefs(file.getInputStream());
            if (!tarifBookNationalProcedureVMs.isEmpty())
                tarifBookNationalProcedureVMs.stream().forEach(
                        tbt -> {
                            if (tbt.getNationalProcedureRefId() != -1 && tbt.getTarifBookId() != -1) {
                                this.save(tbt);
                            }
                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    public TariffBookRefBean save(TarifBookNationalProcedureVM model) {

        TarifBookRef tarifBookRef = tarifBookRefRepository.findOneById(model.getTarifBookId());

        if (tarifBookRef != null) {
            Optional<NationalProcedureRef> optional = nationalProcedureRefRepository.findById(model.getNationalProcedureRefId());
            if (optional.isPresent()) {

                Set<NationalProcedureRef> nationalProcedureRefs = Stream.concat(tarifBookRef.getNationalProcedureRefs().stream(), Stream.of(optional.get())).collect(Collectors.toSet());
                tarifBookRef.setNationalProcedureRefs(nationalProcedureRefs);
            }
        }
        TarifBookRef entity = tarifBookRefRepository.save(tarifBookRef);
        return tariffBookRefMapper.entityToBean(entity);
    }

    @Override
    public Page<TarifBookNationalProcedureVM> findTarifBookNationalProcedure(int page, int size, String  reference) {
        List<TarifBookRef>  tarifBookRefs =  tarifBookRefRepository.findByReferenceIgnoreCaseContains(reference);
        List<TarifBookNationalProcedureVM> list = new ArrayList<>();
        tarifBookRefs.forEach(tarifBookRef -> {
            tarifBookRef.getNationalProcedureRefs().forEach(nationalProcedureRef -> {
                list.add(

                        new TarifBookNationalProcedureVM(tarifBookRef.getId(), nationalProcedureRef.getId(), tarifBookRef.getReference(), nationalProcedureRef.getCode(),
                                " ", " ", " ", " ", " ")

                );
            });
        });


        return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
    }

    @Override
    public ByteArrayInputStream load() {

        List<TarifBookNationalProcedureVM> tarifBookNationalProcedureVMs = getListOfTarifBookNationalProcedureVMs();
        ByteArrayInputStream in = tarifBookNationalProcedureJoinToExcel(tarifBookNationalProcedureVMs);
        return in;
    }

    private List<TarifBookNationalProcedureVM> getListOfTarifBookNationalProcedureVMs() {

        List<TarifBookRef> result = tarifBookRefRepository.findAll();
        List<TarifBookNationalProcedureVM> list = new ArrayList<>();
        result.forEach(tarifBookRef -> {
            tarifBookRef.getNationalProcedureRefs().forEach(nationalProcedureRef -> {
                list.add(

                        new TarifBookNationalProcedureVM(tarifBookRef.getId(), nationalProcedureRef.getId(), tarifBookRef.getReference(), nationalProcedureRef.getCode(),
                                " ", " ", " ", " ", " ")

                );
            });
        });

        return list;
    }

    private Page<TarifBookNationalProcedureVM> getListOfTarifBookNationalProcedureVMs(int page,int size, String codeLang) {
        Page<TarifBookNationalProcedureVMProjection> result = tarifBookRefRepository.getListOfTarifBookNationalProcedure(PageRequest.of(page, size));
        return mapNationalProcedureToTarifBookNationalProcedureVM(result, codeLang);
    }

    @Override
    public ByteArrayInputStream tarifBookNationalProcedureJoinToExcel(List<TarifBookNationalProcedureVM> tarifBookNationalProcedureVMs) {

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
                if (!tarifBookNationalProcedureVMs.isEmpty()) {

                    for (TarifBookNationalProcedureVM model : tarifBookNationalProcedureVMs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(model.getTarifBookReference());
                        row.createCell(1).setCellValue(model.getNationalProcedureCode());

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
    public Page<TarifBookNationalProcedureVM> getAll(int page, int size, String codeLang) {
//        List<TarifBookNationalProcedureVM> list = getListOfTarifBookNationalProcedureVMs();
//        return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
        return getListOfTarifBookNationalProcedureVMs(page,size, codeLang);
    }

    public Page<TarifBookNationalProcedureVM> mapNationalProcedureToTarifBookNationalProcedureVM(Page<TarifBookNationalProcedureVMProjection> tarifBookNationalProcedureVMProjections, String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<TarifBookNationalProcedureVM> tarifBookNationalProcedureVMs = new ArrayList<>();

        return tarifBookNationalProcedureVMProjections.map(tarifBookNationalProcedureVMProjection -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, lang.getId(), tarifBookNationalProcedureVMProjection.getNationalProcedureRefId());
            EntityRefLang entityRefLangTarif = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), tarifBookNationalProcedureVMProjection.getTarifBookId());
            TarifBookNationalProcedureVM agreementLang = new TarifBookNationalProcedureVM();

            agreementLang.setNationalProcedureLabel(entityRefLang!=null?entityRefLang.getLabel():"");
            agreementLang.setNationalProcedureRefId(tarifBookNationalProcedureVMProjection!=null ?
                    tarifBookNationalProcedureVMProjection.getNationalProcedureRefId():null);
            agreementLang.setNationalProcedureCode(tarifBookNationalProcedureVMProjection!=null?
                    tarifBookNationalProcedureVMProjection.getNationalProcedureCode():"");

            agreementLang.setTariffBookLabel(entityRefLangTarif!=null?entityRefLangTarif.getLabel():"");
            agreementLang.setTarifBookId(tarifBookNationalProcedureVMProjection!=null ?tarifBookNationalProcedureVMProjection.getTarifBookId():null);
            agreementLang.setTarifBookReference(tarifBookNationalProcedureVMProjection!=null?tarifBookNationalProcedureVMProjection.getTarifBookReference():"");
            agreementLang.setLang(codeLang);

            tarifBookNationalProcedureVMs.add(agreementLang);

            return  agreementLang ;
        });
    }


    @Override
    public ByteArrayInputStream load(String codeLang, final int page, final int size) {
        List<TarifBookNationalProcedureVM> listTarifBookTarifBookNationalProcedureVM = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        Page<TarifBookRef> tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size));

        for (TarifBookRef t : tarifBookRefs) {
            EntityRefLang tariffBookEntityRefLang = entityRefLangRepository
                    .findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), t.getId());
            if (tariffBookEntityRefLang != null) {
                t.getNationalProcedureRefs().forEach(nationalProcedureRef -> {
                    EntityRefLang nationalProcedureRefLang = entityRefLangRepository
                            .findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, lang.getId(), !Objects.isNull(nationalProcedureRef) ? nationalProcedureRef.getId() : 0);
                    listTarifBookTarifBookNationalProcedureVM.add(new TarifBookNationalProcedureVM(t.getId(),
                            nationalProcedureRef.getId(), t.getReference(), nationalProcedureRef.getCode(),
                            (!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getLabel())) ? tariffBookEntityRefLang.getLabel() : " ",
                            (!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getDescription())) ? tariffBookEntityRefLang.getDescription() : " ",
                            (!Objects.isNull(nationalProcedureRefLang) && !Objects.isNull(nationalProcedureRefLang.getLabel())) ? nationalProcedureRefLang.getLabel() : " ",
                            (!Objects.isNull(nationalProcedureRefLang) && !Objects.isNull(nationalProcedureRefLang.getDescription())) ? nationalProcedureRefLang.getDescription() : " ",
                            codeLang));
                });
            }
        }

        ByteArrayInputStream in = tarifBookNationalProcedureJoinToExcel(listTarifBookTarifBookNationalProcedureVM);

        return in;

    }

    @Override
    public Page<TarifBookNationalProcedureVM> findTarifBookNationalProcedureVMByLang(Long idTarifBookRef, String codeLang, int page, int size) {
        TarifBookRef tarifBookRef = tarifBookRefRepository.findOneById(idTarifBookRef);
        List<TarifBookNationalProcedureVM> listTarifBookNationalProcedureVM = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);

        EntityRefLang entityRefLang = entityRefLangRepository
                .findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), !Objects.isNull(tarifBookRef) ? tarifBookRef.getId() : 0);

        if (Objects.isNull(tarifBookRef) || Objects.isNull(entityRefLang)) {
            throw new TarifBookNationalProcedureJoinNotFoundException(
                    "TarifBookNationalProcedureVM: " + idTarifBookRef);
        }

        if (!Objects.isNull(entityRefLang)) {
            tarifBookRef.getNationalProcedureRefs().forEach(getNationalProcedureRef -> {
                listTarifBookNationalProcedureVM.add(

                        new TarifBookNationalProcedureVM(tarifBookRef.getId(), getNationalProcedureRef.getId(), tarifBookRef.getReference(), getNationalProcedureRef.getCode(),
                                " ", " ", " ", " ",	" ")

                );
            });
        }

        return PageHelper.listConvertToPage(listTarifBookNationalProcedureVM, listTarifBookNationalProcedureVM.size(), PageRequest.of(page, size));

    }

    @Override
    public Page<TarifBookNationalProcedureVM> getAll(String codeLang, int page, int size, String orderDirection) {
        List<TarifBookNationalProcedureVM>tarifBookNationalProcedureVMs = new ArrayList<>();
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
                t.getNationalProcedureRefs().forEach(nationalProcedureRef -> {
                    EntityRefLang nationalProcedureRefLang = entityRefLangRepository
                            .findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, lang.getId(), !Objects.isNull(nationalProcedureRef) ? nationalProcedureRef.getId() : 0);
                    tarifBookNationalProcedureVMs.add(new TarifBookNationalProcedureVM(t.getId(),
                            nationalProcedureRef.getId(), t.getReference(), nationalProcedureRef.getCode(),
                            (!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getLabel())) ? tariffBookEntityRefLang.getLabel() : " ",
                            (!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getDescription())) ? tariffBookEntityRefLang.getDescription() : " ",
                            (!Objects.isNull(nationalProcedureRefLang) && !Objects.isNull(nationalProcedureRefLang.getLabel())) ? nationalProcedureRefLang.getLabel() : " ",
                            (!Objects.isNull(nationalProcedureRefLang) && !Objects.isNull(nationalProcedureRefLang.getDescription())) ? nationalProcedureRefLang.getDescription() : " ",
                            codeLang));
                });
            }
        }

        return PageHelper.listConvertToPage(tarifBookNationalProcedureVMs, tarifBookNationalProcedureVMs.size(), PageRequest.of(page, size));
    }

    @Override
    public ErrorResponse delete(Long id, Long id_tarif) {
        try {
            NationalProcedureRef nationalProcedureRef = nationalProcedureRefRepository.findOneById(id);
            if (nationalProcedureRef != null) {
                Optional<TarifBookRef> optional = tarifBookRefRepository.findById(id_tarif);
                if (optional.isPresent()) {
                    nationalProcedureRef.getTarifBookRefs().remove(optional.get());
                }
            }

            nationalProcedureRefRepository.save(nationalProcedureRef);
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
