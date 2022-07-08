package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.PortRefBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.PortRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.PortRefLangProjection;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.PortRefRepository;
import ma.itroad.aace.eth.coref.service.IPortRefService;
import ma.itroad.aace.eth.coref.service.helper.CustomsRegimRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.PortRefLang;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Service
public class PortRefServiceImpl extends BaseServiceImpl<PortRef, PortRefBean> implements IPortRefService {

	static String[] HEADERs = { "CODE DU PORRT", "REFERENCE DU PAYS",  "LABEL","DESCRIPTION", "LANGUE" };
	static String SHEET = "PortSHEET";

	@Autowired
	Validator validator;

	@Autowired
	private PortRefRepository portRefRepository;

	@Autowired
	private PortRefMapper portRefMapper;

	@Autowired
	private LangRepository langRepository;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

	@Autowired
	private CountryRefRepository countryRefRepository;

	@Autowired
	private InternationalizationHelper internationalizationHelper;

	@Override
	public List<PortRefLang> excelToPortRef(InputStream is) {
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);

			Iterator<Row> rows = sheet.iterator();
			List<PortRefLang> portRefLangs = new ArrayList<PortRefLang>();
			int rowNumber = 0;
			while (rows.hasNext()) {
				Row currentRow = rows.next();
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}
				Iterator<Cell> cellsInRow = currentRow.iterator();
				PortRefLang portRefLang = new PortRefLang();
				for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++)
					for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {

						Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
						String cellValue = null;
						switch (colNum) {
						case 0:
							portRefLang.setCode(Util.cellValue(currentCell));
							break;
						/*case 1:
							if (currentCell.getStringCellValue().isEmpty())
								break;

							CountryRef ountryRef = countryRefRepository
									.findByReference(currentCell.getStringCellValue());

							portRefLang.setCountryRef(ountryRef);
							break;*/
							case 1:
								portRefLang.setCountryRef(Util.cellValue(currentCell));
								break;
						case 2:
							portRefLang.setLabel(Util.cellValue(currentCell));
							break;
						case 3:
							portRefLang.setDescription(Util.cellValue(currentCell));
							break;
						case 4:
							portRefLang.setLang(Util.cellValue(currentCell));
							break;
						default:
							break;
						}
					}
				portRefLangs.add(portRefLang);
			}
			workbook.close();
			return portRefLangs;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}

	@Override
	public ByteArrayInputStream portRefToExcel(List<PortRefLang> portRefLangs) {
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
				if (!portRefLangs.isEmpty()) {

					for (PortRefLang portRefLang : portRefLangs) {
						Row row = sheet.createRow(rowIdx++);
						row.createCell(0).setCellValue(portRefLang.getCode());
						/*CountryRef countryRef = portRef.getCountryRef();
						if (countryRef != null) {
							row.createCell(1).setCellValue(countryRef.getReference());
						}*/
						row.createCell(1).setCellValue(portRefLang.getCountryRef());

						row.createCell(2).setCellValue(portRefLang.getLabel());
						row.createCell(3).setCellValue(portRefLang.getDescription());
						row.createCell(4).setCellValue(portRefLang.getLang());
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
	public Set<ConstraintViolation<PortRefLang>> validatePortRefLang(PortRefLang portRefLang) {

		Set<ConstraintViolation<PortRefLang>> violations = validator.validate(portRefLang);

		return violations;
	}
	
	@Override
	public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {
			List<PortRefLang> portRefLangs = excelToPortRef(file.getInputStream());
			List<PortRefLang> invalidPortRefLangs = new ArrayList<PortRefLang>();
			List<PortRefLang> validPortRefLangs = new ArrayList<PortRefLang>();

			int lenght = portRefLangs.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<PortRefLang>> violations = validatePortRefLang(
						portRefLangs.get(i));
				if (violations.isEmpty())

				{
					validPortRefLangs.add(portRefLangs.get(i));
				} else {
					invalidPortRefLangs.add(portRefLangs.get(i));
				}
			}

			if (!invalidPortRefLangs.isEmpty()) {

				ByteArrayInputStream out = portRefToExcel(invalidPortRefLangs);
				xls = new InputStreamResource(out);
			}

			

			if (!validPortRefLangs.isEmpty()) {
				for (PortRefLang l : validPortRefLangs) {
					PortRef portRef = new PortRef();
					//PortRefLang portRefLang = new PortRefLang();
					portRef.setCode(l.getCode());
					//chapterRef.setSectionRef(sectionRefRepository.findByCode(l.getSectionRef().getCode()));
					portRef.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
					PortRef ltemp = portRefRepository.findByCode(l.getCode());
					if (ltemp == null) {
						Long id = (portRefRepository.save(portRef)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.PORT_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					} else {
						Long id = ltemp.getId();
						portRef.setId(id);
						portRefRepository.save(portRef);
						EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF,langRepository.findByCode(l.getLang()).getId(),id);
						if(entityRefLang == null )
						{
							entityRefLang =new EntityRefLang() ;
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.PORT_REF);
							entityRefLang.setRefId(portRef.getId());
						}
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}

				}
			}
			if (!invalidPortRefLangs.isEmpty())
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);

			if (!validPortRefLangs.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(null);

			return ResponseEntity.status(HttpStatus.OK).body(null);


			
		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}
	}


	@Override
	public void saveFromExcel(MultipartFile file) {

		try {
			List<PortRefLang> portRefLangs = excelToPortRef(file.getInputStream());
			if (!portRefLangs.isEmpty()) {
				for (PortRefLang l : portRefLangs) {
					PortRef portRef = new PortRef();
					//PortRefLang portRefLang = new PortRefLang();
					portRef.setCode(l.getCode());
					//chapterRef.setSectionRef(sectionRefRepository.findByCode(l.getSectionRef().getCode()));
					portRef.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
					PortRef ltemp = portRefRepository.findByCode(l.getCode());
					if (ltemp == null) {
						Long id = (portRefRepository.save(portRef)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.PORT_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					} else {
						Long id = ltemp.getId();
						portRef.setId(id);
						portRefRepository.save(portRef);
						EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF,langRepository.findByCode(l.getLang()).getId(),id);
						if(entityRefLang == null )
						{
							entityRefLang =new EntityRefLang() ;
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.PORT_REF);
							entityRefLang.setRefId(portRef.getId());
						}
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}

				}
			}
		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}
	}

	/*
	@Override
	public void saveFromExcel(MultipartFile file) {

		try {
			List<PortRef> portRefs = excelToPortRef(file.getInputStream());
			if (!portRefs.isEmpty())
				portRefs.stream().forEach(l -> {
					PortRef ltemp = portRefRepository.findByCode(l.getCode());

					if (ltemp == null) {
//						portRefRepository.save(l);
						Long id = (portRefRepository.save(l)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setCreatedOn(new Date());
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.PORT_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					} else {
						l.setId(ltemp.getId());
						portRefRepository.save(l);
						
						EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF,
								langRepository.findByCode(l.getLang()).getId(), l.getId());
						
						if (Objects.isNull(entityRefLang)) {
							Long id = (portRefRepository.save(l)).getId();
							EntityRefLang entityRefLangTemp = new EntityRefLang();
							entityRefLang.setCreatedOn(new Date());
							entityRefLang.setLabel(l.getLabel());
							entityRefLang.setDescription(l.getDescription());
							entityRefLang.setRefId(id);
							entityRefLang.setTableRef(TableRef.PORT_REF);
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLangRepository.save(entityRefLangTemp);					
						}
					}

				});
		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}

	}

	 */
