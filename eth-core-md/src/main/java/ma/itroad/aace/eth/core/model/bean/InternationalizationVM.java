package ma.itroad.aace.eth.core.model.bean;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class InternationalizationVM implements Serializable {

    String language;
    String label;
    String description;
}
