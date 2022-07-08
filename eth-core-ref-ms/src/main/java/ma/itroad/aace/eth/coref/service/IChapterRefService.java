package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.AgreementBean;
import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.model.mapper.projections.ChapterRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.ChapterRefVM;
import ma.itroad.aace.eth.coref.service.helper.ChapterRefLang;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface IChapterRefService extends
		IBaseService<ChapterRef, ChapterRefBean>,
		IBaseRefService,
		IRsqlService<ChapterRef, ChapterRefBean>, ImportDataService{


	Page<ChapterRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

	List<ChapterRef> saveAll(List<ChapterRef> chapterRefs);

	ErrorResponse delete(Long id);

	ErrorResponse deleteList(ListOfObject listOfObject);

	ErrorResponse deleteInternationalisation(String codeLang, Long chapterRefId) ;

	ByteArrayInputStream chapterRefsToExcel(List<ChapterRefLang> chapterRefLangs);

	List<ChapterRefLang> excelToChapterRef(InputStream is);

	ByteArrayInputStream load(String codeLang);

	//Page<ChapterRefBean> getAll(final int page, final int size);

	void addChapter(ChapterRefLang chapterRefLang);

	ChapterRefLang findChapter(Long id, String lang);

	void addInternationalisation(EntityRefLang entityRefLang, String lang);

	Page<ChapterRefLang> filterByCodeOrLabel(String value, int page,int size, String codeLang);

	Page<ChapterRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

    ChapterRefBean findById(Long id);

	ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);

}
