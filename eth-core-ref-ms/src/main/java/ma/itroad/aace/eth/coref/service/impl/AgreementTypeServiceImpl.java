package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.AgreementTypeBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.Agreement;
import ma.itroad.aace.eth.coref.model.entity.AgreementType;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.mapper.AgreementTypeMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.AgreementLangProjection;
import ma.itroad.aace.eth.coref.model.mapper.projections.AgreementTypeLangProjection;
import ma.itroad.aace.eth.coref.repository.AgreementTypeRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.service.IAgreementTypeService;
import ma.itroad.aace.eth.coref.service.helper.AgreementLang;
import ma.itroad.aace.eth.coref.service.helper.AgreementTypeLang;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
import ma.itroad.aace.eth.coref.service.helper.TariffBookRefLang;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Service
public class AgreementTypeServiceImpl extends BaseServiceImpl<AgreementType,AgreementTypeBean> implements IAgreementTypeService {

    static String[] HEADERs = {"CODE AGREEMENT TYPE", "Name AGREMENT TYPE", "LABEL", "DESCRIPTION", "LANGUE"};
    static String SHEET = "AgreementsSHEET";

    @Autowired
    Validator validator;
    
    @Autowired
    AgreementTypeRepository agreementTypeRepository;

    @Autowired
    AgreementTypeMapper agreementTypeMapper;

    @Autowired
    LangRepository langRepository;

    @Autowired
    EntityRefLangRepository entityRefLangRepository;

    @Autowired
    private InternationalizationHelper internationalizationHelper;



    @Override
    public List<AgreementType> saveAll(List<AgreementType> agreementTypes) {
        if (!agreementTypes.isEmpty()) {
            return agreementTypeRepository.saveAll(agreementTypes);
        }
        return null;
    }

    public Page<AgreementTypeBean> getAll(Pageable pageable) {
        Page<AgreementType> entities = agreementTypeRepository.findAll(pageable);
        Page<AgreementTypeBean> result = entities.map(agreementTypeMapper::entityToBean);
        return result;

    }

