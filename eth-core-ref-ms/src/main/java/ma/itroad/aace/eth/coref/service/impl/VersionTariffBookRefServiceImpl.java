package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.exception.TarifBookNotFoundException;
import ma.itroad.aace.eth.coref.exception.TranslationFoundException;
import ma.itroad.aace.eth.coref.exception.VersionTarifBookNotFoundException;
import ma.itroad.aace.eth.coref.model.bean.VersionTariffBookBean;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.entity.TarifBookRef;
import ma.itroad.aace.eth.coref.model.entity.VersionRef;
import ma.itroad.aace.eth.coref.model.entity.VersionTariffBookRef;
import ma.itroad.aace.eth.coref.model.mapper.VersionTariffBookMapper;
import ma.itroad.aace.eth.coref.model.view.MSPTariffBookRefVM;
import ma.itroad.aace.eth.coref.model.view.VersionTariffBookRefVM;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.TarifBookRefRepository;
import ma.itroad.aace.eth.coref.repository.VersionRefRepository;
import ma.itroad.aace.eth.coref.repository.VersionTariffBookRefRepository;
import ma.itroad.aace.eth.coref.service.IVersionTariffBookRefService;
import ma.itroad.aace.eth.coref.service.helper.ExcelCellFormatter;
import ma.itroad.aace.eth.coref.service.helper.PageHelper;
import ma.itroad.aace.eth.coref.service.helper.VersionTarrifBookRefLang;

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
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Service
public class VersionTariffBookRefServiceImpl extends BaseServiceImpl<VersionTariffBookRef, VersionTariffBookBean>
		implements IVersionTariffBookRefService {

	private static final String SHEET = "VersionTariffBookRefsSHEET";
	private static final String[] HEADER = { "TariffBookRef reference", "VersionRef reference",
			"countryRef reference", "Label", "Description", "Lang" };

	@Autowired
	Validator validator;
	
	@Autowired
	VersionTariffBookRefRepository repository;

	@Autowired
	CountryRefRepository countryRefRepository;

	@Autowired
	VersionRefRepository versionRefRepository;

	@Autowired
	TarifBookRefRepository tarifBookRefRepository;

	@Autowired
	VersionTariffBookMapper versionTariffBookMapper;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;
	@Autowired
	private LangRepository langRepository;

	
	@Override
	public Set<ConstraintViolation<VersionTariffBookRefVM>> validateVersionTariffBookRefVM(VersionTariffBookRefVM versionTariffBookRefVM) {

		Set<ConstraintViolation<VersionTariffBookRefVM>> violations = validator.validate(versionTariffBookRefVM);

		return violations;
	}
	
	@Override
	public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {

			
			Collection<VersionTariffBookRefVM> versionTariffBookRefVM = excelToElementsRefs(file.getInputStream());
			List<VersionTariffBookRefVM> versionTariffBookRefVMs = versionTariffBookRefVM.stream().collect(Collectors.toList());
			List<VersionTarrifBookRefLang> invalidVersionTariffBookRefVM = new ArrayList<VersionTarrifBookRefLang>();
			List<VersionTariffBookRefVM> validVersionTariffBookRefVM = new ArrayList<VersionTariffBookRefVM>();

			int lenght = versionTariffBookRefVM.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<VersionTariffBookRefVM>> violations = validateVersionTariffBookRefVM(
						versionTariffBookRefVMs.get(i));
				if (violations.isEmpty())

				{
					validVersionTariffBookRefVM.add(versionTariffBookRefVMs.get(i));
				} else {
					
					VersionTarrifBookRefLang versionTarrifBookRefLang= new VersionTarrifBookRefLang();
					versionTarrifBookRefLang.setTarifBookReference(versionTariffBookRefVMs.get(i).getTarifBookReference());
					versionTarrifBookRefLang.setVersionRefReference(versionTariffBookRefVMs.get(i).getVersionRefReference());
					versionTarrifBookRefLang.setCountryRefReference(versionTariffBookRefVMs.get(i).getCountryRefReference());
					invalidVersionTariffBookRefVM.add(versionTarrifBookRefLang);
				}
				
				if (!invalidVersionTariffBookRefVM.isEmpty()) {

					ByteArrayInputStream out = versionTarifBookToExcel(invalidVersionTariffBookRefVM);
					xls = new InputStreamResource(out);
				}
			
			if (!validVersionTariffBookRefVM.isEmpty())
				validVersionTariffBookRefVM.stream().forEach(item -> {
					this.save(item);
				});
		} 
			
			if (!invalidVersionTariffBookRefVM.isEmpty())
				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);

			if (!validVersionTariffBookRefVM.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(null);

			return ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
		
			catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}
	}

	
	@Override
	public void saveFromExcel(MultipartFile file) {

		try {
			Set<VersionTariffBookRefVM> versionTariffBookRefVMS = (Set<VersionTariffBookRefVM>) excelToElementsRefs(
					file.getInputStream());
			if (!versionTariffBookRefVMS.isEmpty())
				versionTariffBookRefVMS.stream().forEach(item -> {
					this.save(item);
				});
		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}
	}

	@Override
	public ByteArrayInputStream load() {

		List<VersionTariffBookRefVM> list = getListOfVersionTariffBookRefVM();
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

					for (VersionTariffBookRefVM versionTariffBookRefVM : list) {
						Row row = sheet.createRow(rowIdx++);
						row.createCell(0).setCellValue(versionTariffBookRefVM.getTarifBookReference());
						row.createCell(1).setCellValue(versionTariffBookRefVM.getVersionRefReference());
						row.createCell(2).setCellValue(versionTariffBookRefVM.getCountryRefReference());
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
/*
	@Override
	public Page<VersionTariffBookBean> getAll(int page, int size) {

		Page<VersionTariffBookRef> entities = repository
				.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn")));
		Page<VersionTariffBookBean> result = entities.map(versionTariffBookMapper::entityToBean);
		return result;
	}*/
/*
	@Override
	public Page<VersionTariffBookBean> getAll(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<VersionTariffBookRef> taxPage = repository.findAll(pageable);

		Page<VersionTariffBookBean> taxationBeanPage = taxPage.map(version -> {
			return versionTariffBookMapper.entityToBean(version);
		});
		return taxationBeanPage;
	}
	*/
	@Override
	public Page<VersionTariffBookBean> getAll(String lang, int page, int size, String orderDirection) {
		Page<VersionTariffBookRef> entities = null;
		if(orderDirection.equals("DESC")){
			entities = repository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}else if(orderDirection.equals("ASC")){
			entities = repository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}

		List<VersionTariffBookBean> beans = new ArrayList<>();
		
		for (VersionTariffBookRef ref : entities) {
			VersionTariffBookBean versionTariffBookBean = new VersionTariffBookBean();
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_TARIFF_BOOK,
					langRepository.findByCode(lang).getId(), ref.getId());
			if (Objects.isNull(entityRefLang)) {
				ref.setLabel(" ");
				ref.setDescription(" ");
				ref.setLang(lang);
			} else {
				ref.setLabel(entityRefLang.getLabel());
				ref.setDescription(entityRefLang.getDescription());
				ref.setLang(entityRefLang.getLang().getCode());
			}
			
			versionTariffBookBean = toVersionTariffBookMapper(ref);
			beans.add(versionTariffBookBean);
		}
		
		Pageable paging = PageRequest.of(page, size);
		int start = Math.min((int)paging.getOffset(), beans.size());
		int end = Math.min((start + paging.getPageSize()), beans.size());

		return  new PageImpl<>(beans.subList(start, end), paging, beans.size());	
	}

	/*
	@Override
	public ErrorResponse delete(Long id, String lang) {
		try {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_TARIFF_BOOK,
					langRepository.findByCode(lang).getId(), !Objects.isNull(id) ? id : 0);
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
*/
	
	private VersionTariffBookBean toVersionTariffBookMapper(VersionTariffBookRef ref) {
		VersionTariffBookBean out = new VersionTariffBookBean();
		out.setId(ref.getId());
		out.setVersion_nbr(ref.getVersion_nbr());
		//todo Finish this fields later
		out.setCreatedOn(ref.getCreatedOn());
		out.setLabel(ref.getLabel());
		out.setDescription(ref.getDescription());
		out.setLang(ref.getLang());
		return out;
	}



	public boolean isCellValid(String s) {
		return s != null && s.matches("^[ a-zA-Z0-9]*$");
	}

	@Override
	public ByteArrayInputStream load(String codeLang, final int page, final int size) {
		Lang lang = langRepository.findByCode(codeLang);
		Page<VersionTariffBookRef> versionTariffBookRefs = repository.findAll(PageRequest.of(page, size));
		List<VersionTarrifBookRefLang> versionTarrifBookRefLangs = new ArrayList<VersionTarrifBookRefLang>();

		for (VersionTariffBookRef u : versionTariffBookRefs) {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_TARIFF_BOOK,
					lang.getId(), u.getId());
			if (entityRefLang != null) {
				VersionTarrifBookRefLang versionTarrifBookRefLang = new VersionTarrifBookRefLang();
				versionTarrifBookRefLang.setTarifBookReference(
						(u.getTarifBookRef() != null && u.getTarifBookRef().getId() != null) ? u.getTarifBookRef().getId().toString() : null);
				versionTarrifBookRefLang
						.setVersionRefReference(u.getVersionRef() != null ? u.getVersionRef().getVersion() : null);
				versionTarrifBookRefLang
						.setCountryRefReference(u.getCountryRef() != null ? u.getCountryRef().getReference() : null);
				versionTarrifBookRefLang.setLabel(entityRefLang.getLabel());
				versionTarrifBookRefLang.setDescription(entityRefLang.getDescription());
				versionTarrifBookRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null
						? entityRefLang.getLang().getCode()
						: null);
				versionTarrifBookRefLangs.add(versionTarrifBookRefLang);
			}
		}
		ByteArrayInputStream in = versionTarifBookToExcel(versionTarrifBookRefLangs);
		return in;
	}


	@Override
	public ByteArrayInputStream versionTarifBookToExcel(List<VersionTarrifBookRefLang> versionTarrifBookRefLang) {
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
				if (!versionTarrifBookRefLang.isEmpty()) {

					for (VersionTarrifBookRefLang countryRefEntityRefLang : versionTarrifBookRefLang) {
						Row row = sheet.createRow(rowIdx++);
						row.createCell(0).setCellValue(countryRefEntityRefLang.getTarifBookReference());
						row.createCell(1).setCellValue(countryRefEntityRefLang.getVersionRefReference());
						row.createCell(2).setCellValue(countryRefEntityRefLang.getCountryRefReference());
						row.createCell(3).setCellValue(countryRefEntityRefLang.getLabel());
						row.createCell(4).setCellValue(countryRefEntityRefLang.getDescription());
						row.createCell(5).setCellValue(countryRefEntityRefLang.getLang());

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
	public Collection<VersionTariffBookRefVM> excelToElementsRefs(InputStream is) {
		Set<VersionTariffBookRefVM> versionTariffBookRefVMS;
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);
			Iterator<Row> rows = sheet.iterator();
			versionTariffBookRefVMS = new HashSet<>();

			int rowNumber = 0;
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}

				Iterator<Cell> cellsInRow = currentRow.iterator();

				VersionTariffBookRefVM entity = new VersionTariffBookRefVM();

				int cellIdx = 0;
				while (cellsInRow.hasNext()) {
					Cell currentCell = cellsInRow.next();
					int type = currentCell.getCellType();
					String cellValue = ExcelCellFormatter.getCellStringValue(currentCell);
					boolean isCellValid = isCellValid(cellValue);
					if (isCellValid) {
						switch (cellIdx) {
						case 0:
							entity.setTarifBookReference(cellValue);
							break;
						case 1:
							entity.setVersionRefReference(cellValue);
							break;
						case 2:
							entity.setCountryRefReference(cellValue);
							break;
						case 3:
							entity.setVersionLabel((currentCell.getStringCellValue()));
							break;
						case 4:
							entity.setVersionDescription(currentCell.getStringCellValue());
							break;
						case 5:
							entity.setLang(currentCell.getStringCellValue());
							break;
						default:
							break;
						}
						cellIdx++;
					}

				}
				versionTariffBookRefVMS.add(entity);
			}
			workbook.close();

		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
		return versionTariffBookRefVMS;
	}

	private VersionTariffBookRefVM convertToVersionTariffBookRefVM(VersionTariffBookRef entity) {
		final VersionTariffBookRefVM item = new VersionTariffBookRefVM();
		if (entity != null) {
			item.setTarifBookReference(
					entity.getTarifBookRef() != null ? entity.getTarifBookRef().getReference() : null);
			item.setTarifBookId(entity.getTarifBookRef().getId() != null ? entity.getTarifBookRef().getId() : null);
			item.setVersionRefReference(entity.getVersionRef() != null ? entity.getVersionRef().getVersion() : null);
			item.setVersionRefId(entity.getVersionRef().getId() != null ? entity.getVersionRef().getId() : null);
			item.setCountryRefReference(entity.getCountryRef() != null ? entity.getCountryRef().getReference() : null);
			item.setCountryRefId(entity.getCountryRef().getId() != null ? entity.getCountryRef().getId() : null);
			item.setVersionLabel(entity.getLabel());
			item.setVersionDescription(entity.getDescription());
			item.setLang(entity.getLang());
			item.setId(entity.getId());
		}

		return item;
	}

	@Override
	public Page<VersionTariffBookRefVM> getAll(int page, int size) {
//		List<VersionTariffBookRefVM> list = getListOfVersionTariffBookRefVM();
//		return PageHelper.listConvertToPage(list, list.size(), PageRequest.of(page, size));
		return getListOfVersionTariffBookRefVM( page, size);
	}

	private List<VersionTariffBookRefVM> getListOfVersionTariffBookRefVM() {
		List<VersionTariffBookRef> list = repository.findAll();
		List<VersionTariffBookRefVM> result = list.stream().map(this::convertToVersionTariffBookRefVM)
				.collect(Collectors.toList());
		return result;
	}

	private Page<VersionTariffBookRefVM> getListOfVersionTariffBookRefVM(int page,int size) {
		Page<VersionTariffBookRef> list = repository.findAll(PageRequest.of(page, size));
		return list.map(this::convertToVersionTariffBookRefVM);
	}




	private VersionTariffBookBean save(VersionTariffBookRefVM model) {

		CountryRef countryRef = countryRefRepository.findByReference(model.getCountryRefReference());
		VersionRef versionRef = versionRefRepository.findByVersion(model.getVersionRefReference());

		TarifBookRef tarifBookRef = tarifBookRefRepository.findByReference(model.getTarifBookReference())
				/*.orElse(null)*/;

		repository.findByTarifBookRefAndAndCountryRefAndVersionRef(tarifBookRef, countryRef, versionRef);
		if (countryRef != null && versionRef != null && tarifBookRef != null && repository
				.findByTarifBookRefAndAndCountryRefAndVersionRef(tarifBookRef, countryRef, versionRef) == null) {

			VersionTariffBookRef entity = new VersionTariffBookRef();
			entity.setCountryRef(countryRef);
			entity.setVersionRef(versionRef);
			entity.setTarifBookRef(tarifBookRef);
			
			EntityRefLang entityRefLang = new EntityRefLang();
			entityRefLang.setCreatedOn(new Date());
			entityRefLang.setLabel(model.getVersionLabel());
			entityRefLang.setDescription(model.getVersionDescription());
			entityRefLang.setRefId(model.getVersionRefId());
			entityRefLang.setTableRef(TableRef.VERSION_TARIFF_BOOK);
			entityRefLang.setLang(langRepository.findByCode(model.getLang()));
			entityRefLangRepository.save(entityRefLang);

			repository.save(entity);
			return versionTariffBookMapper.entityToBean(entity);
		}
		return null;

	}

	public VersionTariffBookBean add(VersionTariffBookRefVM model) {

		CountryRef countryRef = countryRefRepository.findById(model.getCountryRefId())
				.orElseThrow(()->new NotFoundException("Country With This id Not Found"));

		VersionRef versionRef = versionRefRepository.findById(model.getVersionRefId())
					.orElseThrow(()->new NotFoundException("Version With This id Not Found"));
		TarifBookRef tarifBookRef = tarifBookRefRepository.findById(model.getTarifBookId())
				.orElseThrow(()->new NotFoundException("tarifBookRef With This id Not Found"));

		VersionTariffBookRef versionTariffBookRef = repository.findByTarifBookRefAndAndCountryRefAndVersionRef(tarifBookRef, countryRef, versionRef);
		if (countryRef != null && versionRef != null && tarifBookRef != null && versionTariffBookRef == null) {

			VersionTariffBookRef entity = new VersionTariffBookRef();
			entity.setCountryRef(countryRef);
			entity.setVersionRef(versionRef);
			entity.setTarifBookRef(tarifBookRef);

			EntityRefLang entityRefLang = new EntityRefLang();
			entityRefLang.setCreatedOn(new Date());
			entityRefLang.setLabel(model.getVersionLabel());
			entityRefLang.setDescription(model.getVersionDescription());
			entityRefLang.setRefId(model.getVersionRefId());
			entityRefLang.setTableRef(TableRef.VERSION_TARIFF_BOOK);
			entityRefLang.setLang(langRepository.findByCode(model.getLang()));
			entityRefLangRepository.save(entityRefLang);

			repository.save(entity);
			return versionTariffBookMapper.entityToBean(entity);
		}
		return null;

	}

	@Override
	public VersionTariffBookBean findById(Long id) {
		VersionTariffBookRef result= repository.findById(id).orElseThrow(
				()->new NotFoundException("id not found"));
		return versionTariffBookMapper.entityToBean(result);
	}

	@Override
	public ErrorResponse delete(Long id) {
		ErrorResponse response = new ErrorResponse();
		try {
			repository.deleteById(id);
			response.setStatus(HttpStatus.OK);
			response.setErrorMsg("null");
		} catch (Exception e) {
			response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
			response.setStatus(HttpStatus.CONFLICT);
		}
		return response;
	}

	@Override
	public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
		Optional<VersionTariffBookRef> versionTariffBookRef = repository.findById(entityRefLang.getRefId());
		if (versionTariffBookRef.isPresent()) {			
			EntityRefLang entityRefLangTemp = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.VERSION_TARIFF_BOOK,
					langRepository.findByCode(lang).getId(), !Objects.isNull(entityRefLang) ? entityRefLang.getRefId() : 0);
			if (Objects.isNull(entityRefLangTemp)) {			
				entityRefLang.setTableRef(TableRef.VERSION_TARIFF_BOOK);
				entityRefLang.setLang(langRepository.findByCode(lang));
				entityRefLangRepository.save(entityRefLang);
			} else {
				throw new TranslationFoundException("Une traduction existe déjà pour cette langue");
			}
		} else {
			throw new VersionTarifBookNotFoundException("VersionTarifBook id: " + entityRefLang.getRefId());
		}
	}

	@Override
	public VersionTarrifBookRefLang findVersionTariffBook(Long id, String lang) {
		VersionTarrifBookRefLang versionTarrifBookRefLang = new VersionTarrifBookRefLang();
		VersionTariffBookRef versionTariffBookRef = repository.findOneById(id);
		EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
				TableRef.VERSION_TARIFF_BOOK, langRepository.findByCode(lang).getId(), !Objects.isNull(versionTariffBookRef) ? versionTariffBookRef.getId() : 0);
		
		if (Objects.isNull(versionTariffBookRef) || Objects.isNull(entityRefLang)) {
			throw new VersionTarifBookNotFoundException("VersionTarifBook id: " + id);
		}
		
		versionTarrifBookRefLang.setCountryRefReference(
				versionTariffBookRef.getCountryRef() != null ? versionTariffBookRef.getCountryRef().getReference()
						: null);
		versionTarrifBookRefLang.setTarifBookReference(versionTariffBookRef.getTarifBookRef().getId() != null
				? versionTariffBookRef.getTarifBookRef().getId().toString()
				: null);
		versionTarrifBookRefLang.setVersionRefReference(
				versionTariffBookRef.getVersionRef() != null ? versionTariffBookRef.getVersionRef().getVersion()
						: null);
		versionTarrifBookRefLang.setLabel(entityRefLang.getLabel());
		versionTarrifBookRefLang.setDescription(entityRefLang.getDescription());
		versionTarrifBookRefLang.setLang(lang);
		
		return versionTarrifBookRefLang;
	}

	@Override
	public VersionTariffBookBean addInternationalisation(VersionTarrifBookRefLang versionTarrifBookRefLang) {
		VersionTariffBookRef versionTariffBookRef = new VersionTariffBookRef();
		CountryRef countryRef = countryRefRepository.findByReference(versionTarrifBookRefLang.getCountryRefReference());
		VersionRef versionRef = versionRefRepository.findByVersion(versionTarrifBookRefLang.getVersionRefReference());

		TarifBookRef tarifBookRef = tarifBookRefRepository.findByReference(versionTarrifBookRefLang.getTarifBookReference())/*.orElse(null)*/;
		versionTariffBookRef.setCreatedOn(new Date());
		versionTariffBookRef.setCountryRef(countryRef);
		versionTariffBookRef.setVersionRef(versionRef);
		versionTariffBookRef.setTarifBookRef(tarifBookRef);

		VersionTariffBookRef ltemp = repository.findByTarifBookRefAndAndCountryRefAndVersionRef(tarifBookRef,
				countryRef, versionRef);
		if (ltemp == null) {
			Long id = (repository.save(versionTariffBookRef)).getId();
			EntityRefLang entityRefLang = new EntityRefLang();
			entityRefLang.setLabel(versionTarrifBookRefLang.getLabel());
			entityRefLang.setDescription(versionTarrifBookRefLang.getDescription());
			entityRefLang.setRefId(id);
			entityRefLang.setTableRef(TableRef.VERSION_TARIFF_BOOK);
			entityRefLang.setLang(langRepository.findByCode(versionTarrifBookRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);
		} else {
			Long id = ltemp.getId();
			versionTariffBookRef.setId(id);
			repository.save(versionTariffBookRef);
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
					TableRef.VERSION_TARIFF_BOOK, langRepository.findByCode(versionTarrifBookRefLang.getLang()).getId(),
					id);
			
			if (Objects.isNull(entityRefLang)) {
				EntityRefLang newEntityRefLang = new EntityRefLang();
				newEntityRefLang.setCreatedOn(new Date());
				entityRefLang.setLabel(versionTarrifBookRefLang.getLabel());
				entityRefLang.setDescription(versionTarrifBookRefLang.getDescription());
				newEntityRefLang.setRefId(id);
				newEntityRefLang.setTableRef(TableRef.VERSION_TARIFF_BOOK);
				entityRefLangRepository.save(entityRefLang);				
			}
		}
		
		return versionTariffBookMapper.entityToBean(versionTariffBookRef);
	}


}