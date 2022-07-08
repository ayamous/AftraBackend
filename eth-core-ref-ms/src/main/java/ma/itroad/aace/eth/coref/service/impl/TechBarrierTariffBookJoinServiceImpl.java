package ma.itroad.aace.eth.coref.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarif;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.entity.TarifBookRef;
import ma.itroad.aace.eth.coref.model.entity.TechBarrierRef;
import ma.itroad.aace.eth.coref.model.mapper.TarifBookTechBarrierVMProjection;
import ma.itroad.aace.eth.coref.model.view.TechBarrierTariffBookVM;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.TarifBookRefRepository;
import ma.itroad.aace.eth.coref.repository.TechBarrierRefRepository;
import ma.itroad.aace.eth.coref.service.ITechBarrierTariffBookJoinService;
import ma.itroad.aace.eth.coref.service.helper.ExcelCellFormatter;
import ma.itroad.aace.eth.coref.service.helper.PageHelper;

@Service
public class TechBarrierTariffBookJoinServiceImpl implements ITechBarrierTariffBookJoinService {

	@Autowired
	Validator validator;

    @Autowired
    TechBarrierRefRepository repository;

    @Autowired
    TarifBookRefRepository tarifBookRefRepository;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    private static final String SHEET = "TechBarrierTariffBookJoinSHEET";
    private static final String[] HEADER = {"TechBarrierRef code", "TariffBookRef reference"};

    @Override
    public Page<TechBarrierTariffBookVM> getAll(int page, int size) {
        List<TechBarrierTariffBookVM> list = getListOfTechBarrierTariff();
        return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
    }
    
