/*
package ma.itroad.aace.eth.core.security.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ma.itroad.aace.eth.core.helper.DateUtilsHelper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailsAuthBean implements UserDetails {
    private static final long serialVersionUID = -5116983305509287452L;

    private String username;
    private String password;
    private boolean isLocked = false;
    private boolean isExpired = false;
    private boolean isEnabled = true;
    private Boolean temporalPwd;
    private Date pwdExpirationDate;
    private List<BaseAuthority> authorities;

    @JsonCreator
    public UserDetailsAuthBean() {
    }

    public UserDetailsAuthBean(String username, List<String> authorities) {

        this.password = null;
        this.username = username;
        isLocked = false;
        isExpired = false;
        isEnabled = true;
        temporalPwd = true;
        pwdExpirationDate = null;

        List<BaseAuthority> userAuthorities = new ArrayList<BaseAuthority>();
        for (String role : authorities) {
            userAuthorities.add(new BaseAuthority(role));
        }
        this.authorities = userAuthorities;
    }

    public UserDetailsAuthBean(String username, String password, boolean isLocked,
                               boolean isExpired, boolean isEnabled, List<BaseAuthority> authorities, Date expirationDate) {

        this.username = username;
        this.isLocked = isLocked;
        this.isExpired = isExpired;
        this.isEnabled = isEnabled;

        temporalPwd = true;
        this.password = password;
        pwdExpirationDate = expirationDate;
        this.authorities = authorities;
    }

    @Override
    @JsonProperty("authorities")
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        isExpired = temporalPwd &&
                pwdExpirationDate != null &&
                DateUtilsHelper.isDateNonExpired(pwdExpirationDate);
        return !isExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !isExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

}
*/