    public Page<AgreementTypeLang> getAll(final int page, final int size, String codeLang, String orderDirection) {
        Page<AgreementType> agreementTypes = null;
        if(orderDirection.equals("DESC")){
            agreementTypes = agreementTypeRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            agreementTypes = agreementTypeRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }

        Lang lang = langRepository.findByCode(codeLang);
        List<AgreementTypeLang> agreementTypeLangs = new ArrayList<>();

        return agreementTypes.map(agreementType -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT_TYPE, lang.getId(), agreementType.getId());

            AgreementTypeLang agreementTypeLang = new AgreementTypeLang();

            agreementTypeLang.setId(agreementType.getId());
            agreementTypeLang.setCode(agreementType.getCode()!=null ?agreementType.getCode():"");
            agreementTypeLang.setName(agreementType.getName()!=null ?agreementType.getName():"");

            agreementTypeLang.setCreatedBy(agreementType.getCreatedBy());
            agreementTypeLang.setCreatedOn(agreementType.getCreatedOn());
            agreementTypeLang.setUpdatedBy(agreementType.getUpdatedBy());
            agreementTypeLang.setUpdatedOn(agreementType.getUpdatedOn());

            agreementTypeLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            agreementTypeLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

            agreementTypeLang.setLang(codeLang);

            agreementTypeLangs.add(agreementTypeLang);

            return  agreementTypeLang ;
        });
    }

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.AGREEMENT_TYPE, id);
            for (EntityRefLang entityRefLang : entityRefLangs) {
                entityRefLangRepository.delete(entityRefLang);
            }
            agreementTypeRepository.deleteById(id);
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
                agreementTypeRepository.deleteById(id);
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
    public ErrorResponse deleteInternationalisation(String codeLang, Long agreementTypeId) {

        ErrorResponse response = new ErrorResponse();
        try {

            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT_TYPE, lang.getId(), agreementTypeId);

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
    public ByteArrayInputStream agreementTypesToExcel(List<AgreementTypeLang> agreementTypeLangs) {
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
                if (!agreementTypeLangs.isEmpty()) {

                    for (AgreementTypeLang agreementTypeLang : agreementTypeLangs) {
                        Row row = sheet.createRow(rowIdx++);

                        row.createCell(0).setCellValue(agreementTypeLang.getCode());
                        row.createCell(1).setCellValue(agreementTypeLang.getName());

                        row.createCell(2).setCellValue(agreementTypeLang.getLabel());
                        row.createCell(3).setCellValue(agreementTypeLang.getDescription());
                        row.createCell(4).setCellValue(agreementTypeLang.getLang());
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
    public List<AgreementTypeLang> excelToAgreementTypes(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            List<AgreementTypeLang> agreementTypeLangs = new ArrayList<AgreementTypeLang>();
            Row currentRow;
            AgreementTypeLang agreementTypeLang ;
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                currentRow= sheet.getRow(rowNum);
                agreementTypeLang = new AgreementTypeLang();
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            agreementTypeLang.setCode(Util.cellValue(currentCell));
                            break;
                        case 1:
                            agreementTypeLang.setName(Util.cellValue(currentCell));
                            break;
                        case 2:
                            agreementTypeLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 3:
                            agreementTypeLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 4:
                            agreementTypeLang.setLang(Util.cellValue(currentCell));
                            break;
                        default:
                            break;
                    }
                }
                agreementTypeLangs.add(agreementTypeLang);
            }
            workbook.close();
            return agreementTypeLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
	@Override
	public Set<ConstraintViolation<AgreementTypeLang>> validateAgreementType(AgreementTypeLang agreementTypeLang) {

		Set<ConstraintViolation<AgreementTypeLang>> violations = validator.validate(agreementTypeLang);

		return violations;
	}

    
    @Override
	public ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file) {
		String filename = "Invalid-input.xlsx";
		InputStreamResource xls = null;
		try {
			List<AgreementTypeLang> agreementTypeLangs = excelToAgreementTypes(file.getInputStream());
			List<AgreementTypeLang> invalidAgreementTypeLangs = new ArrayList<AgreementTypeLang>();
			List<AgreementTypeLang> validAgreementTypeLangs = new ArrayList<AgreementTypeLang>();

			int lenght = agreementTypeLangs.size();

			for (int i = 0; i < lenght; i++) {

				Set<ConstraintViolation<AgreementTypeLang>> violations = validateAgreementType(
						agreementTypeLangs.get(i));
				if (violations.isEmpty())

				{
					validAgreementTypeLangs.add(agreementTypeLangs.get(i));
				} else {
					invalidAgreementTypeLangs.add(agreementTypeLangs.get(i));
				}
			}

			if (!invalidAgreementTypeLangs.isEmpty()) {

				ByteArrayInputStream out = agreementTypesToExcel(invalidAgreementTypeLangs);
				xls = new InputStreamResource(out);
			}

			if (!validAgreementTypeLangs.isEmpty()) {
				for (AgreementTypeLang l : validAgreementTypeLangs) {
					AgreementType agreementType = new AgreementType();
					AgreementTypeLang agreementTypeLang = new AgreementTypeLang();
					agreementType.setCode(l.getCode());
					agreementType.setName(l.getName());
					AgreementType ltemp = agreementTypeRepository.findByCode(l.getCode());
					if (ltemp == null) {
						Long id = (agreementTypeRepository.save(agreementType)).getId();
						EntityRefLang entityRefLang = new EntityRefLang();
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setRefId(id);
						entityRefLang.setTableRef(TableRef.AGREEMENT_TYPE);
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					} else {
						agreementType.setId(ltemp.getId());
						agreementTypeRepository.save(agreementType);
						EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(
								TableRef.AGREEMENT_TYPE, langRepository.findByCode(l.getLang()).getId(),
								agreementType.getId());
						if (entityRefLang == null) {
							entityRefLang = new EntityRefLang();
							entityRefLang.setLang(langRepository.findByCode(l.getLang()));
							entityRefLang.setTableRef(TableRef.AGREEMENT_TYPE);
							entityRefLang.setRefId(agreementType.getId());
						}
						entityRefLang.setLabel(l.getLabel());
						entityRefLang.setDescription(l.getDescription());
						entityRefLang.setLang(langRepository.findByCode(l.getLang()));
						entityRefLangRepository.save(entityRefLang);
					}

				}
			}
			if (!invalidAgreementTypeLangs.isEmpty())
				return ResponseEntity.status(HttpStatus.CONFLICT).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
						.contentType(
								new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
						.body(xls);

			if (!validAgreementTypeLangs.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(null);

			return ResponseEntity.status(HttpStatus.OK).body(null);

		} catch (IOException e) {
			throw new RuntimeException("fail to store excel data: " + e.getMessage());
		}
	}


    @Override
    public void saveFromExcel(MultipartFile file) {

        try {
            List<AgreementTypeLang> agreementTypeLangs = excelToAgreementTypes(file.getInputStream());
            if (!agreementTypeLangs.isEmpty()) {
                for (AgreementTypeLang l : agreementTypeLangs) {
                    AgreementType agreementType = new AgreementType();
                    AgreementTypeLang agreementTypeLang = new AgreementTypeLang();
                    agreementType.setCode(l.getCode());
                    agreementType.setName(l.getName());
                    AgreementType ltemp = agreementTypeRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (agreementTypeRepository.save(agreementType)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.AGREEMENT_TYPE);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        agreementType.setId( ltemp.getId());
                        agreementTypeRepository.save(agreementType);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT_TYPE,langRepository.findByCode(l.getLang()).getId(),agreementType.getId());
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.AGREEMENT_TYPE);
                            entityRefLang.setRefId(agreementType.getId());
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
        List<AgreementType> agreementTypes = agreementTypeRepository.findAll();
        ByteArrayInputStream in = null;
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {

        Lang lang = langRepository.findByCode(codeLang);
        List<AgreementType> agreementTypes = agreementTypeRepository.findAll();
        List<AgreementTypeLang> agreementTypeLangs = new ArrayList<>();

        for (AgreementType agreementType : agreementTypes) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT_TYPE, lang.getId(), agreementType.getId());
            if (entityRefLang != null) {
                AgreementTypeLang agreementTypeLang = new AgreementTypeLang();


                agreementTypeLang.setCode(agreementType.getCode());
                agreementTypeLang.setName(agreementType.getName());

                agreementTypeLang.setLabel(entityRefLang.getLabel());
                agreementTypeLang.setDescription(entityRefLang.getDescription());
                agreementTypeLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                agreementTypeLangs.add(agreementTypeLang);
            }
        }
        ByteArrayInputStream in = agreementTypesToExcel(agreementTypeLangs);
        return in;
    }

    @Override
    public void addAgreementType(AgreementTypeLang agreementTypeLang){
        AgreementType agreementType = new AgreementType();

        agreementType.setCode(agreementTypeLang.getCode()!=null?agreementTypeLang.getCode():null);
        agreementType.setName(agreementTypeLang.getName());

        AgreementType ltemp = agreementTypeRepository.findByCode(agreementTypeLang.getCode());

        if (ltemp == null) {
            Long id = (agreementTypeRepository.save(agreementType)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();

            entityRefLang.setLabel(agreementTypeLang.getLabel());
            entityRefLang.setDescription(agreementTypeLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.AGREEMENT_TYPE);
            entityRefLang.setLang(langRepository.findByCode(agreementTypeLang.getLang()));
            entityRefLangRepository.save(entityRefLang);

        } else {
            Long id = ltemp.getId();
            agreementType.setId(id);
            agreementTypeRepository.save(agreementType);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT_TYPE, langRepository.findByCode(agreementTypeLang.getLang()).getId() ,id);
            entityRefLang.setLabel(agreementTypeLang.getLabel());
            entityRefLang.setDescription(agreementTypeLang.getDescription());
            entityRefLang.setLang(langRepository.findByCode(agreementTypeLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.AGREEMENT_TYPE);
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
        entityRefLang.setTableRef(TableRef.AGREEMENT_TYPE);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT_TYPE, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    @Override
    public AgreementTypeLang findAgreementType(Long id, String lang){
        AgreementTypeLang agreementTypeLang = new AgreementTypeLang();
        AgreementType agreementType = agreementTypeRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT_TYPE,langRepository.findByCode(lang).getId(),agreementType.getId());

        agreementTypeLang.setCode(agreementType.getCode());
        agreementTypeLang.setName(agreementType.getName());

        agreementTypeLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        agreementTypeLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

        agreementTypeLang.setLang(lang);
        return agreementTypeLang;
    }

    public Page<AgreementTypeLang> mapAgreementTypesToRefLangs(Page<AgreementType> agreementTypes, String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<AgreementTypeLang> agreementTypeLangs = new ArrayList<>();

        return agreementTypes.map(agreementType -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.AGREEMENT_TYPE, lang.getId(), agreementType.getId());

            AgreementTypeLang agreementTypeLang = new AgreementTypeLang();

            agreementTypeLang.setId(agreementType.getId());
            agreementTypeLang.setCode(agreementType.getCode()!=null ?agreementType.getCode():"");
            agreementTypeLang.setName(agreementType.getName()!=null ?agreementType.getName():"");

            agreementTypeLang.setCreatedBy(agreementType.getCreatedBy());
            agreementTypeLang.setCreatedOn(agreementType.getCreatedOn());
            agreementTypeLang.setUpdatedBy(agreementType.getUpdatedBy());
            agreementTypeLang.setUpdatedOn(agreementType.getUpdatedOn());

            agreementTypeLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            agreementTypeLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

            agreementTypeLang.setLang(codeLang);

            agreementTypeLangs.add(agreementTypeLang);

            return  agreementTypeLang ;
        });
    }

    public Page<AgreementTypeLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang)  {
        return mapAgreementTypesToRefLangs(agreementTypeRepository.filterByCodeOrLabel(value ,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
    }

    @Override
    public Page<AgreementTypeLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
        return agreementTypeRepository.filterByCodeOrLabelProjection(value.toLowerCase(),pageable,langRepository.findByCode(lang).getId());
    }

    @Override
    public AgreementTypeBean findById(Long id) {
        AgreementType agreementType= agreementTypeRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return agreementTypeMapper.entityToBean(agreementType);
    }

}