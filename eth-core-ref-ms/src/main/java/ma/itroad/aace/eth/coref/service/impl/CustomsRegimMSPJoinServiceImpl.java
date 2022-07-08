package ma.itroad.aace.eth.coref.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import ma.itroad.aace.eth.coref.exception.CustomsRegimMSPVMJoinNotFoundException;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarif;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.entity.CustomsRegimRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.entity.SanitaryPhytosanitaryMeasuresRef;
import ma.itroad.aace.eth.coref.model.mapper.CustomsRegimRefMapper;
import ma.itroad.aace.eth.coref.model.view.CustomsRegimMSPVM;
import ma.itroad.aace.eth.coref.repository.CustomsRegimRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.SanitaryPhytosanitaryMeasuresRefRepository;
import ma.itroad.aace.eth.coref.service.ICustomsRegimMSPJoinService;
import ma.itroad.aace.eth.coref.service.helper.PageHelper;
import ma.itroad.aace.eth.coref.service.util.Util;

@Service
public class CustomsRegimMSPJoinServiceImpl implements ICustomsRegimMSPJoinService {

	static String[] HEADERs = { "CUSTOMS REGIM CODE", "MSP TECH REFERENCE"};
	static String SHEET = "msp-customs-regim-join";
	
	@Autowired
	Validator validator;

	@Autowired
	private CustomsRegimRefRepository customsRegimRefRepository;

	@Autowired
	private SanitaryPhytosanitaryMeasuresRefRepository sanitaryPhytosanitaryMeasuresRefRepository;

	@Autowired
	private LangRepository langRepository;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

	@Autowired
	CustomsRegimRefMapper customsRegimRefMapper;