/*
	@Override
	public ByteArrayInputStream load() {
		List<PortRef> portRefs = portRefRepository.findAll();
		ByteArrayInputStream in = portRefToExcel(portRefs);
		return in;
	}
*/
	public Page<PortRefBean> getAll(Pageable pageable) {
		Page<PortRef> entities = portRefRepository.findAll(pageable);
		Page<PortRefBean> result = entities.map(portRefMapper::entityToBean);
		return result;

	}

	@Override
	public ByteArrayInputStream load() {
		List<PortRef> chapterRefs = portRefRepository.findAll();
		ByteArrayInputStream in = null;
		return in;
	}

	public Page<PortRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
		Page<PortRef> portRefs = null;
		if(orderDirection.equals("DESC")){
			portRefs = portRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}else if(orderDirection.equals("ASC")){
			portRefs = portRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}

		Lang lang = langRepository.findByCode(codeLang);
		List<PortRefLang> portRefLangs = new ArrayList<>();

		return portRefs.map(portRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF, lang.getId(), portRef.getId());

			PortRefLang portRefLang = new PortRefLang();

			portRefLang.setId(portRef.getId());
			portRefLang.setCode(portRef.getCode()!=null ?portRef.getCode():"");

			//chapterRefLang.setSectionRef(sectionRefMapper.entityToBean(chapterRef.getSectionRef()));
			portRefLang.setCountryRef(portRef.getCountryRef().getReference());
			//chapterRefLang.setSectionRef(chapterRef.getCode()!=null ?chapterRef.getCode():"");


			portRefLang.setCreatedBy(portRef.getCreatedBy());
			portRefLang.setCreatedOn(portRef.getCreatedOn());
			portRefLang.setUpdatedBy(portRef.getUpdatedBy());
			portRefLang.setUpdatedOn(portRef.getUpdatedOn());

			portRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			portRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			portRefLang.setLang(codeLang);

			portRefLangs.add(portRefLang);

			return  portRefLang ;
		});
	}
