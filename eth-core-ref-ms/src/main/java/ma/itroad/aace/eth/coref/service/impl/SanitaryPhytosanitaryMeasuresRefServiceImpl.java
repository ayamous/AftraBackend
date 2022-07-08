package ma.itroad.aace.eth.coref.service.impl;

import edu.emory.mathcs.backport.java.util.Arrays;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.controller.SanitaryPhytosanitaryMeasuresRefController;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.*;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.SanitaryPhytosanitaryMeasuresRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.SanitaryPhytosanitaryMeasuresRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.MspAndBarriersFilterPayload;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubPortalMspAndBarrsFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.repository.*;
import ma.itroad.aace.eth.coref.service.ISanitaryPhytosanitaryMeasuresRefService;
import ma.itroad.aace.eth.coref.service.helper.CustomsOfficeRefLang;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.SanitaryPhytosanitaryMeasuresRefLang;
import ma.itroad.aace.eth.coref.service.helper.TariffBookRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.SanitaryPhytosanitaryMeasuresRefLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.mappers.HelperMapper;
import ma.itroad.aace.eth.coref.service.impl.exceldto.SanitaryPhytosanitaryMesuresExcelDTO;
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

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class SanitaryPhytosanitaryMeasuresRefServiceImpl extends BaseServiceImpl<SanitaryPhytosanitaryMeasuresRef, SanitaryPhytosanitaryMeasuresRefBean>
		implements ISanitaryPhytosanitaryMeasuresRefService {

	static String[] HEADERs = {"CODE MSP", "REFERENCE PAYS", "REFERENCE ORGANISATION", "LABEL", "DESCRIPTION", "LANGUE", ""};
	static String SHEET = "MSPSHEET";


	@Autowired
	private SanitaryPhytosanitaryMeasuresRefRepository sanitaryPhytosanitaryMeasuresRefRepository;
	@Autowired
	private SanitaryPhytosanitaryMeasuresRefMapper sanitaryPhytosanitaryMeasuresRefMapper;
	@Autowired
	private CountryRefRepository countryRefRepository;
	@Autowired
	private LangRepository langRepository;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

	@Autowired
	private CustomsRegimRefRepository customsRegimRefRepository;

	@Autowired
	private InternationalizationHelper internationalizationHelper;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private EntityManager em;

	@Autowired
	private HelperMapper helperMapper ;

	@Autowired
	private Validator validator;


	public Page<SanitaryPhytosanitaryMeasuresRefBean> getAll(Pageable pageable) {
		Page<SanitaryPhytosanitaryMeasuresRef> entities = sanitaryPhytosanitaryMeasuresRefRepository.findAll(pageable);
		Page<SanitaryPhytosanitaryMeasuresRefBean> result = entities.map(sanitaryPhytosanitaryMeasuresRefMapper::entityToBean);
		return result;

	}

	public Page<SanitaryPhytosanitaryMeasuresRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
		Page<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs = null;
		if(orderDirection.equals("DESC")){
			sanitaryPhytosanitaryMeasuresRefs = sanitaryPhytosanitaryMeasuresRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}else if(orderDirection.equals("ASC")){
			sanitaryPhytosanitaryMeasuresRefs = sanitaryPhytosanitaryMeasuresRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}
		Lang lang = langRepository.findByCode(codeLang);
		List<SanitaryPhytosanitaryMeasuresRefLang> sanitaryPhytosanitaryMeasuresRefLangs = new ArrayList<>();

		return sanitaryPhytosanitaryMeasuresRefs.map(sanitaryPhytosanitaryMeasuresRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), sanitaryPhytosanitaryMeasuresRef.getId());

			SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang = new SanitaryPhytosanitaryMeasuresRefLang();

			sanitaryPhytosanitaryMeasuresRefLang.setId(sanitaryPhytosanitaryMeasuresRef.getId());
			sanitaryPhytosanitaryMeasuresRefLang.setCode(sanitaryPhytosanitaryMeasuresRef.getCode()!=null ?sanitaryPhytosanitaryMeasuresRef.getCode():"");

			sanitaryPhytosanitaryMeasuresRefLang.setCountryRef(sanitaryPhytosanitaryMeasuresRef.getCountryRef()!=null?sanitaryPhytosanitaryMeasuresRef.getCountryRef().getReference():"");
			sanitaryPhytosanitaryMeasuresRefLang.setOrganization(sanitaryPhytosanitaryMeasuresRef.getOrganization()!=null?sanitaryPhytosanitaryMeasuresRef.getOrganization().getReference():"");


			sanitaryPhytosanitaryMeasuresRefLang.setCreatedBy(sanitaryPhytosanitaryMeasuresRef.getCreatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setCreatedOn(sanitaryPhytosanitaryMeasuresRef.getCreatedOn());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedBy(sanitaryPhytosanitaryMeasuresRef.getUpdatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedOn(sanitaryPhytosanitaryMeasuresRef.getUpdatedOn());

			sanitaryPhytosanitaryMeasuresRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			sanitaryPhytosanitaryMeasuresRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			sanitaryPhytosanitaryMeasuresRefLang.setLang(codeLang);

			sanitaryPhytosanitaryMeasuresRefLangs.add(sanitaryPhytosanitaryMeasuresRefLang);

			return  sanitaryPhytosanitaryMeasuresRefLang ;
		});
	}

	@Override
	public List<SanitaryPhytosanitaryMeasuresRef> saveAll(
			List<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs) {
		if (!sanitaryPhytosanitaryMeasuresRefs.isEmpty()) {
			return sanitaryPhytosanitaryMeasuresRefRepository.saveAll(sanitaryPhytosanitaryMeasuresRefs);
		}
		return null;
	}

	@Override
	public ErrorResponse delete(Long id) {
		try {
			sanitaryPhytosanitaryMeasuresRefRepository.deleteById(id);
			return new ErrorResponse(HttpStatus.OK, ErrorMessageType.DELETE_SUCESS.getMessagePattern(), null);
		} catch (Exception e) {
			e.printStackTrace();
			return new ErrorResponse(HttpStatus.CONFLICT, ErrorMessageType.DELETE_ERROR.getMessagePattern(), null);
		}

	}

	@Override
	public ErrorResponse delete(Long id, String lang) {
		try {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF,
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
	public ByteArrayInputStream sanitaryPhytosanitaryMeasuresRefsToExcel(List<SanitaryPhytosanitaryMeasuresRefLang> sanitaryPhytosanitaryMeasuresRefLangs) {
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
				if (!sanitaryPhytosanitaryMeasuresRefLangs.isEmpty()) {

					for (SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang : sanitaryPhytosanitaryMeasuresRefLangs) {
						Row row = sheet.createRow(rowIdx++);

						row.createCell(0).setCellValue(sanitaryPhytosanitaryMeasuresRefLang.getCode());

						row.createCell(1).setCellValue(sanitaryPhytosanitaryMeasuresRefLang.getCountryRef());
						row.createCell(2).setCellValue(sanitaryPhytosanitaryMeasuresRefLang.getOrganization());

						row.createCell(3).setCellValue(sanitaryPhytosanitaryMeasuresRefLang.getLabel());
						row.createCell(4).setCellValue(sanitaryPhytosanitaryMeasuresRefLang.getDescription());
						row.createCell(5).setCellValue(sanitaryPhytosanitaryMeasuresRefLang.getLang());
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
	public Set<ConstraintViolation<SanitaryPhytosanitaryMeasuresRefLang>> validateSanitaryPhytosanitaryMeasures(SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang) {

		Set<ConstraintViolation<SanitaryPhytosanitaryMeasuresRefLang>> violations = validator.validate(sanitaryPhytosanitaryMeasuresRefLang);

		return violations;
	}


	@Override
	public List<SanitaryPhytosanitaryMeasuresRefLang> excelToSanitaryPhytosanitaryMeasuresRefs(InputStream is) {
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);
			List<SanitaryPhytosanitaryMeasuresRefLang> sanitaryPhytosanitaryMeasuresRefLangs = new ArrayList<SanitaryPhytosanitaryMeasuresRefLang>();
			Row currentRow;
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				currentRow= sheet.getRow(rowNum);
				SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang = new SanitaryPhytosanitaryMeasuresRefLang();				for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
					Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
					switch (colNum) {
						case 0:
							sanitaryPhytosanitaryMeasuresRefLang.setCode(Util.cellValue(currentCell));
							break;
						case 1:
							sanitaryPhytosanitaryMeasuresRefLang.setCountryRef(Util.cellValue(currentCell));
							break;
						case 2:
							sanitaryPhytosanitaryMeasuresRefLang.setOrganization(Util.cellValue(currentCell));
							break;
						case 3:
							sanitaryPhytosanitaryMeasuresRefLang.setLabel(Util.cellValue(currentCell));
						case 4:
							sanitaryPhytosanitaryMeasuresRefLang.setDescription(Util.cellValue(currentCell));
						case 5:
							sanitaryPhytosanitaryMeasuresRefLang.setLang(Util.cellValue(currentCell));
							break;
						default:
							break;
					}
				}
				sanitaryPhytosanitaryMeasuresRefLangs.add(sanitaryPhytosanitaryMeasuresRefLang);
			}
			workbook.close();
			return sanitaryPhytosanitaryMeasuresRefLangs;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}

	@Override
	public ByteArrayInputStream load() {
		List<SanitaryPhytosanitaryMeasuresRef>sanitaryPhytosanitaryMeasuresRefs = sanitaryPhytosanitaryMeasuresRefRepository.findAll();
		ByteArrayInputStream in = null;
		return in;
	}

	@Override
	public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {
			List<SanitaryPhytosanitaryMeasuresRefLang> sanitaryPhytosanitaryMeasuresRefLangs = excelToSanitaryPhytosanitaryMeasuresRefs(file.getInputStream());
			List<SanitaryPhytosanitaryMeasuresRefLang> InvalidSanitaryPhytosanitaryMeasuresRefLangs = new ArrayList<SanitaryPhytosanitaryMeasuresRefLang>();
			List<SanitaryPhytosanitaryMeasuresRefLang> ValidSanitaryPhytosanitaryMeasuresRefLangs = new ArrayList<SanitaryPhytosanitaryMeasuresRefLang>();

			int lenght = sanitaryPhytosanitaryMeasuresRefLangs.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<SanitaryPhytosanitaryMeasuresRefLang>> violations = validateSanitaryPhytosanitaryMeasures(sanitaryPhytosanitaryMeasuresRefLangs.get(i));
				if (violations.isEmpty())

				{
					ValidSanitaryPhytosanitaryMeasuresRefLangs.add(sanitaryPhytosanitaryMeasuresRefLangs.get(i));
				} else {
					InvalidSanitaryPhytosanitaryMeasuresRefLangs.add(sanitaryPhytosanitaryMeasuresRefLangs.get(i));
				}
			}

			if (!InvalidSanitaryPhytosanitaryMeasuresRefLangs.isEmpty()) {

				ByteArrayInputStream out = sanitaryPhytosanitaryMeasuresRefsToExcel(InvalidSanitaryPhytosanitaryMeasuresRefLangs);
				xls = new InputStreamResource(out);

			}

			if (!ValidSanitaryPhytosanitaryMeasuresRefLangs.isEmpty()) {
				for (SanitaryPhytosanitaryMeasuresRefLang l : ValidSanitaryPhytosanitaryMeasuresRefLangs) {

					SanitaryPhytosanitaryMeasuresRef sanitaryPhytosanitaryMeasuresRef = new SanitaryPhytosanitaryMeasuresRef();

					sanitaryPhytosanitaryMeasuresRef.setCode(l.getCode());

					sanitaryPhytosanitaryMeasuresRef.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
					sanitaryPhytosanitaryMeasuresRef.setOrganization(organizationRepository.findByReference(l.getOrganization()));
					sanitaryPhytosanitaryMeasuresRef.setGeneralDescription(l.getGeneralDescription());
					SanitaryPhytosanitaryMeasuresRef ltemp = sanitaryPhytosanitaryMeasuresRefRepository.findByCode(l.getCode());

					if (ltemp == null) {

						Long id = (sanitaryPhytosanitaryMeasuresRefRepository.save(sanitaryPhytosanitaryMeasuresRef)).getId();

						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);

					} else {
						Long id = ltemp.getId();
						sanitaryPhytosanitaryMeasuresRef.setId(id);
						sanitaryPhytosanitaryMeasuresRefRepository.save(sanitaryPhytosanitaryMeasuresRef);
						EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF,langRepository.findByCode(l.getLang()).getId(),id);
						if(entityRefLang == null )
						{
							entityRefLang =new EntityRefLang() ;
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF);
							entityRefLang.setRefId(sanitaryPhytosanitaryMeasuresRef.getId());
						}
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}

				}
			}
			if (!InvalidSanitaryPhytosanitaryMeasuresRefLangs.isEmpty())
				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);

			if (!ValidSanitaryPhytosanitaryMeasuresRefLangs.isEmpty())
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
	public Page<SanitaryPhytosanitaryMeasuresRefLang> filterForProductInformationFinder(ProductInformationFinderFilterPayload productInformationFinderFilterPayload, int page, int size, String codeLang, String orderDirection) {
		List<String> countryReferences = Arrays
				.asList(new String[] { productInformationFinderFilterPayload.getExportCountryCode(),
						productInformationFinderFilterPayload.getImportCountryCode() });
		//List<CountryRef> countries = countryReferences.stream().filter(cr -> cr != null).map(cr -> countryRefRepository.findByReference(cr)).collect(Collectors.toList());

		Lang lang = langRepository.findByCode(codeLang);
		List<SanitaryPhytosanitaryMeasuresRefLang> sanitaryPhytosanitaryMeasuresRefLangs = new ArrayList<>();
		Page<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs = null;
		//if(productInformationFinderFilterPayload.getTarifBookReference().equals(""))productInformationFinderFilterPayload.setTarifBookReference(null);
		if (productInformationFinderFilterPayload.getTarifBookReference() != null && !productInformationFinderFilterPayload.getTarifBookReference().equals("")){
			sanitaryPhytosanitaryMeasuresRefs =  sanitaryPhytosanitaryMeasuresRefRepository
					.findByFilter(productInformationFinderFilterPayload.getTarifBookReference(),countryReferences, PageRequest.of(page, size));
		}
		else if (productInformationFinderFilterPayload.getExportCountryCode().equals("") && productInformationFinderFilterPayload.getImportCountryCode().equals("")){
			return getAll(page, size, codeLang, orderDirection);
		}
		else {
			sanitaryPhytosanitaryMeasuresRefs = sanitaryPhytosanitaryMeasuresRefRepository.findByFilterWithoutTarifBook(countryReferences, PageRequest.of(page, size));
		}

		return sanitaryPhytosanitaryMeasuresRefs.map(sanitaryPhytosanitaryMeasuresRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), sanitaryPhytosanitaryMeasuresRef.getId());

			SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang = new SanitaryPhytosanitaryMeasuresRefLang();

			sanitaryPhytosanitaryMeasuresRefLang.setId(sanitaryPhytosanitaryMeasuresRef.getId());
			sanitaryPhytosanitaryMeasuresRefLang.setCode(sanitaryPhytosanitaryMeasuresRef.getCode()!=null ?sanitaryPhytosanitaryMeasuresRef.getCode():"");

			sanitaryPhytosanitaryMeasuresRefLang.setCountryRef(sanitaryPhytosanitaryMeasuresRef.getCountryRef().getReference());
			sanitaryPhytosanitaryMeasuresRefLang.setOrganization(sanitaryPhytosanitaryMeasuresRef.getOrganization().getReference());


			sanitaryPhytosanitaryMeasuresRefLang.setCreatedBy(sanitaryPhytosanitaryMeasuresRef.getCreatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setCreatedOn(sanitaryPhytosanitaryMeasuresRef.getCreatedOn());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedBy(sanitaryPhytosanitaryMeasuresRef.getUpdatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedOn(sanitaryPhytosanitaryMeasuresRef.getUpdatedOn());

			sanitaryPhytosanitaryMeasuresRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			sanitaryPhytosanitaryMeasuresRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			sanitaryPhytosanitaryMeasuresRefLang.setLang(codeLang);

			sanitaryPhytosanitaryMeasuresRefLangs.add(sanitaryPhytosanitaryMeasuresRefLang);

			return  sanitaryPhytosanitaryMeasuresRefLang ;
		});
	}



	public Page<SanitaryPhytosanitaryMeasuresRefLang> filterForPortal(MspAndBarriersFilterPayload mspAndBarriersFilterPayload, int page, int size, String codeLang, String orderDirection) {
		if(mspAndBarriersFilterPayload.getCode().equals("") && mspAndBarriersFilterPayload.getCustomRegimCode().equals("") && mspAndBarriersFilterPayload.getCountryReference().equals("") && mspAndBarriersFilterPayload.getOrganizationReference().equals("")){
			return getAll(page, size, codeLang, orderDirection);
		}
		else {
			if(mspAndBarriersFilterPayload.getCode().equals("")){
				mspAndBarriersFilterPayload.setCode(null);
			}
			if(mspAndBarriersFilterPayload.getCustomRegimCode().equals("")){
				mspAndBarriersFilterPayload.setCustomRegimCode(null);
			}
			if(mspAndBarriersFilterPayload.getCountryReference().equals("")){
				mspAndBarriersFilterPayload.setCountryReference(null);
			}
			if(mspAndBarriersFilterPayload.getOrganizationReference().equals("")){
				mspAndBarriersFilterPayload.setOrganizationReference(null);
			}
		}
		//if(mspAndBarriersFilterPayload.getOrganizationReference().equals("")) mspAndBarriersFilterPayload.setOrganizationReference(null) ;
		Page<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs = mspAndBarriersFilterPayload.getCustomRegimCode() != null
				? sanitaryPhytosanitaryMeasuresRefRepository.findMspsWithCustomRegime(
				mspAndBarriersFilterPayload.getCountryReference(), mspAndBarriersFilterPayload.getCode(),
				mspAndBarriersFilterPayload.getOrganizationReference(),
				mspAndBarriersFilterPayload.getCustomRegimCode(), PageRequest.of(page, size))
				: sanitaryPhytosanitaryMeasuresRefRepository
				.findMspsWithoutCustomRegime(mspAndBarriersFilterPayload.getCountryReference(),
						mspAndBarriersFilterPayload.getCode(),
						mspAndBarriersFilterPayload.getOrganizationReference(), PageRequest.of(page, size));

		Lang lang = langRepository.findByCode(codeLang);
		List<SanitaryPhytosanitaryMeasuresRefLang> sanitaryPhytosanitaryMeasuresRefLangs = new ArrayList<>();

		return sanitaryPhytosanitaryMeasuresRefs.map(sanitaryPhytosanitaryMeasuresRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), sanitaryPhytosanitaryMeasuresRef.getId());

			SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang = new SanitaryPhytosanitaryMeasuresRefLang();

			sanitaryPhytosanitaryMeasuresRefLang.setId(sanitaryPhytosanitaryMeasuresRef.getId());
			sanitaryPhytosanitaryMeasuresRefLang.setCode(sanitaryPhytosanitaryMeasuresRef.getCode()!=null ?sanitaryPhytosanitaryMeasuresRef.getCode():"");

			sanitaryPhytosanitaryMeasuresRefLang.setCountryRef(sanitaryPhytosanitaryMeasuresRef.getCountryRef().getReference());
			sanitaryPhytosanitaryMeasuresRefLang.setOrganization(sanitaryPhytosanitaryMeasuresRef.getOrganization().getReference());


			sanitaryPhytosanitaryMeasuresRefLang.setCreatedBy(sanitaryPhytosanitaryMeasuresRef.getCreatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setCreatedOn(sanitaryPhytosanitaryMeasuresRef.getCreatedOn());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedBy(sanitaryPhytosanitaryMeasuresRef.getUpdatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedOn(sanitaryPhytosanitaryMeasuresRef.getUpdatedOn());

			sanitaryPhytosanitaryMeasuresRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			sanitaryPhytosanitaryMeasuresRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			sanitaryPhytosanitaryMeasuresRefLang.setLang(codeLang);

			sanitaryPhytosanitaryMeasuresRefLangs.add(sanitaryPhytosanitaryMeasuresRefLang);

			return  sanitaryPhytosanitaryMeasuresRefLang ;
		});
	}

	public Page<SanitaryPhytosanitaryMeasuresRefLang> subPortalProductInformationFinderfilter(
			SubportalProductInformationFinderFilterPayload payload, int page, int size, String codeLang, String orderDirection) {
		if(payload.getTarifBookReference().equals("") && payload.getCountryRefCode().equals("") && payload.getCustomRegimeCode().equals("")){
			return getAll(page, size, codeLang, orderDirection);
		}
		else {
			if(payload.getTarifBookReference().equals("")){
				payload.setTarifBookReference(null);
			}
			if(payload.getCountryRefCode().equals("")){
				payload.setCountryRefCode(null);
			}
			if(payload.getCustomRegimeCode().equals("")){
				payload.setCustomRegimeCode(null);
			}
		}

		Page<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs = sanitaryPhytosanitaryMeasuresRefRepository
				.subportalProductInformationFinderFilter(payload.getCountryRefCode(), payload.getCustomRegimeCode(),
						payload.getTarifBookReference(), PageRequest.of(page, size));

		Lang lang = langRepository.findByCode(codeLang);
		List<SanitaryPhytosanitaryMeasuresRefLang> sanitaryPhytosanitaryMeasuresRefLangs = new ArrayList<>();

		return sanitaryPhytosanitaryMeasuresRefs.map(sanitaryPhytosanitaryMeasuresRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), sanitaryPhytosanitaryMeasuresRef.getId());

			SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang = new SanitaryPhytosanitaryMeasuresRefLang();

			sanitaryPhytosanitaryMeasuresRefLang.setId(sanitaryPhytosanitaryMeasuresRef.getId());
			sanitaryPhytosanitaryMeasuresRefLang.setCode(sanitaryPhytosanitaryMeasuresRef.getCode()!=null ?sanitaryPhytosanitaryMeasuresRef.getCode():"");

			sanitaryPhytosanitaryMeasuresRefLang.setCountryRef(sanitaryPhytosanitaryMeasuresRef.getCountryRef().getReference());
			sanitaryPhytosanitaryMeasuresRefLang.setOrganization(sanitaryPhytosanitaryMeasuresRef.getOrganization().getReference());


			sanitaryPhytosanitaryMeasuresRefLang.setCreatedBy(sanitaryPhytosanitaryMeasuresRef.getCreatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setCreatedOn(sanitaryPhytosanitaryMeasuresRef.getCreatedOn());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedBy(sanitaryPhytosanitaryMeasuresRef.getUpdatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedOn(sanitaryPhytosanitaryMeasuresRef.getUpdatedOn());

			sanitaryPhytosanitaryMeasuresRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			sanitaryPhytosanitaryMeasuresRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			sanitaryPhytosanitaryMeasuresRefLang.setLang(codeLang);

			sanitaryPhytosanitaryMeasuresRefLangs.add(sanitaryPhytosanitaryMeasuresRefLang);

			return  sanitaryPhytosanitaryMeasuresRefLang ;
		});
	}

	@Override
	public Page<SanitaryPhytosanitaryMeasuresRefLang> filterForSubPortal(
			SubPortalMspAndBarrsFilterPayload subPortalMspAndBarrsFilterPayload, int page, int size, String codeLang) {

		if(subPortalMspAndBarrsFilterPayload.getExpCountry().equals("")) {
			subPortalMspAndBarrsFilterPayload.setExpCountry(null);
		}
		if(subPortalMspAndBarrsFilterPayload.getImpCounty().equals("")) {
			subPortalMspAndBarrsFilterPayload.setImpCounty(null);
		}
		if(subPortalMspAndBarrsFilterPayload.getOrganizationReference().equals("")) {
			subPortalMspAndBarrsFilterPayload.setOrganizationReference(null);
		}
		List<String> countryRefs = Arrays.asList(new String[] { subPortalMspAndBarrsFilterPayload.getExpCountry(),
				subPortalMspAndBarrsFilterPayload.getImpCounty() });

		Page<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs=sanitaryPhytosanitaryMeasuresRefRepository.findMspsWithCountries(countryRefs,subPortalMspAndBarrsFilterPayload.getCode(), subPortalMspAndBarrsFilterPayload.getOrganizationReference(), PageRequest.of(page, size));;

		Lang lang = langRepository.findByCode(codeLang);
		List<SanitaryPhytosanitaryMeasuresRefLang> sanitaryPhytosanitaryMeasuresRefLangs = new ArrayList<>();

		return sanitaryPhytosanitaryMeasuresRefs.map(sanitaryPhytosanitaryMeasuresRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), sanitaryPhytosanitaryMeasuresRef.getId());

			SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang = new SanitaryPhytosanitaryMeasuresRefLang();

			sanitaryPhytosanitaryMeasuresRefLang.setId(sanitaryPhytosanitaryMeasuresRef.getId());
			sanitaryPhytosanitaryMeasuresRefLang.setCode(sanitaryPhytosanitaryMeasuresRef.getCode()!=null ?sanitaryPhytosanitaryMeasuresRef.getCode():"");

			sanitaryPhytosanitaryMeasuresRefLang.setCountryRef(sanitaryPhytosanitaryMeasuresRef.getCountryRef().getReference());
			sanitaryPhytosanitaryMeasuresRefLang.setOrganization(sanitaryPhytosanitaryMeasuresRef.getOrganization().getReference());


			sanitaryPhytosanitaryMeasuresRefLang.setCreatedBy(sanitaryPhytosanitaryMeasuresRef.getCreatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setCreatedOn(sanitaryPhytosanitaryMeasuresRef.getCreatedOn());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedBy(sanitaryPhytosanitaryMeasuresRef.getUpdatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedOn(sanitaryPhytosanitaryMeasuresRef.getUpdatedOn());

			sanitaryPhytosanitaryMeasuresRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			sanitaryPhytosanitaryMeasuresRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			sanitaryPhytosanitaryMeasuresRefLang.setLang(codeLang);

			sanitaryPhytosanitaryMeasuresRefLangs.add(sanitaryPhytosanitaryMeasuresRefLang);

			return  sanitaryPhytosanitaryMeasuresRefLang ;
		});

	}


	@Override
	public ByteArrayInputStream load(String codeLang) {

		Lang lang = langRepository.findByCode(codeLang);
		List<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs = sanitaryPhytosanitaryMeasuresRefRepository.findAll();
		List<SanitaryPhytosanitaryMeasuresRefLang> sanitaryPhytosanitaryMeasuresRefLangs = new ArrayList<>();

		for (SanitaryPhytosanitaryMeasuresRef sanitaryPhytosanitaryMeasuresRef : sanitaryPhytosanitaryMeasuresRefs) {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), sanitaryPhytosanitaryMeasuresRef.getId());
			if (entityRefLang != null) {
				SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang = new SanitaryPhytosanitaryMeasuresRefLang();


				sanitaryPhytosanitaryMeasuresRefLang.setCode(sanitaryPhytosanitaryMeasuresRef.getCode());


				sanitaryPhytosanitaryMeasuresRefLang.setCountryRef(sanitaryPhytosanitaryMeasuresRef.getCountryRef().getReference());
				sanitaryPhytosanitaryMeasuresRefLang.setOrganization(sanitaryPhytosanitaryMeasuresRef.getOrganization().getReference());


				sanitaryPhytosanitaryMeasuresRefLang.setLabel(entityRefLang.getLabel());
				sanitaryPhytosanitaryMeasuresRefLang.setDescription(entityRefLang.getDescription());
				sanitaryPhytosanitaryMeasuresRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
				sanitaryPhytosanitaryMeasuresRefLangs.add(sanitaryPhytosanitaryMeasuresRefLang);
			}
		}
		ByteArrayInputStream in = sanitaryPhytosanitaryMeasuresRefsToExcel(sanitaryPhytosanitaryMeasuresRefLangs);
		return in;

	}

	public ByteArrayInputStream sPMRefLangsToExcel(List<SanitaryPhytosanitaryMesuresExcelDTO> spmsExcelDtos) {
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
				if (!spmsExcelDtos.isEmpty()) {

					for (SanitaryPhytosanitaryMesuresExcelDTO unitRefEntityRefLang : spmsExcelDtos) {
						Row row = sheet.createRow(rowIdx++);
						row.createCell(0).setCellValue(unitRefEntityRefLang.getCode());
						row.createCell(1).setCellValue(unitRefEntityRefLang.getReferenceCountry());
						row.createCell(2).setCellValue(unitRefEntityRefLang.getOrganization());
						row.createCell(3).setCellValue(unitRefEntityRefLang.getLabel());
						row.createCell(4).setCellValue(unitRefEntityRefLang.getDescription());
						row.createCell(5).setCellValue(unitRefEntityRefLang.getLang());
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
	public SanitaryPhytosanitaryMeasuresRefLang findSanitaryPhytosanitaryMeasures(Long id, String lang){
		SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang = new SanitaryPhytosanitaryMeasuresRefLang();
		SanitaryPhytosanitaryMeasuresRef sanitaryPhytosanitaryMeasuresRef = sanitaryPhytosanitaryMeasuresRefRepository.findOneById(id);
		EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF,langRepository.findByCode(lang).getId(),sanitaryPhytosanitaryMeasuresRef.getId());
		sanitaryPhytosanitaryMeasuresRefLang.setId(sanitaryPhytosanitaryMeasuresRef.getId());
		sanitaryPhytosanitaryMeasuresRef.setVersion_nbr(sanitaryPhytosanitaryMeasuresRef.getVersion_nbr());
		sanitaryPhytosanitaryMeasuresRef.setCreatedOn(sanitaryPhytosanitaryMeasuresRef.getCreatedOn());
		sanitaryPhytosanitaryMeasuresRef.setCreatedBy(sanitaryPhytosanitaryMeasuresRef.getCreatedBy());
		sanitaryPhytosanitaryMeasuresRef.setUpdatedBy(sanitaryPhytosanitaryMeasuresRef.getUpdatedBy());
		sanitaryPhytosanitaryMeasuresRef.setUpdatedOn(sanitaryPhytosanitaryMeasuresRef.getUpdatedOn());
		sanitaryPhytosanitaryMeasuresRefLang.setCode(sanitaryPhytosanitaryMeasuresRef.getCode());
		sanitaryPhytosanitaryMeasuresRefLang.setGeneralDescription(sanitaryPhytosanitaryMeasuresRef.getGeneralDescription());
		sanitaryPhytosanitaryMeasuresRefLang.setCountryRef(sanitaryPhytosanitaryMeasuresRef.getCountryRef().getReference());
		sanitaryPhytosanitaryMeasuresRefLang.setOrganization(sanitaryPhytosanitaryMeasuresRef.getOrganization().getReference());
		sanitaryPhytosanitaryMeasuresRefLang.setLabel(entityRefLang!=null? entityRefLang.getLabel():"");
		sanitaryPhytosanitaryMeasuresRefLang.setDescription(entityRefLang!=null? entityRefLang.getDescription():"");
		sanitaryPhytosanitaryMeasuresRefLang.setGeneralDescription(sanitaryPhytosanitaryMeasuresRef.getGeneralDescription());
		sanitaryPhytosanitaryMeasuresRefLang.setLang(lang);
		return sanitaryPhytosanitaryMeasuresRefLang;
	}

	public SanitaryPhytosanitaryMeasuresRefLangDetailed findSanitaryPhytosanitaryMeasureDetailed( SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang){
		return helperMapper.toDetailedSanitaryPhytosanitaryMeasure(sanitaryPhytosanitaryMeasuresRefLang);
	}

	@Override
	public List<InternationalizationVM> getInternationalizationRefList(Long id) {
		return internationalizationHelper.getInternationalizationRefList(id, TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF);
	}

	@Override
	public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
		entityRefLang.setTableRef(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF);
		entityRefLang.setLang(langRepository.findByCode(lang));
		if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
			entityRefLangRepository.save(entityRefLang);
		else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
	}


	@Override
	public void addSanitaryPhytosanitaryMeasuresRef(SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang){
		SanitaryPhytosanitaryMeasuresRef sanitaryPhytosanitaryMeasuresRef = new SanitaryPhytosanitaryMeasuresRef();

		sanitaryPhytosanitaryMeasuresRef.setCode(sanitaryPhytosanitaryMeasuresRefLang.getCode());

		sanitaryPhytosanitaryMeasuresRef.setCountryRef(countryRefRepository.findByReference(sanitaryPhytosanitaryMeasuresRefLang.getCountryRef()));
		sanitaryPhytosanitaryMeasuresRef.setOrganization(organizationRepository.findByReference(sanitaryPhytosanitaryMeasuresRefLang.getOrganization()));
		sanitaryPhytosanitaryMeasuresRef.setGeneralDescription(sanitaryPhytosanitaryMeasuresRefLang.getGeneralDescription());
		SanitaryPhytosanitaryMeasuresRef ltemp = sanitaryPhytosanitaryMeasuresRefRepository.findByCode(sanitaryPhytosanitaryMeasuresRefLang.getCode());

		if (ltemp == null) {
			Long id = (sanitaryPhytosanitaryMeasuresRefRepository.save(sanitaryPhytosanitaryMeasuresRef)).getId();
			EntityRefLang entityRefLang = new EntityRefLang();

			entityRefLang.setLabel(sanitaryPhytosanitaryMeasuresRefLang.getLabel());
			entityRefLang.setDescription(sanitaryPhytosanitaryMeasuresRefLang.getDescription());
			entityRefLang.setRefId(id);
			entityRefLang.setTableRef(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF);
			entityRefLang.setLang(langRepository.findByCode(sanitaryPhytosanitaryMeasuresRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);

		} else {
			Long id = ltemp.getId();
			sanitaryPhytosanitaryMeasuresRef.setId(id);
			sanitaryPhytosanitaryMeasuresRefRepository.save(sanitaryPhytosanitaryMeasuresRef);
			EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, langRepository.findByCode(sanitaryPhytosanitaryMeasuresRefLang.getLang()).getId() ,id);
			entityRefLang.setLabel(sanitaryPhytosanitaryMeasuresRefLang.getLabel());
			entityRefLang.setDescription(sanitaryPhytosanitaryMeasuresRefLang.getDescription());
			entityRefLang.setLang(  langRepository.findByCode(sanitaryPhytosanitaryMeasuresRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);
		}
	}


	private SanitaryPhytosanitaryMeasuresRefBean toSanitaryPhytosanitaryMeasuresRefBeanMapper(
			SanitaryPhytosanitaryMeasuresRef sanitaryPhytosanitaryMeasuresRef) {
		SanitaryPhytosanitaryMeasuresRefBean sanitaryPhytosanitaryMeasuresRefBean = new SanitaryPhytosanitaryMeasuresRefBean();
		sanitaryPhytosanitaryMeasuresRefBean.setId(sanitaryPhytosanitaryMeasuresRef.getId());
		sanitaryPhytosanitaryMeasuresRefBean.setCreatedOn(sanitaryPhytosanitaryMeasuresRef.getCreatedOn());
		sanitaryPhytosanitaryMeasuresRefBean.setCode(sanitaryPhytosanitaryMeasuresRef.getCode());
		sanitaryPhytosanitaryMeasuresRefBean.setLabel(sanitaryPhytosanitaryMeasuresRef.getLabel());
		sanitaryPhytosanitaryMeasuresRefBean.setDescription(sanitaryPhytosanitaryMeasuresRef.getDescription());
		sanitaryPhytosanitaryMeasuresRefBean.setLang(sanitaryPhytosanitaryMeasuresRef.getLang());
		sanitaryPhytosanitaryMeasuresRefBean.setCountryRef(toCountryRefBeanMapper(sanitaryPhytosanitaryMeasuresRef.getCountryRef()));
		sanitaryPhytosanitaryMeasuresRefBean.setOrganization(toOrganizationBeanMapper(sanitaryPhytosanitaryMeasuresRef.getOrganization()));

		return sanitaryPhytosanitaryMeasuresRefBean;
	}

	private CountryRefBean toCountryRefBeanMapper(CountryRef countryRef) {
		if (Objects.isNull(countryRef)) {
			return null;
		}

		CountryRefBean countryRefBean = new CountryRefBean();
		countryRefBean.setId(countryRef.getId());
		countryRefBean.setCreatedOn(countryRef.getCreatedOn());
		countryRefBean.setUpdatedOn(countryRef.getUpdatedOn());
		countryRefBean.setReference(countryRef.getReference());
		countryRefBean.setCodeIso(countryRef.getCodeIso());

		return countryRefBean;
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

	private CityRefBean toCityRefBeanMapper(CityRef cityRef) {
		if (Objects.isNull(cityRef)) {
			return null;
		}

		CityRefBean cityRefBean = new CityRefBean();
		cityRefBean.setReference(cityRef.getReference());

		return cityRefBean;
	}

	public Page<SanitaryPhytosanitaryMeasuresRefLang> mapSanitaryPhytosanitaryMeasuresRefsToRefLangs(Page<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs, String codeLang) {
		Lang lang = langRepository.findByCode(codeLang);
		List<SanitaryPhytosanitaryMeasuresRefLang> sanitaryPhytosanitaryMeasuresRefLangs = new ArrayList<>();

		return sanitaryPhytosanitaryMeasuresRefs.map(sanitaryPhytosanitaryMeasuresRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.SANITARY_PHYTOSANITARY_MEASURES_REF, lang.getId(), sanitaryPhytosanitaryMeasuresRef.getId());

			SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang = new SanitaryPhytosanitaryMeasuresRefLang();

			sanitaryPhytosanitaryMeasuresRefLang.setId(sanitaryPhytosanitaryMeasuresRef.getId());
			sanitaryPhytosanitaryMeasuresRefLang.setCode(sanitaryPhytosanitaryMeasuresRef.getCode()!=null ?sanitaryPhytosanitaryMeasuresRef.getCode():"");

			sanitaryPhytosanitaryMeasuresRefLang.setCountryRef(sanitaryPhytosanitaryMeasuresRef.getCountryRef().getReference());
			sanitaryPhytosanitaryMeasuresRefLang.setOrganization(sanitaryPhytosanitaryMeasuresRef.getOrganization().getReference());
			sanitaryPhytosanitaryMeasuresRefLang.setGeneralDescription(sanitaryPhytosanitaryMeasuresRef.getGeneralDescription());

			sanitaryPhytosanitaryMeasuresRefLang.setCreatedBy(sanitaryPhytosanitaryMeasuresRef.getCreatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setCreatedOn(sanitaryPhytosanitaryMeasuresRef.getCreatedOn());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedBy(sanitaryPhytosanitaryMeasuresRef.getUpdatedBy());
			sanitaryPhytosanitaryMeasuresRefLang.setUpdatedOn(sanitaryPhytosanitaryMeasuresRef.getUpdatedOn());

			sanitaryPhytosanitaryMeasuresRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			sanitaryPhytosanitaryMeasuresRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			sanitaryPhytosanitaryMeasuresRefLang.setLang(codeLang);

			sanitaryPhytosanitaryMeasuresRefLangs.add(sanitaryPhytosanitaryMeasuresRefLang);

			return  sanitaryPhytosanitaryMeasuresRefLang ;
		});
	}

	public Page<SanitaryPhytosanitaryMeasuresRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang)  {
		return mapSanitaryPhytosanitaryMeasuresRefsToRefLangs(sanitaryPhytosanitaryMeasuresRefRepository.filterByCodeOrLabel(value ,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
	}

	@Override
	public Page<SanitaryPhytosanitaryMeasuresRefLangProjection> filterByCodeOrLabelProjection(String value, String lang, Pageable pageable) {
		return sanitaryPhytosanitaryMeasuresRefRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
	}

	@Override
	public SanitaryPhytosanitaryMeasuresRefBean findById(Long id) {
		SanitaryPhytosanitaryMeasuresRef result= sanitaryPhytosanitaryMeasuresRefRepository.findById(id).orElseThrow(
				()->new NotFoundException("id not found"));
		return sanitaryPhytosanitaryMeasuresRefMapper.entityToBean(result);
	}


}