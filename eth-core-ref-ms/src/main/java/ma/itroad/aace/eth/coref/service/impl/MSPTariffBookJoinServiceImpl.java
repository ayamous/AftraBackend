package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.exception.MSPTariffBookRefVMJoinNotFoundException;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarif;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.RegulationTariffBookRefVMProjection;
import ma.itroad.aace.eth.coref.model.mapper.projections.MSPTariffBookRefVMProjection;
import ma.itroad.aace.eth.coref.model.mapper.SanitaryPhytosanitaryMeasuresRefMapper;
import ma.itroad.aace.eth.coref.model.view.MSPTariffBookRefVM;
import ma.itroad.aace.eth.coref.model.view.RegulationTariffBookRefVM;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.SanitaryPhytosanitaryMeasuresRefRepository;
import ma.itroad.aace.eth.coref.repository.TarifBookRefRepository;
import ma.itroad.aace.eth.coref.service.IMSPTariffBookJoinService;
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
public class MSPTariffBookJoinServiceImpl implements IMSPTariffBookJoinService {

    static String[] HEADERs = {"CODE MSP", "REFERENCE POSITION TARIFAIRE"};
    static String SHEET = "MSP-TariffBook-join";
    
    @Autowired
    Validator validator;
    
    @Autowired
    private SanitaryPhytosanitaryMeasuresRefRepository sanitaryPhytosanitaryMeasuresRefRepository;

    @Autowired
    private TarifBookRefRepository tarifBookRefRepository;
    
	@Autowired
	private LangRepository langRepository;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    private SanitaryPhytosanitaryMeasuresRefMapper mspMapper;


