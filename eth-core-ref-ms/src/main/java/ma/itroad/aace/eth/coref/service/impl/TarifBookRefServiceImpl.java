package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.TariffBookRefBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.TariffBookRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.TariffBookRefLangProjection;
import ma.itroad.aace.eth.coref.repository.ChapterRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.TarifBookRefRepository;
import ma.itroad.aace.eth.coref.repository.UnitRefRepository;
import ma.itroad.aace.eth.coref.service.ITarifBookRefService;
import ma.itroad.aace.eth.coref.service.helper.ChapterRefLang;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.TariffBookRefLang;
import ma.itroad.aace.eth.coref.service.util.Util;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Service
public class TarifBookRefServiceImpl extends BaseServiceImpl<TarifBookRef, TariffBookRefBean> implements ITarifBookRefService {

	static String[] HEADERs = { "REFERENCE", "HS CODE", "ANNEE DE PRODUCTION", "DATE D'EXPIRATION", "REFERENCE CHAPITRE", "REFERENCE DE LA POSITION MERE",
			"REFERENCE UNITE",   "LABEL", "DESCRIPTION", "LANG" };

	static String SHEET = "TariffBookRefsSHEET";

	@Autowired
	private TarifBookRefRepository tarifBookRefRepository;

	@Autowired
	private TariffBookRefMapper tariffBookRefMapper;

	@Autowired
	private ChapterRefRepository chapterRefRepository;

	@Autowired
	private UnitRefRepository unitRefRepository;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

	@Autowired
	private LangRepository langRepository;

	@Autowired
	private InternationalizationHelper internationalizationHelper;

	@Autowired
	private Validator validator;

	@Override
	public List<TarifBookRef> saveAll(List<TarifBookRef> tarifBookRefs) {
		if (!tarifBookRefs.isEmpty()) {
			return tarifBookRefRepository.saveAll(tarifBookRefs);
		}
		return null;
	}

	public Page<TariffBookRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
		Page<TarifBookRef> tarifBookRefs = null;
		if(orderDirection.equals("DESC")){
			tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}else if(orderDirection.equals("ASC")){
			tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}
		Lang lang = langRepository.findByCode(codeLang);
		List<TariffBookRefLang> tariffBookRefLangs = new ArrayList<>();

