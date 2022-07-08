package ma.itroad.aace.eth.coref.config;

import ma.itroad.aace.eth.coref.model.entity.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

@Configuration
public class ExposeEntityIdRestMvcConfiguration extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {

        config.exposeIdsFor(Agreement.class);
        config.exposeIdsFor(AuthorizationSetup.class);
        config.exposeIdsFor(CategoryRef.class);
        config.exposeIdsFor(ChapterRef.class);
        config.exposeIdsFor(CityRef.class);
        config.exposeIdsFor(CountryGroupRef.class);
        config.exposeIdsFor(CountryRef.class);
        config.exposeIdsFor(CustomsOfficeRef.class);
        config.exposeIdsFor(CustomsRegimRef.class);
        config.exposeIdsFor(DeclarationTypeRef.class);
        config.exposeIdsFor(EconomicOperator.class);
        config.exposeIdsFor(ESafeDocument.class);
        config.exposeIdsFor(ESafeTags.class);
        config.exposeIdsFor(ExtendedProcedureRef.class);
        config.exposeIdsFor(Lang.class);
        config.exposeIdsFor(NationalProcedureRef.class);
        config.exposeIdsFor(Organization.class);
        config.exposeIdsFor(PersonalContact.class);
        config.exposeIdsFor(PortRef.class);
        config.exposeIdsFor(RefCurrency.class);
        config.exposeIdsFor(RefPackaging.class);
        config.exposeIdsFor(RefTransportationType.class);
        config.exposeIdsFor(RegulationRef.class);
        config.exposeIdsFor(SanitaryPhytosanitaryMeasuresRef.class);
        config.exposeIdsFor(SectionRef.class);
        config.exposeIdsFor(TableRefName.class);
        config.exposeIdsFor(TarifBookRef.class);
        config.exposeIdsFor(Taxation.class);
        config.exposeIdsFor(TaxRef.class);
        config.exposeIdsFor(TechBarrierRef.class);
        config.exposeIdsFor(UnitRef.class);
        config.exposeIdsFor(UserAccount.class);
        config.exposeIdsFor(VersionRef.class);
        config.exposeIdsFor(VersionTariffBookRef.class);
        config.exposeIdsFor(Profil.class);
    }

}