    @Override
    public List<MSPTariffBookRefVM> excelToElemetsRefs(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<MSPTariffBookRefVM> mspTariffBookRefVMs = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();
                MSPTariffBookRefVM mspTariffBookRefVM = new MSPTariffBookRefVM();
                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    switch (cellIdx) {
                        case 1:

                                mspTariffBookRefVM.setTarifBookReference(Util.cellValue(currentCell));

                            SanitaryPhytosanitaryMeasuresRef msp = sanitaryPhytosanitaryMeasuresRefRepository.findByCode(Util.cellValue(currentCell));

                            Long idMSP = msp != null ? msp.getId() : -1;
                            mspTariffBookRefVM.setMspId(idMSP);

                            break;
                        case 0:
 
                                mspTariffBookRefVM.setMspReference(Util.cellValue(currentCell));
                            TarifBookRef tarifBookRef = tarifBookRefRepository.findByReference(Util.cellValue(currentCell))/*.orElse(null)*/;
                            Long idCurentTarifBook = tarifBookRef != null ?
                                    tarifBookRef.getId() : -1;
                            mspTariffBookRefVM.setTarifBookId(idCurentTarifBook);
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                mspTariffBookRefVMs.add(mspTariffBookRefVM);
            }
            workbook.close();
            return mspTariffBookRefVMs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
    
	@Override
	public Set<ConstraintViolation<MSPTariffBookRefVM>> validatemSPTariffBookRefVM(MSPTariffBookRefVM mSPTariffBookRefVM) {

		Set<ConstraintViolation<MSPTariffBookRefVM>> violations = validator.validate(mSPTariffBookRefVM);

		return violations;
	}
    
    @Override
    public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
	 	String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
        try {
        	
			List<MSPTariffBookRefVM> mSPTariffBookRefVMs = excelToElemetsRefs(file.getInputStream());
			 
			List<MSPTariffBookRefVM> invalidMSPTariffBookRefVM = new ArrayList<MSPTariffBookRefVM>();
			List<MSPTariffBookRefVM> validMSPTariffBookRefVM = new ArrayList<MSPTariffBookRefVM>();

			int lenght = mSPTariffBookRefVMs.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<MSPTariffBookRefVM>> violations = validatemSPTariffBookRefVM(
						mSPTariffBookRefVMs.get(i));
				if (violations.isEmpty())

				{
					validMSPTariffBookRefVM.add(mSPTariffBookRefVMs.get(i));
				} else {
					
					
					invalidMSPTariffBookRefVM.add(mSPTariffBookRefVMs.get(i));
				}
				
				if (!invalidMSPTariffBookRefVM.isEmpty()) {

					ByteArrayInputStream out = mspTariffBookJoinToExcel(invalidMSPTariffBookRefVM);
					xls = new InputStreamResource(out);}
        	
			}
        	
        	
            if (!validMSPTariffBookRefVM.isEmpty())
            	validMSPTariffBookRefVM.stream().forEach(
                        mspTar -> {
                            if (mspTar.getMspId() != -1 && mspTar.getTarifBookId() != -1) {
                                this.save(mspTar);
                            }
                        }
                );
			 if (!invalidMSPTariffBookRefVM.isEmpty())
					return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
							.contentType(
									new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
							.body(xls);

				if (!validMSPTariffBookRefVM.isEmpty())
					return ResponseEntity.status(HttpStatus.OK).body(null);

				return ResponseEntity.status(HttpStatus.OK).body(null);
            
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }


    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            List<MSPTariffBookRefVM> mspTariffBookRefVMs = excelToElemetsRefs(file.getInputStream());
            if (!mspTariffBookRefVMs.isEmpty())
                mspTariffBookRefVMs.stream().forEach(
                        mspTar -> {
                            if (mspTar.getMspId() != -1 && mspTar.getTarifBookId() != -1) {
                                this.save(mspTar);
                            }
                        }
                );
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    public  MSPTariffBookRefVM save(MSPTariffBookRefVM model) {

        SanitaryPhytosanitaryMeasuresRef mspRef = sanitaryPhytosanitaryMeasuresRefRepository.findOneById(model.getMspId());

        if (mspRef != null) {
            Optional<TarifBookRef> optional = tarifBookRefRepository.findById(model.getTarifBookId());
            if (optional.isPresent()) {
                Set<TarifBookRef> tarifBookRefs = Stream.concat(mspRef.getTarifBookRefs().stream(), Stream.of(optional.get())).collect(Collectors.toSet());
                mspRef.setTarifBookRefs(tarifBookRefs);
            }
        }
        SanitaryPhytosanitaryMeasuresRef entity = sanitaryPhytosanitaryMeasuresRefRepository.save(mspRef);

        if(entity != null ){
            return model;
        }

        return null;
    }

    @Override
    public Page<MSPTariffBookRefVM> findMspTarifBookJoin(int page, int size, String reference) {
        Page<SanitaryPhytosanitaryMeasuresRef>  measuresRefs =  sanitaryPhytosanitaryMeasuresRefRepository.findMspTarifBookJoin(reference.toUpperCase(),PageRequest.of(page, size));
        List<MSPTariffBookRefVM> list = new ArrayList<>();
        measuresRefs.forEach(m -> {
            m.getTarifBookRefs().forEach(thr -> {
                list.add(

                        new MSPTariffBookRefVM(m.getCode(), thr.getReference(), m.getId(), thr.getId(),
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

        List<MSPTariffBookRefVM> tarifBookTaxationVMs = getListOfMSPTariffBookRefVMs();
        ByteArrayInputStream in = mspTariffBookJoinToExcel(tarifBookTaxationVMs);
        return in;

    }

    private List<MSPTariffBookRefVM> getListOfMSPTariffBookRefVMs() {

        List<SanitaryPhytosanitaryMeasuresRef> result = sanitaryPhytosanitaryMeasuresRefRepository.findAll();
        List<MSPTariffBookRefVM> list = new ArrayList<>();
        result.forEach(msp -> {
            msp.getTarifBookRefs().forEach(tarifs -> {
                list.add(

                        new MSPTariffBookRefVM(msp.getCode(), tarifs.getReference(), msp.getId(), tarifs.getId(),
								" ", " ", " ", " ", " ")

                );
            });
        });

        return list;
    }

    private Page<MSPTariffBookRefVM> getListOfMSPTariffBookRefVMs(int page, int size, String codeLang) {
        return mapMspRefToMSPTariffBookRefVM(sanitaryPhytosanitaryMeasuresRefRepository.getListOfMSPTariffBookRefVMs(PageRequest.of(page,size)), codeLang);
    }

    public Page<MSPTariffBookRefVM> mapMspRefToMSPTariffBookRefVM(Page<MSPTariffBookRefVMProjection> tarifBookNationalProcedureVMProjections, String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<MSPTariffBookRefVM> tarifBookNationalProcedureVMs = new ArrayList<>();

        return tarifBookNationalProcedureVMProjections.map(tarifBookNationalProcedureVMProjection -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), tarifBookNationalProcedureVMProjection.getMspId());
            EntityRefLang entityRefLangTarif = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), tarifBookNationalProcedureVMProjection.getTarifBookId());
            MSPTariffBookRefVM agreementLang = new MSPTariffBookRefVM();

            agreementLang.setMspLabel(entityRefLang!=null?entityRefLang.getLabel():"");
            agreementLang.setMspId(tarifBookNationalProcedureVMProjection!=null ?
                    tarifBookNationalProcedureVMProjection.getMspId():null);
            agreementLang.setMspReference(tarifBookNationalProcedureVMProjection!=null?
                    tarifBookNationalProcedureVMProjection.getMspReference():"");

            agreementLang.setTarifBookLabel(entityRefLangTarif!=null?entityRefLangTarif.getLabel():"");
            agreementLang.setTarifBookId(tarifBookNationalProcedureVMProjection!=null ?tarifBookNationalProcedureVMProjection.getTarifBookId():null);
            agreementLang.setTarifBookReference(tarifBookNationalProcedureVMProjection!=null?tarifBookNationalProcedureVMProjection.getTarifBookReference():"");
            agreementLang.setLang(codeLang);

            tarifBookNationalProcedureVMs.add(agreementLang);

            return  agreementLang ;
        });
    }

    @Override
    public ByteArrayInputStream mspTariffBookJoinToExcel(List<MSPTariffBookRefVM> mspTariffBookJoinVMs) {

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
                if (!mspTariffBookJoinVMs.isEmpty()) {

                    for (MSPTariffBookRefVM model : mspTariffBookJoinVMs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(model.getMspReference());
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
    public Page<MSPTariffBookRefVM> getAll(int page, int size, String codeLang) {
//        List<MSPTariffBookRefVM> list = getListOfMSPTariffBookRefVMs();
//        return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
        return getListOfMSPTariffBookRefVMs(page,size, codeLang);
    }
    
	@Override
	public ByteArrayInputStream load(String codeLang) {
		List<MSPTariffBookRefVM> listMSPTariffBookRefVM = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);
		List<TarifBookRef> tarifBookRefs = tarifBookRefRepository.findAll();

		for (TarifBookRef t : tarifBookRefs) {
			EntityRefLang tariffBookEntityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), t.getId());
			if (tariffBookEntityRefLang != null) {
				t.getSanitaryPhytosanitaryMeasuresRefs().forEach(msp -> {
					EntityRefLang MSPEntityRefLang = entityRefLangRepository
							.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), !Objects.isNull(msp) ? msp.getId() : 0);
					listMSPTariffBookRefVM.add(new MSPTariffBookRefVM(msp.getCode(),
							t.getReference(), msp.getId(), t.getId(),
							(!Objects.isNull(MSPEntityRefLang) && !Objects.isNull(MSPEntityRefLang.getLabel())) ? MSPEntityRefLang.getLabel() : " ",
							(!Objects.isNull(MSPEntityRefLang) && !Objects.isNull(MSPEntityRefLang.getDescription())) ? MSPEntityRefLang.getDescription() : " ",
							(!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getLabel())) ? tariffBookEntityRefLang.getLabel() : " ",
							(!Objects.isNull(tariffBookEntityRefLang) && !Objects.isNull(tariffBookEntityRefLang.getDescription())) ? tariffBookEntityRefLang.getDescription() : " ",
									codeLang));
				});
			}
		}

