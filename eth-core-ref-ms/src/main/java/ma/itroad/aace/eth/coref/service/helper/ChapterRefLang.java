/**
 * 
 */
package ma.itroad.aace.eth.coref.service.helper;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.model.bean.SectionRefBean;
import ma.itroad.aace.eth.coref.model.entity.SectionRef;
import ma.itroad.aace.eth.coref.service.validator.SectionRefConstraints;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;

/**
 *
 */
@Setter
@Getter
public class ChapterRefLang extends CodeEntityBean {

	@SectionRefConstraints
	private String sectionRef;


	/**
	 * Translation
	 */
	@NotBlank
	private String label;
	@NotBlank
	private String description;
	@NotBlank
	@langConstraints
	private String lang;
}
