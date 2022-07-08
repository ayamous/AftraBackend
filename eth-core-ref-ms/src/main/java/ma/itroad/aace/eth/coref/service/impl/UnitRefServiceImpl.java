package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.UnitRefBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.UnitRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.UnitRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.UnitRefRepository;
import ma.itroad.aace.eth.coref.service.IUnitRefService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.service.helper.*;
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
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Service
public class UnitRefServiceImpl extends BaseServiceImpl<UnitRef,UnitRefBean> implements IUnitRefService {

    static String[] HEADERs = { "CODE", "NAME", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "UnitsSHEET";
    
    @Autowired
    Validator validator;

    @Autowired
    private UnitRefRepository unitRefRepository;

    @Autowired
    private UnitRefMapper unitRefMapper;

    @Autowired
    private EntityRefLangRepository entityRefLangRepository;

    @Autowired
    LangRepository langRepository;

    @Autowired
    InternationalizationHelper internationalizationHelper;


    public Page<UnitRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<UnitRef> unitRefs = null;
        if(orderDirection.equals("DESC")){
            unitRefs = unitRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            unitRefs = unitRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }

        Lang lang = langRepository.findByCode(codeLang);
        List<UnitRefEntityRefLang> unitRefEntityRefLangs = new ArrayList<>();
        return unitRefs.map(unitRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.UNIT_REF, lang.getId(), unitRef.getId());
try {
    System.out.println("entityRefLang  ::" + entityRefLang);
}catch (Exception e){
    e.getMessage();
}
                UnitRefEntityRefLang unitRefEntityRefLang = new UnitRefEntityRefLang();
                unitRefEntityRefLang.setId(unitRef.getId());
                unitRefEntityRefLang.setCode(unitRef.getCode()!=null ?unitRef.getCode():"");
                unitRefEntityRefLang.setName(unitRef.getName()!=null ?unitRef.getName():"");
                unitRefEntityRefLang.setCreatedBy(unitRef.getCreatedBy());
                unitRefEntityRefLang.setCreatedOn(unitRef.getCreatedOn());
                unitRefEntityRefLang.setUpdatedBy(unitRef.getUpdatedBy());
                unitRefEntityRefLang.setUpdatedOn(unitRef.getUpdatedOn());
                unitRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
                unitRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
                unitRefEntityRefLang.setLang(codeLang);
                unitRefEntityRefLangs.add(unitRefEntityRefLang);

            return  unitRefEntityRefLang ;
        });
    }

    @Override
    public List<UnitRef> saveAll(List<UnitRef> unitRefs) {
        if(!unitRefs.isEmpty()){
            return unitRefRepository.saveAll(unitRefs);
        }
        return null;
    }

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.UNIT_REF, id);
            for(EntityRefLang entityRefLang:entityRefLangs ){
                entityRefLangRepository.delete(entityRefLang);
            }
            unitRefRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return  response;
    }

    @Override
    public ErrorResponse deleteInternationalisation(String codeLang, Long unitRefId) {
        ErrorResponse response = new ErrorResponse();
        try {
            Lang lang = langRepository.findByCode(codeLang);
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.UNIT_REF, lang.getId(), unitRefId);

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
    public ByteArrayInputStream unitrefsToExcel(List<UnitRefEntityRefLang> unitRefEntityRefLangs) {

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
                if(!unitRefEntityRefLangs.isEmpty()){

                    for (UnitRefEntityRefLang unitRefEntityRefLang : unitRefEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(unitRefEntityRefLang.getCode());
                        row.createCell(1).setCellValue(unitRefEntityRefLang.getName());
                        row.createCell(2).setCellValue(unitRefEntityRefLang.getLabel());
                        row.createCell(3).setCellValue(unitRefEntityRefLang.getDescription());
                        row.createCell(4).setCellValue(unitRefEntityRefLang.getLang());
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
    public List<UnitRefEntityRefLang> excelToUnitsRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            List<UnitRefEntityRefLang> unitRefEntityRefLangs = new ArrayList<UnitRefEntityRefLang>();
            Row currentRow;
            UnitRefEntityRefLang unitRefEntityRefLang ;
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                currentRow= sheet.getRow(rowNum);
                unitRefEntityRefLang = new UnitRefEntityRefLang();
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            unitRefEntityRefLang.setCode(Util.cellValue(currentCell));
                            break;
                        case 1:
                            unitRefEntityRefLang.setName(Util.cellValue(currentCell));
                            break;
                        case 2:
                            unitRefEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 3:
                            unitRefEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 4:
                            unitRefEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;
                        default:
                            break;
                    }
                }
                unitRefEntityRefLangs.add(unitRefEntityRefLang);
            }
            workbook.close();
            return unitRefEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

	@Override
	public Set<ConstraintViolation<UnitRefEntityRefLang>> validateUnits(UnitRefEntityRefLang unitRefEntityRefLang) {

		Set<ConstraintViolation<UnitRefEntityRefLang>> violations = validator.validate(unitRefEntityRefLang);

		return violations;
	}
	
	@Override
	public ResponseEntity<?> saveFromAfterValidation(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {
			List<UnitRefEntityRefLang> unitRefEntityRefLangs = excelToUnitsRef(file.getInputStream());
			List<UnitRefEntityRefLang> InvalidunitRefEntityRefLangs = new ArrayList<UnitRefEntityRefLang>();
			List<UnitRefEntityRefLang> ValidunitRefEntityRefLangs = new ArrayList<UnitRefEntityRefLang>();

			int lenght = unitRefEntityRefLangs.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<UnitRefEntityRefLang>> violations = validateUnits(unitRefEntityRefLangs.get(i));
				if (violations.isEmpty())

				{
					ValidunitRefEntityRefLangs.add(unitRefEntityRefLangs.get(i));
				} else {
					InvalidunitRefEntityRefLangs.add(unitRefEntityRefLangs.get(i));
				}
			}

			if (!InvalidunitRefEntityRefLangs.isEmpty()) {
				
				ByteArrayInputStream out = unitrefsToExcel(InvalidunitRefEntityRefLangs);
				xls = new InputStreamResource(out);
			}

			if (!ValidunitRefEntityRefLangs.isEmpty()) {
				for (UnitRefEntityRefLang l : ValidunitRefEntityRefLangs) {
					UnitRef unitRef = new UnitRef();
					unitRef.setCode(l.getCode());
					unitRef.setName(l.getName());
					UnitRef ltemp = unitRefRepository.findByCode(l.getCode());
					if (ltemp == null) {
						Long id = (unitRefRepository.save(unitRef)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.UNIT_REF);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					} else {
						unitRef.setId(ltemp.getId());
						unitRefRepository.save(unitRef);
						EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
								TableRef.UNIT_REF, langRepository.findByCode(l.getLang()).getId(), unitRef.getId());
						if (entityRefLang == null) {
							entityRefLang = new EntityRefLang();
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.UNIT_REF);
							entityRefLang.setRefId(unitRef.getId());
						}
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}

				}
				
			}

			if (!InvalidunitRefEntityRefLangs.isEmpty())
				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);

			if (!ValidunitRefEntityRefLangs.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(null);

			 return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (IOException e) {

			  return ResponseEntity.status(HttpStatus.OK).body("fail to store excel data: " + e.getMessage());
		}
	}


    @Override
    public void saveFromExcel(MultipartFile file) {
        try {
            List<UnitRefEntityRefLang> unitRefEntityRefLangs = excelToUnitsRef(file.getInputStream());
            if (!unitRefEntityRefLangs.isEmpty()) {
                for (UnitRefEntityRefLang l : unitRefEntityRefLangs) {
                    UnitRef unitRef = new UnitRef();
                    unitRef.setCode(l.getCode());
                    unitRef.setName(l.getName());
                    UnitRef ltemp = unitRefRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (unitRefRepository.save(unitRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.UNIT_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        unitRef.setId( ltemp.getId());
                        unitRefRepository.save(unitRef);
                       EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.UNIT_REF,langRepository.findByCode(l.getLang()).getId(),unitRef.getId());
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.UNIT_REF);
                            entityRefLang.setRefId(unitRef.getId());
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
        List<UnitRef> unitRefs = unitRefRepository.findAll();
        ByteArrayInputStream in =null;
            //    = unitrefsToExcel(unitRefs);
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<UnitRef> unitRefs = unitRefRepository.findAll();
        List<UnitRefEntityRefLang> unitRefEntityRefLangs = new ArrayList<UnitRefEntityRefLang>();

        for(UnitRef u : unitRefs) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.UNIT_REF, lang.getId(), u.getId());
            if (entityRefLang!=null) {
                UnitRefEntityRefLang unitRefEntityRefLang = new UnitRefEntityRefLang();
                unitRefEntityRefLang.setCode(u.getCode());
                unitRefEntityRefLang.setName(u.getName());
                unitRefEntityRefLang.setLabel(entityRefLang.getLabel());
                unitRefEntityRefLang.setDescription(entityRefLang.getDescription());
                unitRefEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                unitRefEntityRefLangs.add(unitRefEntityRefLang);
            }
        }
        ByteArrayInputStream in = unitrefsToExcel(unitRefEntityRefLangs);
        return in;
    }

    @Override
    public void addUnitRef(UnitRefEntityRefLang unitRefEntityRefLang){
        UnitRef unitRef = new UnitRef();
        unitRef.setCode(unitRefEntityRefLang.getCode());
        unitRef.setName(unitRefEntityRefLang.getName());
        UnitRef ltemp = unitRefRepository.findByCode(unitRefEntityRefLang.getCode());
        if (ltemp == null) {
            Long id = (unitRefRepository.save(unitRef)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(unitRefEntityRefLang.getLabel());
            entityRefLang.setDescription(unitRefEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.UNIT_REF);
            entityRefLang.setLang(  langRepository.findByCode(unitRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            unitRef.setId(id);
            unitRefRepository.save(unitRef);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.UNIT_REF, langRepository.findByCode(unitRefEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(unitRefEntityRefLang.getLabel());
            entityRefLang.setDescription(unitRefEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {

        return internationalizationHelper.getInternationalizationRefList(id, TableRef.UNIT_REF);

    }
/*
    @Override
    public void addInternationalisation(UnitRefEntityRefLang unitRefEntityRefLang){
        UnitRef unitRef = new UnitRef();
        unitRef.setCode(unitRefEntityRefLang.getCode());


        UnitRef ltemp = unitRefRepository.findByCode(unitRefEntityRefLang.getCode());
        if (ltemp == null) {

            Long id = (unitRefRepository.save(unitRef)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();
            entityRefLang.setLabel(unitRefEntityRefLang.getLabel());
            entityRefLang.setDescription(unitRefEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.UNIT_REF);
            entityRefLang.setLang(  langRepository.findByCode(unitRefEntityRefLang.getLang())                   );
            entityRefLangRepository.save(entityRefLang);
        } else {
            Long id = ltemp.getId();
            unitRef.setId(id);
            unitRefRepository.save(unitRef);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.UNIT_REF,langRepository.findByCode(unitRefEntityRefLang.getLang()).getId(),id);
            entityRefLang.setLabel(unitRefEntityRefLang.getLabel());
            entityRefLang.setDescription(unitRefEntityRefLang.getDescription());
            entityRefLangRepository.save(entityRefLang);
        }
    }*/

    public UnitRefEntityRefLang findUnitRef(Long id, String lang){
        UnitRefEntityRefLang unitRefEntityRefLang = new UnitRefEntityRefLang();
        UnitRef unitRef = unitRefRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.UNIT_REF,langRepository.findByCode(lang).getId(),unitRef.getId());
        unitRefEntityRefLang.setCode(unitRef.getCode());
        unitRefEntityRefLang.setName(unitRef.getName());
        unitRefEntityRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
        unitRefEntityRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
        unitRefEntityRefLang.setLang(lang);
        return unitRefEntityRefLang;
    }

    public void addInternationalisation(EntityRefLang entityRefLang,String lang){
        entityRefLang.setTableRef(TableRef.UNIT_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.UNIT_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    public Page<UnitRefBean> getAll(Pageable pageable) {
        Page<UnitRef> entities = unitRefRepository.findAll(pageable);
        Page<UnitRefBean> result = entities.map(unitRefMapper::entityToBean);
        return result;
    }

    public Page<UnitRefEntityRefLang> mapLangToRefLangs(Page<UnitRef> unitRefs, String codeLang) {
        List<UnitRefEntityRefLang> unitRefEntityRefLangs = new ArrayList<>();
        Lang lang = langRepository.findByCode(codeLang);
        return unitRefs.map(unitRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.UNIT_REF, lang.getId(), unitRef.getId());
            UnitRefEntityRefLang unitRefEntityRefLang = new UnitRefEntityRefLang();
            unitRefEntityRefLang.setId(unitRef.getId());

            unitRefEntityRefLang.setCode(unitRef.getCode() != null ? unitRef.getCode() : "");
            unitRefEntityRefLang.setName(unitRef.getName() != null ? unitRef.getName() : "");
            unitRefEntityRefLang.setCreatedBy(unitRef.getCreatedBy());
            unitRefEntityRefLang.setCreatedOn(unitRef.getCreatedOn());
            unitRefEntityRefLang.setUpdatedBy(unitRef.getUpdatedBy());
            unitRefEntityRefLang.setUpdatedOn(unitRef.getUpdatedOn());
            unitRefEntityRefLang.setLabel(entityRefLang != null ? entityRefLang.getLabel() : "");
            unitRefEntityRefLang.setDescription(entityRefLang != null ? entityRefLang.getDescription() : "");
            unitRefEntityRefLang.setLang(codeLang);
            unitRefEntityRefLangs.add(unitRefEntityRefLang);
            return unitRefEntityRefLang;
        });
    }

    public Page<UnitRefEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) {
        return mapLangToRefLangs(unitRefRepository.filterByCodeOrLabel(value,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
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
    public Page<UnitRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
        return unitRefRepository.filterByCodeOrLabelProjection(value.toLowerCase(),langRepository.findByCode(lang).getId(),pageable);
    }

    @Override
    public UnitRefBean findById(Long id) {
        UnitRef result= unitRefRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return unitRefMapper.entityToBean(result);
    }

}