    @Override
	public ByteArrayInputStream TechBarrierTariffBookVMToExcel(List<TechBarrierTariffBookVM> techBarrierTariffBookVM) {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet(SHEET);
			// Header
			Row headerRow = sheet.createRow(0);
			if (HEADER.length > 0) {
				for (int col = 0; col < HEADER.length; col++) {
					Cell cell = headerRow.createCell(col);
					cell.setCellValue(HEADER[col]);
				}
				int rowIdx = 1;
				if (!techBarrierTariffBookVM.isEmpty()) {
					for (TechBarrierTariffBookVM model : techBarrierTariffBookVM) {
						Row row = sheet.createRow(rowIdx++);
						row.createCell(1).setCellValue(model.getTarifBookReference());
						row.createCell(0).setCellValue(model.getTechBarrierRefCode());
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
    public Set<TechBarrierTariffBookVM> excelToElementsRefs(InputStream is) {

        Set<TechBarrierTariffBookVM> TechBarrierTariffBookVMS;
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            TechBarrierTariffBookVMS = new HashSet<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();
                TechBarrierTariffBookVM view = new TechBarrierTariffBookVM();
                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    switch (cellIdx) {
                        case 0:
                            view.setTechBarrierRefCode(ExcelCellFormatter.getCellStringValue(currentCell));
                            Long techBarrierRefId = -1L;
                            TechBarrierRef value = repository.findByCode(currentCell.getStringCellValue());
                            if (value != null) {
                                techBarrierRefId = value.getId();
                            }
                            view.setTechBarrierRefId(techBarrierRefId);

                            break;
                        case 1:
                            view.setTarifBookReference(ExcelCellFormatter.getCellStringValue(currentCell));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                TechBarrierTariffBookVMS.add(view);
            }
            workbook.close();

        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
        return TechBarrierTariffBookVMS;
    }
    
    @Override
	public Set<ConstraintViolation<TechBarrierTariffBookVM>> validatetechBarrierTariffBookVM(
			TechBarrierTariffBookVM techBarrierTariffBookVM) {

		Set<ConstraintViolation<TechBarrierTariffBookVM>> violations = validator.validate(techBarrierTariffBookVM);

		return violations;
	}

	@Override
	public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {
			Set<TechBarrierTariffBookVM> techBarrierTariffBookVM = excelToElementsRefs(file.getInputStream());
			List<TechBarrierTariffBookVM> techBarrierTariffBookVMs = new ArrayList<TechBarrierTariffBookVM>(techBarrierTariffBookVM);
			
			List<TechBarrierTariffBookVM> invalidTechBarrierTariffBookVM = new ArrayList<TechBarrierTariffBookVM>();
			List<TechBarrierTariffBookVM> validTechBarrierTariffBookVM= new ArrayList<TechBarrierTariffBookVM>();

			int lenght = techBarrierTariffBookVM.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<TechBarrierTariffBookVM>> violations = validatetechBarrierTariffBookVM(
						techBarrierTariffBookVMs.get(i));
				if (violations.isEmpty())

				{
					validTechBarrierTariffBookVM.add(techBarrierTariffBookVMs.get(i));
				} else {

					invalidTechBarrierTariffBookVM.add(techBarrierTariffBookVMs.get(i));
				}

				if (!invalidTechBarrierTariffBookVM.isEmpty()) {

					ByteArrayInputStream out = TechBarrierTariffBookVMToExcel(invalidTechBarrierTariffBookVM);
					xls = new InputStreamResource(out);
				}

			}

		
			if (!techBarrierTariffBookVM.isEmpty())
				techBarrierTariffBookVM.stream().forEach(item -> {
					this.save(item);
				});
			if (!invalidTechBarrierTariffBookVM.isEmpty())
				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);

			if (!validTechBarrierTariffBookVM.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(null);

			return ResponseEntity.status(HttpStatus.OK).body(null);

		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}
	}


    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            Set<TechBarrierTariffBookVM> techBarrierTariffBookVMS = excelToElementsRefs(file.getInputStream());
            if (!techBarrierTariffBookVMS.isEmpty())
                techBarrierTariffBookVMS.stream().forEach(item -> {
                    this.save(item);
                });
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }


    @Override
    public ByteArrayInputStream load() {

        List<TechBarrierTariffBookVM> list = getListOfTechBarrierTariff();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET);
            // Header
            Row headerRow = sheet.createRow(0);
            if (HEADER.length > 0) {
                for (int col = 0; col < HEADER.length; col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(HEADER[col]);
                }
                int rowIdx = 1;
                if (!list.isEmpty()) {

                    for (TechBarrierTariffBookVM view : list) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(view.getTechBarrierRefCode());
                        row.createCell(1).setCellValue(view.getTarifBookReference());
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

    private List<TechBarrierTariffBookVM> getListOfTechBarrierTariff() {

        List<TechBarrierRef> response = repository.findAll();
        List<TechBarrierTariffBookVM> list = new ArrayList<>();
        response.forEach(item -> {
            item.getTariffBookRefs().forEach(tariffBookRef -> {
                TechBarrierTariffBookVM t = new TechBarrierTariffBookVM();
                t.setTechBarrierRefId(item.getId());
                t.setTechBarrierRefCode(item.getCode());
                t.setTarifBookId(tariffBookRef.getId());
                t.setTarifBookReference(tariffBookRef.getReference());
                list.add(t);
            });
        });
        return list;
    }

    @Override
    public Page<TechBarrierTariffBookVM> getAll(int page, int size, String codeLang) {
        return getListOfTarifBookTaxationVMsa( page, size, codeLang);
    }
    private Page<TechBarrierTariffBookVM> getListOfTarifBookTaxationVMsa(int page, int size, String codeLang) {
        return  mapTarifBookTaxationVMProjectionToTarifBookNationalProcedureVM(tarifBookRefRepository.getTarifBookTechBarrierData(PageRequest.of(page, size)), codeLang);
    }
    public Page<TechBarrierTariffBookVM> mapTarifBookTaxationVMProjectionToTarifBookNationalProcedureVM(Page<TarifBookTechBarrierVMProjection> tarifBookNationalProcedureVMProjections, String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<TechBarrierTariffBookVM> tarifBookNationalProcedureVMs = new ArrayList<>();

        return tarifBookNationalProcedureVMProjections.map(tarifBookNationalProcedureVMProjection -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF, lang.getId(), tarifBookNationalProcedureVMProjection.getTechBarrierId());
            EntityRefLang entityRefLangTarif = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), tarifBookNationalProcedureVMProjection.getTarifBookId());
            TechBarrierTariffBookVM agreementLang = new TechBarrierTariffBookVM();

            agreementLang.setTechBarrierRefLabel(entityRefLang!=null?entityRefLang.getLabel():"");
            agreementLang.setTechBarrierRefId(tarifBookNationalProcedureVMProjection!=null ?
                    tarifBookNationalProcedureVMProjection.getTechBarrierId():null);
            agreementLang.setTechBarrierRefCode(tarifBookNationalProcedureVMProjection!=null?
                    tarifBookNationalProcedureVMProjection.getTechBarrierReference():"");

            agreementLang.setTariffBookLabel(entityRefLangTarif!=null?entityRefLangTarif.getLabel():"");
            agreementLang.setTarifBookId(tarifBookNationalProcedureVMProjection!=null ?tarifBookNationalProcedureVMProjection.getTarifBookId():null);
            agreementLang.setTarifBookReference(tarifBookNationalProcedureVMProjection!=null?tarifBookNationalProcedureVMProjection.getTarifBookReference():"");
            agreementLang.setLang(codeLang);

            tarifBookNationalProcedureVMs.add(agreementLang);

            return  agreementLang ;
        });
    }



    public TechBarrierTariffBookVM save(TechBarrierTariffBookVM item) {
        if (item.getTechBarrierRefId() != -1 && item.getTechBarrierRefId() != null  ) {
            TechBarrierRef entity = repository.findOneById(item.getTechBarrierRefId());
            TarifBookRef tarifBookRef = tarifBookRefRepository.findOneById(item.getTarifBookId());
            if (tarifBookRef != null) {
                Set<TarifBookRef> techBarrierRefs = Stream.concat(entity.getTariffBookRefs().stream(),
                        Stream.of(tarifBookRef)).collect(Collectors.toSet());
                entity.setTariffBookRefs(techBarrierRefs);
            }
            repository.save(entity);

            if(entity != null ){
                return item;
            }

        }
        return null;
    }

    @Override
    public Page<TechBarrierTariffBookVM> findTechBarTarifBookJoin(int page, int size, String reference) {
        Page<TechBarrierRef>  techBarrierRefs =  repository.findTechBarrierTarifBook(reference.toUpperCase(),PageRequest.of(page, size));
        List<TechBarrierTariffBookVM> list = new ArrayList<>();
        techBarrierRefs.forEach(thr -> {
            thr.getTariffBookRefs().forEach(tarifBookRef -> {
                TechBarrierTariffBookVM t = new TechBarrierTariffBookVM();
                t.setTechBarrierRefId(thr.getId());
                t.setTechBarrierRefCode(thr.getCode());
                t.setTarifBookId(tarifBookRef.getId());
                t.setTarifBookReference(tarifBookRef.getReference());
                list.add(t);
            });
        });


        return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
    }

    @Override
    public ErrorResponse delete(Long id, Long id_tarif) {
        try {
            TechBarrierRef techBarrierRef = repository.findOneById(id);
            if (techBarrierRef != null) {
                Optional<TarifBookRef> optional = tarifBookRefRepository.findById(id_tarif);
                if (optional.isPresent()) {
                    techBarrierRef.getTariffBookRefs().remove(optional.get());
                }
            }

            repository.save(techBarrierRef);
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
