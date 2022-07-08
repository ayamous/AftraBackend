/*package ma.itroad.aace.eth.core.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ma.itroad.aace.eth.core.security.provider.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import io.jsonwebtoken.JwtException;

*//**
 * 
 * @author A.Jbili
 * 
 * TODO : Logging and Exception Management to be reviewed
 *//*

public class JwtTokenFilter extends GenericFilterBean {
    private JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {
    	
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null) {
            try {
                if (StringUtils.hasText(token)) {
                	jwtTokenProvider.validateToken(token);
                    Authentication authentication = this.jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid JWT token");
                throw new JwtException("Invalid JWT token" + HttpStatus.UNAUTHORIZED);
            }
            
        }
        filterChain.doFilter(req, res);

    }
}*/