		return tarifBookRefs.map(tarifBookRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), tarifBookRef.getId());

			TariffBookRefLang tariffBookRefLang = new TariffBookRefLang();

			tariffBookRefLang.setId(tarifBookRef.getId());
			tariffBookRefLang.setReference(tarifBookRef.getReference()!=null ?tarifBookRef.getReference():"");
			tariffBookRefLang.setHsCode(tarifBookRef.getHsCode()!=null ?tarifBookRef.getHsCode():"");
			tariffBookRefLang.setProductYear(tarifBookRef.getProductYear());
			tariffBookRefLang.setProductExpiryDate((tarifBookRef.getProductExpiryDate()));

			tariffBookRefLang.setChapterRef(tarifBookRef.getChapterRef() !=null? tarifBookRef.getChapterRef().getCode():null);
			tariffBookRefLang.setParent(tarifBookRef.getParent()!=null ?tarifBookRef.getParent().getReference():null);
			tariffBookRefLang.setUnitRef(tarifBookRef.getUnitRef()!=null ?tarifBookRef.getUnitRef().getCode():null);



			tariffBookRefLang.setCreatedBy(tarifBookRef.getCreatedBy());
			tariffBookRefLang.setCreatedOn(tarifBookRef.getCreatedOn());
			tariffBookRefLang.setUpdatedBy(tarifBookRef.getUpdatedBy());
			tariffBookRefLang.setUpdatedOn(tarifBookRef.getUpdatedOn());

			tariffBookRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			tariffBookRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			tariffBookRefLang.setLang(codeLang);

			tariffBookRefLangs.add(tariffBookRefLang);

			return  tariffBookRefLang ;
		});
	}

	@Override
	public ErrorResponse delete(Long id) {
		ErrorResponse response = new ErrorResponse();
		try {
			List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.TARIFF_BOOK_REF, id);
			for (EntityRefLang entityRefLang : entityRefLangs) {
				entityRefLangRepository.delete(entityRefLang);
			}
			tarifBookRefRepository.deleteById(id);
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
				List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.TARIFF_BOOK_REF, id);
				for (EntityRefLang entityRefLang : entityRefLangs) {
					entityRefLangRepository.delete(entityRefLang);
				}
				tarifBookRefRepository.deleteById(id);
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
	public ByteArrayInputStream tariffBookRefsToExcel(List<TariffBookRefLang> tariffBookRefLangs) {
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
				if (!tariffBookRefLangs.isEmpty()) {

					for (TariffBookRefLang tariffBookRefLang : tariffBookRefLangs) {
						Row row = sheet.createRow(rowIdx++);

						row.createCell(0).setCellValue(tariffBookRefLang.getReference());
						row.createCell(1).setCellValue(tariffBookRefLang.getHsCode());
						row.createCell(2).setCellValue(tariffBookRefLang.getProductYear()!=null ?tariffBookRefLang.getProductYear():"");
						row.createCell(3).setCellValue(tariffBookRefLang.getProductExpiryDate()!=null && tariffBookRefLang.getProductExpiryDate().toString()!= null ?tariffBookRefLang.getProductExpiryDate().toString():"");

						//row.createCell(1).setCellValue(chapterRefLang.getSectionRef().getCode());

						row.createCell(4).setCellValue(tariffBookRefLang.getChapterRef());
						row.createCell(5).setCellValue(tariffBookRefLang.getParent());
						row.createCell(6).setCellValue(tariffBookRefLang.getUnitRef());

						row.createCell(7).setCellValue(tariffBookRefLang.getLabel());
						row.createCell(8).setCellValue(tariffBookRefLang.getDescription());
						row.createCell(9).setCellValue(tariffBookRefLang.getLang());
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
	public List<TariffBookRefLang> excelToTariffBookRefs(InputStream is) {
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);
			List<TariffBookRefLang> tariffBookRefLangs = new ArrayList<TariffBookRefLang>();
			Row currentRow;
			TariffBookRefLang tariffBookRefLang ;
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				currentRow= sheet.getRow(rowNum);
				tariffBookRefLang = new TariffBookRefLang();
					for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
						Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
						switch (colNum) {
							case 0:
								tariffBookRefLang.setReference(Util.cellValue(currentCell));
								break;
							case 1:
								tariffBookRefLang.setHsCode(Util.cellValue(currentCell));
								break;
							case 2:
								tariffBookRefLang.setProductYear(Util.cellValue(currentCell));
								break;
							case 3:
								tariffBookRefLang.setProductExpiryDate(Util.cellValue(currentCell) != null?LocalDate.parse(Util.cellValue(currentCell)):null);
								break;
							case 4:
								tariffBookRefLang.setChapterRef(Util.cellValue(currentCell));
								break;
							case 5:
								tariffBookRefLang.setParent(Util.cellValue(currentCell));
								break;
							case 6:
								tariffBookRefLang.setUnitRef(Util.cellValue(currentCell));
								break;
							case 7:
								tariffBookRefLang.setLabel(Util.cellValue(currentCell));
								break;
							case 8:
								tariffBookRefLang.setDescription(Util.cellValue(currentCell));
								break;
							case 9:
								tariffBookRefLang.setLang(Util.cellValue(currentCell));
								break;
							default:
								break;
						}
					}
				tariffBookRefLangs.add(tariffBookRefLang);
				}
			workbook.close();
			return tariffBookRefLangs;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}

	@Override
	public ByteArrayInputStream load() {
		List<TarifBookRef> tarifBookRefs = tarifBookRefRepository.findAll();
		ByteArrayInputStream in = null;
		return in;
	}

	@Override
	public ByteArrayInputStream load(String codeLang, final int page, final int size, String orderDirection) {
		Lang lang = langRepository.findByCode(codeLang);

		Page<TarifBookRef> tarifBookRefs = null;
		if(orderDirection.equals("DESC")){
			tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}else if(orderDirection.equals("ASC")){
			tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}

		//Page<TarifBookRef> tarifBookRefs = tarifBookRefRepository.findAll(PageRequest.of(page, size));
		List<TariffBookRefLang> tariffBookRefLangs = new ArrayList<>();

		for (TarifBookRef tarifBookRef : tarifBookRefs) {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), tarifBookRef.getId());
			if (entityRefLang != null) {
				TariffBookRefLang tariffBookRefLang = new TariffBookRefLang();
				tariffBookRefLang.setReference(tarifBookRef.getReference());
				tariffBookRefLang.setHsCode(tarifBookRef.getHsCode());
				tariffBookRefLang.setProductYear(tarifBookRef.getProductYear());
				tariffBookRefLang.setProductExpiryDate(tarifBookRef.getProductExpiryDate());
				tariffBookRefLang.setChapterRef(tarifBookRef.getChapterRef()!= null ?tarifBookRef.getChapterRef().getCode():null);
				tariffBookRefLang.setParent(tarifBookRef.getParent() != null ?tarifBookRef.getParent().getReference(): null);
				tariffBookRefLang.setUnitRef(tarifBookRef.getUnitRef()!= null ?tarifBookRef.getUnitRef().getCode():null);

				tariffBookRefLang.setLabel(entityRefLang.getLabel());
				tariffBookRefLang.setDescription(entityRefLang.getDescription());
				tariffBookRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
				tariffBookRefLangs.add(tariffBookRefLang);
			}
		}
		ByteArrayInputStream in = tariffBookRefsToExcel(tariffBookRefLangs);
		return in;

	}

	@Override
	public void saveFromExcel(MultipartFile file) {
		try {
			List<TariffBookRefLang> tariffBookRefLangs = excelToTariffBookRefs(file.getInputStream());
			if (!tariffBookRefLangs.isEmpty()) {
				for (TariffBookRefLang l : tariffBookRefLangs) {
					TarifBookRef tarifBookRef = new TarifBookRef();
					tarifBookRef.setReference(l.getReference());
					tarifBookRef.setHsCode(l.getHsCode());
					tarifBookRef.setProductYear(l.getProductYear());
					tarifBookRef.setProductExpiryDate(l.getProductExpiryDate());
					tarifBookRef.setChapterRef(chapterRefRepository.findByCode(l.getChapterRef()));
					tarifBookRef.setParent(tarifBookRefRepository.findByReference(l.getParent()));
					tarifBookRef.setUnitRef(unitRefRepository.findByCode(l.getUnitRef()));
					TarifBookRef ltemp = tarifBookRefRepository.findByReference(l.getReference());
					if (ltemp == null) {
						Long id = (tarifBookRefRepository.save(tarifBookRef)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.TARIFF_BOOK_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					} else {
						tarifBookRef.setId( ltemp.getId());
						tarifBookRefRepository.save(tarifBookRef);
						EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF,langRepository.findByCode(l.getLang()).getId(),tarifBookRef.getId());
						if(entityRefLang == null )
						{
							entityRefLang =new EntityRefLang() ;
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.TARIFF_BOOK_REF);
							entityRefLang.setRefId(tarifBookRef.getId());
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

	public Page<TariffBookRefBean> getAll(Pageable pageable) {
		Page<TarifBookRef> entities = tarifBookRefRepository.findAll(pageable);
		Page<TariffBookRefBean> result = entities.map(tariffBookRefMapper::entityToBean);
		return result;

	}

@Override
public void addTarifBookRef(TariffBookRefLang tariffBookRefLang){
	TarifBookRef tarifBookRef = new TarifBookRef();

	tarifBookRef.setReference(tariffBookRefLang.getReference());
	tarifBookRef.setHsCode(tariffBookRefLang.getHsCode());
	tarifBookRef.setProductYear(tariffBookRefLang.getProductYear());
	tarifBookRef.setProductExpiryDate(tariffBookRefLang.getProductExpiryDate());
    tarifBookRef.setChapterRef(chapterRefRepository.findByCode(tariffBookRefLang.getChapterRef()));
	tarifBookRef.setParent(tarifBookRefRepository.findByReference(tariffBookRefLang.getParent()));
	tarifBookRef.setUnitRef(unitRefRepository.findByCode(tariffBookRefLang.getUnitRef()));
    TarifBookRef ltemp = tarifBookRefRepository.findByReference(tariffBookRefLang.getReference());

	if (ltemp == null) {
		Long id = (tarifBookRefRepository.save(tarifBookRef)).getId();
		EntityRefLang entityRefLang = new EntityRefLang();

		entityRefLang.setLabel(tariffBookRefLang.getLabel());
		entityRefLang.setDescription(tariffBookRefLang.getDescription());
		entityRefLang.setRefId(id);
		entityRefLang.setTableRef(TableRef.TARIFF_BOOK_REF);
		entityRefLang.setLang(langRepository.findByCode(tariffBookRefLang.getLang()));
		entityRefLangRepository.save(entityRefLang);

	} else {
		Long id = ltemp.getId();
		tarifBookRef.setId(id);
		tarifBookRefRepository.save(tarifBookRef);
		EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, langRepository.findByCode(tariffBookRefLang.getLang()).getId() ,id);
		entityRefLang.setLabel(tariffBookRefLang.getLabel());
		entityRefLang.setDescription(tariffBookRefLang.getDescription());
		entityRefLang.setLang(  langRepository.findByCode(tariffBookRefLang.getLang()));
		entityRefLangRepository.save(entityRefLang);
	}
}

	@Override
	public List<InternationalizationVM> getInternationalizationRefList(Long id) {
		return internationalizationHelper.getInternationalizationRefList(id, TableRef.TARIFF_BOOK_REF);
	}

	@Override
	public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
		entityRefLang.setTableRef(TableRef.TARIFF_BOOK_REF);
		entityRefLang.setLang(langRepository.findByCode(lang));
		if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
			entityRefLangRepository.save(entityRefLang);
		else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
	}

	@Override
	public ErrorResponse deleteInternationalisation(String codeLang, Long tarifBookRefId) {

		ErrorResponse response = new ErrorResponse();
		try {

			Lang lang = langRepository.findByCode(codeLang);

			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), tarifBookRefId);

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
	public TariffBookRefLang findTariffBook(Long id, String lang){
		TariffBookRefLang tariffBookRefLang = new TariffBookRefLang();
		TarifBookRef tarifBookRef = tarifBookRefRepository.findOneById(id);
		EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF,langRepository.findByCode(lang).getId(),tarifBookRef.getId());

		tariffBookRefLang.setReference(tarifBookRef.getReference());
		tariffBookRefLang.setHsCode(tarifBookRef.getHsCode());
		tariffBookRefLang.setProductYear(tarifBookRef.getProductYear());
		tariffBookRefLang.setProductExpiryDate(tarifBookRef.getProductExpiryDate());
		//chapterRefLang.setSectionRef(sectionRefMapper.entityToBean(chapterRef.getSectionRef()));

		tariffBookRefLang.setChapterRef(tarifBookRef.getChapterRef()!=null?tarifBookRef.getChapterRef().getCode():"");
		tariffBookRefLang.setParent(tarifBookRef.getParent()!=null?tarifBookRef.getParent().getReference():"");
		tariffBookRefLang.setUnitRef(tarifBookRef.getUnitRef()!=null?tarifBookRef.getUnitRef().getCode():"");

		tariffBookRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
		tariffBookRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
		tariffBookRefLang.setLang(lang);
		return tariffBookRefLang;
	}

	public Page<TariffBookRefLang> mapToRefLangs(Page<TarifBookRef> tarifBookRefs, String codeLang) {
		Lang lang = langRepository.findByCode(codeLang);
		List<TariffBookRefLang> tariffBookRefLangs = new ArrayList<>();

		return tarifBookRefs.map(tarifBookRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF, lang.getId(), tarifBookRef.getId());

			TariffBookRefLang tariffBookRefLang = new TariffBookRefLang();
			tariffBookRefLang.setId(tarifBookRef.getId());
			tariffBookRefLang.setReference(tarifBookRef.getReference()!=null ?tarifBookRef.getReference():"");
			tariffBookRefLang.setHsCode(tarifBookRef.getHsCode()!=null ?tarifBookRef.getHsCode():"");
			tariffBookRefLang.setProductYear(tarifBookRef.getProductYear());
			tariffBookRefLang.setProductExpiryDate(tarifBookRef.getProductExpiryDate());

			tariffBookRefLang.setChapterRef(tarifBookRef.getChapterRef()!=null ?tarifBookRef.getChapterRef().getCode():"");
			tariffBookRefLang.setParent(tarifBookRef.getParent()!=null ?tarifBookRef.getParent().getReference():"");
			tariffBookRefLang.setUnitRef(tarifBookRef.getUnitRef()!=null ?tarifBookRef.getUnitRef().getCode():"");

			tariffBookRefLang.setCreatedBy(tarifBookRef.getCreatedBy());
			tariffBookRefLang.setCreatedOn(tarifBookRef.getCreatedOn());
			tariffBookRefLang.setUpdatedBy(tarifBookRef.getUpdatedBy());
			tariffBookRefLang.setUpdatedOn(tarifBookRef.getUpdatedOn());

			tariffBookRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			tariffBookRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			tariffBookRefLang.setLang(codeLang);

			tariffBookRefLangs.add(tariffBookRefLang);

			return  tariffBookRefLang ;
		});
	}

	public Page<TariffBookRefLang>  filterByReferenceOrLabel(String value, Pageable pageable, String codeLang)  {
		return mapToRefLangs(tarifBookRefRepository.filterByReferenceOrLabel(value ,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
	}

	@Override
	public Page<TariffBookRefLangProjection> filterByReferenceOrLabelProjection(String value, String lang, Pageable pageable) {
		return tarifBookRefRepository.filterByReferenceOrLabelProjecton(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);

	}

	@Override
	public TarifBookRef updateTarifBookRef(Long id,TariffBookRefLang tariffBookRefLang) {

		TarifBookRef tarifBookRef = tarifBookRefRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("there is no TaridBook with this id : " + id));

		tarifBookRef.setReference(tariffBookRefLang.getReference());
		tarifBookRef.setHsCode(tariffBookRefLang.getHsCode());
		tarifBookRef.setProductYear(tariffBookRefLang.getProductYear());
		tarifBookRef.setChapterRef(chapterRefRepository.findByCode(tariffBookRefLang.getChapterRef()));
		tarifBookRef.setId(id);
		return tarifBookRefRepository.save(tarifBookRef);
	}

	@Override
	public TariffBookRefBean findById(Long id) {
		TarifBookRef result= tarifBookRefRepository.findById(id).orElseThrow(
				()->new NotFoundException("id not found"));
		return tariffBookRefMapper.entityToBean(result);
	}
	
	@Override
	public Set<ConstraintViolation<TariffBookRefLang>> validateItems(TariffBookRefLang tariffBookRefLang) {
		Set<ConstraintViolation<TariffBookRefLang>> violations = validator.validate(tariffBookRefLang);
		return violations;
	}

	@Override
	public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {
			List<TariffBookRefLang> itemsList = excelToTariffBookRefs(file.getInputStream());
			List<TariffBookRefLang> invalidItems = new ArrayList<TariffBookRefLang>();
			List<TariffBookRefLang> validItems = new ArrayList<TariffBookRefLang>();

			int lenght = itemsList.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<TariffBookRefLang>> violations = validateItems(itemsList.get(i));
				if (violations.isEmpty())

				{
					validItems.add(itemsList.get(i));
				} else {
					invalidItems.add(itemsList.get(i));
				}
			}

			if (!invalidItems.isEmpty()) {

				ByteArrayInputStream out = tariffBookRefsToExcel(invalidItems);
				xls = new InputStreamResource(out);

			}

			if (!validItems.isEmpty()) {
				for (TariffBookRefLang l : validItems) {
					TarifBookRef tarifBookRef = new TarifBookRef();
					tarifBookRef.setReference(l.getReference());
					tarifBookRef.setHsCode(l.getHsCode());
					tarifBookRef.setProductYear(l.getProductYear());
					tarifBookRef.setProductExpiryDate(l.getProductExpiryDate());
					tarifBookRef.setChapterRef(chapterRefRepository.findByCode(l.getChapterRef()));
					tarifBookRef.setParent(tarifBookRefRepository.findByReference(l.getParent()));
					tarifBookRef.setUnitRef(unitRefRepository.findByCode(l.getUnitRef()));
					TarifBookRef ltemp = tarifBookRefRepository.findByReference(l.getReference());
					if (ltemp == null) {
						Long id = (tarifBookRefRepository.save(tarifBookRef)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.TARIFF_BOOK_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					} else {
						tarifBookRef.setId( ltemp.getId());
						tarifBookRefRepository.save(tarifBookRef);
						EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.TARIFF_BOOK_REF,langRepository.findByCode(l.getLang()).getId(),tarifBookRef.getId());
						if(entityRefLang == null )
						{
							entityRefLang =new EntityRefLang() ;
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.TARIFF_BOOK_REF);
							entityRefLang.setRefId(tarifBookRef.getId());
						}
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}

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
