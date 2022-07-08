package ma.itroad.aace.eth.coref.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

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

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.exception.ReguationTariffBookRefVMJoinNotFoundException;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarif;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.entity.RegulationRef;
import ma.itroad.aace.eth.coref.model.entity.TarifBookRef;
import ma.itroad.aace.eth.coref.model.mapper.RegulationRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.RegulationTariffBookRefVMProjection;
import ma.itroad.aace.eth.coref.model.view.RegulationTariffBookRefVM;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.RegulationRefRepository;
import ma.itroad.aace.eth.coref.repository.SanitaryPhytosanitaryMeasuresRefRepository;
import ma.itroad.aace.eth.coref.repository.TarifBookRefRepository;
import ma.itroad.aace.eth.coref.service.IRegulationTariffBookJoinService;
import ma.itroad.aace.eth.coref.service.helper.PageHelper;


@Service
public class RegulationTariffBookJoinServiceImpl implements IRegulationTariffBookJoinService {

    static String[] HEADERs = {"CODE Regulation", "REFERENCE POSITION TARIFAIRE"};
    static String SHEET = "Regulation-TariffBook-join";
    
    @Autowired
    Validator validator;

    @Autowired
    private SanitaryPhytosanitaryMeasuresRefRepository sanitaryPhytosanitaryMeasuresRefRepository;

    @Autowired
    private RegulationRefRepository regulationRefRepository;

    @Autowired
    private RegulationRefMapper regulationRefMapper;

