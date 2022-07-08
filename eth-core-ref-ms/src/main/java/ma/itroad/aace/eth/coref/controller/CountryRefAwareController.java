package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resources;

//@BasePathAwareController
@RepositoryRestController
public class CountryRefAwareController {

    private final CountryRefRepository countryRefRepository;

    public CountryRefAwareController(CountryRefRepository countryRefRepository) {
        this.countryRefRepository = countryRefRepository;
    }

    /*
    @RequestMapping(path = "countryRefs", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public Resources getAllcountryRefsWithDynamicInternationalization() {
        // Do your filtering and end up with a HATEOAS resources to return
        Resources resources = null;
        // countryRefRepository.findAll();

        return resources;
        // return   countryRefRepository.findAll();
    }
    */

}
