/*
package ma.itroad.aace.eth.core.security.provider;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import ma.itroad.aace.eth.core.security.bean.UserDetailsAuthBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


*/
/**
 * 
 * @author A. Jbili
 * 
 * TODO : Logging and Exception Management to be reviewed
 *//*


@Component
public class JwtTokenProvider {

	//private final Logger log = LoggerFactory.getLogger(this.getClass());

	public static final String AUTHORITIES_KEY = "auth";
	public static final String AUTHORIZATION = "Authorization";

	private String secretKey = "secret-key";
	private long validityInMilliseconds = 3600000; // 1h

	@PostConstruct
	protected void init() {
		secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
	}

	public String createToken(String username, List<String> roles) {

		Date now = new Date();
		Date validity = new Date(now.getTime() + validityInMilliseconds);

		String token = Jwts.builder().setSubject(username).claim(AUTHORITIES_KEY, roles).setIssuedAt(now)
				.setExpiration(validity).signWith(SignatureAlgorithm.HS512, secretKey).compact();

		return token;
	}

	public String createToken(Authentication authentication) {
		List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		return createToken(authentication.getName(), authorities);
	}

	public String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7, bearerToken.length());
		}
		return null;
	}

	public void validateToken(String authToken) throws JwtException, IllegalArgumentException {
		Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
	}

	// user details with out database hit
	public UserDetailsAuthBean getUserDetails(String token) {
		String userName = getUsername(token);
		List<String> roleList = getRoleList(token);
		UserDetailsAuthBean userDetails = new UserDetailsAuthBean(userName, roleList);
		return userDetails;
	}

	@SuppressWarnings("unchecked")
	public List<String> getRoleList(String token) {
		return (List<String>) Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody()
				.get(AUTHORITIES_KEY);
	}

	public String getUsername(String token) {
		return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
	}

	public Authentication getAuthentication(String token) {
		// using data base: uncomment when you want to fetch data from data base
		// UserDetails userDetails =
		// userDetailsService.loadUserByUsername(getUsername(token));
		// from token take user value. comment below line for changing it taking from
		// data base
		UserDetailsAuthBean userDetails = getUserDetails(token);
		return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
	}

}
*/