/*
	@Override
	public Page<PortRefBean> getAll(String lang, int page, int size) {
		Page<PortRef> entities = portRefRepository
				.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn")));
		
//		List<PortRef> portRefs = new ArrayList<>();
		List<PortRefBean> portRefBeans = new ArrayList<>();
		
		for (PortRef portRef : entities) {
			PortRefBean portRefBean = new PortRefBean();
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF,
					langRepository.findByCode(lang).getId(), portRef.getId());
			if (Objects.isNull(entityRefLang)) {
				portRef.setLabel(" ");
				portRef.setDescription(" ");
				portRef.setLang(lang);
			} else {
				portRef.setLabel(entityRefLang.getLabel());
				portRef.setDescription(entityRefLang.getDescription());
				portRef.setLang(entityRefLang.getLang().getCode());
			}
			
			portRefBean = toPortRefBeanMapper(portRef);
			portRefBeans.add(portRefBean);
		}
		
		Pageable paging = PageRequest.of(page, size);
		int start = Math.min((int)paging.getOffset(), portRefBeans.size());
		int end = Math.min((start + paging.getPageSize()), portRefBeans.size());

		Page<PortRefBean> portRefsBeans = new PageImpl<>(portRefBeans.subList(start, end), paging, portRefBeans.size());	
		
		return portRefsBeans;
	}


 */

	@Override
	public ErrorResponse delete(Long id) {
		ErrorResponse response = new ErrorResponse();
		try {
			List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.PORT_REF, id);
			for (EntityRefLang entityRefLang : entityRefLangs) {
				entityRefLangRepository.delete(entityRefLang);
			}
			portRefRepository.deleteById(id);
			response.setStatus(HttpStatus.OK);
			response.setErrorMsg("null");
		} catch (Exception e) {
			response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
			response.setStatus(HttpStatus.CONFLICT);
		}
		return response;
	}

	@Override
	public ErrorResponse deleteList(ListOfObject listOfObject) {
		ErrorResponse response = new ErrorResponse();

		try {
			for(Long id : listOfObject.getListOfObject()){
				delete(id);
			}

			response.setStatus(HttpStatus.OK);
			response.setErrorMsg("null");
		} catch (Exception e) {
			response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
			response.setStatus(HttpStatus.CONFLICT);
		}
		return response;
	}

	/*
	@Override
	public ByteArrayInputStream portRefLangsToExcel(List<PortRefExcelDTO> portRefExcelDTOs) {
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
				if (!portRefExcelDTOs.isEmpty()) {

					for (PortRefExcelDTO unitRefEntityRefLang : portRefExcelDTOs) {
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

	 */