    @Autowired
    private TarifBookRefRepository tarifBookRefRepository;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Override
    public List<RegulationTariffBookRefVM> excelToElemetsRefs(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<RegulationTariffBookRefVM> regulationTariffBookRefVMs = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();
                RegulationTariffBookRefVM regulationTariffBookRefVM = new RegulationTariffBookRefVM();
                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    switch (cellIdx) {
                        case 0:
                            if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                regulationTariffBookRefVM.setTarifBookReference(currentCell.getNumericCellValue() + "");
                            else
                                regulationTariffBookRefVM.setTarifBookReference(currentCell.getStringCellValue());

                            RegulationRef regulation = regulationRefRepository.findByCode(currentCell.getStringCellValue());

                            Long idRegulation = regulation != null ? regulation.getId() : -1;
                            regulationTariffBookRefVM.setRegulationId(idRegulation);

                            break;
                        case 1:
                            if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                                regulationTariffBookRefVM.setRegulationReference(currentCell.getNumericCellValue() + "");
                            else
                                regulationTariffBookRefVM.setRegulationReference(currentCell.getStringCellValue());
                            TarifBookRef tarifBookRef = tarifBookRefRepository.findByReference(currentCell.getStringCellValue())/*.orElse(null)*/;
                            Long idCurentTarifBook = tarifBookRef != null ?
                                    tarifBookRef.getId() : -1;
                            regulationTariffBookRefVM.setTarifBookId(idCurentTarifBook);
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                regulationTariffBookRefVMs.add(regulationTariffBookRefVM);
            }
            workbook.close();
            return regulationTariffBookRefVMs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
    
    @Override
	public Set<ConstraintViolation<RegulationTariffBookRefVM>> validateRegulationTariffBookRefVM(RegulationTariffBookRefVM regulationTariffBookRefVM) {

		Set<ConstraintViolation<RegulationTariffBookRefVM>> violations = validator.validate(regulationTariffBookRefVM);

		return violations;
	}
	
    @Override
	public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
        try {
        	
        	
        	List<RegulationTariffBookRefVM> regulationTariffBookRefVM = excelToElemetsRefs(file.getInputStream());
			 
			List<RegulationTariffBookRefVM> invalidRegulationTariffBookRefVM = new ArrayList<RegulationTariffBookRefVM>();
			List<RegulationTariffBookRefVM> validRegulationTariffBookRefVM = new ArrayList<RegulationTariffBookRefVM>();

			int lenght = regulationTariffBookRefVM.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<RegulationTariffBookRefVM>> violations = validateRegulationTariffBookRefVM(
						regulationTariffBookRefVM.get(i));
				if (violations.isEmpty())

				{
					validRegulationTariffBookRefVM.add(regulationTariffBookRefVM.get(i));
				} else {
					
					
					invalidRegulationTariffBookRefVM.add(regulationTariffBookRefVM.get(i));
				}
				
				if (!invalidRegulationTariffBookRefVM.isEmpty()) {

					ByteArrayInputStream out = regulationTariffBookJoinToExcel(invalidRegulationTariffBookRefVM);
					xls = new InputStreamResource(out);}
        	
			}
    
            if (!validRegulationTariffBookRefVM.isEmpty())
            	validRegulationTariffBookRefVM.stream().forEach(
                        regTar -> {
                            if (regTar.getRegulationId() != -1 && regTar.getTarifBookId() != -1) {
                                this.save(regTar);
                            }
                        }
                );
            
            if (!invalidRegulationTariffBookRefVM.isEmpty())
     				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
     						.contentType(
     								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
     						.body(xls);

     			if (!validRegulationTariffBookRefVM.isEmpty())
     				return ResponseEntity.status(HttpStatus.OK).body(null);

     			return ResponseEntity.status(HttpStatus.OK).body(null);
     			
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }



    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            List<RegulationTariffBookRefVM> regulationTariffBookRefVMs = excelToElemetsRefs(file.getInputStream());
            if (!regulationTariffBookRefVMs.isEmpty())
                regulationTariffBookRefVMs.stream().forEach(
                        regTar -> {
                            if (regTar.getRegulationId() != -1 && regTar.getTarifBookId() != -1) {
                                this.save(regTar);
                            }
                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    public  RegulationTariffBookRefVM save(RegulationTariffBookRefVM model) {

        RegulationRef regRef = regulationRefRepository.findOneById(model.getRegulationId());

        if (regRef != null) {
            Optional<TarifBookRef> optional = tarifBookRefRepository.findById(model.getTarifBookId());
            if (optional.isPresent()) {
                Set<TarifBookRef> tarifBookRefs = Stream.concat(regRef.getTarifBookRefs().stream(), Stream.of(optional.get())).collect(Collectors.toSet());
                regRef.setTarifBookRefs(tarifBookRefs);
            }
        }
        RegulationRef entity = regulationRefRepository.save(regRef);

        if(entity != null ){
            return model;
        }

        return null;
    }

    @Override
    public Page<RegulationTariffBookRefVM> findRegulationTarifBookJoin(int page, int size, String reference) {
        Page<RegulationRef>  regulationRefs =  regulationRefRepository.findRegulationTarifBookJoin(reference.toUpperCase(), PageRequest.of(page, size));
        List<RegulationTariffBookRefVM> list = new ArrayList<>();
        regulationRefs.forEach(m -> {
            m.getTarifBookRefs().forEach(thr -> {
                list.add(

                        new RegulationTariffBookRefVM(m.getCode(), thr.getReference(), m.getId(), thr.getId(),
                                " ",
                                " ",
                                " ",
                                " ",
                                " ")

                );
            });
        });


        return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));

    }


    @Override
    public ByteArrayInputStream load() {

        List<RegulationTariffBookRefVM> tarifBookTaxationVMs = getListOfRegulationTariffBookRefVMs();
        ByteArrayInputStream in = regulationTariffBookJoinToExcel(tarifBookTaxationVMs);
        return in;

    }

    private List<RegulationTariffBookRefVM> getListOfRegulationTariffBookRefVMs() {

        List<RegulationRef> result = regulationRefRepository.findAll();
        List<RegulationTariffBookRefVM> list = new ArrayList<>();
        result.forEach(reg -> {
            reg.getTarifBookRefs().forEach(tarifs -> {
                list.add(

                        new RegulationTariffBookRefVM(reg.getCode(), tarifs.getReference(), reg.getId(), tarifs.getId(),
                                " ", " ", " ", " ", " ")

                );
            });
        });

        return list;
    }

    private Page<RegulationTariffBookRefVM> getListOfRegulationTariffBookRefVMs(int page, int size, String codeLang) {
        return mapNationalProcedureToTarifBookNationalProcedureVM(regulationRefRepository.getListOfRegulationTariffBookRefVMs(PageRequest.of(page,size)), codeLang);
    }

    public Page<RegulationTariffBookRefVM> mapNationalProcedureToTarifBookNationalProcedureVM(Page<RegulationTariffBookRefVMProjection> tarifBookNationalProcedureVMProjections, String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<RegulationTariffBookRefVM> tarifBookNationalProcedureVMs = new ArrayList<>();

        return tarifBookNationalProcedureVMProjections.map(tarifBookNationalProcedureVMProjection -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF, lang.getId(), tarifBookNationalProcedureVMProjection.getRegulationId());
            EntityRefLang entityRefLangTarif = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), tarifBookNationalProcedureVMProjection.getTarifBookId());
            RegulationTariffBookRefVM agreementLang = new RegulationTariffBookRefVM();

