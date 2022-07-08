package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.LangBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.mapper.LangMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.LangEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.service.ILangService;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.LangEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.PortRefLang;
import ma.itroad.aace.eth.coref.service.util.Util;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.*;
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
public class ILangServiceImpl extends BaseServiceImpl<Lang, LangBean> implements ILangService {

    static String[] HEADERs = {"CODE", "NAME", "DEFAULT", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "LangsSHEET";
    
	@Autowired
	Validator validator;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    private LangMapper langMapper;


    public Page<LangBean> getAll(Pageable pageable) {
        Page<Lang> entities = langRepository.findAll(pageable);
        Page<LangBean> result = entities.map(langMapper::entityToBean);
        return result;
    }

    public Page<LangEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<Lang> langs = null;
        if(orderDirection.equals("DESC")){
             langs = langRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            langs = langRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }

        Lang lang = langRepository.findByCode(codeLang);
        List<LangEntityRefLang> langEntityRefLangs = new ArrayList<>();

        return langs.map(langRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.LANG, lang.getId(), langRef.getId());

            LangEntityRefLang langEntityRefLang = new LangEntityRefLang();

            langEntityRefLang.setId(langRef.getId());
            langEntityRefLang.setCode(langRef.getCode()!=null?langRef.getCode():"");
            langEntityRefLang.setName(langRef.getName()!=null?langRef.getName():"");
            langEntityRefLang.setDef(langRef.getDef()!=null?langRef.getDef():"");
            langEntityRefLang.setCreatedBy(langRef.getCreatedBy());
            langEntityRefLang.setCreatedOn(langRef.getCreatedOn());
            langEntityRefLang.setUpdatedBy(langRef.getUpdatedBy());
            langEntityRefLang.setUpdatedOn(langRef.getUpdatedOn());

            langEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            langEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
            langEntityRefLang.setLang(codeLang);
            langEntityRefLangs.add(langEntityRefLang);
            return  langEntityRefLang ;
        });
    }

@Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.LANG);
    }


    @Override
    public List<Lang> saveAll(List<Lang> langs) {
        if (!langs.isEmpty()) {
            return langRepository.saveAll(langs);
        }
        return null;
    }

    @Override
    public List<EntityRefLang> findByTableRefAndRefId(TableRef tableRef, Long id) {
        return entityRefLangRepository.findByTableRefAndRefId(tableRef, id);
    }

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.LANG, id);
            for(EntityRefLang entityRefLang:entityRefLangs ){
                entityRefLangRepository.delete(entityRefLang);
            }
            langRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return  response;
    }

    @Override
    public ErrorResponse deleteInternationalisation(String codeLang, Long langsId) {

        ErrorResponse response = new ErrorResponse();
        try {

            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.LANG, lang.getId(), langsId);

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
    public ByteArrayInputStream langsToExcel(List<LangEntityRefLang> langEntityRefLangs) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET);
            // Header
            Row headerRow = sheet.createRow(0);
            if(HEADERs.length>0){
                for (int col = 0; col < HEADERs.length; col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(HEADERs[col]);
                }
                int rowIdx = 1;
                if(!langEntityRefLangs.isEmpty()){

                    for (LangEntityRefLang langEntityRefLang : langEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(langEntityRefLang.getCode());
                        row.createCell(1).setCellValue(langEntityRefLang.getName());
                        row.createCell(2).setCellValue(langEntityRefLang.getDef());
                        row.createCell(3).setCellValue(langEntityRefLang.getLabel());
                        row.createCell(4).setCellValue(langEntityRefLang.getDescription());
                        row.createCell(5).setCellValue(langEntityRefLang.getLang());
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
    public List<LangEntityRefLang> excelToLangsRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<LangEntityRefLang> langEntityRefLangs = new ArrayList<LangEntityRefLang>();
            Row currentRow = rows.next();
            while (rows.hasNext()) {
                currentRow = rows.next();
                LangEntityRefLang langEntityRefLang = new LangEntityRefLang();
                int cellIdx = 0;
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            langEntityRefLang.setCode(Util.cellValue(currentCell));
                            break;
                        case 1:
                            langEntityRefLang.setName(Util.cellValue(currentCell));
                            break;
                        case 2:
                            langEntityRefLang.setDef(Util.cellValue(currentCell));
                            break;
                        case 3:
                            langEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 4:
                            langEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 5:
                            langEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                langEntityRefLangs.add(langEntityRefLang);
            }
            workbook.close();
            return langEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public boolean isCellValid(String s) {
        return s != null && s.matches("^[ a-zA-Z0-9]*$");
    }
    
	@Override
	public Set<ConstraintViolation<LangEntityRefLang>> validateLangEntityRefLang(LangEntityRefLang langEntityRefLang) {

		Set<ConstraintViolation<LangEntityRefLang>> violations = validator.validate(langEntityRefLang);

		return violations;
	}
    
    @Override
    public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
        try {
        	
        	List<LangEntityRefLang> langEntityRefLang = excelToLangsRef(file.getInputStream());
			List<LangEntityRefLang> invalidLangEntityRefLang = new ArrayList<LangEntityRefLang>();
			List<LangEntityRefLang> validLangEntityRefLangs = new ArrayList<LangEntityRefLang>();

			int lenght = langEntityRefLang.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<LangEntityRefLang>> violations = validateLangEntityRefLang(
						langEntityRefLang.get(i));
				if (violations.isEmpty())

				{
					validLangEntityRefLangs.add(langEntityRefLang.get(i));
				} else {
					invalidLangEntityRefLang.add(langEntityRefLang.get(i));
				}
			}

			if (!invalidLangEntityRefLang.isEmpty()) {

				ByteArrayInputStream out = langsToExcel(invalidLangEntityRefLang);
				xls = new InputStreamResource(out);
			}

      
            if (!validLangEntityRefLangs.isEmpty()) {
                for (LangEntityRefLang l : validLangEntityRefLangs) {
                    Lang lang = new Lang();
                    lang.setCode(l.getCode());
                    lang.setName(l.getName());
                    lang.setDef(l.getDef());

                    Lang ltemp = langRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (langRepository.save(lang)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.LANG);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        Long id = ltemp.getId();
                        lang.setId(id);
                        langRepository.save(lang);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.LANG, langRepository.findByCode(l.getLang()).getId() ,id);
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.LANG);
                            entityRefLang.setRefId(lang.getId());
                        }
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    }
                }
            }
			if (!invalidLangEntityRefLang.isEmpty())
                return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(
                                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(xls);

			if (!validLangEntityRefLangs.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body("null");

			return ResponseEntity.status(HttpStatus.OK).body("bad");
            
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    @Override
    public void saveFromExcel(MultipartFile file) {
        try {
            List<LangEntityRefLang> langEntityRefLangs = excelToLangsRef(file.getInputStream());
            if (!langEntityRefLangs.isEmpty()) {
                for (LangEntityRefLang l : langEntityRefLangs) {
                    Lang lang = new Lang();
                    lang.setCode(l.getCode());
                    lang.setName(l.getName());
                    lang.setDef(l.getDef());

                    Lang ltemp = langRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (langRepository.save(lang)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.LANG);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        Long id = ltemp.getId();
                        lang.setId(id);
                        langRepository.save(lang);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.LANG, langRepository.findByCode(l.getLang()).getId() ,id);
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.LANG);
                            entityRefLang.setRefId(lang.getId());
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

    @Override
    public ByteArrayInputStream load() {
        List<Lang> langs = langRepository.findAll();
        ByteArrayInputStream in = null;
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<Lang> langs = langRepository.findAll();
        List<LangEntityRefLang> langEntityRefLangs = new ArrayList<LangEntityRefLang>();

        for(Lang u : langs) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.LANG, lang.getId(), u.getId());
            if (entityRefLang!=null) {
                LangEntityRefLang langEntityRefLang = new LangEntityRefLang();
                langEntityRefLang.setCode(u.getCode());
                langEntityRefLang.setName(u.getName());
                langEntityRefLang.setDef(u.getDef());
                langEntityRefLang.setLabel(entityRefLang.getLabel());
                langEntityRefLang.setDescription(entityRefLang.getDescription());
                langEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                langEntityRefLangs.add(langEntityRefLang);
            }
        }
        ByteArrayInputStream in = langsToExcel(langEntityRefLangs);
        return in;
    }

    @Override
    public void addLang(LangEntityRefLang langEntityRefLang){
        Lang lang = new Lang();
        lang.setCode(langEntityRefLang.getCode());
        lang.setName(langEntityRefLang.getName());
        lang.setDef(langEntityRefLang.getDef());
        Lang ltemp = langRepository.findByCode(langEntityRefLang.getCode());
        if (ltemp == null) {
            Long id = (langRepository.save(lang)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(langEntityRefLang.getLabel());
            entityRefLang.setDescription(langEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.LANG);
            entityRefLang.setLang(  langRepository.findByCode(langEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            lang.setId(id);
            langRepository.save(lang);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.LANG, langRepository.findByCode(langEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(langEntityRefLang.getLabel());
            entityRefLang.setDescription(langEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public LangEntityRefLang findLang(Long id, String lang){
        LangEntityRefLang langEntityRefLang = new LangEntityRefLang();
        Lang langs = langRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.LANG,langRepository.findByCode(lang).getId(),langs.getId());
        langEntityRefLang.setCode(langs.getCode());
        langEntityRefLang.setName(langs.getName());
        langEntityRefLang.setDef(langs.getDef());
        langEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        langEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        langEntityRefLang.setLang(lang);
        return langEntityRefLang;
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang,String lang){
        entityRefLang.setTableRef(TableRef.LANG);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.LANG, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    public Page<LangEntityRefLang> mapLangToRefLangs(Page<Lang> langs, String codeLang) {
        List<LangEntityRefLang> langEntityRefLangs = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        return langs.map(lng -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.LANG, lang.getId(), lng.getId());
            LangEntityRefLang langEntityRefLang = new LangEntityRefLang();
            langEntityRefLang.setId(lng.getId());

            langEntityRefLang.setCode(lng.getCode() != null ? lng.getCode() : "");
            langEntityRefLang.setName(lng.getName() != null ? lng.getName() : "");
            langEntityRefLang.setDef(lng.getDef() != null ? lng.getDef() : "");
            langEntityRefLang.setCreatedBy(lng.getCreatedBy());
            langEntityRefLang.setCreatedOn(lng.getCreatedOn());
            langEntityRefLang.setUpdatedBy(lng.getUpdatedBy());
            langEntityRefLang.setUpdatedOn(lng.getUpdatedOn());
            langEntityRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
            langEntityRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
            langEntityRefLang.setLang(codeLang);
            langEntityRefLangs.add(langEntityRefLang);
            return langEntityRefLang;
        });
    }

    public Page<LangEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
        return mapLangToRefLangs(langRepository.filterByCodeOrLabel(value, langRepository.findByCode(codeLang).getId(),  pageable), codeLang);
    }

    @Override
    public ErrorResponse deleteList(ListOfObject listOfObject) {
        ErrorResponse response = new ErrorResponse();

        try {
            for(Long id : listOfObject.getListOfObject()){
                langRepository.deleteById(id);
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
    public LangBean findById(Long id) {
        Lang result= langRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return langMapper.entityToBean(result);
    }

    @Override
    public Page<LangEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
        return langRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
    }

}
