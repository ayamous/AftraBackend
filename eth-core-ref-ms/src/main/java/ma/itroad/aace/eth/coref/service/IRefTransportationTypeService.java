package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.RefTransportationTypeBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.RefTransportationType;
import ma.itroad.aace.eth.coref.model.mapper.projections.TransportationTypeEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.TransportationTypeEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.UnitRefEntityRefLang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface IRefTransportationTypeService extends
        IBaseService<RefTransportationType, RefTransportationTypeBean>,
        IBaseRefService,
        IRsqlService<RefTransportationType, RefTransportationTypeBean>, ImportDataService {

    Page<TransportationTypeEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<RefTransportationType> saveAll(List<RefTransportationType> refTransportationTypes);

    ErrorResponse delete(Long id);

    ErrorResponse deleteInternationalisation(String codeLang, Long refTransportationTypeId) ;

    ByteArrayInputStream refTransportationTypesToExcel(List<TransportationTypeEntityRefLang> refTransportationTypes);

    List<TransportationTypeEntityRefLang> excelToRefTransportationTypes(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addTransportationTypeEntityRef(TransportationTypeEntityRefLang transportationTypeEntityRefLang);

    TransportationTypeEntityRefLang findTransportationType(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<TransportationTypeEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

    ErrorResponse deleteList(ListOfObject listOfObject);

    RefTransportationTypeBean findById(Long id);

    Page<TransportationTypeEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

    Set<ConstraintViolation<TransportationTypeEntityRefLang>> validateTransportationType(TransportationTypeEntityRefLang transportationTypeEntityRefLang);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);
}
