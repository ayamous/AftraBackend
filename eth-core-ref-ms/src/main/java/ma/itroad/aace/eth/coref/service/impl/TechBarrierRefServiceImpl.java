package ma.itroad.aace.eth.coref.service.impl;

import edu.emory.mathcs.backport.java.util.Arrays;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ChapterNotFoundException;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.exception.TechBarrierNotFoundException;
import ma.itroad.aace.eth.coref.exception.TranslationFoundException;
import ma.itroad.aace.eth.coref.model.bean.*;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.ChapterRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.TechBarrierRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.TechnicalBarrierRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.*;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.repository.CustomsRegimRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.OrganizationRepository;
import ma.itroad.aace.eth.coref.repository.TechBarrierRefRepository;
import ma.itroad.aace.eth.coref.service.ITechBarrierRefService;
import ma.itroad.aace.eth.coref.service.helper.*;
import ma.itroad.aace.eth.coref.service.helper.detailed.AgreementLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.detailed.RegulationRefLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.detailed.TechnicalBarrierRefLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.mappers.HelperMapper;
import ma.itroad.aace.eth.coref.service.impl.exceldto.TechnicalBarrierExcelDto;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TechBarrierRefServiceImpl extends BaseServiceImpl<TechBarrierRef, TechBarrierRefBean> implements ITechBarrierRefService {

	static String[] HEADERs = {"techBarrierRef code", "countryRef reference", "CustomsRegim reference", "Organization reference", "LABEL","DESCRIPTION", "LANGUE"};
	static String SHEET = "TechBarrierRefsSHEET";

	@Autowired
	TechBarrierRefRepository techBarrierRefRepository;

	@Autowired
	TechBarrierRefMapper techBarrierRefMapper;

	@Autowired
	CountryRefRepository countryRefRepository;
	
	@Autowired
	CustomsRegimRefRepository customsRegimRefRepository;
	
	@Autowired
	OrganizationRepository organizationRepository;
	
	ModelMapper modelMapper = new ModelMapper();

	@Autowired
	private HelperMapper helperMapper ;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

	@Autowired
	LangRepository langRepository;

	@Autowired
	private InternationalizationHelper internationalizationHelper;


	@Override
	public List<TechBarrierRef> saveAll(List<TechBarrierRef> techBarrierRefs) {
		if (!techBarrierRefs.isEmpty()) {
			return techBarrierRefRepository.saveAll(techBarrierRefs);
		}
		return null;
	}


	public Page<TechnicalBarrierRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
		Page<TechBarrierRef> techBarrierRefs = null;
		if(orderDirection.equals("DESC")){
			techBarrierRefs = techBarrierRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}else if(orderDirection.equals("ASC")){
			techBarrierRefs = techBarrierRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}
		Lang lang = langRepository.findByCode(codeLang);
		List<TechnicalBarrierRefLang> technicalBarrierRefLangs = new ArrayList<>();

		return techBarrierRefs.map(techBarrierRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF, lang.getId(), techBarrierRef.getId());

			TechnicalBarrierRefLang technicalBarrierRefLang = new TechnicalBarrierRefLang();

			technicalBarrierRefLang.setId(techBarrierRef.getId());
			technicalBarrierRefLang.setCode(techBarrierRef.getCode()!=null ?techBarrierRef.getCode():"");


			technicalBarrierRefLang.setCountryRef(techBarrierRef.getCountryRef()!=null ?techBarrierRef.getCountryRef().getReference():"");
			technicalBarrierRefLang.setCustomsRegimRef(techBarrierRef.getCustomsRegimRef()!=null ?techBarrierRef.getCustomsRegimRef().getCode():"");
			technicalBarrierRefLang.setOrganization(techBarrierRef.getOrganization()!=null ?techBarrierRef.getOrganization().getReference():"");


			technicalBarrierRefLang.setCreatedBy(techBarrierRef.getCreatedBy());
			technicalBarrierRefLang.setCreatedOn(techBarrierRef.getCreatedOn());
			technicalBarrierRefLang.setUpdatedBy(techBarrierRef.getUpdatedBy());
			technicalBarrierRefLang.setUpdatedOn(techBarrierRef.getUpdatedOn());

			technicalBarrierRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			technicalBarrierRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");

			technicalBarrierRefLang.setLang(codeLang);

			technicalBarrierRefLangs.add(technicalBarrierRefLang);

			return  technicalBarrierRefLang ;
		});
	}


	@Override
	public void saveFromExcel(MultipartFile file) {

		try {
			List<TechnicalBarrierRefLang> technicalBarrierRefLangs = excelToTechBarrierRefs(file.getInputStream());
			if (!technicalBarrierRefLangs.isEmpty()) {
				for (TechnicalBarrierRefLang l : technicalBarrierRefLangs) {
					TechBarrierRef techBarrierRef = new TechBarrierRef();
					techBarrierRef.setCode(l.getCode());
					techBarrierRef.setCountryRef(countryRefRepository.findByReference(l.getCountryRef()));
					techBarrierRef.setCustomsRegimRef(customsRegimRefRepository.findByCode(l.getCustomsRegimRef()));
					techBarrierRef.setOrganization(organizationRepository.findByReference(l.getOrganization()));
					//techBarrierRef.setGeneralDescription(l.getGeneralDescription());
					TechBarrierRef ltemp = techBarrierRefRepository.findByCode(l.getCode());
					if (ltemp == null) {
						Long id = (techBarrierRefRepository.save(techBarrierRef)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setGeneralDescription(l.getGeneralDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.TECH_BARRIER_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}
					else {
						techBarrierRef.setId( ltemp.getId());
						techBarrierRefRepository.save(techBarrierRef);
						EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF,langRepository.findByCode(l.getLang()).getId(),techBarrierRef.getId());
						if(entityRefLang == null )
						{
							entityRefLang =new EntityRefLang() ;
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.TECH_BARRIER_REF);
							entityRefLang.setRefId(techBarrierRef.getId());
						}
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setGeneralDescription(l.getGeneralDescription());
						entityRefLangRepository.save(entityRefLang);
					}

				}
			}
		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}
	}

	@Override
	public ByteArrayInputStream load() {
		List<TechBarrierRef> techBarrierRefs = techBarrierRefRepository.findAll();
		ByteArrayInputStream in = null;
		return in;
	}

	public Page<TechBarrierRefBean> getAll(Pageable pageable) {
		Page<TechBarrierRef> entities = techBarrierRefRepository.findAll(pageable);
		Page<TechBarrierRefBean> result = entities.map(techBarrierRefMapper::entityToBean);
		return result;

	}


	@Override
	public ByteArrayInputStream load(String codeLang) {

		Lang lang = langRepository.findByCode(codeLang);
		List<TechBarrierRef> techBarrierRefs = techBarrierRefRepository.findAll();
		List<TechnicalBarrierRefLang> technicalBarrierRefLangs = new ArrayList<>();

		for (TechBarrierRef techBarrierRef : techBarrierRefs) {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF, lang.getId(), techBarrierRef.getId());
			if (entityRefLang != null) {
				TechnicalBarrierRefLang technicalBarrierRefLang = new TechnicalBarrierRefLang();


				technicalBarrierRefLang.setCode(techBarrierRef.getCode());

				technicalBarrierRefLang.setCountryRef(techBarrierRef.getCountryRef().getReference());
				technicalBarrierRefLang.setCustomsRegimRef(techBarrierRef.getCustomsRegimRef().getCode());
				technicalBarrierRefLang.setOrganization(techBarrierRef.getOrganization().getReference());


				technicalBarrierRefLang.setLabel(entityRefLang.getLabel());
				technicalBarrierRefLang.setGeneralDescription(entityRefLang.getGeneralDescription());
				technicalBarrierRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
				technicalBarrierRefLangs.add(technicalBarrierRefLang);
			}
		}
		ByteArrayInputStream in = techBarrierRefsToExcel(technicalBarrierRefLangs);
		return in;

	}

	@Override
	public ByteArrayInputStream techBarrierRefsToExcel(List<TechnicalBarrierRefLang> technicalBarrierRefLangs) {
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
				if (!technicalBarrierRefLangs.isEmpty()) {

					for (TechnicalBarrierRefLang technicalBarrierRefLang : technicalBarrierRefLangs) {
						Row row = sheet.createRow(rowIdx++);

						row.createCell(0).setCellValue(technicalBarrierRefLang.getCode());

						row.createCell(1).setCellValue(technicalBarrierRefLang.getCountryRef());
						row.createCell(2).setCellValue(technicalBarrierRefLang.getCustomsRegimRef());
						row.createCell(3).setCellValue(technicalBarrierRefLang.getOrganization());

						row.createCell(4).setCellValue(technicalBarrierRefLang.getLabel());
						row.createCell(5).setCellValue(technicalBarrierRefLang.getGeneralDescription());
						row.createCell(6).setCellValue(technicalBarrierRefLang.getLang());
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

	String cellValue( Cell cell) {
		switch (cell.getCellType()) {
			case 0:
				return Integer.valueOf((int) cell.getNumericCellValue()).toString();
			case 1:
				return cell.getStringCellValue() ;
			default:return null ;
		}
	}

	public boolean isCellValid(String s) {
		return s != null && s.matches("^[ a-zA-Z0-9]*$");
	}

	@Override
	public List<TechnicalBarrierRefLang> excelToTechBarrierRefs(InputStream is) {
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);
			Iterator<Row> rows = sheet.iterator();
			List<TechnicalBarrierRefLang> technicalBarrierRefLangs = new ArrayList<TechnicalBarrierRefLang>();
			Row currentRow = rows.next();
			while (rows.hasNext()) {
				currentRow = rows.next();
				TechnicalBarrierRefLang technicalBarrierRefLang = new TechnicalBarrierRefLang();
				int cellIdx = 0;
				for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
					Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
					switch (cellIdx) {
						case 0:
							technicalBarrierRefLang.setCode(cellValue(currentCell));
							break;
						case 1:
							technicalBarrierRefLang.setCountryRef(cellValue(currentCell));
							break;
						case 2:
							technicalBarrierRefLang.setCustomsRegimRef(cellValue(currentCell));
							break;
						case 3:
							technicalBarrierRefLang.setOrganization(cellValue(currentCell));
							break;
						case 4:
							technicalBarrierRefLang.setLabel(cellValue(currentCell));
							break;
						case 5:
							technicalBarrierRefLang.setGeneralDescription(cellValue(currentCell));
							break;
						case 6:
							technicalBarrierRefLang.setLang(cellValue(currentCell));
							break;
						default:
							break;
					}
					cellIdx++;
				}
				technicalBarrierRefLangs.add(technicalBarrierRefLang);
			}
			workbook.close();
			return technicalBarrierRefLangs;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}

	@Override
	public ErrorResponse delete(Long id) {
		ErrorResponse response = new ErrorResponse();
		try {
			List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.TECH_BARRIER_REF, id);
			for (EntityRefLang entityRefLang : entityRefLangs) {
				entityRefLangRepository.delete(entityRefLang);
			}
			techBarrierRefRepository.deleteById(id);
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

	@Override
	public void addTechnicalBarrier(TechnicalBarrierRefLang technicalBarrierRefLang){
		TechBarrierRef techBarrierRef = new TechBarrierRef();

		techBarrierRef.setCode(technicalBarrierRefLang.getCode());

		techBarrierRef.setCountryRef(countryRefRepository.findByReference(technicalBarrierRefLang.getCountryRef()));
		techBarrierRef.setCustomsRegimRef(customsRegimRefRepository.findByCode(technicalBarrierRefLang.getCustomsRegimRef()));
		techBarrierRef.setOrganization(organizationRepository.findByReference(technicalBarrierRefLang.getOrganization()));
		//techBarrierRef.setGeneralDescription(technicalBarrierRefLang.getGeneralDescription());

		TechBarrierRef ltemp = techBarrierRefRepository.findByCode(technicalBarrierRefLang.getCode());

		if (ltemp == null) {
			Long id = (techBarrierRefRepository.save(techBarrierRef)).getId();
			EntityRefLang entityRefLang = new EntityRefLang();

			entityRefLang.setLabel(technicalBarrierRefLang.getLabel());
			entityRefLang.setGeneralDescription(technicalBarrierRefLang.getGeneralDescription());
			entityRefLang.setRefId(id);
			entityRefLang.setTableRef(TableRef.TECH_BARRIER_REF);
			entityRefLang.setLang(langRepository.findByCode(technicalBarrierRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);

		} else {
			Long id = ltemp.getId();
			techBarrierRef.setId(id);
			techBarrierRefRepository.save(techBarrierRef);
			EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF, langRepository.findByCode(technicalBarrierRefLang.getLang()).getId() ,id);
			entityRefLang.setLabel(technicalBarrierRefLang.getLabel());
			entityRefLang.setGeneralDescription(technicalBarrierRefLang.getGeneralDescription());
			entityRefLang.setLang(  langRepository.findByCode(technicalBarrierRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);
		}
	}

	@Override
	public List<InternationalizationVM> getInternationalizationRefList(Long id) {
		return internationalizationHelper.getInternationalizationRefList(id, TableRef.TECH_BARRIER_REF);
	}

	@Override
	public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
		entityRefLang.setTableRef(TableRef.TECH_BARRIER_REF);
		entityRefLang.setLang(langRepository.findByCode(lang));
		if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
			entityRefLangRepository.save(entityRefLang);
		else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
	}

	@Override
	public ErrorResponse deleteInternationalisation(String codeLang, Long chapterRefId) {

		ErrorResponse response = new ErrorResponse();
		try {

			Lang lang = langRepository.findByCode(codeLang);

			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF, lang.getId(), chapterRefId);

			entityRefLangRepository.delete(entityRefLang);
			response.setStatus(HttpStatus.OK);
			response.setErrorMsg("null");


		} catch (Exception e) {
			response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
			response.setStatus(HttpStatus.CONFLICT);
		}
		return response;
	}

	public Page<TechnicalBarrierRefLang> subPortalProductInformationFinderfilter(
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
		Page<TechBarrierRef> techBarrierRefs = techBarrierRefRepository
				.subportalProductInformationFinderFilter(payload.getCountryRefCode(), payload.getCustomRegimeCode(),
						payload.getTarifBookReference(), PageRequest.of(page, size));

		Lang lang = langRepository.findByCode(codeLang);
		List<TechnicalBarrierRefLang> technicalBarrierRefLangs = new ArrayList<>();
		return  techBarrierRefs.map(techBarrierRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF, lang.getId(), techBarrierRef.getId());

			TechnicalBarrierRefLang technicalBarrierRefLang = new TechnicalBarrierRefLang();

			technicalBarrierRefLang.setId(techBarrierRef.getId());
			technicalBarrierRefLang.setCode(techBarrierRef.getCode()!=null ?techBarrierRef.getCode():"");


			technicalBarrierRefLang.setCountryRef(techBarrierRef.getCountryRef()!=null ?techBarrierRef.getCountryRef().getReference():"");
			technicalBarrierRefLang.setCustomsRegimRef(techBarrierRef.getCustomsRegimRef()!=null ?techBarrierRef.getCustomsRegimRef().getCode():"");
			technicalBarrierRefLang.setOrganization(techBarrierRef.getOrganization()!=null ?techBarrierRef.getOrganization().getReference():"");


			technicalBarrierRefLang.setCreatedBy(techBarrierRef.getCreatedBy());
			technicalBarrierRefLang.setCreatedOn(techBarrierRef.getCreatedOn());
			technicalBarrierRefLang.setUpdatedBy(techBarrierRef.getUpdatedBy());
			technicalBarrierRefLang.setUpdatedOn(techBarrierRef.getUpdatedOn());

			technicalBarrierRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			technicalBarrierRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");

			technicalBarrierRefLang.setLang(codeLang);

			technicalBarrierRefLangs.add(technicalBarrierRefLang);

			return  technicalBarrierRefLang ;
		});
	}

	@Override
	public TechnicalBarrierRefLang findTechnicalBarrier(Long id, String lang){
		TechnicalBarrierRefLang technicalBarrierRefLang = new TechnicalBarrierRefLang();
		TechBarrierRef techBarrierRef = techBarrierRefRepository.findOneById(id);
		EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF,langRepository.findByCode(lang).getId(),techBarrierRef.getId());
		technicalBarrierRefLang.setCode(techBarrierRef.getCode());
		//technicalBarrierRefLang.setGeneralDescription(techBarrierRef.getGeneralDescription());
		technicalBarrierRefLang.setCountryRef(techBarrierRef.getCountryRef()!=null?techBarrierRef.getCountryRef().getReference():null);
		technicalBarrierRefLang.setOrganization(techBarrierRef.getOrganization()!=null?techBarrierRef.getOrganization().getReference():null);
		technicalBarrierRefLang.setCustomsRegimRef(techBarrierRef.getCustomsRegimRef()!=null?techBarrierRef.getCustomsRegimRef().getCode():null);

		technicalBarrierRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
		technicalBarrierRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");
		technicalBarrierRefLang.setLang(lang);
		return technicalBarrierRefLang;
	}


	@Override
	public Page<TechnicalBarrierRefLang> filterForSubPortal(
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

		Page<TechBarrierRef> techBarrierRefs = techBarrierRefRepository
				.findTechBarrsWithCountries(countryRefs, subPortalMspAndBarrsFilterPayload.getCode(),
						subPortalMspAndBarrsFilterPayload.getOrganizationReference(), PageRequest.of(page, size));

		Lang lang = langRepository.findByCode(codeLang);
		List<TechnicalBarrierRefLang> technicalBarrierRefLangs = new ArrayList<>();
		return  techBarrierRefs.map(techBarrierRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF, lang.getId(), techBarrierRef.getId());

			TechnicalBarrierRefLang technicalBarrierRefLang = new TechnicalBarrierRefLang();

			technicalBarrierRefLang.setId(techBarrierRef.getId());
			technicalBarrierRefLang.setCode(techBarrierRef.getCode()!=null ?techBarrierRef.getCode():"");


			technicalBarrierRefLang.setCountryRef(techBarrierRef.getCountryRef().getReference());
			technicalBarrierRefLang.setCustomsRegimRef(
					techBarrierRef.getCustomsRegimRef()!=null ? techBarrierRef.getCustomsRegimRef().getCode() : ""
					);
			technicalBarrierRefLang.setOrganization(techBarrierRef.getOrganization()!=null ?
					techBarrierRef.getOrganization().getReference() : "");


			technicalBarrierRefLang.setCreatedBy(techBarrierRef.getCreatedBy());
			technicalBarrierRefLang.setCreatedOn(techBarrierRef.getCreatedOn());
			technicalBarrierRefLang.setUpdatedBy(techBarrierRef.getUpdatedBy());
			technicalBarrierRefLang.setUpdatedOn(techBarrierRef.getUpdatedOn());

			technicalBarrierRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			technicalBarrierRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");

			technicalBarrierRefLang.setLang(codeLang);

			technicalBarrierRefLangs.add(technicalBarrierRefLang);

			return  technicalBarrierRefLang ;
		});


	}

	public Page<TechnicalBarrierRefLang> techBarFilter(MspAndBarriersFilterPayload mspAndBarriersFilterPayload, int page, int size, String codeLang, String orderDirection) {
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
		Page<TechBarrierRef> techBarrierRefs = techBarrierRefRepository
				.techBarFilter2(mspAndBarriersFilterPayload.getCountryReference(),
						mspAndBarriersFilterPayload.getCustomRegimCode(), mspAndBarriersFilterPayload.getCode(),
						mspAndBarriersFilterPayload.getOrganizationReference(), PageRequest.of(page, size));

		Lang lang = langRepository.findByCode(codeLang);
		List<TechnicalBarrierRefLang> technicalBarrierRefLangs = new ArrayList<>();
		return  techBarrierRefs.map(techBarrierRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF, lang.getId(), techBarrierRef.getId());

			TechnicalBarrierRefLang technicalBarrierRefLang = new TechnicalBarrierRefLang();

			technicalBarrierRefLang.setId(techBarrierRef.getId());
			technicalBarrierRefLang.setCode(techBarrierRef.getCode()!=null ?techBarrierRef.getCode():"");


			technicalBarrierRefLang.setCountryRef(techBarrierRef.getCountryRef().getReference());
			technicalBarrierRefLang.setCustomsRegimRef(techBarrierRef.getCustomsRegimRef().getCode());
			technicalBarrierRefLang.setOrganization(techBarrierRef.getOrganization().getReference());


			technicalBarrierRefLang.setCreatedBy(techBarrierRef.getCreatedBy());
			technicalBarrierRefLang.setCreatedOn(techBarrierRef.getCreatedOn());
			technicalBarrierRefLang.setUpdatedBy(techBarrierRef.getUpdatedBy());
			technicalBarrierRefLang.setUpdatedOn(techBarrierRef.getUpdatedOn());

			technicalBarrierRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			technicalBarrierRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");

			technicalBarrierRefLang.setLang(codeLang);

			technicalBarrierRefLangs.add(technicalBarrierRefLang);

			return  technicalBarrierRefLang ;
		});
	}

	@Override
	public Page<TechnicalBarrierRefLang> techBarFilterByTarifBook(ProductInformationFinderFilterPayload productInformationFinderFilterPayload, int page, int size, String codeLang, String orderDirection) {

		List<String> countryReferences = Arrays
				.asList(new String[] { productInformationFinderFilterPayload.getExportCountryCode(),
						productInformationFinderFilterPayload.getImportCountryCode() });
		List<CountryRef> countries = countryReferences.stream().filter(cr -> cr != null)
				.map(cr -> countryRefRepository.findByReference(cr)).collect(Collectors.toList());

		Lang lang = langRepository.findByCode(codeLang);
		List<TechnicalBarrierRefLang> technicalBarrierRefLangs = new ArrayList<>();
		Page<TechBarrierRef> techBarrierRefs = null;
		//if(productInformationFinderFilterPayload.getTarifBookReference()=="")productInformationFinderFilterPayload.setTarifBookReference(null);
		if (productInformationFinderFilterPayload.getTarifBookReference() != null && !productInformationFinderFilterPayload.getTarifBookReference().equals("")){
			techBarrierRefs =  techBarrierRefRepository
					.techBarFilter(productInformationFinderFilterPayload.getTarifBookReference(),countryReferences, PageRequest.of(page, size));
		}
		else if (productInformationFinderFilterPayload.getExportCountryCode().equals("") && productInformationFinderFilterPayload.getImportCountryCode().equals("")){
			return getAll(page, size, codeLang, orderDirection);
		}
		else {
			techBarrierRefs = techBarrierRefRepository.findByCountryRefInList(countries, PageRequest.of(page, size));
		}

		return techBarrierRefs.map(techBarrierRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF, lang.getId(), techBarrierRef.getId());

			TechnicalBarrierRefLang technicalBarrierRefLang = new TechnicalBarrierRefLang();

			technicalBarrierRefLang.setId(techBarrierRef.getId());
			technicalBarrierRefLang.setCode(techBarrierRef.getCode()!=null ?techBarrierRef.getCode():"");

			technicalBarrierRefLang.setCountryRef(techBarrierRef.getCountryRef()!=null ?techBarrierRef.getCountryRef().getReference():"");
			technicalBarrierRefLang.setCustomsRegimRef(techBarrierRef.getCustomsRegimRef()!=null ?techBarrierRef.getCustomsRegimRef().getCode():"");
			technicalBarrierRefLang.setOrganization(techBarrierRef.getOrganization()!=null ?techBarrierRef.getOrganization().getReference():"");

			technicalBarrierRefLang.setCreatedBy(techBarrierRef.getCreatedBy());
			technicalBarrierRefLang.setCreatedOn(techBarrierRef.getCreatedOn());
			technicalBarrierRefLang.setUpdatedBy(techBarrierRef.getUpdatedBy());
			technicalBarrierRefLang.setUpdatedOn(techBarrierRef.getUpdatedOn());

			technicalBarrierRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			technicalBarrierRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");


			technicalBarrierRefLang.setLang(codeLang);

			technicalBarrierRefLangs.add(technicalBarrierRefLang);

			return  technicalBarrierRefLang ;
		});
	}

	public Page<TechnicalBarrierRefLang> mapLangToRefLangs(Page<TechBarrierRef> techBarrierRefs, String codeLang) {
		List<TechnicalBarrierRefLang> TechnicalBarrierRefLangs = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);
		return techBarrierRefs.map(techBarrierRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TECH_BARRIER_REF, lang.getId(), techBarrierRef.getId());
			TechnicalBarrierRefLang technicalBarrierRefLang = new TechnicalBarrierRefLang();

			technicalBarrierRefLang.setId(techBarrierRef.getId());
			technicalBarrierRefLang.setCode(techBarrierRef.getCode()!=null ?techBarrierRef.getCode():"");

			technicalBarrierRefLang.setCountryRef(techBarrierRef.getCountryRef()!=null ?techBarrierRef.getCountryRef().getReference():"");
			technicalBarrierRefLang.setCustomsRegimRef(techBarrierRef.getCustomsRegimRef()!=null ?techBarrierRef.getCustomsRegimRef().getCode():"");
			technicalBarrierRefLang.setOrganization(techBarrierRef.getOrganization()!=null ?techBarrierRef.getOrganization().getReference():"");
			technicalBarrierRefLang.setGeneralDescription(techBarrierRef.getGeneralDescription());

			technicalBarrierRefLang.setCreatedBy(techBarrierRef.getCreatedBy());
			technicalBarrierRefLang.setCreatedOn(techBarrierRef.getCreatedOn());
			technicalBarrierRefLang.setUpdatedBy(techBarrierRef.getUpdatedBy());
			technicalBarrierRefLang.setUpdatedOn(techBarrierRef.getUpdatedOn());

			technicalBarrierRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			technicalBarrierRefLang.setGeneralDescription(entityRefLang!=null ?entityRefLang .getGeneralDescription():"");
			technicalBarrierRefLang.setLang(codeLang);
			TechnicalBarrierRefLangs.add(technicalBarrierRefLang);
			return technicalBarrierRefLang;
		});
	}

	public Page<TechnicalBarrierRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
		return mapLangToRefLangs(techBarrierRefRepository.filterByCodeOrLabel(value,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
	}

	public TechnicalBarrierRefLangDetailed findTechnicalBarrierDetailedDetailed(TechnicalBarrierRefLang technicalBarrierRefLang ){
		return  helperMapper.toDetailedTechnicalBarrier(technicalBarrierRefLang);
	}


	@Override
	public Page<TechnicalBarrierRefLangProjection> filterByCodeOrLabelProjection(String value, String lang, Pageable pageable) {
		return techBarrierRefRepository.filterTechByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
	}

	@Override
	public TechBarrierRefBean findById(Long id) {
		TechBarrierRef result= techBarrierRefRepository.findById(id).orElseThrow(
				()->new NotFoundException("id not found"));
		return techBarrierRefMapper.entityToBean(result);
	}

}