	private List<CustomsRegimMSPVM> getListOfCustomsRegimMSPs(String codeLang) {
		List<CustomsRegimRef> result = customsRegimRefRepository.findAll();
		List<CustomsRegimMSPVM> list = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);
		result.forEach(customsRegimRef -> {
			customsRegimRef.getSanitaryPhytosanitaryMeasuresRef().forEach(sanitaryPhytosanitaryMeasuresRef -> {
				EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), sanitaryPhytosanitaryMeasuresRef.getId());
				EntityRefLang entityRefLangCustomRegim = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSREGIM_REF, lang.getId(), customsRegimRef.getId());
				if(entityRefLang!=null && entityRefLangCustomRegim!=null){
					list.add(new CustomsRegimMSPVM(sanitaryPhytosanitaryMeasuresRef.getCode(), customsRegimRef.getCode(),
							sanitaryPhytosanitaryMeasuresRef.getId(), customsRegimRef.getId(),
							entityRefLang.getLabel(), " ", entityRefLangCustomRegim.getLabel(), " ", " "));
				}
				else{
					list.add(new CustomsRegimMSPVM(sanitaryPhytosanitaryMeasuresRef.getCode(), customsRegimRef.getCode(),
							sanitaryPhytosanitaryMeasuresRef.getId(), customsRegimRef.getId(),
							"", " ", "", " ", " "));
				}

			});
		});
		return list;
	}

	@Override
	public ByteArrayInputStream load() {
		List<CustomsRegimMSPVM> customsRegimMSPVMS = getListOfCustomsRegimMSPs("");
		ByteArrayInputStream in = customsRegimMSPJoinToExcel(customsRegimMSPVMS);
		return in;
	}

	@Override
	public ByteArrayInputStream customsRegimMSPJoinToExcel(List<CustomsRegimMSPVM> customsRegimMSPVMS) {
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
				if (!customsRegimMSPVMS.isEmpty()) {
					for (CustomsRegimMSPVM model : customsRegimMSPVMS) {
						Row row = sheet.createRow(rowIdx++);
						row.createCell(0).setCellValue(model.getCustomsRegimReference());
						row.createCell(1).setCellValue(model.getMspReference());
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
	public Page<CustomsRegimMSPVM> getAll(final int page, final int size, String codeLang) {
		List<CustomsRegimMSPVM> list = getListOfCustomsRegimMSPs(codeLang);
		return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
	}

	@Override
	public List<CustomsRegimMSPVM> excelToElemetsRefs(InputStream is) {
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);
			List<CustomsRegimMSPVM> customsRegimMSPVMS = new ArrayList<>();
			Row currentRow;
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				currentRow= sheet.getRow(rowNum);
				CustomsRegimMSPVM customsRegimMSPVM = new CustomsRegimMSPVM();
				for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
					Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
					switch (colNum) {
					case 0:
						customsRegimMSPVM.setCustomsRegimReference(Util.cellValue(currentCell));
						Long idCurentCustomsRegim = customsRegimRefRepository
								.findByCode(Util.cellValue(currentCell)) != null
										? customsRegimRefRepository.findByCode(Util.cellValue(currentCell)).getId()
										: -1;
						customsRegimMSPVM.setCustomsRegimId(idCurentCustomsRegim);
						break;
					case 1:
						customsRegimMSPVM.setMspReference(Util.cellValue(currentCell));
						Long idMSP = sanitaryPhytosanitaryMeasuresRefRepository
								.findByCode(Util.cellValue(currentCell)) != null
										? sanitaryPhytosanitaryMeasuresRefRepository
												.findByCode(Util.cellValue(currentCell)).getId()
										: -1;
						customsRegimMSPVM.setMsptId(idMSP);
						break;
					default:
						break;
					}
				}
				customsRegimMSPVMS.add(customsRegimMSPVM);
			}
			workbook.close();
			return customsRegimMSPVMS;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}

	@Override
	public Set<ConstraintViolation<CustomsRegimMSPVM>> validateCustomsRegimMSPVM(CustomsRegimMSPVM customsRegimMSPVM) {

		Set<ConstraintViolation<CustomsRegimMSPVM>> violations = validator.validate(customsRegimMSPVM);

		return violations;
	}
	
	@Override
	public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
	 	String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {
			List<CustomsRegimMSPVM> customsRegimMSPVM = excelToElemetsRefs(file.getInputStream());
			 
			List<CustomsRegimMSPVM> invalidCustomsRegimMSPVM = new ArrayList<CustomsRegimMSPVM>();
			List<CustomsRegimMSPVM> validCustomsRegimMSPVM = new ArrayList<CustomsRegimMSPVM>();

			int lenght = customsRegimMSPVM.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<CustomsRegimMSPVM>> violations = validateCustomsRegimMSPVM(
						customsRegimMSPVM.get(i));
				if (violations.isEmpty())

				{
					validCustomsRegimMSPVM.add(customsRegimMSPVM.get(i));
				} else {
					
					
					invalidCustomsRegimMSPVM.add(customsRegimMSPVM.get(i));
				}
				
				if (!invalidCustomsRegimMSPVM.isEmpty()) {

					ByteArrayInputStream out = customsRegimMSPJoinToExcel(invalidCustomsRegimMSPVM);
					xls = new InputStreamResource(out);}
        	
			}

			if (!validCustomsRegimMSPVM.isEmpty())
				validCustomsRegimMSPVM.stream().forEach(dc -> {
					if (dc.getCustomsRegimId() != -1 && dc.getMsptId() != -1)
						this.save(dc);
				});
			
			 if (!invalidCustomsRegimMSPVM.isEmpty())
					return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
							.contentType(
									new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
							.body(xls);

				if (!validCustomsRegimMSPVM.isEmpty())
					return ResponseEntity.status(HttpStatus.OK).body(null);

				return ResponseEntity.status(HttpStatus.OK).body(null);
			
			
		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}

	}

	
	@Override
	public void saveFromExcel(MultipartFile file) {
		try {
			List<CustomsRegimMSPVM> customsRegimMSPVMS = excelToElemetsRefs(file.getInputStream());
			if (!customsRegimMSPVMS.isEmpty())
				customsRegimMSPVMS.stream().forEach(dc -> {
					if (dc.getCustomsRegimId() != -1 && dc.getMsptId() != -1)
						this.save(dc);
				});
		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}

	}

	public CustomsRegimMSPVM save(CustomsRegimMSPVM model) {

		CustomsRegimRef customsRegimRef = customsRegimRefRepository.findOneById(model.getCustomsRegimId());

		if (customsRegimRef != null) {
			Optional<SanitaryPhytosanitaryMeasuresRef> optional = sanitaryPhytosanitaryMeasuresRefRepository
					.findById(model.getMsptId());
			if (optional.isPresent()) {
				Set<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs = Stream
						.concat(customsRegimRef.getSanitaryPhytosanitaryMeasuresRef().stream(),
								Stream.of(optional.get()))
						.collect(Collectors.toSet());
				customsRegimRef.setSanitaryPhytosanitaryMeasuresRef(sanitaryPhytosanitaryMeasuresRefs);
			}
		}
		CustomsRegimRef entity = customsRegimRefRepository.save(customsRegimRef);
		if(entity != null ){
			return model;
		}

		return null;
	}

	@Override
	public ErrorResponse delete(Long id, Long msptId) {
		try {
			CustomsRegimRef customsRegimRef = customsRegimRefRepository.findOneById(id);

			if (customsRegimRef != null) {
				Optional<SanitaryPhytosanitaryMeasuresRef> optional = sanitaryPhytosanitaryMeasuresRefRepository.findById(msptId);
				if (optional.isPresent()) {
					customsRegimRef.getSanitaryPhytosanitaryMeasuresRef().remove(optional.get());
				}
			}

			customsRegimRefRepository.save(customsRegimRef);
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

	@Override
	public Page<CustomsRegimMSPVM> findCustomRegimeMspJoin(int page, int size, String reference) {
		Page<CustomsRegimRef> customsRegimRefs = customsRegimRefRepository.findCustomRegimMsp(reference.toUpperCase(),
				PageRequest.of(page, size));
		List<CustomsRegimMSPVM> list = new ArrayList<>();
		customsRegimRefs.forEach(customsRegimRef -> {
			customsRegimRef.getSanitaryPhytosanitaryMeasuresRef().forEach(msp -> {
				list.add(

						new CustomsRegimMSPVM(msp.getCode(), customsRegimRef.getCode(), msp.getId(),
								customsRegimRef.getId(),
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
	public ByteArrayInputStream load(String codeLang) {
		List<CustomsRegimMSPVM> customsRegimMSPVMs = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);
		List<CustomsRegimRef> customsRegimRefs = customsRegimRefRepository.findAll();

		for (CustomsRegimRef crr : customsRegimRefs) {
			EntityRefLang customsRegimEntityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSREGIM_REF, lang.getId(), crr.getId());
			if (customsRegimEntityRefLang != null) {
				crr.getSanitaryPhytosanitaryMeasuresRef().forEach(sanitaryPhytosanitaryMeasuresRef -> {
					EntityRefLang MSPEntityRefLang = entityRefLangRepository
							.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), !Objects.isNull(sanitaryPhytosanitaryMeasuresRef) ? sanitaryPhytosanitaryMeasuresRef.getId() : 0);
					customsRegimMSPVMs.add(new CustomsRegimMSPVM(sanitaryPhytosanitaryMeasuresRef.getCode(),
							crr.getCode(), sanitaryPhytosanitaryMeasuresRef.getId(), crr.getId(),
							(!Objects.isNull(MSPEntityRefLang) && !Objects.isNull(MSPEntityRefLang.getLabel())) ? MSPEntityRefLang.getLabel() : " ",
							(!Objects.isNull(MSPEntityRefLang) && !Objects.isNull(MSPEntityRefLang.getDescription())) ? MSPEntityRefLang.getDescription() : " ",
							(!Objects.isNull(customsRegimEntityRefLang) && !Objects.isNull(customsRegimEntityRefLang.getLabel())) ? customsRegimEntityRefLang.getLabel() : " ",
							(!Objects.isNull(customsRegimEntityRefLang) && !Objects.isNull(customsRegimEntityRefLang.getDescription())) ? customsRegimEntityRefLang.getDescription() : " ",
									codeLang));
				});
			}
		}

		ByteArrayInputStream in = customsRegimMSPJoinToExcel(customsRegimMSPVMs);

		return in;

	}

	@Override
	public Page<CustomsRegimMSPVM> findCustomRegimeMspJoin(Long idCustomsRegimRef, String codeLang, int page, int size) {
		CustomsRegimRef customsRegimRef = customsRegimRefRepository.findOneById(idCustomsRegimRef);
		List<CustomsRegimMSPVM> listCustomsRegimMSPVM = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);

		EntityRefLang customsRegimEntityRefLang = entityRefLangRepository
				.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSREGIM_REF, lang.getId(), !Objects.isNull(customsRegimRef) ? customsRegimRef.getId() : 0);

		if (Objects.isNull(customsRegimRef) || Objects.isNull(customsRegimEntityRefLang)) {
			throw new CustomsRegimMSPVMJoinNotFoundException(
					"CustomsRegimMSPVM avec CustomRegimRef: " + idCustomsRegimRef);
		}

		if (!Objects.isNull(customsRegimEntityRefLang)) {
			customsRegimRef.getSanitaryPhytosanitaryMeasuresRef().forEach(msp -> {
				EntityRefLang MSPEntityRefLang = entityRefLangRepository
						.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), !Objects.isNull(msp) ? msp.getId() : 0);
				
				listCustomsRegimMSPVM.add(
					new CustomsRegimMSPVM(msp.getCode(), customsRegimRef.getCode(), msp.getId(),
							customsRegimRef.getId(),
							(!Objects.isNull(MSPEntityRefLang) && !Objects.isNull(MSPEntityRefLang.getLabel())) ? MSPEntityRefLang.getLabel() : " ",
							(!Objects.isNull(MSPEntityRefLang) && !Objects.isNull(MSPEntityRefLang.getDescription())) ? MSPEntityRefLang.getDescription() : " ",
							(!Objects.isNull(customsRegimEntityRefLang) && !Objects.isNull(customsRegimEntityRefLang.getLabel())) ? customsRegimEntityRefLang.getLabel() : " ",
							(!Objects.isNull(customsRegimEntityRefLang) && !Objects.isNull(customsRegimEntityRefLang.getDescription())) ? customsRegimEntityRefLang.getDescription() : " ",
							codeLang)
				);
			});
		}

		return PageHelper.listConvertToPage(listCustomsRegimMSPVM, listCustomsRegimMSPVM.size(), PageRequest.of(page, size));
//		return listCustomsRegimMSPVM;
	}

	@Override
	public Page<CustomsRegimMSPVM> getAll(String codeLang, int page, int size, String orderDirection) {
		Page<CustomsRegimRef> customsRegimRefs = null;
		if(orderDirection.equals("DESC")){
			customsRegimRefs = customsRegimRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}else if(orderDirection.equals("ASC")){
			customsRegimRefs = customsRegimRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}
		List<CustomsRegimMSPVM> customsRegimMSPVMs = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);

		for (CustomsRegimRef crr : customsRegimRefs) {
			EntityRefLang customsRegimEntityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSREGIM_REF, lang.getId(), crr.getId());
			if (customsRegimEntityRefLang != null) {
				crr.getSanitaryPhytosanitaryMeasuresRef().forEach(sanitaryPhytosanitaryMeasuresRef -> {
					EntityRefLang MSPEntityRefLang = entityRefLangRepository
							.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), !Objects.isNull(sanitaryPhytosanitaryMeasuresRef) ? sanitaryPhytosanitaryMeasuresRef.getId() : 0);
					customsRegimMSPVMs.add(new CustomsRegimMSPVM(sanitaryPhytosanitaryMeasuresRef.getCode(),
							crr.getCode(), sanitaryPhytosanitaryMeasuresRef.getId(), crr.getId(),
							(!Objects.isNull(MSPEntityRefLang) && !Objects.isNull(MSPEntityRefLang.getLabel())) ? MSPEntityRefLang.getLabel() : " ",
							(!Objects.isNull(MSPEntityRefLang) && !Objects.isNull(MSPEntityRefLang.getDescription())) ? MSPEntityRefLang.getDescription() : " ",
							(!Objects.isNull(customsRegimEntityRefLang) && !Objects.isNull(customsRegimEntityRefLang.getLabel())) ? customsRegimEntityRefLang.getLabel() : " ",
							(!Objects.isNull(customsRegimEntityRefLang) && !Objects.isNull(customsRegimEntityRefLang.getDescription())) ? customsRegimEntityRefLang.getDescription() : " ",
							codeLang));
				});
			}
		}

		return PageHelper.listConvertToPage(customsRegimMSPVMs, customsRegimMSPVMs.size(), PageRequest.of(page, size));
	}

}
