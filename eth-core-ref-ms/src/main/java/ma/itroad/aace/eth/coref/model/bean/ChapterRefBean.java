package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;

import java.io.Serializable;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ChapterRefBean extends CodeEntityBean implements Serializable {
	private static final long serialVersionUID = 3847205726858956918L;

	private SectionRefBean sectionRef;
	private String label;
	private String description;
	private String lang;

//    private SectionRefBean sectionRef;
}
