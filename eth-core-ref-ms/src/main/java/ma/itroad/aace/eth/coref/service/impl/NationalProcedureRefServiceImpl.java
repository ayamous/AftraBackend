package ma.itroad.aace.eth.coref.service.impl;

import edu.emory.mathcs.backport.java.util.Arrays;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.exception.NationalProcedureNotFoundException;
import ma.itroad.aace.eth.coref.exception.TranslationFoundException;
import ma.itroad.aace.eth.coref.model.bean.*;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.NationalProcedureRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.NationalProcedureRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.NationalProcedureRefVM;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubPortalNationalProcedureAndRegulationFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.repository.*;
import ma.itroad.aace.eth.coref.service.INationalProcedureRefService;
import ma.itroad.aace.eth.coref.service.helper.*;
import ma.itroad.aace.eth.coref.service.helper.detailed.NationalProcedureRefLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.mappers.HelperMapper;
import ma.itroad.aace.eth.coref.service.impl.exceldto.NationalProcedureExcelDTO;
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

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NationalProcedureRefServiceImpl extends BaseServiceImpl<NationalProcedureRef, NationalProcedureRefBean> implements INationalProcedureRefService {

	static String[] HEADERs = { "CODE", "REFERENCE DU PAYS", "REFERENCE DU REGIME", "REFERENCE ORGANISATION", "lABEL", "DESCRIPTION", "LANG" };
	static String SHEET = "NationalProcedureRefSHEET";

	@Autowired
	private NationalProcedureRefMapper nationalProcedureRefMapper;

	@Autowired
	private NationalProcedureRefRepository nationalProcedureRefRepository;

	@Autowired
	private CountryRefRepository countryRefRepository;

	@Autowired
	private CustomsRegimRefRepository customsRegimRefRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

	@Autowired
	private LangRepository langRepository;

	@Autowired
	private InternationalizationHelper internationalizationHelper;

	@Autowired
	private Validator validator;

@Autowired
   private HelperMapper helperMapper ;
	@Override
	public List<NationalProcedureRef> saveAll(List<NationalProcedureRef> nationalProceduresRefs) {
		if (!nationalProceduresRefs.isEmpty()) {
			return nationalProcedureRefRepository.saveAll(nationalProceduresRefs);
		}
		return null;
	}

	@Override
	public ErrorResponse delete(Long id) {
		try {
			nationalProcedureRefRepository.deleteById(id);
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


	@Override
	public ByteArrayInputStream nationalProcedureRefsToExcel(List<NationalProcedureRefLang> nationalProcedureRefLangs) {
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
				if (!nationalProcedureRefLangs.isEmpty()) {

					for (NationalProcedureRefLang nationalProcedureRefLang : nationalProcedureRefLangs) {
						Row row = sheet.createRow(rowIdx++);

						row.createCell(0).setCellValue(nationalProcedureRefLang.getCode());

						row.createCell(1).setCellValue(nationalProcedureRefLang.getCountryRef());
						row.createCell(2).setCellValue(nationalProcedureRefLang.getCustomsRegimRef());
						row.createCell(3).setCellValue(nationalProcedureRefLang.getOrganization());

						row.createCell(4).setCellValue(nationalProcedureRefLang.getLabel());
						row.createCell(5).setCellValue(nationalProcedureRefLang.getGeneralDescription());
						row.createCell(6).setCellValue(nationalProcedureRefLang.getLang());

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
	public Set<ConstraintViolation<NationalProcedureRefLang>> validateNationalProcedure(NationalProcedureRefLang nationalProcedureRefLang) {
		Set<ConstraintViolation<NationalProcedureRefLang>> violations = validator.validate(nationalProcedureRefLang);
		return violations;
	}

	@Override
	public List<NationalProcedureRefLang> excelToNationalProcedureRefs(InputStream is) {
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);
			Iterator<Row> rows = sheet.iterator();
			List<NationalProcedureRefLang> nationalProcedureRefLangs = new ArrayList<NationalProcedureRefLang>();
			Row currentRow = rows.next();
			while (rows.hasNext()) {
				currentRow = rows.next();
				NationalProcedureRefLang nationalProcedureRefLang = new NationalProcedureRefLang();
				int cellIdx = 0;
				for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
					Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);

					switch (cellIdx) {
						case 0:
							nationalProcedureRefLang.setCode(Util.cellValue(currentCell));
							break;
						case 1:
							nationalProcedureRefLang.setCountryRef(Util.cellValue(currentCell));
							break;
						case 2:
							nationalProcedureRefLang.setCustomsRegimRef(Util.cellValue(currentCell));
							break;
						case 3:
							nationalProcedureRefLang.setOrganization(Util.cellValue(currentCell));
							break;
						case 4:
							nationalProcedureRefLang.setLabel(Util.cellValue(currentCell));
						case 5:
							nationalProcedureRefLang.setGeneralDescription(Util.cellValue(currentCell));
						case 6:
							nationalProcedureRefLang.setLang(Util.cellValue(currentCell));
							break;
						default:
							break;
					}
					cellIdx++;
				}
				nationalProcedureRefLangs.add(nationalProcedureRefLang);
			}
			workbook.close();
			return nationalProcedureRefLangs;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {

			List<NationalProcedureRefLang> nationalProcedureRefLang = excelToNationalProcedureRefs(file.getInputStream());
			List<NationalProcedureRefLang> invalidNationalProcedureRefLang = new ArrayList<NationalProcedureRefLang>();
			List<NationalProcedureRefLang> validNationalProcedureRefLangs = new ArrayList<NationalProcedureRefLang>();

			int lenght = nationalProcedureRefLang.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<NationalProcedureRefLang>> violations = validateNationalProcedure(nationalProcedureRefLang.get(i));
				if (violations.isEmpty())

				{
					validNationalProcedureRefLangs.add(nationalProcedureRefLang.get(i));
				} else {
					invalidNationalProcedureRefLang.add(nationalProcedureRefLang.get(i));
				}
			}

			if (!invalidNationalProcedureRefLang.isEmpty()) {

				ByteArrayInputStream out = nationalProcedureRefsToExcel(invalidNationalProcedureRefLang);
				xls = new InputStreamResource(out);
			}





			List<NationalProcedureRefLang> nationalProcedureRefLangs = excelToNationalProcedureRefs(file.getInputStream());
			if (!nationalProcedureRefLangs.isEmpty()) {
				for (NationalProcedureRefLang l : nationalProcedureRefLangs) {

					NationalProcedureRef nationalProcedureRef = new NationalProcedureRef();
					nationalProcedureRef.setGeneralDescription(l.getGeneralDescription());
					nationalProcedureRef.setCode(l.getCode());
					nationalProcedureRef.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
					nationalProcedureRef.setCustomsRegimRef(customsRegimRefRepository.findByCode(l.getCustomsRegimRef()));
					nationalProcedureRef.setOrganization(organizationRepository.findByReference(l.getOrganization()));


					NationalProcedureRef ltemp = nationalProcedureRefRepository.findByCode(l.getCode());

					if (ltemp == null) {

						Long id = (nationalProcedureRefRepository.save(nationalProcedureRef)).getId();

						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setGeneralDescription(l.getGeneralDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.NATIONAL_PROCEDURE_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);

					} else {
						Long id = ltemp.getId();
						nationalProcedureRef.setId(id);
						nationalProcedureRefRepository.save(nationalProcedureRef);
						EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, langRepository.findByCode(l.getLang()).getId(), id);
						if (entityRefLang == null) {
							entityRefLang = new EntityRefLang();
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.NATIONAL_PROCEDURE_REF);
							entityRefLang.setRefId(nationalProcedureRef.getId());
						}
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setGeneralDescription(l.getGeneralDescription());
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}

				}
			}
			if (!invalidNationalProcedureRefLang.isEmpty())



				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);


			if (!validNationalProcedureRefLangs.isEmpty())



				return ResponseEntity.status(HttpStatus.OK).body(null);

			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}
	}

	@Override
	public void saveFromExcel(MultipartFile file) {
		// TODO Auto-generated method stub

	}

	@Override
	public ByteArrayInputStream load() {
		List<NationalProcedureRef> nationalProcedureRefs = nationalProcedureRefRepository.findAll();
		ByteArrayInputStream in = null;
		return in;
	}

	private NationalProcedureRefLang procedureToProcedureLang( NationalProcedureRef nationalProcedureRef , EntityRefLang entityRefLang ){
		NationalProcedureRefLang nationalProcedureRefLang = new NationalProcedureRefLang();

		nationalProcedureRefLang.setId(nationalProcedureRef.getId());
		nationalProcedureRefLang.setCode(nationalProcedureRef.getCode()!=null ?nationalProcedureRef.getCode():"");

		nationalProcedureRefLang.setCountryRef(nationalProcedureRef.getCountryRef()!=null?nationalProcedureRef.getCountryRef().getReference():"");
		nationalProcedureRefLang.setCustomsRegimRef(nationalProcedureRef.getCustomsRegimRef()!=null?nationalProcedureRef.getCustomsRegimRef().getCode():"");
		nationalProcedureRefLang.setOrganization(nationalProcedureRef.getOrganization()!=null?nationalProcedureRef.getOrganization().getReference():"");
		nationalProcedureRefLang.setGeneralDescription(nationalProcedureRef.getGeneralDescription());
		nationalProcedureRefLang.setOrganization(nationalProcedureRef.getOrganization()!=null?nationalProcedureRef.getOrganization().getReference():null);


		nationalProcedureRefLang.setCreatedBy(nationalProcedureRef.getCreatedBy());
		nationalProcedureRefLang.setCreatedOn(nationalProcedureRef.getCreatedOn());
		nationalProcedureRefLang.setUpdatedBy(nationalProcedureRef.getUpdatedBy());
		nationalProcedureRefLang.setUpdatedOn(nationalProcedureRef.getUpdatedOn());

		nationalProcedureRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
		nationalProcedureRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");

		nationalProcedureRefLang.setLang(entityRefLang!=null && entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);

		return  nationalProcedureRefLang ;
	}

	@Override
	public Page<NationalProcedureRefLang> filterForPortal(final int page, final int size, ProductInformationFinderFilterPayload productInformationFinderFilterPayload, String codeLang, String orderDirection) {
		List<String> countryReferences = Arrays.asList(new String[]{productInformationFinderFilterPayload.getExportCountryCode(),
				productInformationFinderFilterPayload.getImportCountryCode()});
		List<CountryRef> countries = countryReferences.stream().filter(cr -> cr != null)
				.map(cr -> countryRefRepository.findByReference(cr)).collect(Collectors.toList());

		Lang lang = langRepository.findByCode(codeLang);
		Page<NationalProcedureRef> nationalProcedureRefs = null;

		if (productInformationFinderFilterPayload.getTarifBookReference() != null && !productInformationFinderFilterPayload.getTarifBookReference().equals("")) {
			nationalProcedureRefs = nationalProcedureRefRepository
					.findByFilterWithTarifBook(productInformationFinderFilterPayload.getTarifBookReference(), countryReferences, PageRequest.of(page, size));
		} else if (productInformationFinderFilterPayload.getExportCountryCode().equals("") && productInformationFinderFilterPayload.getImportCountryCode().equals("")){
			return getAll(page, size, codeLang, orderDirection);
		}
		else {
			nationalProcedureRefs = nationalProcedureRefRepository
					.findByFilterWithoutTarifBook(countries, PageRequest.of(page, size));
		}


		return nationalProcedureRefs.map(nationalProcedureRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, lang.getId(), nationalProcedureRef.getId());
			return procedureToProcedureLang(nationalProcedureRef, entityRefLang);
		});

	}

	@Override
	public Page<NationalProcedureRefLang> nationalProcedureFilter(NationalProcedureRefVM nationalProcedureRefVM,
																  int page, int size, String codeLang, String orderDirection) {

		if(nationalProcedureRefVM.getProcedureCode().equals("") && nationalProcedureRefVM.getCustomRegimeCode().equals("") && nationalProcedureRefVM.getCountryRef().equals("")){
			return getAll(page, size, codeLang, orderDirection);
		}
		else {
			if(nationalProcedureRefVM.getProcedureCode().equals("")){
				nationalProcedureRefVM.setProcedureCode(null);
			}
			if(nationalProcedureRefVM.getCustomRegimeCode().equals("")){
				nationalProcedureRefVM.setCustomRegimeCode(null);
			}
			if(nationalProcedureRefVM.getCountryRef().equals("")){
				nationalProcedureRefVM.setCountryRef(null);
			}
			if(nationalProcedureRefVM.getOrganization().equals("")){
				nationalProcedureRefVM.setOrganization(null);
			}
		}

		Page<NationalProcedureRef> nationalProcedureRefs = nationalProcedureRefRepository
				.nationalProcedureFilter(nationalProcedureRefVM.getProcedureCode(),
						nationalProcedureRefVM.getCustomRegimeCode(), nationalProcedureRefVM.getCountryRef(),PageRequest.of(page, size));
		Lang lang = langRepository.findByCode(codeLang);
		return nationalProcedureRefs.map(nationalProcedureRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, lang.getId(), nationalProcedureRef.getId());
			return procedureToProcedureLang(nationalProcedureRef, entityRefLang);
		});
	}

	@Override
	public Page<NationalProcedureRefLang> filterForSubPortal(
			SubPortalNationalProcedureAndRegulationFilterPayload subPortalNationalProcedureAndRegulationFilterPayload,
			int page, int size, String codeLang) {
		if(subPortalNationalProcedureAndRegulationFilterPayload.getExpCountry().equals("")) {
			subPortalNationalProcedureAndRegulationFilterPayload.setExpCountry(null);
		}
		if(subPortalNationalProcedureAndRegulationFilterPayload.getImpCountry().equals("")) {
			subPortalNationalProcedureAndRegulationFilterPayload.setImpCountry(null);
		}
		List<String> refCountries = Arrays
				.asList(new String[]{subPortalNationalProcedureAndRegulationFilterPayload.getExpCountry(),
						subPortalNationalProcedureAndRegulationFilterPayload.getImpCountry()});
		Page<NationalProcedureRef> nationalProcedureRefs = nationalProcedureRefRepository.findProcedureWithCountries(refCountries,
				subPortalNationalProcedureAndRegulationFilterPayload.getCode(), PageRequest.of(page, size));

		Lang lang = langRepository.findByCode(codeLang);
		List<NationalProcedureRefLang> nationalProcedureRefLangs = new ArrayList<>();

		return nationalProcedureRefs.map(nationalProcedureRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, lang.getId(), nationalProcedureRef.getId());
			return procedureToProcedureLang(nationalProcedureRef, entityRefLang);
		});
	}

	@Override
	public Page<NationalProcedureRefLang> procedureSubportalProductInformationFinderFilter(
			SubportalProductInformationFinderFilterPayload payload, int page, int size, String codeLang, String orderDirection) {

		if(payload.getCountryRefCode().equals("") && payload.getCustomRegimeCode().equals("")){
			return getAll(page, size, codeLang,orderDirection);
		}
		else {
			if(payload.getCountryRefCode().equals("")){
				payload.setCountryRefCode(null);
			}
			if(payload.getCustomRegimeCode().equals("")){
				payload.setCustomRegimeCode(null);
			}
		}

		Page<NationalProcedureRef> nationalProcedureRefs = nationalProcedureRefRepository
				.procedureSubportalProductInformationFinderFilter(payload.getTarifBookReference(), payload.getCustomRegimeCode(),
						payload.getCountryRefCode(), PageRequest.of(page, size));

		Lang lang = langRepository.findByCode(codeLang);
		return nationalProcedureRefs.map(nationalProcedureRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, lang.getId(), nationalProcedureRef.getId());
			return procedureToProcedureLang(nationalProcedureRef, entityRefLang);
		});
	}


	@Override
	public Page<NationalProcedureRefBean> getAll(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<NationalProcedureRef> procedurePage = nationalProcedureRefRepository.findAll(pageable);

		Page<NationalProcedureRefBean> nationalProcedureRefBeanPage = procedurePage.map(nationalProcedureRef -> {
			return nationalProcedureRefMapper.entityToBean(nationalProcedureRef);
		});
		return nationalProcedureRefBeanPage;
	}


	@Override
	public Page<NationalProcedureRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
		Page<NationalProcedureRef> nationalProcedureRefs = null;
		if(orderDirection.equals("DESC")){
			nationalProcedureRefs = nationalProcedureRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}else if(orderDirection.equals("ASC")){
			nationalProcedureRefs = nationalProcedureRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}

		Lang lang = langRepository.findByCode(codeLang);
		return nationalProcedureRefs.map(nationalProcedureRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, lang.getId(), nationalProcedureRef.getId());

			return procedureToProcedureLang(nationalProcedureRef, entityRefLang);
		});
	}


	@Override
	public ByteArrayInputStream load(String codeLang) {

		Lang lang = langRepository.findByCode(codeLang);
		List<NationalProcedureRef> nationalProcedureRefs = nationalProcedureRefRepository.findAll();
		return nationalProcedureRefsToExcel(nationalProcedureRefs.stream().map(nationalProcedureRef -> {
					EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, lang.getId(), nationalProcedureRef.getId());
					return procedureToProcedureLang(nationalProcedureRef, entityRefLang);
				}
		).collect(Collectors.toList()));
	}

	@Override
	public NationalProcedureRefBean addInternationalisation(NationalProcedureRefLang nationalProcedureRefLang) {
		return null;
	}

	@Override
	  public NationalProcedureRefBean addNationalProcedure(NationalProcedureRefLang nationalProcedureRefLang) {
		NationalProcedureRef nationalProcedureRef = new NationalProcedureRef();

		nationalProcedureRef.setCode(nationalProcedureRefLang.getCode());
		nationalProcedureRef.setCountryRef(countryRefRepository.findByReference(nationalProcedureRefLang.getCountryRef()));
		nationalProcedureRef.setCustomsRegimRef(customsRegimRefRepository.findByCode(nationalProcedureRefLang.getCustomsRegimRef()));
		nationalProcedureRef.setOrganization(organizationRepository.findByReference(nationalProcedureRefLang.getOrganization()));
		nationalProcedureRef.setGeneralDescription(nationalProcedureRefLang.getGeneralDescription());
		NationalProcedureRef ltemp = nationalProcedureRefRepository.findByCode(nationalProcedureRefLang.getCode());

		if (ltemp == null) {
			Long id = (nationalProcedureRefRepository.save(nationalProcedureRef)).getId();
			EntityRefLang entityRefLang = new EntityRefLang();
			entityRefLang.setLabel(nationalProcedureRefLang.getLabel());
			entityRefLang.setDescription(nationalProcedureRefLang.getDescription());
			entityRefLang.setRefId(id);
			entityRefLang.setTableRef(TableRef.NATIONAL_PROCEDURE_REF);
			entityRefLang.setLang(  langRepository.findByCode(nationalProcedureRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);
		} else {
			Long id = ltemp.getId();
			nationalProcedureRef.setId(id);
			nationalProcedureRefRepository.save(nationalProcedureRef);
			EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, langRepository.findByCode(nationalProcedureRefLang.getLang()).getId() ,id);
			entityRefLang.setLabel(nationalProcedureRefLang.getLabel());
			entityRefLang.setDescription(nationalProcedureRefLang.getDescription());
			entityRefLangRepository.save(entityRefLang);
		}

		return nationalProcedureRefMapper.entityToBean(nationalProcedureRef);

	}

	@Override
	public NationalProcedureRefLang findNationalProcedure(Long id, String lang) {
		NationalProcedureRefLang nationalProcedureRefLang = new NationalProcedureRefLang();
		NationalProcedureRef nationalProcedureRef = nationalProcedureRefRepository.findOneById(id);
		EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
				TableRef.NATIONAL_PROCEDURE_REF, langRepository.findByCode(lang).getId(),
				!Objects.isNull(nationalProcedureRef) ? nationalProcedureRef.getId() : 0);

		if (Objects.isNull(nationalProcedureRef) || Objects.isNull(entityRefLang)) {
			throw new NationalProcedureNotFoundException("NationalProcedure id: " + id);
		}
		nationalProcedureRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
		nationalProcedureRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
		nationalProcedureRefLang.setLang(lang);
		nationalProcedureRefLang.setCode(nationalProcedureRef.getCode());
		nationalProcedureRefLang.setCountryRef(nationalProcedureRef.getCountryRef()!=null?nationalProcedureRef.getCountryRef().getReference():"");
		nationalProcedureRefLang.setCustomsRegimRef(nationalProcedureRef.getCustomsRegimRef()!=null?nationalProcedureRef.getCustomsRegimRef().getCode():"");
		nationalProcedureRefLang.setOrganization(nationalProcedureRef.getOrganization()!= null ? nationalProcedureRef.getOrganization().getReference():"");
		nationalProcedureRefLang.setGeneralDescription(nationalProcedureRef.getGeneralDescription());
		return nationalProcedureRefLang;

	}

	public NationalProcedureRefLangDetailed findNationalProcedureDetailed(NationalProcedureRefLang nationalProcedureRefLang){
		return helperMapper.toDetailedNationalProcedure(nationalProcedureRefLang) ;
	}

	@Override
	public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
		Optional<NationalProcedureRef> nationalProcedureRef = nationalProcedureRefRepository.findById(entityRefLang.getRefId());
		if (nationalProcedureRef.isPresent()) {			
			EntityRefLang entityRefLangTemp = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF,
					langRepository.findByCode(lang).getId(), !Objects.isNull(entityRefLang) ? entityRefLang.getRefId() : 0);
			if (Objects.isNull(entityRefLangTemp)) {			
				entityRefLang.setTableRef(TableRef.NATIONAL_PROCEDURE_REF);
				entityRefLang.setLang(langRepository.findByCode(lang));
				entityRefLangRepository.save(entityRefLang);
			} else {
				throw new TranslationFoundException("Une traduction existe déjà pour cette langue");
			}
		} else {
			throw new NationalProcedureNotFoundException("NationalProcedure id: " + entityRefLang.getRefId());
		}
	}

	private NationalProcedureRefBean toNationalProcedureRefBeanMapper(NationalProcedureRef nationalProcedureRef) {
		NationalProcedureRefBean nationalProcedureRefBean = new NationalProcedureRefBean();
		nationalProcedureRefBean.setId(nationalProcedureRef.getId());
		nationalProcedureRefBean.setCreatedOn(nationalProcedureRef.getCreatedOn());
		nationalProcedureRefBean.setCode(nationalProcedureRef.getCode());
		nationalProcedureRefBean.setLabel(nationalProcedureRef.getLabel());
		nationalProcedureRefBean.setDescription(nationalProcedureRef.getDescription());
		nationalProcedureRefBean.setLang(nationalProcedureRef.getLang());
		nationalProcedureRefBean.setCountryRef(toCountryRefBeanMapper(nationalProcedureRef.getCountryRef()));
		nationalProcedureRefBean.setCustomsRegimRef(toCustomsRegimRefBeanMapper(nationalProcedureRef.getCustomsRegimRef()));
		nationalProcedureRefBean.setOrganization(toOrganizationBeanMapper(nationalProcedureRef.getOrganization()));

		
		return nationalProcedureRefBean;
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

	private CityRefBean toCityRefBeanMapper(CityRef cityRef) {
		if (Objects.isNull(cityRef)) {
			return null;
		}

		CityRefBean cityRefBean = new CityRefBean();
		cityRefBean.setReference(cityRef.getReference());

		return cityRefBean;
	}

	private CategoryRefBean toCategoryRefBeanMapper(CategoryRef categoryRef) {
		if (Objects.isNull(categoryRef)) {
			return null;
		}

		CategoryRefBean categoryRefBean = new CategoryRefBean();
		categoryRefBean.setCode(categoryRef.getCode());
		categoryRefBean.setCreatedOn(categoryRef.getCreatedOn());
		categoryRefBean.setId(categoryRef.getId());

		return categoryRefBean;
	}

	private CustomsRegimRefBean toCustomsRegimRefBeanMapper(CustomsRegimRef customsRegimRef) {
		CustomsRegimRefBean customsRegimRefBean = new CustomsRegimRefBean();
		customsRegimRefBean.setId(customsRegimRef.getId());
		customsRegimRefBean.setCode(customsRegimRef.getCode());
		customsRegimRefBean.setCreatedOn(customsRegimRef.getCreatedOn());
		customsRegimRefBean.setRegimType(customsRegimRef.getRegimType());
		
		return customsRegimRefBean;
	}

	private OrganizationBean toOrganizationBeanMapper(Organization organization) {
		if (Objects.isNull(organization)) {
			return null;
		}

		OrganizationBean organizationBean = new OrganizationBean();
		organizationBean.setReference(organization.getReference());
		organizationBean.setAcronym(organization.getAcronym());
		organizationBean.setName(organization.getName());
		organizationBean.setCountryRef(toCountryRefBeanMapper(organization.getCountryRef()));
		organizationBean.setParent(toOrganizationBeanMapper(organization.getParent()));
		organizationBean.setCityRef(toCityRefBeanMapper(organization.getCityRef()));
		organizationBean.setCategoryRef(toCategoryRefBeanMapper(organization.getCategoryRef()));

		return organizationBean;
	}


	public Page<NationalProcedureRefLang> mapLangToRefLangs(Page<NationalProcedureRef> nationalProcedureRefs, String codeLang) {
		List<NationalProcedureRefLang> nationalProcedureRefLangs = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);
		return nationalProcedureRefs.map(nationalProcedureRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.NATIONAL_PROCEDURE_REF, lang.getId(), nationalProcedureRef.getId());

			NationalProcedureRefLang nationalProcedureRefLang = new NationalProcedureRefLang();

			nationalProcedureRefLang.setId(nationalProcedureRef.getId());
			nationalProcedureRefLang.setCode(nationalProcedureRef.getCode()!=null ?nationalProcedureRef.getCode():"");

			nationalProcedureRefLang.setCountryRef(nationalProcedureRef.getCountryRef()!=null?nationalProcedureRef.getCountryRef().getReference():null);
			nationalProcedureRefLang.setCustomsRegimRef(nationalProcedureRef.getCustomsRegimRef()!=null?nationalProcedureRef.getCustomsRegimRef().getCode():null);
			nationalProcedureRefLang.setOrganization(nationalProcedureRef.getOrganization()!=null?nationalProcedureRef.getOrganization().getReference():null);

			nationalProcedureRefLang.setGeneralDescription(nationalProcedureRef.getGeneralDescription());


			nationalProcedureRefLang.setCreatedBy(nationalProcedureRef.getCreatedBy());
			nationalProcedureRefLang.setCreatedOn(nationalProcedureRef.getCreatedOn());
			nationalProcedureRefLang.setUpdatedBy(nationalProcedureRef.getUpdatedBy());
			nationalProcedureRefLang.setUpdatedOn(nationalProcedureRef.getUpdatedOn());

			nationalProcedureRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			nationalProcedureRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			nationalProcedureRefLang.setLang(codeLang);
			nationalProcedureRefLangs.add(nationalProcedureRefLang);
			return nationalProcedureRefLang;
		});
	}

	public Page<NationalProcedureRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
		return mapLangToRefLangs(nationalProcedureRefRepository.filterByCodeOrLabel(value,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
	}

	@Override
	public Page<NationalProcedureRefLangProjection> filterByCodeOrLabelProjection(String value, String lang, int page, int size) {
		return nationalProcedureRefRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),PageRequest.of(page,size));
	}

	@Override
	public NationalProcedureRefBean findById(Long id) {
		NationalProcedureRef result= nationalProcedureRefRepository.findById(id).orElseThrow(
				()->new NotFoundException("id not found"));
		return nationalProcedureRefMapper.entityToBean(result);
	}

}