/*
	@Override
	public ByteArrayInputStream load(String codeLang) {
		Lang lang = langRepository.findByCode(codeLang);
		List<PortRef> portRefs = portRefRepository.findAll();
		List<PortRefExcelDTO> portRefExcelDTOs = new ArrayList<PortRefExcelDTO>();

		for (PortRef p : portRefs) {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF,
					lang.getId(), p.getId());
			if (entityRefLang != null) {
				PortRefExcelDTO portRefExcelDTO = new PortRefExcelDTO();
				portRefExcelDTO.setCode(p.getCode());
				portRefExcelDTO.setCountryRef(p.getCountryRef().getReference());
				portRefExcelDTO.setDescription(entityRefLang.getDescription() != null ? entityRefLang.getDescription() : null);
				portRefExcelDTO.setLabel(entityRefLang.getLabel() != null ? entityRefLang.getLabel() : null);
				portRefExcelDTO.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null
						? entityRefLang.getLang().getCode()
						: null);
				portRefExcelDTOs.add(portRefExcelDTO);
			}
		}

		ByteArrayInputStream in = portRefLangsToExcel(portRefExcelDTOs);

		return in;

	}
*/
	@Override
	public ByteArrayInputStream load(String codeLang) {

		Lang lang = langRepository.findByCode(codeLang);
		List<PortRef> portRefs = portRefRepository.findAll();
		List<PortRefLang> portRefLangs = new ArrayList<>();

		for (PortRef portRef : portRefs) {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF, lang.getId(), portRef.getId());
			if (entityRefLang != null) {
				PortRefLang portRefLang = new PortRefLang();


				portRefLang.setCode(portRef.getCode());

				//chapterRefLang.setSectionRef((sectionRefMapper.entityToBean(chapterRef.getSectionRef()) ));

				portRefLang.setCountryRef(portRef.getCountryRef().getReference());

				portRefLang.setLabel(entityRefLang.getLabel());
				portRefLang.setDescription(entityRefLang.getDescription());
				portRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
				portRefLangs.add(portRefLang);
			}
		}
		ByteArrayInputStream in = portRefToExcel(portRefLangs);
		return in;

	}

	@Override
	public void addPort(PortRefLang portRefLang){
		PortRef portRef = new PortRef();

		portRef.setCode(portRefLang.getCode());

		//chapterRef.setSectionRef(sectionRefRepository.findByCode(chapterRefLang.getSectionRef().getCode()));

		portRef.setCountryRef(countryRefRepository.findByReference(portRefLang.getCountryRef()));

		PortRef ltemp = portRefRepository.findByCode(portRefLang.getCode());

		if (ltemp == null) {
			Long id = (portRefRepository.save(portRef)).getId();
			EntityRefLang entityRefLang = new EntityRefLang();

			entityRefLang.setLabel(portRefLang.getLabel());
			entityRefLang.setDescription(portRefLang.getDescription());
			entityRefLang.setRefId(id);
			entityRefLang.setTableRef(TableRef.PORT_REF);
			entityRefLang.setLang(langRepository.findByCode(portRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);

		} else {
			Long id = ltemp.getId();
			portRef.setId(id);
			portRefRepository.save(portRef);
			EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF, langRepository.findByCode(portRefLang.getLang()).getId() ,id);
			entityRefLang.setLabel(portRefLang.getLabel());
			entityRefLang.setDescription(portRefLang.getDescription());
			entityRefLang.setLang(  langRepository.findByCode(portRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);
		}
	}

	@Override
	public List<InternationalizationVM> getInternationalizationRefList(Long id) {
		return internationalizationHelper.getInternationalizationRefList(id, TableRef.PORT_REF);
	}

	@Override
	public PortRefLang findPort(Long id, String lang){
		PortRefLang portRefLang = new PortRefLang();
		PortRef portRef = portRefRepository.findOneById(id);
		EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF,langRepository.findByCode(lang).getId(),portRef.getId());
		portRefLang.setCode(portRef.getCode());
		//chapterRefLang.setSectionRef(sectionRefMapper.entityToBean(chapterRef.getSectionRef()));
		portRefLang.setCountryRef(portRef.getCountryRef().getReference());
		portRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
		portRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
		portRefLang.setLang(lang);
		return portRefLang;
	}

	@Override
	public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
		entityRefLang.setTableRef(TableRef.PORT_REF);
		entityRefLang.setLang(langRepository.findByCode(lang));
		if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
			entityRefLangRepository.save(entityRefLang);
		else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
	}

	@Override
	public PortRefBean findById(Long id) {
		PortRef result= portRefRepository.findById(id).orElseThrow(
				()->new NotFoundException("id not found"));
		return portRefMapper.entityToBean(result);
	}

	@Override
	public Page<PortRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
		return portRefRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
	}

	@Override
	public ErrorResponse deleteInternationalisation(String codeLang, Long portRefId) {

		ErrorResponse response = new ErrorResponse();
		try {

			Lang lang = langRepository.findByCode(codeLang);

			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF, lang.getId(), portRefId);

			entityRefLangRepository.delete(entityRefLang);
			response.setStatus(HttpStatus.OK);
			response.setErrorMsg("null");


		} catch (Exception e) {
			response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
			response.setStatus(HttpStatus.CONFLICT);
		}
		return response;
	}

	@Override
	public List<PortRef> saveAll(List<PortRef> portRefs) {
		if (!portRefs.isEmpty()) {
			return portRefRepository.saveAll(portRefs);
		}
		return null;
	}

	public Page<PortRefLang> mapLangToRefLangs(Page<PortRef> portRefs, String codeLang) {
		List<PortRefLang> customsRegimRefEntityRefLangs = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);
		return portRefs.map(portRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.PORT_REF, lang.getId(), portRef.getId());
			PortRefLang portRefLangEntityRefLang = new PortRefLang();
			portRefLangEntityRefLang.setId(portRef.getId());

			portRefLangEntityRefLang.setCode(portRef.getCode() != null ? portRef.getCode() : "");
			portRefLangEntityRefLang.setCountryRef(portRef.getCountryRef().getReference());

			portRefLangEntityRefLang.setCreatedBy(portRef.getCreatedBy());
			portRefLangEntityRefLang.setCreatedOn(portRef.getCreatedOn());
			portRefLangEntityRefLang.setUpdatedBy(portRef.getUpdatedBy());
			portRefLangEntityRefLang.setUpdatedOn(portRef.getUpdatedOn());
			portRefLangEntityRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
			portRefLangEntityRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
			portRefLangEntityRefLang.setLang(codeLang);
			customsRegimRefEntityRefLangs.add(portRefLangEntityRefLang);
			return portRefLangEntityRefLang;
		});
	}

	public Page<PortRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
		return mapLangToRefLangs(portRefRepository.filterByCodeOrLabel(value,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
	}
	

}