            agreementLang.setRegulationLabel(entityRefLang!=null?entityRefLang.getLabel():"");
            agreementLang.setRegulationId(tarifBookNationalProcedureVMProjection!=null ?
                    tarifBookNationalProcedureVMProjection.getRegulationId():null);
            agreementLang.setRegulationReference(tarifBookNationalProcedureVMProjection!=null?
                    tarifBookNationalProcedureVMProjection.getRegulationReference():"");

            agreementLang.setTarifBookLabel(entityRefLangTarif!=null?entityRefLangTarif.getLabel():"");
            agreementLang.setTarifBookId(tarifBookNationalProcedureVMProjection!=null ?tarifBookNationalProcedureVMProjection.getTarifBookId():null);
            agreementLang.setTarifBookReference(tarifBookNationalProcedureVMProjection!=null?tarifBookNationalProcedureVMProjection.getTarifBookReference():"");
            agreementLang.setLang(codeLang);

            tarifBookNationalProcedureVMs.add(agreementLang);

            return  agreementLang ;
        });
    }

    @Override
    public ByteArrayInputStream regulationTariffBookJoinToExcel(List<RegulationTariffBookRefVM> regulationTariffBookRefVMs) {

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
                if (!regulationTariffBookRefVMs.isEmpty()) {

                    for (RegulationTariffBookRefVM model : regulationTariffBookRefVMs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(model.getRegulationReference());
                        row.createCell(1).setCellValue(model.getTarifBookReference());

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
    public Page<RegulationTariffBookRefVM> getAll(int page, int size, String codeLang) {
//        List<RegulationTariffBookRefVM> list = getListOfRegulationTariffBookRefVMs();
//        return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
        return getListOfRegulationTariffBookRefVMs(page,size, codeLang);
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {
        List<RegulationTariffBookRefVM> listRegTariffBookRefVM = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        List<TarifBookRef> tarifBookRefs = tarifBookRefRepository.findAll();

        for (TarifBookRef t : tarifBookRefs) {
            EntityRefLang tariffBookEntityRefLang = entityRefLangRepository
                    .findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), t.getId());
            if (tariffBookEntityRefLang != null) {
                t.getRegulationRefs().forEach(reg -> {
                    EntityRefLang RegEntityRefLang = entityRefLangRepository
                            .findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF, lang.getId(), !Objects.isNull(reg) ? reg.getId() : 0);
                    listRegTariffBookRefVM.add(new RegulationTariffBookRefVM(reg.getCode(),
                            t.getReference(), reg.getId(), t.getId(),
                            (!Objects.isNull(RegEntityRefLang) && !Objects.isNull(RegEntityRefLang.getLabel())) ? RegEntityRefLang.getLabel() : " ",
                            (!Objects.isNull(RegEntityRefLang) && !Objects.isNull(RegEntityRefLang.getDescription())) ? RegEntityRefLang.getDescription() : " ",
                            (!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getLabel())) ? tariffBookEntityRefLang.getLabel() : " ",
                            (!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getDescription())) ? tariffBookEntityRefLang.getDescription() : " ",
                            codeLang));
                });
            }
        }

        ByteArrayInputStream in = regulationTariffBookJoinToExcel(listRegTariffBookRefVM);

        return in;

    }


    @Override
    public Page<RegulationTariffBookRefVM> findRegulationTarifBookJoin(Long idTarifBookRef, String codeLang, int page, int size) {
        TarifBookRef tarifBookRef = tarifBookRefRepository.findOneById(idTarifBookRef);
        List<RegulationTariffBookRefVM> listRegTariffBookRefVM = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);

        EntityRefLang tariffBookEntityRefLang = entityRefLangRepository
                .findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), !Objects.isNull(tarifBookRef) ? tarifBookRef.getId() : 0);

        if (Objects.isNull(tarifBookRef) || Objects.isNull(tariffBookEntityRefLang)) {
            throw new ReguationTariffBookRefVMJoinNotFoundException(
                    "RegulationTariffBookRefVM: " + idTarifBookRef);
        }

        if (!Objects.isNull(tariffBookEntityRefLang)) {
            tarifBookRef.getRegulationRefs().forEach(reg -> {
                EntityRefLang RegEntityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
                        TableRef.REGULATION_REF, lang.getId(), !Objects.isNull(reg) ? reg.getId() : 0);
                listRegTariffBookRefVM.add(

                        new RegulationTariffBookRefVM(reg.getCode(), tarifBookRef.getReference(), reg.getId(),
                                tarifBookRef.getId(),
                                (!Objects.isNull(RegEntityRefLang) && !Objects.isNull(RegEntityRefLang.getLabel()))
                                        ? RegEntityRefLang.getLabel()
                                        : " ",
                                (!Objects.isNull(RegEntityRefLang)
                                        && !Objects.isNull(RegEntityRefLang.getDescription()))
                                        ? RegEntityRefLang.getDescription()
                                        : " ",
                                (!Objects.isNull(tariffBookEntityRefLang)
                                        && !Objects.isNull(tariffBookEntityRefLang.getLabel()))
                                        ? tariffBookEntityRefLang.getLabel()
                                        : " ",
                                (!Objects.isNull(tariffBookEntityRefLang)
                                        && !Objects.isNull(tariffBookEntityRefLang.getDescription()))
                                        ? tariffBookEntityRefLang.getDescription()
                                        : " ",
                                codeLang)

                );
            });
        }

        return PageHelper.listConvertToPage(listRegTariffBookRefVM, listRegTariffBookRefVM.size(), PageRequest.of(page, size));

    }



    @Override
    public Page<RegulationTariffBookRefVM> getAll(String codeLang, int page, int size, String orderDirection) {
        List<RegulationTariffBookRefVM> RegulationTariffBookRefVMs = new ArrayList<>();
        Page<TarifBookRef> tarifBookRefs = null;
        if(orderDirection.equals("DESC")){
            tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
        Lang lang = langRepository.findByCode(codeLang);

        for (TarifBookRef t : tarifBookRefs) {
            EntityRefLang tariffBookEntityRefLang = entityRefLangRepository
                    .findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), t.getId());
            if (tariffBookEntityRefLang != null) {
                t.getRegulationRefs().forEach(reg -> {
                    EntityRefLang RegEntityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
                            TableRef.REGULATION_REF, lang.getId(), !Objects.isNull(reg) ? reg.getId() : 0);
                    RegulationTariffBookRefVMs.add(new RegulationTariffBookRefVM(reg.getCode(), t.getReference(), reg.getId(),
                            t.getId(),
                            (!Objects.isNull(RegEntityRefLang) && !Objects.isNull(RegEntityRefLang.getLabel()))
                                    ? RegEntityRefLang.getLabel()
                                    : " ",
                            (!Objects.isNull(RegEntityRefLang) && !Objects.isNull(RegEntityRefLang.getDescription()))
                                    ? RegEntityRefLang.getDescription()
                                    : " ",
                            (!Objects.isNull(tariffBookEntityRefLang)
                                    && !Objects.isNull(tariffBookEntityRefLang.getLabel()))
                                    ? tariffBookEntityRefLang.getLabel()
                                    : " ",
                            (!Objects.isNull(tariffBookEntityRefLang)
                                    && !Objects.isNull(tariffBookEntityRefLang.getDescription()))
                                    ? tariffBookEntityRefLang.getDescription()
                                    : " ",
                            codeLang));
                });
            }
        }

        return PageHelper.listConvertToPage(RegulationTariffBookRefVMs, RegulationTariffBookRefVMs.size(), PageRequest.of(page, size));
    }

    @Override
    public ErrorResponse delete(Long id, Long id_tarif) {
        try {
            RegulationRef regulationRef = regulationRefRepository.findOneById(id);
            if (regulationRef != null) {
                Optional<TarifBookRef> optional = tarifBookRefRepository.findById(id_tarif);
                if (optional.isPresent()) {
                    regulationRef.getTarifBookRefs().remove(optional.get());
                }
            }
            regulationRefRepository.save(regulationRef);
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
