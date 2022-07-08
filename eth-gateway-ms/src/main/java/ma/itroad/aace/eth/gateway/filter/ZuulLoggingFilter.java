package ma.itroad.aace.eth.gateway.filter;


import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class ZuulLoggingFilter extends ZuulFilter {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext requestCtx = RequestContext.getCurrentContext();
        HttpServletRequest request = requestCtx.getRequest();
        String bearer = request.getHeader("Authorization");
        requestCtx.addZuulRequestHeader("Authorization", bearer);
        requestCtx.addZuulRequestHeader("from","api");
        logger.info("request -> {} request uri -> {}", request, request.getRequestURI());
        return null;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

}
