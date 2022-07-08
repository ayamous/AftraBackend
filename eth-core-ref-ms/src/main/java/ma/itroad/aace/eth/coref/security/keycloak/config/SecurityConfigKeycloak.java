
package ma.itroad.aace.eth.coref.security.keycloak.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@KeycloakConfiguration
public class SecurityConfigKeycloak extends KeycloakWebSecurityConfigurerAdapter {
    private   String [] publicPathsRequest ={
    "/api/coref/agreements/productInformationFinderfilter" ,
    "/api/coref/regulationrefs/productInformationFinderfilter" ,
    "/api/coref/nationalProcedureRefs/productInformationFinderfilter" ,
    "/api/coref/sanitaryPhytosanitaryMeasuresRefs/productInformationFinderfilter",
    "/api/coref/techBarrierRefs/productInformationFinderfilter",
    "/api/coref/taxations/productInformationFinderfilter",
    "/api/coref/agreements/tradeAgreementFilter",
    "/api/coref/sanitaryPhytosanitaryMeasuresRefs/filter" ,
    "/api/coref/regulationrefs/filter" ,
    "/api/coref/nationalProcedureRefs/filter" ,
    "/api/coref/techBarrierRefs/filter"};
///**
//     * Secure appropriate endpoints
//     *//*

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.cors().and().csrf().disable()
                .exceptionHandling()
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().authorizeRequests().antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/test/**").permitAll()
                .anyRequest().authenticated();
    }

//
///**
//     * Provide a session authentication strategy bean which should be of type
//     * RegisterSessionAuthenticationStrategy for public or confidential applications
//     * and NullAuthenticatedSessionStrategy for bearer-only applications.
//     *//*

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }


///**
//     * Registers the KeycloakAuthenticationProvider with the authentication manager.
//     *//*

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider =
                keycloakAuthenticationProvider();
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }



//    */
///**
//     * Use properties in application.properties instead of keycloak.json
//     *//*

    @Bean
    @Primary
    public KeycloakConfigResolver keycloakConfigResolver(KeycloakSpringBootProperties properties) {
        return new CustomKeycloakSpringBootConfigResolver(properties);
    }


}
