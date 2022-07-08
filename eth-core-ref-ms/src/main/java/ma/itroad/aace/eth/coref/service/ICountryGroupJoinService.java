package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CountryGroupRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.mapper.projections.CountryGroupReferenceJoinProjection;
import ma.itroad.aace.eth.coref.model.view.CountryGroupReferenceVM;
import ma.itroad.aace.eth.coref.service.helper.CountryGroupReferenceVMEntityRefLang;
import org.springframework.data.domain.Page;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface ICountryGroupJoinService extends ImportDataService {

    Page<CountryGroupReferenceVM> getAll(final int page, final int size, String orderDirection);

    ByteArrayInputStream countryGroupJoinToExcel(List<CountryGroupReferenceVMEntityRefLang> countryGroupReferenceVMEntityRefLangs);

    List<CountryGroupReferenceVM> excelToElemetsRefsRef(InputStream is);
     ByteArrayInputStream load(String codeLang);
    CountryGroupReferenceVM save(CountryGroupReferenceVM model);
    CountryGroupReferenceVMEntityRefLang findCountryGroupReferenceVM(Long idCountry,Long idCountryGroup, String lang);
    ErrorResponse deleteList(ListOfObjectTarifBook listOfObjectTarifBook);
    ErrorResponse delete(Long id, Long id_tarif);
    Page<CountryGroupReferenceJoinProjection> findByRefrence(int page, int size, String reference);
}
