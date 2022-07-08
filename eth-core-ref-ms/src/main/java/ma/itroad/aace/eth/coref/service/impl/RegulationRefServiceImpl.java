package ma.itroad.aace.eth.coref.service.impl;

import edu.emory.mathcs.backport.java.util.Arrays;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.RegulationRefBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.RegulationRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.RegulationRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.*;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.RegulationRefRepository;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.service.ICountryRefService;
import ma.itroad.aace.eth.coref.service.ICustomsRegimRefService;
import ma.itroad.aace.eth.coref.service.IRegulationRefService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.service.helper.*;
import ma.itroad.aace.eth.coref.service.helper.detailed.AgreementLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.detailed.RegulationRefLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.mappers.HelperMapper;
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
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Service
public class RegulationRefServiceImpl extends BaseServiceImpl<RegulationRef, RegulationRefBean>
		implements IRegulationRefService {

	static String[] HEADERs = { "CODE DU REGULATION", "REFERENCE DU PAYS", "LABEL", "DESCRIPTION", "LANGUE" };
	static String SHEET = "RegulationSHEET";

	@Autowired
	private Validator validator;

	@Autowired
	private RegulationRefRepository regulationRefRepository;

	@Autowired
	private RegulationRefMapper regulationRefMapper;

	@Autowired
	private LangRepository langRepository;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

	@Autowired
	private CountryRefRepository countryRefRepository;

	@Autowired
	private InternationalizationHelper internationalizationHelper;

	@Autowired
	private HelperMapper helperMapper;

	@Autowired
	ICustomsRegimRefService iCustomsRegimRefService;

	@Autowired
	ICountryRefService iCountryRefService;

	@Override
	public List<RegulationRefLang> excelToRegulationRef(InputStream is) {
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);

			Iterator<Row> rows = sheet.iterator();
			List<RegulationRefLang> regulationRefLangs = new ArrayList<RegulationRefLang>();
			int rowNumber = 0;
			while (rows.hasNext()) {
				Row currentRow = rows.next();
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}
				Iterator<Cell> cellsInRow = currentRow.iterator();
				RegulationRefLang regulationRefLang = new RegulationRefLang();
				for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++)
					for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {

						Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
						String cellValue = null;
						switch (colNum) {
						case 0:
							regulationRefLang.setCode(Util.cellValue(currentCell));
							break;
						case 1:
							regulationRefLang.setCountryRef(Util.cellValue(currentCell));
							break;
						case 2:
							regulationRefLang.setLabel(Util.cellValue(currentCell));
							break;
						case 3:
							regulationRefLang.setDescription(Util.cellValue(currentCell));
							break;
						case 4:
							regulationRefLang.setLang(Util.cellValue(currentCell));
							break;
						default:
							break;
						}
					}
				regulationRefLangs.add(regulationRefLang);
			}
			workbook.close();
			return regulationRefLangs;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}

	@Override
	public ByteArrayInputStream regulationRefToExcel(List<RegulationRefLang> regulationRefLangs) {
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
				if (!regulationRefLangs.isEmpty()) {

					for (RegulationRefLang regulationRefLang : regulationRefLangs) {
						Row row = sheet.createRow(rowIdx++);
						row.createCell(0).setCellValue(regulationRefLang.getCode());
						row.createCell(1).setCellValue(regulationRefLang.getCountryRef());
						row.createCell(2).setCellValue(regulationRefLang.getLabel());
						row.createCell(3).setCellValue(regulationRefLang.getDescription());
						row.createCell(4).setCellValue(regulationRefLang.getLang());
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
	public Set<ConstraintViolation<RegulationRefLang>> validateRegulation(RegulationRefLang regulationRefLang) {

		Set<ConstraintViolation<RegulationRefLang>> violations = validator.validate(regulationRefLang);

		return violations;
	}

	@Override
	public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {
			List<RegulationRefLang> regulationRefLangs = excelToRegulationRef(file.getInputStream());
			List<RegulationRefLang> invalidRegulationRefLang = new ArrayList<RegulationRefLang>();
			List<RegulationRefLang> validRegulationRefLang = new ArrayList<RegulationRefLang>();

			int lenght = regulationRefLangs.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<RegulationRefLang>> violations = validateRegulation(regulationRefLangs.get(i));
				if (violations.isEmpty())

				{
					validRegulationRefLang.add(regulationRefLangs.get(i));
				} else {
					invalidRegulationRefLang.add(regulationRefLangs.get(i));
				}
			}

			if (!invalidRegulationRefLang.isEmpty()) {

				ByteArrayInputStream out = regulationRefToExcel(invalidRegulationRefLang);
				xls = new InputStreamResource(out);
			}

			if (!validRegulationRefLang.isEmpty()) {
				for (RegulationRefLang l : validRegulationRefLang) {
					RegulationRef regulationRef = new RegulationRef();
					regulationRef.setCode(l.getCode());
					regulationRef.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
					regulationRef.setGeneralDescription(l.getGeneralDescription());
					RegulationRef ltemp = regulationRefRepository.findByCode(l.getCode());
					if (ltemp == null) {
						Long id = (regulationRefRepository.save(regulationRef)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.REGULATION_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					} else {
						Long id = ltemp.getId();
						regulationRef.setId(id);
						regulationRefRepository.save(regulationRef);
						EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
								TableRef.REGULATION_REF, langRepository.findByCode(l.getLang()).getId(), id);
						if (entityRefLang == null) {
							entityRefLang = new EntityRefLang();
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.REGULATION_REF);
							entityRefLang.setRefId(regulationRef.getId());
						}
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}
				}
			}
			if (!invalidRegulationRefLang.isEmpty())
				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);

			if (!validRegulationRefLang.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(null);

			return ResponseEntity.status(HttpStatus.OK).body(null);

		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}

	}

	@Override
	public void saveFromExcel(MultipartFile file) {
		try {
			List<RegulationRefLang> regulationRefLang = excelToRegulationRef(file.getInputStream());
			if (!regulationRefLang.isEmpty()) {
				for (RegulationRefLang l : regulationRefLang) {
					RegulationRef regulationRef = new RegulationRef();
					regulationRef.setCode(l.getCode());
					regulationRef.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
					regulationRef.setGeneralDescription(l.getGeneralDescription());
					RegulationRef ltemp = regulationRefRepository.findByCode(l.getCode());
					if (ltemp == null) {
						Long id = (regulationRefRepository.save(regulationRef)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.REGULATION_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					} else {
						Long id = ltemp.getId();
						regulationRef.setId(id);
						regulationRefRepository.save(regulationRef);
						EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
								TableRef.REGULATION_REF, langRepository.findByCode(l.getLang()).getId(), id);
						if (entityRefLang == null) {
							entityRefLang = new EntityRefLang();
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.REGULATION_REF);
							entityRefLang.setRefId(regulationRef.getId());
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

	public Page<RegulationRefBean> getAll(Pageable pageable) {
		Page<RegulationRef> entities = regulationRefRepository.findAll(pageable);
		Page<RegulationRefBean> result = entities.map(regulationRefMapper::entityToBean);
		return result;
	}

	@Override
	public ByteArrayInputStream load() {
		List<RegulationRef> regulationRefs = regulationRefRepository.findAll();
		ByteArrayInputStream in = null;
		return in;
	}

	public Page<RegulationRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
		Page<RegulationRef> regulationRefs = null;
		if (orderDirection.equals("DESC")) {
			regulationRefs = regulationRefRepository
					.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		} else if (orderDirection.equals("ASC")) {
			regulationRefs = regulationRefRepository
					.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}

		Lang lang = langRepository.findByCode(codeLang);
		List<RegulationRefLang> regulationRefLangs = new ArrayList<>();

		return regulationRefs.map(regulationRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF, lang.getId(), regulationRef.getId());
			RegulationRefLang regulationRefLang = new RegulationRefLang();
			regulationRefLang.setId(regulationRef.getId());
			regulationRefLang.setCode(regulationRef.getCode() != null ? regulationRef.getCode() : "");
			regulationRefLang.setOrganization(
					regulationRef.getOrganization() != null ? regulationRef.getOrganization().getReference() : null);
			regulationRefLang.setCountryRef(
					regulationRef.getCountryRef() != null ? regulationRef.getCountryRef().getReference() : "");
			regulationRefLang.setCreatedBy(regulationRef.getCreatedBy());
			regulationRefLang.setCreatedOn(regulationRef.getCreatedOn());
			regulationRefLang.setUpdatedBy(regulationRef.getUpdatedBy());
			regulationRefLang.setUpdatedOn(regulationRef.getUpdatedOn());
			regulationRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
			regulationRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
			regulationRefLang.setLang(codeLang);
			regulationRefLangs.add(regulationRefLang);
			return regulationRefLang;
		});
	}

	@Override
	public ErrorResponse delete(Long id) {
		ErrorResponse response = new ErrorResponse();
		try {
			List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.REGULATION_REF,
					id);
			for (EntityRefLang entityRefLang : entityRefLangs) {
				entityRefLangRepository.delete(entityRefLang);
			}
			regulationRefRepository.deleteById(id);
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
			for (Long id : listOfObject.getListOfObject()) {
				List<EntityRefLang> entityRefLangs = entityRefLangRepository
						.findByTableRefAndRefId(TableRef.REGULATION_REF, id);
				for (EntityRefLang entityRefLang : entityRefLangs) {
					entityRefLangRepository.delete(entityRefLang);
				}
				regulationRefRepository.deleteById(id);
			}

			response.setStatus(HttpStatus.OK);
			response.setErrorMsg("null");
		} catch (Exception e) {
			response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
			response.setStatus(HttpStatus.CONFLICT);
		}
		return response;
	}

	@Override
	public ByteArrayInputStream load(String codeLang) {

		Lang lang = langRepository.findByCode(codeLang);
		List<RegulationRef> regulationRefs = regulationRefRepository.findAll();
		List<RegulationRefLang> regulationRefLangs = new ArrayList<>();

		for (RegulationRef regulationRef : regulationRefs) {
			EntityRefLang entityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF, lang.getId(), regulationRef.getId());
			if (entityRefLang != null) {
				RegulationRefLang regulationRefLang = new RegulationRefLang();
				regulationRefLang.setCode(regulationRef.getCode());
				regulationRefLang.setCountryRef(regulationRef.getCountryRef().getReference());
				regulationRefLang.setLabel(entityRefLang.getLabel());
				regulationRefLang.setDescription(entityRefLang.getDescription());
				regulationRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null
						? entityRefLang.getLang().getCode()
						: null);
				regulationRefLangs.add(regulationRefLang);
			}
		}
		ByteArrayInputStream in = regulationRefToExcel(regulationRefLangs);
		return in;
	}

	@Override
	public void addRegulationRef(RegulationRefLang regulationRefLang) {
		RegulationRef regulationRef = new RegulationRef();

		regulationRef.setCode(regulationRefLang.getCode());
		regulationRef.setCountryRef(countryRefRepository.findByReference(regulationRefLang.getCountryRef()));
		regulationRef.setGeneralDescription(regulationRefLang.getGeneralDescription());
		RegulationRef ltemp = regulationRefRepository.findByCode(regulationRefLang.getCode());
		if (ltemp == null) {
			Long id = (regulationRefRepository.save(regulationRef)).getId();
			EntityRefLang entityRefLang = new EntityRefLang();

			entityRefLang.setLabel(regulationRefLang.getLabel());
			entityRefLang.setDescription(regulationRefLang.getDescription());
			entityRefLang.setRefId(id);
			entityRefLang.setTableRef(TableRef.REGULATION_REF);
			entityRefLang.setLang(langRepository.findByCode(regulationRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);

		} else {
			Long id = ltemp.getId();
			regulationRef.setId(id);
			regulationRefRepository.save(regulationRef);
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
					TableRef.REGULATION_REF, langRepository.findByCode(regulationRefLang.getLang()).getId(), id);
			entityRefLang.setLabel(regulationRefLang.getLabel());
			entityRefLang.setDescription(regulationRefLang.getDescription());
			entityRefLang.setLang(langRepository.findByCode(regulationRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);
		}
	}

	@Override
	public List<InternationalizationVM> getInternationalizationRefList(Long id) {
		return internationalizationHelper.getInternationalizationRefList(id, TableRef.REGULATION_REF);
	}

	@Override
	public RegulationRefLang findRegulationRef(Long id, String lang) {
		RegulationRefLang regulationRefLang = new RegulationRefLang();
		RegulationRef regulationRef = regulationRefRepository.findOneById(id);
		EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF,
				langRepository.findByCode(lang).getId(), regulationRef.getId());
		regulationRefLang.setId(regulationRef.getId());
		regulationRefLang.setUpdatedBy(regulationRef.getUpdatedBy());
		regulationRefLang.setUpdatedOn(regulationRef.getUpdatedOn());
		regulationRefLang.setCreatedBy(regulationRef.getCreatedBy());
		regulationRefLang.setCreatedOn(regulationRef.getCreatedOn());
		regulationRefLang.setCode(regulationRef.getCode());
		regulationRefLang.setCountryRef(
				regulationRef.getCountryRef() != null ? regulationRef.getCountryRef().getReference() : "");
		regulationRefLang.setGeneralDescription(regulationRef.getGeneralDescription());
		regulationRefLang.setLabel(entityRefLang.getLabel() != null ? entityRefLang.getLabel() : null);
		regulationRefLang.setDescription(entityRefLang.getDescription() != null ? entityRefLang.getDescription() : "");
		regulationRefLang.setLang(lang);
		return regulationRefLang;
	}

	@Override
	public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
		entityRefLang.setTableRef(TableRef.REGULATION_REF);
		entityRefLang.setLang(langRepository.findByCode(lang));
		if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF,
				entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
			entityRefLangRepository.save(entityRefLang);
		else
			throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
	}

	@Override
	public ErrorResponse deleteInternationalisation(String codeLang, Long portRefId) {

		ErrorResponse response = new ErrorResponse();
		try {

			Lang lang = langRepository.findByCode(codeLang);

			EntityRefLang entityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF, lang.getId(), portRefId);

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
	public List<RegulationRef> saveAll(List<RegulationRef> regulationRefs) {
		if (!regulationRefs.isEmpty()) {
			return regulationRefRepository.saveAll(regulationRefs);
		}
		return null;
	}

	public Page<RegulationRefLang> mapLangToRefLangs(Page<RegulationRef> regulationRefs, String codeLang) {
		List<RegulationRefLang> regulationRefLangs = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);
		return regulationRefs.map(regulationRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF, lang.getId(), regulationRef.getId());
			RegulationRefLang regulationRefLang = new RegulationRefLang();
			regulationRefLang.setId(regulationRef.getId());

			regulationRefLang.setCode(regulationRef.getCode() != null ? regulationRef.getCode() : "");
			regulationRefLang.setCountryRef(
					regulationRef.getCountryRef() != null ? regulationRef.getCountryRef().getReference() : "");
			regulationRefLang.setGeneralDescription(regulationRef.getGeneralDescription());

			regulationRefLang.setCreatedBy(regulationRef.getCreatedBy());
			regulationRefLang.setCreatedOn(regulationRef.getCreatedOn());
			regulationRefLang.setUpdatedBy(regulationRef.getUpdatedBy());
			regulationRefLang.setUpdatedOn(regulationRef.getUpdatedOn());
			regulationRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
			regulationRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
			regulationRefLang.setLang(codeLang);
			regulationRefLangs.add(regulationRefLang);
			return regulationRefLang;
		});
	}

	public Page<RegulationRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
		return mapLangToRefLangs(regulationRefRepository.filterByCodeOrLabel(value,
				langRepository.findByCode(codeLang).getId(), pageable), codeLang);
	}

	public Page<RegulationRefLang> filterForPortal(MspAndBarriersFilterPayload mspAndBarriersFilterPayload, int page,
			int size, String codeLang, String orderDirection) {
		if (mspAndBarriersFilterPayload.getCode().equals("")
				&& mspAndBarriersFilterPayload.getCustomRegimCode().equals("")
				&& mspAndBarriersFilterPayload.getCountryReference().equals("")
				&& mspAndBarriersFilterPayload.getOrganizationReference().equals("")) {
			return getAll(page, size, codeLang, orderDirection);
		} else {
			if (mspAndBarriersFilterPayload.getCode().equals("")) {
				mspAndBarriersFilterPayload.setCode(null);
			}
			if (mspAndBarriersFilterPayload.getCustomRegimCode().equals("")) {
				mspAndBarriersFilterPayload.setCustomRegimCode(null);
			}
			if (mspAndBarriersFilterPayload.getCountryReference().equals("")) {
				mspAndBarriersFilterPayload.setCountryReference(null);
			}
		}
		Page<RegulationRef> regulationRefs = mspAndBarriersFilterPayload.getCustomRegimCode() != null
				? regulationRefRepository.findMspsWithCustomRegime(mspAndBarriersFilterPayload.getCountryReference(),
						mspAndBarriersFilterPayload.getCode(), mspAndBarriersFilterPayload.getCustomRegimCode(),
						PageRequest.of(page, size))
				: regulationRefRepository.findMspsWithoutCustomRegime(mspAndBarriersFilterPayload.getCountryReference(),
						mspAndBarriersFilterPayload.getCode(), PageRequest.of(page, size));

		Lang lang = langRepository.findByCode(codeLang);
		List<RegulationRefLang> regulationRefLangs = new ArrayList<>();

		return regulationRefs.map(regulationRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF, lang.getId(), regulationRef.getId());

			RegulationRefLang regulationRefLang = new RegulationRefLang();

			regulationRefLang.setId(regulationRef.getId());

			regulationRefLang.setCode(regulationRef.getCode() != null ? regulationRef.getCode() : "");
			regulationRefLang.setOrganization(
					regulationRef.getOrganization() != null ? regulationRef.getOrganization().getReference() : "");
			regulationRefLang.setCountryRef(
					regulationRef.getCountryRef() != null ? regulationRef.getCountryRef().getReference() : "");
			regulationRefLang.setCreatedBy(regulationRef.getCreatedBy());
			regulationRefLang.setCode(regulationRef.getCode() != null ? regulationRef.getCode() : "");
			regulationRefLang.setCreatedOn(regulationRef.getCreatedOn());
			regulationRefLang.setUpdatedBy(regulationRef.getUpdatedBy());
			regulationRefLang.setUpdatedOn(regulationRef.getUpdatedOn());
			regulationRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
			regulationRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
			regulationRefLang.setLang(codeLang);
			regulationRefLangs.add(regulationRefLang);
			return regulationRefLang;
		});
	}

	@Override
	public Page<RegulationRefLang> filterForProductInformationFinder(
			ProductInformationFinderFilterPayload productInformationFinderFilterPayload, int page, int size,
			String codeLang, String orderDirection) {
		List<String> countryReferences = Arrays
				.asList(new String[] { productInformationFinderFilterPayload.getExportCountryCode(),
						productInformationFinderFilterPayload.getImportCountryCode() });

		Lang lang = langRepository.findByCode(codeLang);
		List<RegulationRefLang> regulationRefLangs = new ArrayList<>();
		Page<RegulationRef> regulationRefs = null;
		// if(productInformationFinderFilterPayload.getTarifBookReference().equals(""))productInformationFinderFilterPayload.setTarifBookReference(null);
		if (productInformationFinderFilterPayload.getTarifBookReference() != null
				&& !productInformationFinderFilterPayload.getTarifBookReference().equals("")) {
			regulationRefs = regulationRefRepository.findByFilter(
					productInformationFinderFilterPayload.getTarifBookReference(), countryReferences,
					PageRequest.of(page, size));
		} else if (productInformationFinderFilterPayload.getExportCountryCode().equals("")
				&& productInformationFinderFilterPayload.getImportCountryCode().equals("")) {
			return getAll(page, size, codeLang, orderDirection);
		} else {
			List<CountryRef> countries = countryReferences.stream().filter(cr -> cr != null)
					.map(cr -> countryRefRepository.findByReference(cr)).collect(Collectors.toList());
			regulationRefs = regulationRefRepository.findByFilterWithoutTarifBook(countries,
					PageRequest.of(page, size));
		}

		return regulationRefs.map(regulationRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF, lang.getId(), regulationRef.getId());

			RegulationRefLang regulationRefLang = new RegulationRefLang();

			regulationRefLang.setId(regulationRef.getId());
			regulationRefLang.setCode(regulationRef.getCode() != null ? regulationRef.getCode() : "");
			regulationRefLang.setOrganization(
					regulationRef.getOrganization() != null ? regulationRef.getOrganization().getReference() : "");
			regulationRefLang.setCode(regulationRef.getCode() != null ? regulationRef.getCode() : "");
			regulationRefLang.setCountryRef(
					regulationRef.getCountryRef() != null ? regulationRef.getCountryRef().getReference() : "");
			regulationRefLang.setCreatedBy(regulationRef.getCreatedBy());
			regulationRefLang.setCreatedOn(regulationRef.getCreatedOn());
			regulationRefLang.setUpdatedBy(regulationRef.getUpdatedBy());
			regulationRefLang.setUpdatedOn(regulationRef.getUpdatedOn());
			regulationRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
			regulationRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
			regulationRefLang.setLang(codeLang);
			regulationRefLangs.add(regulationRefLang);
			return regulationRefLang;
		});
	}

	@Override
	public Page<RegulationRefLang> filterForSubPortal(
			SubPortalMspAndBarrsFilterPayload subPortalMspAndBarrsFilterPayload, int page, int size, String codeLang) {

		if (subPortalMspAndBarrsFilterPayload.getExpCountry().equals("")) {
			subPortalMspAndBarrsFilterPayload.setExpCountry(null);
		}
		if (subPortalMspAndBarrsFilterPayload.getImpCounty().equals("")) {
			subPortalMspAndBarrsFilterPayload.setImpCounty(null);
		}

		List<String> countryRefs = Arrays.asList(new String[] { subPortalMspAndBarrsFilterPayload.getExpCountry(),
				subPortalMspAndBarrsFilterPayload.getImpCounty() });
		Page<RegulationRef> regulationRefs = regulationRefRepository.findMspsWithCountries(countryRefs,
				subPortalMspAndBarrsFilterPayload.getCode(), PageRequest.of(page, size));

		Lang lang = langRepository.findByCode(codeLang);
		List<RegulationRefLang> regulationRefLangs = new ArrayList<>();

		return regulationRefs.map(regulationRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF, lang.getId(), regulationRef.getId());

			RegulationRefLang regulationRefLang = new RegulationRefLang();

			regulationRefLang.setId(regulationRef.getId());
			regulationRefLang.setCode(regulationRef.getCode() != null ? regulationRef.getCode() : "");
			regulationRefLang.setOrganization(
					regulationRef.getOrganization() != null ? regulationRef.getOrganization().getReference() : "");
			regulationRefLang.setCountryRef(
					regulationRef.getCountryRef() != null ? regulationRef.getCountryRef().getReference() : "");
			regulationRefLang.setCreatedBy(regulationRef.getCreatedBy());
			regulationRefLang.setCreatedOn(regulationRef.getCreatedOn());
			regulationRefLang.setUpdatedBy(regulationRef.getUpdatedBy());
			regulationRefLang.setUpdatedOn(regulationRef.getUpdatedOn());
			regulationRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
			regulationRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
			regulationRefLang.setLang(codeLang);
			regulationRefLangs.add(regulationRefLang);
			return regulationRefLang;
		});
	}

	public Page<RegulationRefLang> subPortalProductInformationFinderfilter(
			SubportalProductInformationFinderFilterPayload payload, int page, int size, String codeLang,
			String orderDirection) {
		if (payload.getTarifBookReference().equals("") && payload.getCountryRefCode().equals("")
				&& payload.getCustomRegimeCode().equals("")) {
			return getAll(page, size, codeLang, orderDirection);
		} else {
			if (payload.getTarifBookReference().equals("")) {
				payload.setTarifBookReference(null);
			}
			if (payload.getCountryRefCode().equals("")) {
				payload.setCountryRefCode(null);
			}
			if (payload.getCustomRegimeCode().equals("")) {
				payload.setCustomRegimeCode(null);
			}
		}
		Page<RegulationRef> regulationRefs = regulationRefRepository.subportalProductInformationFinderFilter(
				payload.getCountryRefCode(), payload.getCustomRegimeCode(), payload.getTarifBookReference(),
				PageRequest.of(page, size));

		Lang lang = langRepository.findByCode(codeLang);
		List<RegulationRefLang> regulationRefLangs = new ArrayList<>();

		return regulationRefs.map(regulationRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository
					.findByTableRefAndLang_IdAndRefId(TableRef.REGULATION_REF, lang.getId(), regulationRef.getId());

			RegulationRefLang regulationRefLang = new RegulationRefLang();

			regulationRefLang.setId(regulationRef.getId());
			regulationRefLang.setCode(regulationRef.getCode() != null ? regulationRef.getCode() : "");
			regulationRefLang.setOrganization(
					regulationRef.getOrganization() != null ? regulationRef.getOrganization().getReference() : "");
			regulationRefLang.setCountryRef(
					regulationRef.getCountryRef() != null ? regulationRef.getCountryRef().getReference() : "");
			regulationRefLang.setCreatedBy(regulationRef.getCreatedBy());
			regulationRefLang.setCreatedOn(regulationRef.getCreatedOn());
			regulationRefLang.setUpdatedBy(regulationRef.getUpdatedBy());
			regulationRefLang.setUpdatedOn(regulationRef.getUpdatedOn());
			regulationRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
			regulationRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
			regulationRefLang.setLang(codeLang);
			regulationRefLangs.add(regulationRefLang);
			return regulationRefLang;
		});
	}

	public RegulationRefLangDetailed findRegulationDetailed(RegulationRefLang regulationRefLang) {
		return helperMapper.toDetailedRegulation(regulationRefLang);
	}

	@Override
	public RegulationRefBean findById(Long id) {
		RegulationRef result = regulationRefRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("id not found"));
		return regulationRefMapper.entityToBean(result);
	}

	@Override
	public Page<RegulationRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable,
			String lang) {
		return regulationRefRepository.filterByCodeOrLabelProjection(value.toLowerCase(),
				langRepository.findByCode(lang).getId(), pageable);
	}

}