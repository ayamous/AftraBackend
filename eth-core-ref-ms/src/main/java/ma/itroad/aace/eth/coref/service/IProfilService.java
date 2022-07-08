package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ProfilBean;
import ma.itroad.aace.eth.coref.model.entity.Profil;
import org.springframework.data.domain.Page;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface IProfilService extends
        IBaseService<Profil, ProfilBean>,
        IRsqlService<Profil, ProfilBean>,
        ImportDataService {

    Page<ProfilBean> getAll(final int page, final int size);

    List<Profil> excelToProfils(InputStream is);

    ByteArrayInputStream profilsToExcel(List<Profil> profils);

    ErrorResponse delete(Long id);
}
