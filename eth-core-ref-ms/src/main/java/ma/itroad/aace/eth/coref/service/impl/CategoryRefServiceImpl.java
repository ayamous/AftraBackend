package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.security.enums.ErrorMessageType;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CategoryRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.CategoryRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.mapper.CategoryRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.CategoryRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.CategoryRefRepository;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.service.ICategoryRefService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.service.helper.CategoryRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.InternationalizationHelper;
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
import java.util.Set;

@Service
public class CategoryRefServiceImpl extends BaseServiceImpl<CategoryRef,CategoryRefBean> implements ICategoryRefService {

    static String[] HEADERs = {"Code catégorie Organisation", "Label", "Description", "Langue"};
    static String SHEET = "Categorie";

    @Autowired
    private CategoryRefRepository categoryRefRepository;

    @Autowired
    CategoryRefMapper categoryRefMapper;

    @Autowired
    LangRepository langRepository;

    @Autowired
    EntityRefLangRepository entityRefLangRepository;

    @Autowired
    private Validator validator;


    @Autowired
    private InternationalizationHelper internationalizationHelper;

    @Override
    public List<CategoryRef> saveAll(List<CategoryRef> categoryRefs) {
        if (!categoryRefs.isEmpty()) {
            return categoryRefRepository.saveAll(categoryRefs);
        }
        return null;
    }

    public Page<CategoryRefBean> getAll(Pageable pageable) {
        Page<CategoryRef> entities = categoryRefRepository.findAll(pageable);
        Page<CategoryRefBean> result = entities.map(categoryRefMapper::entityToBean);
        return result;

    }

