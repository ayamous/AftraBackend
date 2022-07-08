package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.exception.ExtendedProcedureNotFoundException;
import ma.itroad.aace.eth.coref.exception.TranslationFoundException;
import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.bean.ExtendedProcedureRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.NationalProcedureRefBean;
import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.ExtendedProcedureRef;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.entity.NationalProcedureRef;
import ma.itroad.aace.eth.coref.model.mapper.ExtendedProcedureRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.ExtendedProcedureRefLangProjection;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.ExtendedProcedureRefRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.NationalProcedureRefRepository;
import ma.itroad.aace.eth.coref.service.IExtendedProcedureRefService;
import ma.itroad.aace.eth.coref.service.helper.ExtendedProcedureRefLang;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.impl.exceldto.ExtendedProcedureExcelDTO;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
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

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ExtendedProcedureRefServiceImpl extends BaseServiceImpl<ExtendedProcedureRef, ExtendedProcedureRefBean> implements IExtendedProcedureRefService {

	static String[] HEADERs = { "CODE", "REFERENCE PROCEDURE", "LABEL", "DESCRIPTION", "LANG" };
	static String SHEET = "ExtendedProcedureRefSHEET";

	@Autowired
	private ExtendedProcedureRefRepository extendedProcedureRefRepository;

	@Autowired
	private ExtendedProcedureRefMapper extendedProcedureRefMapper;

	@Autowired
	private NationalProcedureRefRepository nationalProcedureRefRepository;

	@Autowired
	private EntityRefLangRepository entityRefLangRepository;

	@Autowired
	private LangRepository langRepository;

	@Autowired
	private InternationalizationHelper internationalizationHelper;

	@Autowired
	private Validator validator;


	@Override
	public List<ExtendedProcedureRef> saveAll(List<ExtendedProcedureRef> extendedProcedureRefs) {
		if (!extendedProcedureRefs.isEmpty()) {
			return extendedProcedureRefRepository.saveAll(extendedProcedureRefs);
		}
		return null;
	}

	@Override
	public ErrorResponse delete(Long id) {
		ErrorResponse response = new ErrorResponse();
		try {
			List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.EXTENDED_PROCEDURE_REF, id);
			for (EntityRefLang entityRefLang : entityRefLangs) {
				entityRefLangRepository.delete(entityRefLang);
			}
			extendedProcedureRefRepository.deleteById(id);
			response.setStatus(HttpStatus.OK);
			response.setErrorMsg("null");
		} catch (Exception e) {
			response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
			response.setStatus(HttpStatus.CONFLICT);
		}
		return response;
	}

	@Override
	public Set<ConstraintViolation<ExtendedProcedureRefLang>> validateExtendedProcedure(ExtendedProcedureRefLang extendedProcedureRefLang) {

		Set<ConstraintViolation<ExtendedProcedureRefLang>> violations = validator.validate(extendedProcedureRefLang);

		return violations;
	}

	@Override
	public List<ExtendedProcedureRefLang> excelToExtendedProcedureRefs(InputStream is) {
		try {
			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheet(SHEET);
			Iterator<Row> rows = sheet.iterator();
			List<ExtendedProcedureRefLang> extendedProcedureRefLangs = new ArrayList<ExtendedProcedureRefLang>();
			Row currentRow = rows.next();
			int rowNumber = 0;
			while (rows.hasNext()) {
				currentRow = rows.next();

				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}

				ExtendedProcedureRefLang extendedProcedureRefLang = new ExtendedProcedureRefLang();
				int cellIdx = 0;
				for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
					Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);

					switch (cellIdx) {
						case 0:
							extendedProcedureRefLang.setCode(currentCell.getStringCellValue());
							break;
						case 1:
							extendedProcedureRefLang.setNationalProcedureRef(currentCell.getStringCellValue());
							break;
						case 2:
							extendedProcedureRefLang.setLabel(currentCell.getStringCellValue());
						case 3:
							extendedProcedureRefLang.setDescription(currentCell.getStringCellValue());
						case 4:
							extendedProcedureRefLang.setLang(currentCell.getStringCellValue());
							break;
						default:
							break;
					}
					cellIdx++;
				}
				extendedProcedureRefLangs.add(extendedProcedureRefLang);
			}
			workbook.close();
			return extendedProcedureRefLangs;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
	}


	@Override
	public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;

		try {
			List<ExtendedProcedureRefLang> extendedProcedureRefLangs = excelToExtendedProcedureRefs(file.getInputStream());
			List<ExtendedProcedureRefLang> InvalidExtendedProcedureRefLangs = new ArrayList<ExtendedProcedureRefLang>();
			List<ExtendedProcedureRefLang> ValidExtendedProcedureRefLangs = new ArrayList<ExtendedProcedureRefLang>();

			int lenght = extendedProcedureRefLangs.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<ExtendedProcedureRefLang>> violations = validateExtendedProcedure(extendedProcedureRefLangs.get(i));
				if (violations.isEmpty())

				{
					ValidExtendedProcedureRefLangs.add(extendedProcedureRefLangs.get(i));
				} else {
					InvalidExtendedProcedureRefLangs.add(extendedProcedureRefLangs.get(i));
				}
			}

			if (!InvalidExtendedProcedureRefLangs.isEmpty()) {

				ByteArrayInputStream out = extendedProcedureRefsToExcel(InvalidExtendedProcedureRefLangs);
				xls = new InputStreamResource(out);

			}

			if (!ValidExtendedProcedureRefLangs.isEmpty()) {
				for (ExtendedProcedureRefLang l : ValidExtendedProcedureRefLangs) {

					ExtendedProcedureRef extendedProcedureRef = new ExtendedProcedureRef();

					extendedProcedureRef.setCode(l.getCode());

					extendedProcedureRef.setNationalProcedureRef(nationalProcedureRefRepository.findByCode(l.getNationalProcedureRef()));


					ExtendedProcedureRef ltemp = extendedProcedureRefRepository.findByCode(l.getCode());

					if (ltemp == null) {

						Long id = (extendedProcedureRefRepository.save(extendedProcedureRef)).getId();

						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.EXTENDED_PROCEDURE_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);

					} else {
						Long id = ltemp.getId();
						extendedProcedureRef.setId(id);
						extendedProcedureRefRepository.save(extendedProcedureRef);
						EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EXTENDED_PROCEDURE_REF,langRepository.findByCode(l.getLang()).getId(),id);
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}

				}
			}
			if (!InvalidExtendedProcedureRefLangs.isEmpty())
				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);

			if (!ValidExtendedProcedureRefLangs.isEmpty())
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
		List<ExtendedProcedureRef> extendedProcedureRefs = extendedProcedureRefRepository.findAll();
		ByteArrayInputStream in = null;
		return in;
	}

	public Page<ExtendedProcedureRefBean> getAll(Pageable pageable) {
		Page<ExtendedProcedureRef> entities = extendedProcedureRefRepository.findAll(pageable);
		Page<ExtendedProcedureRefBean> result = entities.map(extendedProcedureRefMapper::entityToBean);
		return result;
	}

	public Page<ExtendedProcedureRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {

		Page<ExtendedProcedureRef> extendedProcedureRefs = null;
		if(orderDirection.equals("DESC")){
			extendedProcedureRefs = extendedProcedureRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}else if(orderDirection.equals("ASC")){
			extendedProcedureRefs = extendedProcedureRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
		}else if (orderDirection=="") {
			extendedProcedureRefs = extendedProcedureRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
		}

		Lang lang = langRepository.findByCode(codeLang);
		List<ExtendedProcedureRefLang> extendedProcedureRefLangs = new ArrayList<>();

		return extendedProcedureRefs.map(extendedProcedureRef -> {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EXTENDED_PROCEDURE_REF, lang.getId(), extendedProcedureRef.getId());

			ExtendedProcedureRefLang extendedProcedureRefLang = new ExtendedProcedureRefLang();

			extendedProcedureRefLang.setId(extendedProcedureRef.getId());
			extendedProcedureRefLang.setCode(extendedProcedureRef.getCode()!=null ?extendedProcedureRef.getCode():"");

			extendedProcedureRefLang.setNationalProcedureRef(extendedProcedureRef.getNationalProcedureRef().getCode());

			extendedProcedureRefLang.setCreatedBy(extendedProcedureRef.getCreatedBy());
			extendedProcedureRefLang.setCreatedOn(extendedProcedureRef.getCreatedOn());
			extendedProcedureRefLang.setUpdatedBy(extendedProcedureRef.getUpdatedBy());
			extendedProcedureRefLang.setUpdatedOn(extendedProcedureRef.getUpdatedOn());

			extendedProcedureRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
			extendedProcedureRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

			extendedProcedureRefLang.setLang(codeLang);

			extendedProcedureRefLangs.add(extendedProcedureRefLang);

			return  extendedProcedureRefLang ;
		});
	}

	@Override
	public ByteArrayInputStream load(String codeLang) {

		Lang lang = langRepository.findByCode(codeLang);
		List<ExtendedProcedureRef> extendedProcedureRefs = extendedProcedureRefRepository.findAll();
		List<ExtendedProcedureRefLang> extendedProcedureRefLangs = new ArrayList<>();

		for (ExtendedProcedureRef extendedProcedureRef : extendedProcedureRefs) {
			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EXTENDED_PROCEDURE_REF, lang.getId(), extendedProcedureRef.getId());
			if (entityRefLang != null) {
				ExtendedProcedureRefLang extendedProcedureRefLang = new ExtendedProcedureRefLang();

				extendedProcedureRefLang.setCode(extendedProcedureRef.getCode());

				extendedProcedureRefLang.setNationalProcedureRef(extendedProcedureRef.getNationalProcedureRef().getCode());

				extendedProcedureRefLang.setLabel(entityRefLang.getLabel());
				extendedProcedureRefLang.setDescription(entityRefLang.getDescription());
				extendedProcedureRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
				extendedProcedureRefLangs.add(extendedProcedureRefLang);
			}
		}
		ByteArrayInputStream in = extendedProcedureRefsToExcel(extendedProcedureRefLangs);
		return in;

	}

	@Override
	public ByteArrayInputStream  extendedProcedureRefsToExcel(List<ExtendedProcedureRefLang> extendedProcedureRefLangs) {
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
				if (!extendedProcedureRefLangs.isEmpty()) {

					for (ExtendedProcedureRefLang extendedProcedureRefLang : extendedProcedureRefLangs) {
						Row row = sheet.createRow(rowIdx++);

						row.createCell(0).setCellValue(extendedProcedureRefLang.getCode());


						row.createCell(1).setCellValue(extendedProcedureRefLang.getNationalProcedureRef());

						row.createCell(2).setCellValue(extendedProcedureRefLang.getLabel());
						row.createCell(3).setCellValue(extendedProcedureRefLang.getDescription());
						row.createCell(4).setCellValue(extendedProcedureRefLang.getLang());
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
	public void addExtendedProcedure(ExtendedProcedureRefLang extendedProcedureRefLang){
		ExtendedProcedureRef extendedProcedureRef = new ExtendedProcedureRef();

		extendedProcedureRef.setCode(extendedProcedureRefLang.getCode());

		extendedProcedureRef.setNationalProcedureRef(nationalProcedureRefRepository.findByCode(extendedProcedureRefLang.getNationalProcedureRef()));

		ExtendedProcedureRef ltemp = extendedProcedureRefRepository.findByCode(extendedProcedureRefLang.getCode());

		if (ltemp == null) {
			Long id = (extendedProcedureRefRepository.save(extendedProcedureRef)).getId();
			EntityRefLang entityRefLang = new EntityRefLang();

			entityRefLang.setLabel(extendedProcedureRefLang.getLabel());
			entityRefLang.setDescription(extendedProcedureRefLang.getDescription());
			entityRefLang.setRefId(id);
			entityRefLang.setTableRef(TableRef.EXTENDED_PROCEDURE_REF);
			entityRefLang.setLang(langRepository.findByCode(extendedProcedureRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);

		} else {
			Long id = ltemp.getId();
			extendedProcedureRef.setId(id);
			extendedProcedureRefRepository.save(extendedProcedureRef);
			EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EXTENDED_PROCEDURE_REF, langRepository.findByCode(extendedProcedureRefLang.getLang()).getId() ,id);
			entityRefLang.setLabel(extendedProcedureRefLang.getLabel());
			entityRefLang.setDescription(extendedProcedureRefLang.getDescription());
			entityRefLang.setLang(  langRepository.findByCode(extendedProcedureRefLang.getLang()));
			entityRefLangRepository.save(entityRefLang);
		}
	}

	@Override
	public List<InternationalizationVM> getInternationalizationRefList(Long id) {
		return internationalizationHelper.getInternationalizationRefList(id, TableRef.EXTENDED_PROCEDURE_REF);
	}

	@Override
	public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
		entityRefLang.setTableRef(TableRef.EXTENDED_PROCEDURE_REF);
		entityRefLang.setLang(langRepository.findByCode(lang));
		if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EXTENDED_PROCEDURE_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
			entityRefLangRepository.save(entityRefLang);
		else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
	}

	@Override
	public Page<ExtendedProcedureRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
		return extendedProcedureRefRepository.findByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
	}

	@Override
	public ErrorResponse deleteInternationalisation(String codeLang, Long extendedProcedureId) {

		ErrorResponse response = new ErrorResponse();
		try {

			Lang lang = langRepository.findByCode(codeLang);

			EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EXTENDED_PROCEDURE_REF, lang.getId(), extendedProcedureId);

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
	public ExtendedProcedureRefLang findExtendedProcedure(Long id, String lang){
		ExtendedProcedureRefLang extendedProcedureRefLang = new ExtendedProcedureRefLang();
		ExtendedProcedureRef extendedProcedureRef = extendedProcedureRefRepository.findOneById(id);
		EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.EXTENDED_PROCEDURE_REF,langRepository.findByCode(lang).getId(),extendedProcedureRef.getId());
		extendedProcedureRefLang.setCode(extendedProcedureRef.getCode());

		extendedProcedureRefLang.setNationalProcedureRef(extendedProcedureRef.getNationalProcedureRef().getCode());

		extendedProcedureRefLang.setLabel(entityRefLang.getLabel());
		extendedProcedureRefLang.setDescription(entityRefLang.getDescription());
		extendedProcedureRefLang.setLang(lang);
		return extendedProcedureRefLang;
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
}
