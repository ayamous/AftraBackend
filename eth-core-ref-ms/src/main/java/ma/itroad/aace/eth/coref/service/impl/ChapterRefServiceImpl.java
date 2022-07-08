package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ChapterNotFoundException;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.*;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.ChapterRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.CityRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.CountryRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.SectionRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.ChapterRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.ChapterRefVM;
import ma.itroad.aace.eth.coref.repository.ChapterRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.SectionRefRepository;
import ma.itroad.aace.eth.coref.service.IChapterRefService;
import ma.itroad.aace.eth.coref.service.helper.*;
import ma.itroad.aace.eth.coref.service.impl.exceldto.ChapterRefExcelDTO;

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

@Service
public class ChapterRefServiceImpl extends BaseServiceImpl<ChapterRef, ChapterRefBean> implements IChapterRefService {

	static String[] HEADERs = {"CODE DU CHAPITRE", "CODE SECTION","LABEL","DESCRIPTION", "LANGUE"};
	static String SHEET = "ChapterRefsSHEET";

	@Autowired
	private ChapterRefRepository chapterRefRepository;

	@Autowired
	private SectionRefRepository sectionRefRepository;

	@Autowired
	private SectionRefMapper sectionRefMapper;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

	@Autowired
	private ChapterRefMapper chapterRefMapper;

	@Autowired
	private Validator validator;

	@Autowired
	LangRepository langRepository;

	@Autowired
	private InternationalizationHelper internationalizationHelper;



	@Override
	public List<ChapterRef> saveAll(List<ChapterRef> chapterRefs) {
		if (!chapterRefs.isEmpty()) {
			return chapterRefRepository.saveAll(chapterRefs);
		}
		return null;
	}

