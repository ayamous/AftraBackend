package ma.itroad.aace.eth.coref.service.impl;

import edu.emory.mathcs.backport.java.util.Arrays;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.*;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.TaxationMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.TaxationRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.repository.*;
import ma.itroad.aace.eth.coref.service.ITaxationService;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.TariffBookRefLang;
import ma.itroad.aace.eth.coref.service.helper.TaxationRefLang;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.service.helper.TechnicalBarrierRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.TaxationRefLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.detailed.TechnicalBarrierRefLangDetailed;
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

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaxationServiceImpl extends BaseServiceImpl<Taxation, TaxationBean> implements ITaxationService {

	static String[] HEADERs = { "REFERENCE TAXATION", "TAUX IMPOSITION", "VALEUR IMPOSITION", "REFERENCE PAYS", "CODE REGIME", "CODE UNITE REF", "CODE TYPE DE TAXE", "LABEL", "DESCRIPTION", "LANG" };
	static String SHEET = "TaxationSHEET";

	@Autowired
	private TaxationRepository taxationRepository;

	@Autowired
	private Validator validator;

	@Autowired
	private TaxationMapper taxationMapper;

	@Autowired
	private CountryRefRepository countryRefRepository;

	@Autowired
	private CustomsRegimRefRepository customsRegimRefRepository;

	@Autowired
	private UnitRefRepository unitRefRepository;

	@Autowired
	private TaxRefRepository taxRefRepository;

	@Autowired
	private LangRepository langRepository;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

	@Autowired
	private InternationalizationHelper internationalizationHelper;

	@Autowired
	private HelperMapper helperMapper ;


	@Override
	public List<Taxation> saveAll(List<Taxation> taxations) {
		if (!taxations.isEmpty()) {
			return taxationRepository.saveAll(taxations);
		}
		return null;
	}

	public Page<TaxationRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
		Page<Taxation> taxations = null;
		if(orderDirection.equals("DESC")){
			taxations = taxationRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}else if(orderDirection.equals("ASC")){
			taxations = taxationRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}

		Lang lang = langRepository.findByCode(codeLang);
		List<TaxationRefLang> taxationRefLangs = new ArrayList<>();
		return taxations.map(taxation -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF, lang.getId(), taxation.getId());
			TaxationRefLang taxationRefLang = new TaxationRefLang( ) ;
			taxationRefLang.setId(taxation.getId());

			taxationRefLang.setReference(taxation.getReference()!=null ?taxation.getReference():"");
			taxationRefLang.setRate(taxation.getRate()!=null ?taxation.getRate():"");
			taxationRefLang.setValue(taxation.getValue()!=null ?taxation.getValue():"");

			taxationRefLang.setCountryRef(taxation.getCountryRef().getReference()!=null ?taxation.getCountryRef().getReference():"");
			taxationRefLang.setCustomsRegimRef(taxation.getCustomsRegimRef().getCode()!=null ?taxation.getCustomsRegimRef().getCode():"");
			taxationRefLang.setUnitRef(taxation.getUnitRef().getCode()!=null ?taxation.getUnitRef().getCode():"");
			taxationRefLang.setTaxRef(taxation.getTaxRef().getCode()!=null ?taxation.getTaxRef().getCode():"");

			taxationRefLang.setCreatedBy(taxation.getCreatedBy());
			taxationRefLang.setCreatedOn(taxation.getCreatedOn());
			taxationRefLang.setUpdatedBy(taxation.getUpdatedBy());
			taxationRefLang.setUpdatedOn(taxation.getUpdatedOn());
			taxationRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			taxationRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");
			taxationRefLang.setLang(codeLang);
			taxationRefLangs.add(taxationRefLang);
			return  taxationRefLang ;
		});
	}

	private UnitRefBean toUnitRefBean(UnitRef unitRef) {
		UnitRefBean out = new UnitRefBean();
		out.setCode(unitRef.getCode());
		out.setCreatedBy(unitRef.getCreatedBy());
		out.setId(unitRef.getId());
		out.setVersion_nbr(unitRef.getVersion_nbr());
		return out;
	}

	private TaxationBean toBeanMapper(Taxation taxation) {
		TaxationBean out = new TaxationBean();
		out.setId(taxation.getId());
		out.setCreatedOn(taxation.getCreatedOn());
		out.setReference(taxation.getReference());
		if (taxation.getUnitRef() != null) {
			out.setUnitRef(toUnitRefBean(taxation.getUnitRef()));
		}
		out.setLabel(taxation.getLabel());
		out.setGeneralDescription(taxation.getGeneralDescription());
		out.setLang(taxation.getLang());
		if (taxation.getCountryRef() != null) {
			out.setCountryRef(toCountryRefBeanMapper(taxation.getCountryRef()));
		}
		out.setVersion_nbr(taxation.getVersion_nbr());
		
		if (!Objects.isNull(taxation.getCustomsRegimRef())) {
			out.setCustomsRegimRef(toCustomsRegimRefBeanMapper(taxation.getCustomsRegimRef()));
		}
		
		if (!Objects.isNull(taxation.getTaxRef())) {
			out.setTaxRef(toTaxRefBeanMapper(taxation.getTaxRef()));
		}
		
		return out;
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
	
	private CustomsRegimRefBean toCustomsRegimRefBeanMapper(CustomsRegimRef customsRegimRef) {
		CustomsRegimRefBean customsRegimRefBean = new CustomsRegimRefBean();
		customsRegimRefBean.setId(customsRegimRef.getId());
		customsRegimRefBean.setCode(customsRegimRef.getCode());
		customsRegimRefBean.setCreatedOn(customsRegimRef.getCreatedOn());
		customsRegimRefBean.setRegimType(customsRegimRef.getRegimType());
		
		return customsRegimRefBean;
	}
	
	private TaxRefBean toTaxRefBeanMapper(TaxRef taxRef) {
		TaxRefBean taxRefBean = new TaxRefBean();
		taxRefBean.setId(taxRef.getId());
		taxRefBean.setCode(taxRef.getCode());
		taxRefBean.setCreatedOn(taxRef.getCreatedOn());
		taxRefBean.setCountryRef(toCountryRefBeanMapper(taxRef.getCountryRef()));
		
		return taxRefBean;
	}

	@Override
	public void saveFromExcel(MultipartFile file) {
		try {
			List<TaxationRefLang> taxationRefLangs = excelToTaxations(file.getInputStream());
			if (!taxationRefLangs.isEmpty()) {
				for (TaxationRefLang l : taxationRefLangs) {
					Taxation taxation = new Taxation();

					taxation.setReference(l.getReference());
					taxation.setRate(l.getRate());
					taxation.setValue(l.getValue());

					taxation.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
					taxation.setCustomsRegimRef(customsRegimRefRepository.findByCode(l.getCustomsRegimRef()));
					taxation.setUnitRef(unitRefRepository.findByCode(l.getUnitRef()));
					taxation.setTaxRef(taxRefRepository.findByCode(l.getTaxRef()));


					Taxation ltemp = taxationRepository.findByReference(l.getReference());
					if (ltemp == null) {
						Long id = (taxationRepository.save(taxation)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setGeneralDescription(l.getGeneralDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.TAXATION_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()) : langRepository.findAll().get(0));
						entityRefLangRepository.save(entityRefLang);
					} else {
						Long id = ltemp.getId();
						taxation.setId(id);
						taxationRepository.save(taxation);
						EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF,langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()).getId() : langRepository.findAll().get(0).getId(),id);
						if(entityRefLang == null )
						{
							entityRefLang =new EntityRefLang() ;
							entityRefLang.setLang(langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()) : langRepository.findAll().get(0));
							entityRefLang.setTableRef(TableRef.TAXATION_REF);
							entityRefLang.setRefId(taxation.getId());
						}
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setGeneralDescription(l.getGeneralDescription());
						entityRefLang.setLang(langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()) : langRepository.findAll().get(0));
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
	public Page<TaxationBean> getAll(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Taxation> taxPage = taxationRepository.findAll(pageable);

		Page<TaxationBean> taxationBeanPage = taxPage.map(taxation -> {
			return taxationMapper.entityToBean(taxation);
		});
		return taxationBeanPage;
	}*/

	public Page<TaxationBean> getAll(Pageable pageable) {
		Page<Taxation> entities = taxationRepository.findAll(pageable);
		Page<TaxationBean> result = entities.map(taxationMapper::entityToBean);
		return result;
	}

	@Override
	public List<InternationalizationVM> getInternationalizationRefList(Long id) {
		return internationalizationHelper.getInternationalizationRefList(id, TableRef.TAXATION_REF);
	}

	@Override
	public List<TaxationRefLang> excelToTaxations(InputStream is) {
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);
			List<TaxationRefLang> taxationRefLangs = new ArrayList<TaxationRefLang>();
			Row currentRow;
			TaxationRefLang taxationRefLang ;
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				currentRow= sheet.getRow(rowNum);
				taxationRefLang = new TaxationRefLang();
				for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
					Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
					switch (colNum) {
						case 0:
							taxationRefLang.setReference(Util.cellValue(currentCell));
							break;
						case 1:
							taxationRefLang.setRate(Util.cellValue(currentCell));
							break;
						case 2:
							taxationRefLang.setValue(Util.cellValue(currentCell));
							break;

						case 3:
							taxationRefLang.setCountryRef(Util.cellValue(currentCell));
							break;
						case 4:
							taxationRefLang.setCustomsRegimRef(Util.cellValue(currentCell));
							break;
						case 5:
							taxationRefLang.setUnitRef(Util.cellValue(currentCell));
							break;
						case 6:
							taxationRefLang.setTaxRef(Util.cellValue(currentCell));
							break;
						case 7:
							taxationRefLang.setLabel(Util.cellValue(currentCell));
							break;
						case 8:
							taxationRefLang.setGeneralDescription(Util.cellValue(currentCell));
							break;
						case 9:
							taxationRefLang.setLang(Util.cellValue(currentCell));
							break;
						default:
							break;
					}
				}
				taxationRefLangs.add(taxationRefLang);
			}
			workbook.close();
			return taxationRefLangs;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}

	@Override
	public ByteArrayInputStream load() {
		List<Taxation> taxations = taxationRepository.findAll();
		ByteArrayInputStream in = null;
		return in;
	}

	@Override
	public ByteArrayInputStream load(String codeLang) {
		Lang lang = langRepository.findByCode(codeLang);
		List<Taxation> taxations = taxationRepository.findAll();
		List<TaxationRefLang> taxationRefLangs = new ArrayList<TaxationRefLang>();

		for(Taxation u : taxations) {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF, lang.getId(), u.getId());
			if (entityRefLang!=null) {
				TaxationRefLang taxationRefLang = new TaxationRefLang();

				taxationRefLang.setReference(u.getReference());
				taxationRefLang.setRate(u.getRate());
				taxationRefLang.setValue(u.getValue());

				taxationRefLang.setCountryRef(u.getCountryRef().getReference());
				taxationRefLang.setCustomsRegimRef(u.getCustomsRegimRef().getCode());
				taxationRefLang.setUnitRef(u.getUnitRef().getCode());
				taxationRefLang.setTaxRef(u.getTaxRef().getCode());

				taxationRefLang.setLabel(entityRefLang.getLabel() != null ? entityRefLang.getLabel() : null);
				taxationRefLang.setGeneralDescription(entityRefLang.getGeneralDescription() != null ? entityRefLang.getGeneralDescription() : null);
				taxationRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
				taxationRefLangs.add(taxationRefLang);
			}
		}
		ByteArrayInputStream in = taxationsToExcel(taxationRefLangs);
		return in;
	}


	@Override
	public ByteArrayInputStream taxationsToExcel(List<TaxationRefLang> taxationRefLangs) {
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
				if (!taxationRefLangs.isEmpty()) {

					for (TaxationRefLang taxationRefLang : taxationRefLangs) {
						Row row = sheet.createRow(rowIdx++);
						row.createCell(0).setCellValue(taxationRefLang.getReference());
						row.createCell(1).setCellValue(taxationRefLang.getRate());
						row.createCell(2).setCellValue(taxationRefLang.getValue());

						row.createCell(3).setCellValue(taxationRefLang.getCountryRef());
						row.createCell(4).setCellValue(taxationRefLang.getCustomsRegimRef());
						row.createCell(5).setCellValue(taxationRefLang.getUnitRef());
						row.createCell(6).setCellValue(taxationRefLang.getTaxRef());

						row.createCell(7).setCellValue(taxationRefLang.getLabel());
						row.createCell(8).setCellValue(taxationRefLang.getGeneralDescription());
						row.createCell(9).setCellValue(taxationRefLang.getLang());
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

	/*public Page<TaxationBean> filterForPortal(
			ProductInformationFinderFilterPayload productInformationFinderFilterPayload, int page, int size) {
		List<String> countryReferences = Arrays
				.asList(new String[] { productInformationFinderFilterPayload.getExportCountryCode(),
						productInformationFinderFilterPayload.getImportCountryCode() });
		Page<TaxationBean> response = taxationRepository
				.findByFilter(productInformationFinderFilterPayload.getTarifBookReference(), countryReferences,
						PageRequest.of(page, size))
				.map(ta -> taxationMapper.entityToBean(ta));
		return response;
	}
	 */
	@Override
	public Page<TaxationRefLang> filterForPortal(ProductInformationFinderFilterPayload productInformationFinderFilterPayload, int page, int size,String codeLang, String orderDirection) {
		List<String> countryReferences = Arrays
				.asList(new String[] { productInformationFinderFilterPayload.getExportCountryCode(),
						productInformationFinderFilterPayload.getImportCountryCode() });
		List<CountryRef> countries = countryReferences.stream().filter(cr -> cr != null)
				.map(cr -> countryRefRepository.findByReference(cr)).collect(Collectors.toList());

		Lang lang = langRepository.findByCode(codeLang);
		List<TaxationRefLang> taxationRefLangs = new ArrayList<>();
		Page<Taxation> taxations=null;

		if (productInformationFinderFilterPayload.getTarifBookReference() != null && !productInformationFinderFilterPayload.getTarifBookReference().equals("")){
			taxations = taxationRepository.findByFilter(productInformationFinderFilterPayload.getTarifBookReference(), countryReferences,
					PageRequest.of(page, size));
		}
		else if (productInformationFinderFilterPayload.getExportCountryCode().equals("") && productInformationFinderFilterPayload.getImportCountryCode().equals("")){
			return getAll(page, size, codeLang, orderDirection);
		} else {
			taxations = taxationRepository.findByFilterWithoutTarifBook(countries, PageRequest.of(page, size));
		}

		return taxations.map(taxation -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF, lang.getId(), taxation.getId());
			TaxationRefLang taxationRefLang = new TaxationRefLang( ) ;
			taxationRefLang.setId(taxation.getId());

			taxationRefLang.setReference(taxation.getReference()!=null ?taxation.getReference():"");
			taxationRefLang.setRate(taxation.getRate()!=null ?taxation.getRate():"");
			taxationRefLang.setValue(taxation.getValue()!=null ?taxation.getValue():"");

			taxationRefLang.setCountryRef(taxation.getCountryRef().getReference()!=null ?taxation.getCountryRef().getReference():"");
			taxationRefLang.setCustomsRegimRef(taxation.getCustomsRegimRef().getCode()!=null ?taxation.getCustomsRegimRef().getCode():"");
			taxationRefLang.setUnitRef(taxation.getUnitRef().getCode()!=null ?taxation.getUnitRef().getCode():"");
			taxationRefLang.setTaxRef(taxation.getTaxRef().getCode()!=null ?taxation.getTaxRef().getCode():"");

			taxationRefLang.setCreatedBy(taxation.getCreatedBy());
			taxationRefLang.setCreatedOn(taxation.getCreatedOn());
			taxationRefLang.setUpdatedBy(taxation.getUpdatedBy());
			taxationRefLang.setUpdatedOn(taxation.getUpdatedOn());
			taxationRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			taxationRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");
			taxationRefLang.setLang(codeLang);
			taxationRefLangs.add(taxationRefLang);
			return  taxationRefLang ;
		});
	}

	@Override
	public ErrorResponse delete(Long id) {
		ErrorResponse response = new ErrorResponse();
		try {
			List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.TAXATION_REF, id);
			for(EntityRefLang entityRefLang:entityRefLangs ){
				entityRefLangRepository.delete(entityRefLang);
			}
			taxationRepository.deleteById(id);
			response.setStatus(HttpStatus.OK);
			response.setErrorMsg("null");
		} catch (Exception e) {
			response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
			response.setStatus(HttpStatus.CONFLICT);
		}
		return  response;
	}

	@Override
	public ErrorResponse deleteList(ListOfObject listOfObject) {
		ErrorResponse response = new ErrorResponse();

		try {
			for(Long id : listOfObject.getListOfObject()){
				List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.TAXATION_REF, id);
				for(EntityRefLang entityRefLang:entityRefLangs ){
					entityRefLangRepository.delete(entityRefLang);
				}
				taxationRepository.deleteById(id);
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
	public ErrorResponse deleteInternationalisation(String codeLang, Long taxationId) {
		ErrorResponse response = new ErrorResponse();
		try {
			Lang lang = langRepository.findByCode(codeLang);
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF, lang.getId(), taxationId);
			entityRefLangRepository.delete(entityRefLang);
			response.setStatus(HttpStatus.OK);
			response.setErrorMsg("null");
		} catch (Exception e) {
			response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
			response.setStatus(HttpStatus.CONFLICT);
		}
		return response;
	}


	public Page<TaxationRefLang> subPortalProductInformationFinderfilter(
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
		Page<Taxation> taxations = taxationRepository.subportalProductInformationFinderFilter(payload.getCountryRefCode(),
				payload.getCustomRegimeCode(), payload.getTarifBookReference(), PageRequest.of(page, size));
		Lang lang = langRepository.findByCode(codeLang);
		List<TaxationRefLang> taxationRefLangs = new ArrayList<>();

		return taxations.map(taxation -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF, lang.getId(), taxation.getId());
			TaxationRefLang taxationRefLang = new TaxationRefLang( ) ;
			taxationRefLang.setId(taxation.getId());

			taxationRefLang.setReference(taxation.getReference()!=null ?taxation.getReference():"");
			taxationRefLang.setRate(taxation.getRate()!=null ?taxation.getRate():"");
			taxationRefLang.setValue(taxation.getValue()!=null ?taxation.getValue():"");

			taxationRefLang.setCountryRef(taxation.getCountryRef().getReference()!=null ?taxation.getCountryRef().getReference():"");
			taxationRefLang.setCustomsRegimRef(taxation.getCustomsRegimRef().getCode()!=null ?taxation.getCustomsRegimRef().getCode():"");
			taxationRefLang.setUnitRef(taxation.getUnitRef().getCode()!=null ?taxation.getUnitRef().getCode():"");
			taxationRefLang.setTaxRef(taxation.getTaxRef().getCode()!=null ?taxation.getTaxRef().getCode():"");

			taxationRefLang.setCreatedBy(taxation.getCreatedBy());
			taxationRefLang.setCreatedOn(taxation.getCreatedOn());
			taxationRefLang.setUpdatedBy(taxation.getUpdatedBy());
			taxationRefLang.setUpdatedOn(taxation.getUpdatedOn());
			taxationRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			taxationRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");
			taxationRefLang.setLang(codeLang);
			taxationRefLangs.add(taxationRefLang);
			return  taxationRefLang ;
		});
	}

	@Override
	public TaxationRefLang findTaxation(Long id, String lang){
		TaxationRefLang taxationRefLang = new TaxationRefLang();

		Taxation taxation = taxationRepository.findOneById(id);
		EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF,langRepository.findByCode(lang).getId(),taxation.getId());

		taxationRefLang.setCountryRef(taxation.getCountryRef().getReference());
		taxationRefLang.setCustomsRegimRef(taxation.getCustomsRegimRef().getCode());
		taxationRefLang.setUnitRef(taxation.getUnitRef().getCode());
		taxationRefLang.setTaxRef(taxation.getTaxRef().getCode());

		taxationRefLang.setReference(taxation.getReference());
		taxationRefLang.setRate(taxation.getRate());
		taxationRefLang.setValue(taxation.getValue());
		taxationRefLang.setGeneralDescription(taxation.getGeneralDescription());

		taxationRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
		taxationRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");
		taxationRefLang.setLang(lang);
		return taxationRefLang;
	}

	@Override
	public void addInternationalisation(EntityRefLang entityRefLang,String lang){
		entityRefLang.setTableRef(TableRef.TAXATION_REF);
		entityRefLang.setLang(langRepository.findByCode(lang));
		if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
			entityRefLangRepository.save(entityRefLang);
		else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
	}


	@Override
	public void addTaxation(TaxationRefLang taxationRefLang){
		Taxation taxation = new Taxation();

		taxation.setReference(taxationRefLang.getReference());
		taxation.setRate(taxationRefLang.getRate());
		taxation.setValue(taxationRefLang.getValue());

		taxation.setCountryRef(countryRefRepository.findByReference(taxationRefLang.getCountryRef()));
		taxation.setCustomsRegimRef(customsRegimRefRepository.findByCode(taxationRefLang.getCustomsRegimRef()));
		taxation.setUnitRef(unitRefRepository.findByCode(taxationRefLang.getUnitRef()));
		taxation.setTaxRef(taxRefRepository.findByCode(taxationRefLang.getTaxRef()));

		Taxation ltemp = taxationRepository.findByReference(taxationRefLang.getReference());
		if (ltemp == null) {
			Long id = (taxationRepository.save(taxation)).getId();
			EntityRefLang entityRefLang = new EntityRefLang();
			entityRefLang.setLabel(taxationRefLang.getLabel());
			entityRefLang.setGeneralDescription(taxationRefLang.getGeneralDescription());
			entityRefLang.setRefId(id);
			entityRefLang.setTableRef(TableRef.TAXATION_REF);
			entityRefLang.setLang(langRepository.findByCode(taxationRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);
		} else {
			Long id = ltemp.getId();
			taxation.setId(id);
			taxationRepository.save(taxation);
			EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF, langRepository.findByCode(taxationRefLang.getLang()).getId() ,id);
			entityRefLang.setLabel(taxationRefLang.getLabel());
			entityRefLang.setGeneralDescription(taxationRefLang.getGeneralDescription());
			entityRefLangRepository.save(entityRefLang);
		}
	}

	public Page<TaxationRefLang> mapToRefLangs(Page<Taxation> taxations, String codeLang) {
		Lang lang = langRepository.findByCode(codeLang);
		List<TaxationRefLang> taxationRefLangs = new ArrayList<>();

		return taxations.map(taxation -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF, lang.getId(), taxation.getId());
			TaxationRefLang taxationRefLang = new TaxationRefLang( ) ;
			taxationRefLang.setId(taxation.getId());

			taxationRefLang.setReference(taxation.getReference()!=null ?taxation.getReference():"");
			taxationRefLang.setRate(taxation.getRate()!=null ?taxation.getRate():"");
			taxationRefLang.setValue(taxation.getValue()!=null ?taxation.getValue():"");

			taxationRefLang.setCountryRef(taxation.getCountryRef().getReference()!=null ?taxation.getCountryRef().getReference():"");
			taxationRefLang.setCustomsRegimRef(taxation.getCustomsRegimRef().getCode()!=null ?taxation.getCustomsRegimRef().getCode():"");
			taxationRefLang.setUnitRef(taxation.getUnitRef().getCode()!=null ?taxation.getUnitRef().getCode():"");
			taxationRefLang.setTaxRef(taxation.getTaxRef().getCode()!=null ?taxation.getTaxRef().getCode():"");

			taxationRefLang.setCreatedBy(taxation.getCreatedBy());
			taxationRefLang.setCreatedOn(taxation.getCreatedOn());
			taxationRefLang.setUpdatedBy(taxation.getUpdatedBy());
			taxationRefLang.setUpdatedOn(taxation.getUpdatedOn());
			taxationRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			taxationRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");
			taxationRefLang.setLang(codeLang);
			taxationRefLangs.add(taxationRefLang);
			return  taxationRefLang ;
		});
	}

	public Page<TaxationRefLang> filterByReferenceOrLabel(String value, Pageable pageable, String codeLang)  {
		return mapToRefLangs(taxationRepository.filterByReferenceOrLabel(value ,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
	}

	public TaxationRefLangDetailed findTaxationDetailedDetailed(TaxationRefLang taxationRefLang){
		return  helperMapper.toDetailedTaxation(taxationRefLang);
	}

	@Override
	public Page<TaxationRefLangProjection> filterByReferenceOrLabelProjection(String value, String lang, Pageable pageable) {
		return taxationRepository.filterByReferenceOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
	}

	@Override
	public TaxationBean findById(Long id) {
		Taxation result= taxationRepository.findById(id).orElseThrow(
				()->new NotFoundException("id not found"));
		return taxationMapper.entityToBean(result);
	}

	public Set<ConstraintViolation<TaxationRefLang>> validateItems(TaxationRefLang taxationRefLang) {
		Set<ConstraintViolation<TaxationRefLang>> violations = validator.validate(taxationRefLang);
		return violations;
	}

	@Override
	public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {
			List<TaxationRefLang> itemsList = excelToTaxations(file.getInputStream());
			List<TaxationRefLang> invalidItems = new ArrayList<TaxationRefLang>();
			List<TaxationRefLang> validItems = new ArrayList<TaxationRefLang>();

			int lenght = itemsList.size();


			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<TaxationRefLang>> violations = validateItems(itemsList.get(i));
				if (violations.isEmpty())
				{
					validItems.add(itemsList.get(i));
				} else {
					invalidItems.add(itemsList.get(i));
				}
			}

			if (!invalidItems.isEmpty()) {

				ByteArrayInputStream out = taxationsToExcel(invalidItems);
				xls = new InputStreamResource(out);

			}


			for (TaxationRefLang l : validItems) {
				Taxation taxation = new Taxation();

				taxation.setReference(l.getReference());
				taxation.setRate(l.getRate());
				taxation.setValue(l.getValue());

				taxation.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
				taxation.setCustomsRegimRef(customsRegimRefRepository.findByCode(l.getCustomsRegimRef()));
				taxation.setUnitRef(unitRefRepository.findByCode(l.getUnitRef()));
				taxation.setTaxRef(taxRefRepository.findByCode(l.getTaxRef()));


				Taxation ltemp = taxationRepository.findByReference(l.getReference());
				if (ltemp == null) {
					Long id = (taxationRepository.save(taxation)).getId();
					EntityRefLang entityRefLang = new EntityRefLang();
					entityRefLang.setLabel(l.getLabel());
					entityRefLang.setGeneralDescription(l.getGeneralDescription());
					entityRefLang.setRefId(id);
					entityRefLang.setTableRef(TableRef.TAXATION_REF);
					entityRefLang.setLang(langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()) : langRepository.findAll().get(0));
					entityRefLangRepository.save(entityRefLang);
				} else {
					Long id = ltemp.getId();
					taxation.setId(id);
					taxationRepository.save(taxation);
					EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TAXATION_REF,langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()).getId() : langRepository.findAll().get(0).getId(),id);
					if(entityRefLang == null )
					{
						entityRefLang =new EntityRefLang() ;
						entityRefLang.setLang(langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()) : langRepository.findAll().get(0));
						entityRefLang.setTableRef(TableRef.TAXATION_REF);
						entityRefLang.setRefId(taxation.getId());
					}
					entityRefLang.setLabel(l.getLabel());
					entityRefLang.setGeneralDescription(l.getGeneralDescription());
					entityRefLang.setLang(langRepository.findByCode(l.getLang())!=null?langRepository.findByCode(l.getLang()) : langRepository.findAll().get(0));
					entityRefLangRepository.save(entityRefLang);
				}

			}
			if (!invalidItems.isEmpty())
				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);

			if (!validItems.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(null);

			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (IOException e) {

			return ResponseEntity.status(HttpStatus.OK).body("fail to store excel data: " + e.getMessage());
		}
	}

}