    public Page<CategoryRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection) {

        Page<CategoryRef> categoryRefs = null;
        if(orderDirection.equals("DESC")){
            categoryRefs = categoryRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedOn")));
        }else if(orderDirection.equals("ASC")){
            categoryRefs = categoryRefRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "updatedOn")));
        }
        Lang lang = langRepository.findByCode(codeLang);
        List<CategoryRefEntityRefLang> categoryRefEntityRefLangs = new ArrayList<>();

        return categoryRefs.map(categoryRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CATEGORY_REF, lang.getId(), categoryRef.getId());

            CategoryRefEntityRefLang categoryRefEntityRefLang = new CategoryRefEntityRefLang();

            categoryRefEntityRefLang.setId(categoryRef.getId());
            categoryRefEntityRefLang.setCode(categoryRef.getCode()!=null ?categoryRef.getCode():"");

            categoryRefEntityRefLang.setCreatedBy(categoryRef.getCreatedBy());
            categoryRefEntityRefLang.setCreatedOn(categoryRef.getCreatedOn());
            categoryRefEntityRefLang.setUpdatedBy(categoryRef.getUpdatedBy());
            categoryRefEntityRefLang.setUpdatedOn(categoryRef.getUpdatedOn());

            categoryRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            categoryRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

            categoryRefEntityRefLang.setLang(codeLang);

            categoryRefEntityRefLangs.add(categoryRefEntityRefLang);

            return  categoryRefEntityRefLang ;
        });
    }

    @Override
    public ErrorResponse delete(Long id) {
        ErrorResponse response = new ErrorResponse();
        try {
            List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(TableRef.CATEGORY_REF, id);
            for (EntityRefLang entityRefLang : entityRefLangs) {
                entityRefLangRepository.delete(entityRefLang);
            }
            categoryRefRepository.deleteById(id);
            response.setStatus(HttpStatus.OK);
            response.setErrorMsg("null");
        } catch (Exception e) {
            response.setErrorMsg(ErrorMessageType.DELETE_ERROR.getMessagePattern());
            response.setStatus(HttpStatus.CONFLICT);
        }
        return response;
    }

    @Override
    public ErrorResponse deleteInternationalisation(String codeLang, Long categoryRefId) {

        ErrorResponse response = new ErrorResponse();
        try {

            Lang lang = langRepository.findByCode(codeLang);

            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CATEGORY_REF, lang.getId(), categoryRefId);

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
    public ByteArrayInputStream categoryRefToExcel(List<CategoryRefEntityRefLang> categoryRefEntityRefLangs) {
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
                if (!categoryRefEntityRefLangs.isEmpty()) {

                    for (CategoryRefEntityRefLang categoryRefEntityRefLang : categoryRefEntityRefLangs) {
                        Row row = sheet.createRow(rowIdx++);

                        row.createCell(0).setCellValue(categoryRefEntityRefLang.getCode());
                        row.createCell(1).setCellValue(categoryRefEntityRefLang.getLabel());
                        row.createCell(2).setCellValue(categoryRefEntityRefLang.getDescription());
                        row.createCell(3).setCellValue(categoryRefEntityRefLang.getLang());
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
    public List<CategoryRefEntityRefLang> excelToCategoryRef(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            List<CategoryRefEntityRefLang> categoryRefEntityRefLangs = new ArrayList<CategoryRefEntityRefLang>();
            Row currentRow;
            CategoryRefEntityRefLang categoryRefEntityRefLang ;
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                currentRow= sheet.getRow(rowNum);
                categoryRefEntityRefLang = new CategoryRefEntityRefLang();
                for (int colNum = 0; colNum < currentRow.getLastCellNum(); colNum++) {
                    Cell currentCell = currentRow.getCell(colNum, Row.CREATE_NULL_AS_BLANK);
                    switch (colNum) {
                        case 0:
                            categoryRefEntityRefLang.setCode(Util.cellValue(currentCell));
                            break;
                        case 1:
                            categoryRefEntityRefLang.setLabel(Util.cellValue(currentCell));
                            break;
                        case 2:
                            categoryRefEntityRefLang.setDescription(Util.cellValue(currentCell));
                            break;
                        case 3:
                            categoryRefEntityRefLang.setLang(Util.cellValue(currentCell));
                            break;
                        default:
                            break;
                    }
                }
                categoryRefEntityRefLangs.add(categoryRefEntityRefLang);
            }
            workbook.close();
            return categoryRefEntityRefLangs;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public void saveFromExcel(MultipartFile file) {
        try {
            List<CategoryRefEntityRefLang> categoryRefEntityRefLangs = excelToCategoryRef(file.getInputStream());
            if (!categoryRefEntityRefLangs.isEmpty()) {
                for (CategoryRefEntityRefLang l : categoryRefEntityRefLangs) {
                    CategoryRef categoryRef = new CategoryRef();
                    categoryRef.setCode(l.getCode());
                    CategoryRef ltemp = categoryRefRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (categoryRefRepository.save(categoryRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.CATEGORY_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        categoryRef.setId( ltemp.getId());
                        categoryRefRepository.save(categoryRef);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CATEGORY_REF,langRepository.findByCode(l.getLang()).getId(),categoryRef.getId());
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.CATEGORY_REF);
                            entityRefLang.setRefId(categoryRef.getId());
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
        List<CategoryRef> categoryRefs = categoryRefRepository.findAll();
        ByteArrayInputStream in = null;
        return in;
    }

    @Override
    public ByteArrayInputStream load(String codeLang) {

        Lang lang = langRepository.findByCode(codeLang);
        List<CategoryRef> categoryRefs = categoryRefRepository.findAll();
        List<CategoryRefEntityRefLang> categoryRefEntityRefLangs = new ArrayList<>();

        for (CategoryRef categoryRef : categoryRefs) {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CATEGORY_REF, lang.getId(), categoryRef.getId());
            if (entityRefLang != null) {
                CategoryRefEntityRefLang categoryRefEntityRefLang = new CategoryRefEntityRefLang();


                categoryRefEntityRefLang.setCode(categoryRef.getCode());

                categoryRefEntityRefLang.setLabel(entityRefLang.getLabel());
                categoryRefEntityRefLang.setDescription(entityRefLang.getDescription());
                categoryRefEntityRefLang.setLang(entityRefLang.getLang() != null && entityRefLang.getLang().getCode() != null ? entityRefLang.getLang().getCode() : null);
                categoryRefEntityRefLangs.add(categoryRefEntityRefLang);
            }
        }
        ByteArrayInputStream in = categoryRefToExcel(categoryRefEntityRefLangs);
        return in;
    }

    @Override
    public void addCategory(CategoryRefEntityRefLang categoryRefEntityRefLang){
        CategoryRef categoryRef = new CategoryRef();

        categoryRef.setCode(categoryRefEntityRefLang.getCode());

        CategoryRef ltemp = categoryRefRepository.findByCode(categoryRefEntityRefLang.getCode());

        if (ltemp == null) {
            Long id = (categoryRefRepository.save(categoryRef)).getId();
            EntityRefLang entityRefLang = new EntityRefLang();

            entityRefLang.setLabel(categoryRefEntityRefLang.getLabel());
            entityRefLang.setDescription(categoryRefEntityRefLang.getDescription());
            entityRefLang.setRefId(id);
            entityRefLang.setTableRef(TableRef.CATEGORY_REF);
            entityRefLang.setLang(langRepository.findByCode(categoryRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);

        } else {
            Long id = ltemp.getId();
            categoryRef.setId(id);
            categoryRefRepository.save(categoryRef);
            EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CATEGORY_REF, langRepository.findByCode(categoryRefEntityRefLang.getLang()).getId() ,id);
            entityRefLang.setLabel(categoryRefEntityRefLang.getLabel());
            entityRefLang.setDescription(categoryRefEntityRefLang.getDescription());
            entityRefLang.setLang(langRepository.findByCode(categoryRefEntityRefLang.getLang()));
            entityRefLangRepository.save(entityRefLang);
        }
    }

    @Override
    public List<InternationalizationVM> getInternationalizationRefList(Long id) {
        return internationalizationHelper.getInternationalizationRefList(id, TableRef.CATEGORY_REF);
    }

    @Override
    public void addInternationalisation(EntityRefLang entityRefLang, String lang) {
        entityRefLang.setTableRef(TableRef.CATEGORY_REF);
        entityRefLang.setLang(langRepository.findByCode(lang));
        if (entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CATEGORY_REF, entityRefLang.getLang().getId(), entityRefLang.getRefId()) == null)
            entityRefLangRepository.save(entityRefLang);
        else throw new RuntimeException("il y'a deja une traduction a cet enregistrement avec cette langue");
    }

    @Override
    public CategoryRefEntityRefLang findCategory(Long id, String lang){
        CategoryRefEntityRefLang categoryRefEntityRefLang = new CategoryRefEntityRefLang();
        CategoryRef categoryRef = categoryRefRepository.findOneById(id);
        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CATEGORY_REF,langRepository.findByCode(lang).getId(),categoryRef.getId());

        categoryRefEntityRefLang.setCode(categoryRef.getCode());

        categoryRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
        categoryRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");
        categoryRefEntityRefLang.setLang(lang);
        return categoryRefEntityRefLang;
    }

    public Page<CategoryRefEntityRefLang> mapcategoryRefsToRefLangs(Page<CategoryRef> categoryRefs, String codeLang) {
        Lang lang = langRepository.findByCode(codeLang);
        List<CategoryRefEntityRefLang> categoryRefEntityRefLangs = new ArrayList<>();

        return categoryRefs.map(categoryRef -> {
            EntityRefLang entityRefLang = entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CATEGORY_REF, lang.getId(), categoryRef.getId());

            CategoryRefEntityRefLang categoryRefEntityRefLang = new CategoryRefEntityRefLang();

            categoryRefEntityRefLang.setId(categoryRef.getId());
            categoryRefEntityRefLang.setCode(categoryRef.getCode()!=null ?categoryRef.getCode():"");

            categoryRefEntityRefLang.setCreatedBy(categoryRef.getCreatedBy());
            categoryRefEntityRefLang.setCreatedOn(categoryRef.getCreatedOn());
            categoryRefEntityRefLang.setUpdatedBy(categoryRef.getUpdatedBy());
            categoryRefEntityRefLang.setUpdatedOn(categoryRef.getUpdatedOn());

            categoryRefEntityRefLang.setLabel(entityRefLang!=null ?entityRefLang .getLabel():"");
            categoryRefEntityRefLang.setDescription(entityRefLang!=null ?entityRefLang .getDescription():"");

            categoryRefEntityRefLang.setLang(codeLang);

            categoryRefEntityRefLangs.add(categoryRefEntityRefLang);

            return  categoryRefEntityRefLang ;
        });
    }


//    @Override
//    public Page<CategoryRefEntityRefLang> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang) {
//        return mapcategoryRefsToRefLangs(categoryRefRepository.filterByCodeOrLabel(value ,langRepository.findByCode(lang).getId(), pageable), lang);
//    }


    public Page<CategoryRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String codeLang)  {
        return categoryRefRepository.filterByCodeOrLabelProjection(value.toLowerCase() ,langRepository.findByCode(codeLang).getId(), pageable);
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
    public Page<CategoryRefEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang)  {
        return mapcategoryRefsToRefLangs(categoryRefRepository.filterByCodeOrLabel(value ,langRepository.findByCode(codeLang).getId(), pageable), codeLang);
    }


    @Override
    public CategoryRefBean findById(Long id) {
        CategoryRef categoryRef= categoryRefRepository.findById(id).orElseThrow(
                ()->new NotFoundException("id not found"));
        return categoryRefMapper.entityToBean(categoryRef);
    }

    public Set<ConstraintViolation<CategoryRefEntityRefLang>> validateItems(CategoryRefEntityRefLang categoryRefEntityRefLang) {
        Set<ConstraintViolation<CategoryRefEntityRefLang>> violations = validator.validate(categoryRefEntityRefLang);
        return violations;
    }

    @Override
    public ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file) {
        String filename = "Invalid-input.xlsx";
        InputStreamResource xls = null;
        try {
            List<CategoryRefEntityRefLang> itemsList = excelToCategoryRef(file.getInputStream());
            List<CategoryRefEntityRefLang> invalidItems = new ArrayList<CategoryRefEntityRefLang>();
            List<CategoryRefEntityRefLang> validItems = new ArrayList<CategoryRefEntityRefLang>();

            int lenght = itemsList.size();

            for (int i = 0; i < lenght; i++) {

                Set<ConstraintViolation<CategoryRefEntityRefLang>> violations = validateItems(itemsList.get(i));
                if (violations.isEmpty())

                {
                    validItems.add(itemsList.get(i));
                } else {
                    invalidItems.add(itemsList.get(i));
                }
            }

            if (!invalidItems.isEmpty()) {

                ByteArrayInputStream out = categoryRefToExcel(invalidItems);
                xls = new InputStreamResource(out);

            }

            if (!validItems.isEmpty()) {
                for (CategoryRefEntityRefLang l : validItems) {
                    CategoryRef categoryRef = new CategoryRef();
                    categoryRef.setCode(l.getCode());
                    CategoryRef ltemp = categoryRefRepository.findByCode(l.getCode());
                    if (ltemp == null) {
                        Long id = (categoryRefRepository.save(categoryRef)).getId();
                        EntityRefLang entityRefLang = new EntityRefLang();
                        entityRefLang.setLabel(l.getLabel());
                        entityRefLang.setDescription(l.getDescription());
                        entityRefLang.setRefId(id);
                        entityRefLang.setTableRef(TableRef.CATEGORY_REF);
                        entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                        entityRefLangRepository.save(entityRefLang);
                    } else {
                        categoryRef.setId( ltemp.getId());
                        categoryRefRepository.save(categoryRef);
                        EntityRefLang entityRefLang =  entityRefLangRepository.findByTableRefAndLang_IdAndRefId(TableRef.CATEGORY_REF,langRepository.findByCode(l.getLang()).getId(),categoryRef.getId());
                        if(entityRefLang == null )
                        {
                            entityRefLang =new EntityRefLang() ;
                            entityRefLang.setLang(langRepository.findByCode(l.getLang()));
                            entityRefLang.setTableRef(TableRef.CATEGORY_REF);
                            entityRefLang.setRefId(categoryRef.getId());
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
