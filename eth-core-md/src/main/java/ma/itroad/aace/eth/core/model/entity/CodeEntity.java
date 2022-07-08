package ma.itroad.aace.eth.core.model.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;

import java.util.List;

@Getter
@Setter
@MappedSuperclass
public abstract class CodeEntity extends AuditEntity {
    private static final long serialVersionUID = -1775849166653256400L;


    @Pattern(regexp = "^[ a-zA-Z0-9]*$")
    @Column(name = "code", length = 128, nullable = false, unique = true)
    protected String code;

}

