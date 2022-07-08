package ma.itroad.aace.eth.coref.service.converter;

import ma.itroad.aace.eth.coref.model.enums.CountryType;
import org.springframework.core.convert.converter.Converter;

public class CountryTypeToEnumConverter implements Converter<String, CountryType> {

    @Override
    public CountryType convert(String from) {
        return CountryType.valueOf(from.toUpperCase());
    }
}