		ByteArrayInputStream in = mspTariffBookJoinToExcel(listMSPTariffBookRefVM);

		return in;

	}

	@Override
	public Page<MSPTariffBookRefVM> findMspTarifBookJoin(Long idTarifBookRef, String codeLang, int page, int size) {
		TarifBookRef tarifBookRef = tarifBookRefRepository.findOneById(idTarifBookRef);
		List<MSPTariffBookRefVM> listMSPTariffBookRefVM = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);

		EntityRefLang tariffBookEntityRefLang = entityRefLangRepository
				.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), !Objects.isNull(tarifBookRef) ? tarifBookRef.getId() : 0);

		if (Objects.isNull(tarifBookRef) || Objects.isNull(tariffBookEntityRefLang)) {
			throw new MSPTariffBookRefVMJoinNotFoundException(
					"MSPTariffBookRefVM: " + idTarifBookRef);
		}

		if (!Objects.isNull(tariffBookEntityRefLang)) {
			tarifBookRef.getSanitaryPhytosanitaryMeasuresRefs().forEach(msp -> {
				EntityRefLang MSPEntityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
						TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), !Objects.isNull(msp) ? msp.getId() : 0);
				listMSPTariffBookRefVM.add(

						new MSPTariffBookRefVM(msp.getCode(), tarifBookRef.getReference(), msp.getId(),
								tarifBookRef.getId(),
								(!Objects.isNull(MSPEntityRefLang) && !Objects.isNull(MSPEntityRefLang.getLabel()))
										? MSPEntityRefLang.getLabel()
										: " ",
								(!Objects.isNull(MSPEntityRefLang)
										&& !Objects.isNull(MSPEntityRefLang.getDescription()))
												? MSPEntityRefLang.getDescription()
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
		
		return PageHelper.listConvertToPage(listMSPTariffBookRefVM, listMSPTariffBookRefVM.size(), PageRequest.of(page, size));

	}

	@Override
	public Page<MSPTariffBookRefVM> getAll(String codeLang, int page, int size, String orderDirection) {
        Page<TarifBookRef> tarifBookRefs = null;
        if(orderDirection.equals("DESC")){
            tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
		List<MSPTariffBookRefVM> MSPTariffBookRefVMs = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);

		for (TarifBookRef t : tarifBookRefs) {
			EntityRefLang tariffBookEntityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), t.getId());
			if (tariffBookEntityRefLang != null) {
				t.getSanitaryPhytosanitaryMeasuresRefs().forEach(msp -> {
					EntityRefLang MSPEntityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
							TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), !Objects.isNull(msp) ? msp.getId() : 0);
					MSPTariffBookRefVMs.add(new MSPTariffBookRefVM(msp.getCode(), t.getReference(), msp.getId(),
							t.getId(),
							(!Objects.isNull(MSPEntityRefLang) && !Objects.isNull(MSPEntityRefLang.getLabel()))
									? MSPEntityRefLang.getLabel()
									: " ",
							(!Objects.isNull(MSPEntityRefLang) && !Objects.isNull(MSPEntityRefLang.getDescription()))
									? MSPEntityRefLang.getDescription()
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

		return PageHelper.listConvertToPage(MSPTariffBookRefVMs, MSPTariffBookRefVMs.size(), PageRequest.of(page, size));
	}

    @Override
    public ErrorResponse delete(Long id, Long id_tarif) {
        try {
            SanitaryPhytosanitaryMeasuresRef sanitaryPhytosanitaryMeasuresRef = sanitaryPhytosanitaryMeasuresRefRepository.findOneById(id);
            if (sanitaryPhytosanitaryMeasuresRef != null) {
                Optional<TarifBookRef> optional = tarifBookRefRepository.findById(id_tarif);
                if (optional.isPresent()) {
                    sanitaryPhytosanitaryMeasuresRef.getTarifBookRefs().remove(optional.get());
                }
            }

            sanitaryPhytosanitaryMeasuresRefRepository.save(sanitaryPhytosanitaryMeasuresRef);
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