	public Page<ChapterRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {

		Page<ChapterRef> chapterRefs = null;
		if(orderDirection.equals("DESC")){
			chapterRefs = chapterRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}else if(orderDirection.equals("ASC")){
			chapterRefs = chapterRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}

		Lang lang = langRepository.findByCode(codeLang);
		List<ChapterRefLang> chapterRefLangs = new ArrayList<>();

		return chapterRefs.map(chapterRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CHAPTER_REF, lang.getId(), chapterRef.getId());

			ChapterRefLang chapterRefLang = new ChapterRefLang();

			chapterRefLang.setId(chapterRef.getId());
			chapterRefLang.setCode(chapterRef.getCode()!=null ?chapterRef.getCode():"");

			//chapterRefLang.setSectionRef(sectionRefMapper.entityToBean(chapterRef.getSectionRef()));
			chapterRefLang.setSectionRef(chapterRef.getSectionRef()!=null ? chapterRef.getSectionRef().getCode():"");
			//chapterRefLang.setSectionRef(chapterRef.getCode()!=null ?chapterRef.getCode():"");


			chapterRefLang.setCreatedBy(chapterRef.getCreatedBy());
			chapterRefLang.setCreatedOn(chapterRef.getCreatedOn());
			chapterRefLang.setUpdatedBy(chapterRef.getUpdatedBy());
			chapterRefLang.setUpdatedOn(chapterRef.getUpdatedOn());

			chapterRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			chapterRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			chapterRefLang.setLang(codeLang);

			chapterRefLangs.add(chapterRefLang);

			return  chapterRefLang ;
		});
	}

	@Override
	public ErrorResponse delete(Long id) {
		ErrorResponse response = new ErrorResponse();
		try {
			List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.CHAPTER_REF, id);
			for (EntityRefLang entityRefLang : entityRefLangs) {
				entityRefLangRepository.delete(entityRefLang);
			}
			chapterRefRepository.deleteById(id);
			response.setStatus(HttpStatus.OK);
			response.setErrorMsg("null");
		} catch (Exception e) {
			response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
			response.setStatus(HttpStatus.CONFLICT);
		}
		return response;
	}

	public ErrorResponse deleteList(ListOfObject listOfObject) {
		ErrorResponse response = new ErrorResponse();

		try {
			for(Long id : listOfObject.getListOfObject()){
				List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.CHAPTER_REF, id);
				for (EntityRefLang entityRefLang : entityRefLangs) {
					entityRefLangRepository.delete(entityRefLang);
				}
				chapterRefRepository.deleteById(id);
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
	public ByteArrayInputStream chapterRefsToExcel(List<ChapterRefLang> chapterRefLangs) {
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
				if (!chapterRefLangs.isEmpty()) {

					for (ChapterRefLang chapterRefLang : chapterRefLangs) {
						Row row = sheet.createRow(rowIdx++);

						row.createCell(0).setCellValue(chapterRefLang.getCode());

						//row.createCell(1).setCellValue(chapterRefLang.getSectionRef().getCode());

						row.createCell(1).setCellValue(chapterRefLang.getSectionRef());

						row.createCell(2).setCellValue(chapterRefLang.getLabel());
						row.createCell(3).setCellValue(chapterRefLang.getDescription());
						row.createCell(4).setCellValue(chapterRefLang.getLang());
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

	@Override
	public List<ChapterRefLang> excelToChapterRef(InputStream is) {
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);
			Iterator<Row> rows = sheet.iterator();
			List<ChapterRefLang> chapterRefLangs = new ArrayList<ChapterRefLang>();
			Row currentRow = rows.next();
			while (rows.hasNext()) {
				currentRow = rows.next();
				ChapterRefLang chapterRefLang = new ChapterRefLang();
				int cellIdx = 0;
				for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
					Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
					switch (cellIdx) {
						case 0:
							chapterRefLang.setCode(cellValue(currentCell));
							break;
						case 1:
							chapterRefLang.setSectionRef(cellValue(currentCell));
							break;
						case 2:
							chapterRefLang.setLabel(cellValue(currentCell));
							break;
						case 3:
							chapterRefLang.setDescription(cellValue(currentCell));
							break;
						case 4:
							chapterRefLang.setLang(cellValue(currentCell));
							break;
						default:
							break;
					}
					cellIdx++;
				}
				chapterRefLangs.add(chapterRefLang);
			}
			workbook.close();
			return chapterRefLangs;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}


	@Override
	public ByteArrayInputStream load() {
		List<ChapterRef> chapterRefs = chapterRefRepository.findAll();
		ByteArrayInputStream in = null;
		return in;
	}

	@Override
	public ByteArrayInputStream load(String codeLang) {

		Lang lang = langRepository.findByCode(codeLang);
		List<ChapterRef> chapterRefs = chapterRefRepository.findAll();
		List<ChapterRefLang> chapterRefLangs = new ArrayList<>();

		for (ChapterRef chapterRef : chapterRefs) {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CHAPTER_REF, lang.getId(), chapterRef.getId());
			if (entityRefLang != null) {
				ChapterRefLang chapterRefLang = new ChapterRefLang();


				chapterRefLang.setCode(chapterRef.getCode());

				chapterRefLang.setSectionRef(chapterRef.getSectionRef().getCode());


				chapterRefLang.setLabel(entityRefLang.getLabel());
				chapterRefLang.setDescription(entityRefLang.getDescription());
				chapterRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
				chapterRefLangs.add(chapterRefLang);
			}
		}
		ByteArrayInputStream in = chapterRefsToExcel(chapterRefLangs);
		return in;

	}

	@Override
	public void saveFromExcel(MultipartFile file) {

		try {
			List<ChapterRefLang> chapterRefLangs = excelToChapterRef(file.getInputStream());
			if (!chapterRefLangs.isEmpty()) {
				for (ChapterRefLang l : chapterRefLangs) {
					ChapterRef chapterRef = new ChapterRef();
					chapterRef.setCode(l.getCode());
					chapterRef.setSectionRef(sectionRefRepository.findByCode(l.getSectionRef()));
					ChapterRef ltemp = chapterRefRepository.findByCode(l.getCode());
					if (ltemp == null) {
						Long id = (chapterRefRepository.save(chapterRef)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.CHAPTER_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}
					else {
						chapterRef.setId( ltemp.getId());
						chapterRefRepository.save(chapterRef);
						EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CHAPTER_REF,langRepository.findByCode(l.getLang()).getId(),chapterRef.getId());
						if(entityRefLang == null )
						{
							entityRefLang =new EntityRefLang() ;
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.CHAPTER_REF);
							entityRefLang.setRefId(chapterRef.getId());
						}
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLangRepository.save(entityRefLang);
					}

				}
			}
		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}
	}

	public Page<ChapterRefBean> getAll(Pageable pageable) {
		Page<ChapterRef> entities = chapterRefRepository.findAll(pageable);
		Page<ChapterRefBean> result = entities.map(chapterRefMapper::entityToBean);
		return result;

	}


	@Override
	public void addChapter(ChapterRefLang chapterRefLang){
		ChapterRef chapterRef = new ChapterRef();

		chapterRef.setCode(chapterRefLang.getCode());

		//chapterRef.setSectionRef(sectionRefRepository.findByCode(chapterRefLang.getSectionRef().getCode()));

		chapterRef.setSectionRef(sectionRefRepository.findByCode(chapterRefLang.getSectionRef()));

		ChapterRef ltemp = chapterRefRepository.findByCode(chapterRefLang.getCode());

		if (ltemp == null) {
			Long id = (chapterRefRepository.save(chapterRef)).getId();
			EntityRefLang entityRefLang = new EntityRefLang();

			entityRefLang.setLabel(chapterRefLang.getLabel());
			entityRefLang.setDescription(chapterRefLang.getDescription());
			entityRefLang.setRefId(id);
			entityRefLang.setTableRef(TableRef.CHAPTER_REF);
			entityRefLang.setLang(langRepository.findByCode(chapterRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);

		} else {
			Long id = ltemp.getId();
			chapterRef.setId(id);
			chapterRefRepository.save(chapterRef);
			EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CHAPTER_REF, langRepository.findByCode(chapterRefLang.getLang()).getId() ,id);
			entityRefLang.setLabel(chapterRefLang.getLabel());
			entityRefLang.setDescription(chapterRefLang.getDescription());
			entityRefLang.setLang(  langRepository.findByCode(chapterRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);
		}
	}

	@Override
	public List<InternationalizationVM> getInternationalizationRefList(Long id) {
		return internationalizationHelper.getInternationalizationRefList(id, TableRef.CHAPTER_REF);
	}

	@Override
	public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
		entityRefLang.setTableRef(TableRef.CHAPTER_REF);
		entityRefLang.setLang(langRepository.findByCode(lang));
		if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CHAPTER_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
			entityRefLangRepository.save(entityRefLang);
		else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
	}

	@Override
	public ErrorResponse deleteInternationalisation(String codeLang, Long chapterRefId) {

		ErrorResponse response = new ErrorResponse();
		try {

			Lang lang = langRepository.findByCode(codeLang);

			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CHAPTER_REF, lang.getId(), chapterRefId);

			entityRefLangRepository.delete(entityRefLang);
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
	public ChapterRefLang findChapter(Long id, String lang) {
		ChapterRefLang chapterRefLang = new ChapterRefLang();
		ChapterRef chapterRef = chapterRefRepository.findOneById(id);
		EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CHAPTER_REF,
				langRepository.findByCode(lang).getId(), !Objects.isNull(chapterRef) ? chapterRef.getId() : 0);

		if (Objects.isNull(chapterRef) || Objects.isNull(entityRefLang)) {
			throw new ChapterNotFoundException("Chapter id: " + id);
		}

		chapterRefLang.setLabel(entityRefLang.getLabel());
		chapterRefLang.setDescription(entityRefLang.getDescription());
		chapterRefLang.setLang(lang);
		chapterRefLang.setCode(chapterRef.getCode());
		chapterRefLang.setSectionRef(chapterRef.getSectionRef().getCode());

		return chapterRefLang;

	}
	*/

	@Override
	public ChapterRefLang findChapter(Long id, String lang){
		ChapterRefLang chapterRefLang = new ChapterRefLang();
		ChapterRef chapterRef = chapterRefRepository.findOneById(id);
		EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CHAPTER_REF,langRepository.findByCode(lang).getId(),chapterRef.getId());
		chapterRefLang.setCode(chapterRef.getCode());
		//chapterRefLang.setSectionRef(sectionRefMapper.entityToBean(chapterRef.getSectionRef()));
		chapterRefLang.setSectionRef(chapterRef.getSectionRef().getCode());
		chapterRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
		chapterRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

		chapterRefLang.setLang(lang);
		return chapterRefLang;
	}

	public Page<ChapterRefLang> mapLangToRefLangs(Page<ChapterRef> chapterRefs, String codeLang) {
		List<ChapterRefLang> chapterRefLangs = new ArrayList<>();
		Lang lang = langRepository.findByCode(codeLang);
		return chapterRefs.map(chapterRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CHAPTER_REF, lang.getId(), chapterRef.getId());

			ChapterRefLang chapterRefLang = new ChapterRefLang();

			chapterRefLang.setId(chapterRef.getId());
			chapterRefLang.setCode(chapterRef.getCode()!=null ?chapterRef.getCode():"");

			//chapterRefLang.setSectionRef(sectionRefMapper.entityToBean(chapterRef.getSectionRef()));
			chapterRefLang.setSectionRef(chapterRef.getSectionRef().getCode());
			//chapterRefLang.setSectionRef(chapterRef.getCode()!=null ?chapterRef.getCode():"");


			chapterRefLang.setCreatedBy(chapterRef.getCreatedBy());
			chapterRefLang.setCreatedOn(chapterRef.getCreatedOn());
			chapterRefLang.setUpdatedBy(chapterRef.getUpdatedBy());
			chapterRefLang.setUpdatedOn(chapterRef.getUpdatedOn());

			chapterRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			chapterRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			chapterRefLang.setLang(codeLang);

			chapterRefLangs.add(chapterRefLang);

			return  chapterRefLang ;
		});
	}

	public Page<ChapterRefLang> filterByCodeOrLabel(String value, int page,int size, String codeLang) {

		if (value.equals(" ") || value == null)
			return getAll(page, size, codeLang, "ASC");
		else
		return mapLangToRefLangs(chapterRefRepository.filterByCodeOrLabel(value,langRepository.findByCode(codeLang).getId(), PageRequest.of(page,size)), codeLang);
	}

	@Override
	public Page<ChapterRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
		return chapterRefRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
	}

	@Override
	public ChapterRefBean findById(Long id) {
		ChapterRef result= chapterRefRepository.findById(id).orElseThrow(
				()->new NotFoundException("id not found"));
		return chapterRefMapper.entityToBean(result);
	}

	public Set<ConstraintViolation<ChapterRefLang>> validateItems(ChapterRefLang chapterRefLang) {
		Set<ConstraintViolation<ChapterRefLang>> violations = validator.validate(chapterRefLang);
		return violations;
	}

	@Override
	public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {
			List<ChapterRefLang> itemsList = excelToChapterRef(file.getInputStream());
			List<ChapterRefLang> invalidItems = new ArrayList<ChapterRefLang>();
			List<ChapterRefLang> validItems = new ArrayList<ChapterRefLang>();

			int lenght = itemsList.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<ChapterRefLang>> violations = validateItems(itemsList.get(i));
				if (violations.isEmpty())

				{
					validItems.add(itemsList.get(i));
				} else {
					invalidItems.add(itemsList.get(i));
				}
			}

			if (!invalidItems.isEmpty()) {

				ByteArrayInputStream out = chapterRefsToExcel(invalidItems);
				xls = new InputStreamResource(out);

			}

			if (!validItems.isEmpty()) {
				for (ChapterRefLang l : validItems) {
					ChapterRef chapterRef = new ChapterRef();
					chapterRef.setCode(l.getCode());
					chapterRef.setSectionRef(sectionRefRepository.findByCode(l.getSectionRef()));
					ChapterRef ltemp = chapterRefRepository.findByCode(l.getCode());
					if (ltemp == null) {
						Long id = (chapterRefRepository.save(chapterRef)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.CHAPTER_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}
					else {
						chapterRef.setId( ltemp.getId());
						chapterRefRepository.save(chapterRef);
						EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CHAPTER_REF,langRepository.findByCode(l.getLang()).getId(),chapterRef.getId());
						if(entityRefLang == null )
						{
							entityRefLang =new EntityRefLang() ;
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.CHAPTER_REF);
							entityRefLang.setRefId(chapterRef.getId());
						}
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
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


