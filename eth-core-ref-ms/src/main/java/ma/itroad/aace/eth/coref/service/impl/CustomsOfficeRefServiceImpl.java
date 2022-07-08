package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.CustomsOfficeNotFoundException;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.exception.TranslationFoundException;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.model.bean.CustomsOfficeRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.CustomsOfficeRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.CustomsOfficeRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.CustomsOfficeRefVM;
import ma.itroad.aace.eth.coref.repository.CustomsOfficeRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.service.ICustomsOfficeRefService;
import ma.itroad.aace.eth.coref.service.helper.AgreementTypeLang;
import ma.itroad.aace.eth.coref.service.helper.CustomsOfficeRefLang;
import ma.itroad.aace.eth.coref.service.helper.SanitaryPhytosanitaryMeasuresRefLang;
import ma.itroad.aace.eth.coref.service.impl.exceldto.CustomsOfficeRefExcelDTO;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.service.util.Util;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Service
public class CustomsOfficeRefServiceImpl extends BaseServiceImpl<CustomsOfficeRef, CustomsOfficeRefBean>
		implements ICustomsOfficeRefService {

	@Autowired
	private Validator validator;

	@Autowired
	private CustomsOfficeRefRepository customsOfficeRefRepository;

	@Autowired
	private CountryRefRepository countryRefRepository;

	@Autowired
	private LangRepository langRepository;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

	@Autowired
	private CustomsOfficeRefMapper customsOfficeRefMapper;

	static String[] HEADERs = { "Code du bureau", "Référence du pays", "Label", "Description", "Lang" };
	static String SHEET = "Sheet1";

	@Override
	public Set<ConstraintViolation<CustomsOfficeRefLang>> validateCustomsOfficeRef(CustomsOfficeRefLang customsOfficeRefBean) {

		Set<ConstraintViolation<CustomsOfficeRefLang>> violations = validator.validate(customsOfficeRefBean);

		return violations;
	}
    
    @Override
    public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
    	String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
    	
    	try {
    		List<CustomsOfficeRef> CustomsOfficeRefs = excelToCustomsOfficeRef(file.getInputStream());
			List<CustomsOfficeRef> invalidCustomsOfficeRef = new ArrayList<CustomsOfficeRef>();
			List<CustomsOfficeRef> validCustomsOfficeRef = new ArrayList<CustomsOfficeRef>();

			int lenght = CustomsOfficeRefs.size();

			for (int i = 0; i < lenght; i++) {

				CustomsOfficeRefLang customsOfficeRefLang = new CustomsOfficeRefLang();
				customsOfficeRefLang.setCode(CustomsOfficeRefs.get(i).getCode());
				
				if(CustomsOfficeRefs.get(i).getCountryRef() != null)
				customsOfficeRefLang.setCountryRef(CustomsOfficeRefs.get(i).getCountryRef().getCodeIso());
				
				customsOfficeRefLang.setLabel(CustomsOfficeRefs.get(i).getLabel());
				customsOfficeRefLang.setDescription(CustomsOfficeRefs.get(i).getDescription());
				customsOfficeRefLang.setLang(CustomsOfficeRefs.get(i).getLang());
				
				
				Set<ConstraintViolation<CustomsOfficeRefLang>> violations = validateCustomsOfficeRef(customsOfficeRefLang);
				if (violations.isEmpty())

				{
					validCustomsOfficeRef.add(CustomsOfficeRefs.get(i));
				} else {
					invalidCustomsOfficeRef.add(CustomsOfficeRefs.get(i));
				}
			}

			if (!invalidCustomsOfficeRef.isEmpty()) {

				ByteArrayInputStream out = customsOfficeRefToExcel(invalidCustomsOfficeRef);
				xls = new InputStreamResource(out);
			}
			
            if (!validCustomsOfficeRef.isEmpty())
            	validCustomsOfficeRef.stream().forEach(
                        l -> {
                            CustomsOfficeRef ltemp = customsOfficeRefRepository.findByCode(l.getCode());

                            if (ltemp == null) {
                                customsOfficeRefRepository.save(l);
        						Long id = (customsOfficeRefRepository.save(l)).getId();
        						EntityRefLang entityRefLang = new EntityRefLang();
        						entityRefLang.setCreatedOn(new Date());
        						entityRefLang.setLabel(l.getLabel());
        						entityRefLang.setDescription(l.getDescription());
        						entityRefLang.setRefId(id);
        						entityRefLang.setTableRef(TableRef.CUSTOMSOFFICE_REF);
        						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
        						entityRefLangRepository.save(entityRefLang);

                            } else {
                                l.setId(ltemp.getId());
                                customsOfficeRefRepository.save(l);
                                EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSOFFICE_REF, langRepository.findByCode(l.getLang()).getId(), l.getId());
								if(entityRefLang == null )
								{
									entityRefLang =new EntityRefLang() ;
									entityRefLang.setLang(langRepository.findByCode(l.getLang()));
									entityRefLang.setTableRef(TableRef.CUSTOMSOFFICE_REF);
									entityRefLang.setRefId(l.getId());
								}
        						if (Objects.isNull(entityRefLang)) {
        							Long id = (customsOfficeRefRepository.save(l)).getId();
        							EntityRefLang entityRefLangTemp = new EntityRefLang();
        							entityRefLang.setCreatedOn(new Date());
        							entityRefLang.setLabel(l.getLabel());
        							entityRefLang.setDescription(l.getDescription());
        							entityRefLang.setRefId(id);
        							entityRefLang.setTableRef(TableRef.CUSTOMSOFFICE_REF);
        							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
        							entityRefLangRepository.save(entityRefLangTemp);					
        						}

                            }

                        }
                );
            
        	if (!invalidCustomsOfficeRef.isEmpty())
				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);

			if (!validCustomsOfficeRef.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(null);

			return ResponseEntity.status(HttpStatus.OK).body(null);

        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }

    }

	
	@Override
	public void saveFromExcel(MultipartFile file) {
		try {
			List<CustomsOfficeRef> customsOfficeRefs = excelToCustomsOfficeRef(file.getInputStream());
			if (!customsOfficeRefs.isEmpty())
				customsOfficeRefs.stream().forEach(l -> {
					CustomsOfficeRef ltemp = customsOfficeRefRepository.findByCode(l.getCode());

					if (ltemp == null) {
						customsOfficeRefRepository.save(l);
						Long id = (customsOfficeRefRepository.save(l)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setCreatedOn(new Date());
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.CUSTOMSOFFICE_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);

					} else {
						l.setId(ltemp.getId());
						customsOfficeRefRepository.save(l);
						EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
								TableRef.CUSTOMSOFFICE_REF, langRepository.findByCode(l.getLang()).getId(), l.getId());
						if (entityRefLang == null) {
							entityRefLang = new EntityRefLang();
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.CUSTOMSOFFICE_REF);
							entityRefLang.setRefId(l.getId());
						}
						if (Objects.isNull(entityRefLang)) {
							Long id = (customsOfficeRefRepository.save(l)).getId();
							EntityRefLang entityRefLangTemp = new EntityRefLang();
							entityRefLang.setCreatedOn(new Date());
							entityRefLang.setLabel(l.getLabel());
							entityRefLang.setDescription(l.getDescription());
							entityRefLang.setRefId(id);
							entityRefLang.setTableRef(TableRef.CUSTOMSOFFICE_REF);
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLangRepository.save(entityRefLangTemp);
						}

					}

				});
		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}

	}

	@Override
	public List<CustomsOfficeRef> excelToCustomsOfficeRef(InputStream is) {
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);

			Iterator<Row> rows = sheet.iterator();
			List<CustomsOfficeRef> customsOfficeRefs = new ArrayList<CustomsOfficeRef>();
			int rowNumber = 0;
			while (rows.hasNext()) {
				Row currentRow = rows.next();
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}
				Iterator<Cell> cellsInRow = currentRow.iterator();
				CustomsOfficeRef customsOfficeRef = new CustomsOfficeRef();

				for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++)
					for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {

						Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
						String cellValue = null;
						switch (colNum) {
						case 0:
							customsOfficeRef.setCode(Util.cellValue(currentCell));
							break;
						case 1:
							CountryRef ountryRef = countryRefRepository.findByReference(Util.cellValue(currentCell));
							customsOfficeRef.setCountryRef(ountryRef);
							break;
						case 2:
							customsOfficeRef.setLabel(Util.cellValue(currentCell));
							break;
						case 3:
							customsOfficeRef.setDescription(Util.cellValue(currentCell));
							break;
						case 4:
							customsOfficeRef.setLang(Util.cellValue(currentCell));
							break;

						default:
							break;
						}
					}
				customsOfficeRefs.add(customsOfficeRef);
			}
			workbook.close();
			return customsOfficeRefs;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}

	@Override
	public ByteArrayInputStream load() {
		List<CustomsOfficeRef> customsOfficeRefs = customsOfficeRefRepository.findAll();
		ByteArrayInputStream in = customsOfficeRefToExcel(customsOfficeRefs);
		return in;
	}

	@Override
	public Page<CustomsOfficeRefBean> getAll(int page, int size) {
		Page<CustomsOfficeRef> entities = customsOfficeRefRepository
				.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn")));
		Page<CustomsOfficeRefBean> result = entities.map(customsOfficeRefMapper::entityToBean);
		return result;
	}

	@Override
	public Page<CustomsOfficeRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
		Page<CustomsOfficeRef> customsOfficeRefs = null;
		if (orderDirection.equals("DESC")) {
			customsOfficeRefs = customsOfficeRefRepository
					.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		} else if (orderDirection.equals("ASC")) {
			customsOfficeRefs = customsOfficeRefRepository
					.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}
		Lang lang = langRepository.findByCode(codeLang);
		List<CustomsOfficeRefLang> customsOfficeRefLangs = new ArrayList<>();

		return customsOfficeRefs.map(customsOfficeRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
					TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), customsOfficeRef.getId());

			CustomsOfficeRefLang customsOfficeRefLang = new CustomsOfficeRefLang();

			customsOfficeRefLang.setId(customsOfficeRef.getId());
			customsOfficeRefLang.setCode(customsOfficeRef.getCode() != null ? customsOfficeRef.getCode() : "");

			customsOfficeRefLang.setCountryRef(
					customsOfficeRef.getCountryRef() != null ? customsOfficeRef.getCountryRef().getReference() : "");

			customsOfficeRefLang.setCreatedBy(customsOfficeRef.getCreatedBy());
			customsOfficeRefLang.setCreatedOn(customsOfficeRef.getCreatedOn());
			customsOfficeRefLang.setUpdatedBy(customsOfficeRef.getUpdatedBy());
			customsOfficeRefLang.setUpdatedOn(customsOfficeRef.getUpdatedOn());

			customsOfficeRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
			customsOfficeRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");

			customsOfficeRefLang.setLang(codeLang);

			customsOfficeRefLangs.add(customsOfficeRefLang);

			return customsOfficeRefLang;
		});
	}

	/**
	 * public Page<CustomsOfficeRefLang> getAll(String lang, int page, int size) {
	 * Page<CustomsOfficeRef> entities =
	 * customsOfficeRefRepository.findAll(PageRequest.of(page, size,
	 * Sort.by(Sort.Direction.DESC, "createdOn"))); List<CustomsOfficeRefBean>
	 * customsOfficeRefBeans = new ArrayList<CustomsOfficeRefBean>();
	 * 
	 * for (CustomsOfficeRef customsOfficeRef : entities) { CustomsOfficeRefBean
	 * customsOfficeRefBean = new CustomsOfficeRefBean(); EntityRefLang
	 * entityRefLang =
	 * entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSOFFICE_REF,
	 * langRepository.findByCode(lang).getId(), customsOfficeRef.getId()); if
	 * (Objects.isNull(entityRefLang)) { customsOfficeRef.setLabel(" ");
	 * customsOfficeRef.setDescription(" "); customsOfficeRef.setLang(lang); } else
	 * { customsOfficeRef.setLabel(entityRefLang.getLabel());
	 * customsOfficeRef.setDescription(entityRefLang.getDescription());
	 * customsOfficeRef.setLang(entityRefLang.getLang().getCode()); }
	 * 
	 * customsOfficeRefBean = toCustomsOfficeRefBeanMapper(customsOfficeRef);
	 * customsOfficeRefBeans.add(customsOfficeRefBean); }
	 * 
	 * Pageable paging = PageRequest.of(page, size); int start =
	 * Math.min((int)paging.getOffset(), customsOfficeRefBeans.size()); int end =
	 * Math.min((start + paging.getPageSize()), customsOfficeRefBeans.size());
	 * 
	 * Page<CustomsOfficeRefBean> customsOfficeRefsBeans = new
	 * PageImpl<>(customsOfficeRefBeans.subList(start, end), paging,
	 * entities.getTotalElements());
	 * 
	 * return customsOfficeRefsBeans; }
	 * 
	 **/

	@Override
	public ErrorResponse delete(Long id) {
		try {
			customsOfficeRefRepository.deleteById(id);
			return new ErrorResponse(HttpStatus.OK, ErrorMessageType.DELETE_SUCESS.getMessagePattern(), null);
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse(HttpStatus.CONFLICT, ErrorMessageType.DELETE_ERROR.getMessagePattern(), null);
		}

	}

	@Override
	public ErrorResponse deleteList(ListOfObject listOfObject) {
		ErrorResponse response = new ErrorResponse();

		try {
			for (Long id : listOfObject.getListOfObject()) {
				customsOfficeRefRepository.deleteById(id);
			}

			response.setStatus(HttpStatus.OK);
			response.setErrorMsg("null");
		} catch (Exception e) {
			response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
			response.setStatus(HttpStatus.CONFLICT);
		}
		return response;
	}

	private CustomsOfficeRefVM convertToCustomsOfficeRefVM(CustomsOfficeRef entity) {
		final CustomsOfficeRefVM item = new CustomsOfficeRefVM();
		item.setCustomsOfficeCode(entity.getCode());
		item.setCustomsOfficeId(entity.getId());
		item.setCountryRef(entity.getCountryRef() == null ? null : entity.getCountryRef().getReference());
		return item;
	}

	@Override
	public ByteArrayInputStream customsOfficeRefToExcel(List<CustomsOfficeRef> customsOfficeRefs) {

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
				if (!customsOfficeRefs.isEmpty()) {

					for (CustomsOfficeRef customsOfficeRef : customsOfficeRefs) {
						Row row = sheet.createRow(rowIdx++);
						row.createCell(0).setCellValue(customsOfficeRef.getCode());
						CountryRef countryRef = customsOfficeRef.getCountryRef();
						if (countryRef != null) {
							row.createCell(1).setCellValue(countryRef.getReference());
						}
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
	public ByteArrayInputStream customsOfficeRefLangsToExcel(List<CustomsOfficeRefExcelDTO> customsOfficeRefExcelDTOs) {
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
				if (!customsOfficeRefExcelDTOs.isEmpty()) {

					for (CustomsOfficeRefExcelDTO unitRefEntityRefLang : customsOfficeRefExcelDTOs) {
						Row row = sheet.createRow(rowIdx++);
						row.createCell(0).setCellValue(unitRefEntityRefLang.getCode());
						String country = unitRefEntityRefLang.getCountryRef();
						if (!Objects.isNull(country)) {
							row.createCell(1).setCellValue(unitRefEntityRefLang.getCountryRef());
						}
						row.createCell(2).setCellValue(unitRefEntityRefLang.getLabel());
						row.createCell(3).setCellValue(unitRefEntityRefLang.getDescription());
						row.createCell(4).setCellValue(unitRefEntityRefLang.getLang());
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
	public ByteArrayInputStream load(String codeLang) {
		Lang lang = langRepository.findByCode(codeLang);
		List<CustomsOfficeRef> customsOfficeRefs = customsOfficeRefRepository.findAll();
		List<CustomsOfficeRefExcelDTO> customsOfficeRefExcelDTOs = new ArrayList<CustomsOfficeRefExcelDTO>();

		for (CustomsOfficeRef c : customsOfficeRefs) {
			EntityRefLang entityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.CUSTOMSOFFICE_REF, lang.getId(), c.getId());
			if (entityRefLang != null) {
				CustomsOfficeRefExcelDTO customsOfficeRefExcelDTO = new CustomsOfficeRefExcelDTO();
				customsOfficeRefExcelDTO.setCode(c.getCode());
				customsOfficeRefExcelDTO.setCountryRef(c.getCountryRef().getReference());
				customsOfficeRefExcelDTO
						.setDescription(entityRefLang.getDescription() != null ? entityRefLang.getDescription() : null);
				customsOfficeRefExcelDTO.setLabel(entityRefLang.getLabel() != null ? entityRefLang.getLabel() : null);
				customsOfficeRefExcelDTO
						.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null
								? entityRefLang.getLang().getCode()
								: null);
				customsOfficeRefExcelDTOs.add(customsOfficeRefExcelDTO);
			}
		}

		ByteArrayInputStream in = customsOfficeRefLangsToExcel(customsOfficeRefExcelDTOs);

		return in;

	}

	@Override
	public CustomsOfficeRefBean addInternationalisation(CustomsOfficeRefLang customsOfficeRefLang) {
		CustomsOfficeRef customsOfficeRef = new CustomsOfficeRef();
		customsOfficeRef.setCode(customsOfficeRefLang.getCode());
		customsOfficeRef.setCountryRef(countryRefRepository.findByReference(customsOfficeRefLang.getCountryRef()));

		CustomsOfficeRef customsOfficeRefTemp = customsOfficeRefRepository.findByCode(customsOfficeRefLang.getCode());
		if (Objects.isNull(customsOfficeRefTemp)) {
			Long id = (customsOfficeRefRepository.save(customsOfficeRef)).getId();
			EntityRefLang entityRefLang = new EntityRefLang();
			entityRefLang.setCreatedOn(new Date());
			entityRefLang.setLabel(customsOfficeRefLang.getLabel());
			entityRefLang.setDescription(customsOfficeRefLang.getDescription());
			entityRefLang.setRefId(id);
			entityRefLang.setTableRef(TableRef.CUSTOMSOFFICE_REF);
			entityRefLang.setLang(langRepository.findByCode(customsOfficeRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);
		} else {
			Long id = customsOfficeRefTemp.getId();
			customsOfficeRef.setId(id);
			customsOfficeRefRepository.save(customsOfficeRef);
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
					TableRef.CUSTOMSOFFICE_REF, langRepository.findByCode(customsOfficeRefLang.getLang()).getId(), id);
			entityRefLang.setLabel(!Objects.isNull(customsOfficeRefLang) ? customsOfficeRefLang.getLabel() : "");
			entityRefLang
					.setDescription(!Objects.isNull(customsOfficeRefLang) ? customsOfficeRefLang.getDescription() : "");
			entityRefLangRepository.save(entityRefLang);
		}

		return customsOfficeRefMapper.entityToBean(customsOfficeRef);
	}

	@Override
	public CustomsOfficeRefLang findCustomsOffice(Long id, String lang) {
		CustomsOfficeRefLang customsOfficeRefLang = new CustomsOfficeRefLang();
		CustomsOfficeRef customsOfficeRef = customsOfficeRefRepository.findOneById(id);
		EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
				TableRef.CUSTOMSOFFICE_REF, langRepository.findByCode(lang).getId(),
				!Objects.isNull(customsOfficeRef) ? customsOfficeRef.getId() : 0);

		if (Objects.isNull(customsOfficeRef) || Objects.isNull(entityRefLang)) {
			throw new CustomsOfficeNotFoundException("CustomsOffice id: " + id);
		}

		customsOfficeRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
		customsOfficeRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
		customsOfficeRefLang.setLang(lang);
		customsOfficeRefLang.setCode(customsOfficeRef.getCode());
		customsOfficeRefLang.setCountryRef(customsOfficeRef.getCountryRef().getReference());

		return customsOfficeRefLang;
	}

	@Override
	public EntityRefLang addInternationalisation(EntityRefLang entityRefLang, String lang) {
		Optional<CustomsOfficeRef> customsOfficeRef = customsOfficeRefRepository.findById(entityRefLang.getRefId());
		if (customsOfficeRef.isPresent()) {
			EntityRefLang entityRefLangTemp = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
					TableRef.CUSTOMSOFFICE_REF, langRepository.findByCode(lang).getId(),
					!Objects.isNull(entityRefLang) ? entityRefLang.getRefId() : 0);
			if (Objects.isNull(entityRefLangTemp)) {
				entityRefLang.setTableRef(TableRef.CUSTOMSOFFICE_REF);
				entityRefLang.setLang(langRepository.findByCode(lang));
				return entityRefLangRepository.save(entityRefLang);
			} else {
				throw new TranslationFoundException("Une traduction existe déjà pour cette langue");
			}
		} else {
			throw new CustomsOfficeNotFoundException("Customs Office id: " + entityRefLang.getRefId());
		}
	}

	@Override
	public List<CustomsOfficeRef> saveAll(List<CustomsOfficeRef> customsOfficeRefs) {
		return !customsOfficeRefs.isEmpty() ? customsOfficeRefRepository.saveAll(customsOfficeRefs) : null;
	}

	@Override
	public ErrorResponse delete(Long id, String lang) {
		try {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
					TableRef.CUSTOMSOFFICE_REF, langRepository.findByCode(lang).getId(), !Objects.isNull(id) ? id : 0);
			if (!Objects.isNull(entityRefLang)) {
				entityRefLangRepository.deleteById(entityRefLang.getId());
				return new ErrorResponse(HttpStatus.OK, ErrorMessageType.DELETE_SUCESS.getMessagePattern(), null);
			}

			return new ErrorResponse(HttpStatus.NOT_FOUND, ErrorMessageType.RECORD_NOT_FOUND.getMessagePattern(), null);

		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse(HttpStatus.CONFLICT, ErrorMessageType.DELETE_ERROR.getMessagePattern(), null);
		}

	}

	private CustomsOfficeRefBean toCustomsOfficeRefBeanMapper(CustomsOfficeRef customsOfficeRef) {
		CustomsOfficeRefBean customsOfficeRefBean = new CustomsOfficeRefBean();
		customsOfficeRefBean.setId(customsOfficeRef.getId());
		customsOfficeRefBean.setCreatedOn(customsOfficeRef.getCreatedOn());
		customsOfficeRefBean.setCode(customsOfficeRef.getCode());
		customsOfficeRefBean.setLabel(customsOfficeRef.getLabel());
		customsOfficeRefBean.setDescription(customsOfficeRef.getDescription());
		customsOfficeRefBean.setLang(customsOfficeRef.getLang());
		customsOfficeRefBean.setCountryRef(toCountryRefBeanMapper(customsOfficeRef.getCountryRef()));

		return customsOfficeRefBean;
	}

	private CountryRefBean toCountryRefBeanMapper(CountryRef countryRef) {
		CountryRefBean countryRefBean = new CountryRefBean();
		countryRefBean.setId(countryRef.getId());
		countryRefBean.setCreatedOn(countryRef.getCreatedOn());
		countryRefBean.setUpdatedOn(countryRef.getUpdatedOn());
		countryRefBean.setReference(countryRef.getReference());
		countryRefBean.setCodeIso(countryRef.getCodeIso());

		return countryRefBean;
	}

	public Page<CustomsOfficeRefLang> mapCustomsOfficesToRefLangs(Page<CustomsOfficeRef> customsOfficeRefs,
			String codeLang) {
		Lang lang = langRepository.findByCode(codeLang);
		List<CustomsOfficeRefLang> agreementTypeLangs = new ArrayList<>();

		return customsOfficeRefs.map(customsOfficeRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
					TableRef.CUSTOMSOFFICE_REF, lang.getId(), customsOfficeRef.getId());

			CustomsOfficeRefLang agreementTypeLang = new CustomsOfficeRefLang();

			agreementTypeLang.setId(customsOfficeRef.getId());
			agreementTypeLang.setCode(customsOfficeRef.getCode() != null ? customsOfficeRef.getCode() : "");
			agreementTypeLang.setCountryRef(
					customsOfficeRef.getCountryRef() != null ? customsOfficeRef.getCountryRef().getReference() : "");

			agreementTypeLang.setCreatedBy(customsOfficeRef.getCreatedBy());
			agreementTypeLang.setCreatedOn(customsOfficeRef.getCreatedOn());
			agreementTypeLang.setUpdatedBy(customsOfficeRef.getUpdatedBy());
			agreementTypeLang.setUpdatedOn(customsOfficeRef.getUpdatedOn());

			agreementTypeLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
			agreementTypeLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");

			agreementTypeLang.setLang(codeLang);

			agreementTypeLangs.add(agreementTypeLang);

			return agreementTypeLang;
		});
	}

	public Page<CustomsOfficeRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
		Page<CustomsOfficeRef> customsOfficeRef = customsOfficeRefRepository.findBycode(value, pageable);
		if (customsOfficeRef != null) {
			return mapCustomsOfficesToRefLangs(customsOfficeRef, codeLang);
		}
		return mapCustomsOfficesToRefLangs(customsOfficeRefRepository.filterByCodeOrLabel(value,
				langRepository.findByCode(codeLang).getId(), pageable), codeLang);
	}

	@Override
	public CustomsOfficeRefBean findById(Long id) {
		CustomsOfficeRef customsOfficeRef = customsOfficeRefRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Custom with this id not found"));
		return customsOfficeRefMapper.entityToBean(customsOfficeRef);
	}

	@Override
	public Page<CustomsOfficeRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable,
			String lang) {
		return customsOfficeRefRepository.filterByCodeOrLabelProjection(value.toLowerCase(),
				langRepository.findByCode(lang).getId(), pageable);
	}

	@Override
	public CustomsOfficeRef updateItem(Long id, CustomsOfficeRefLang customsOfficeRefLang) {
		CustomsOfficeRef customsOfficeRef = customsOfficeRefRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Custom with this id not found"));
		customsOfficeRef.setCode(
				customsOfficeRefLang.getCode() != null ? customsOfficeRefLang.getCode() : customsOfficeRef.getCode());
		customsOfficeRef.setCountryRef(countryRefRepository.findByReference(customsOfficeRefLang.getCountryRef()));
		customsOfficeRef = customsOfficeRefRepository.save(customsOfficeRef);
		return customsOfficeRef;
	}
}
