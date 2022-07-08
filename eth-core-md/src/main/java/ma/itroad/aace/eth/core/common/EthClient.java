package ma.itroad.aace.eth.core.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ma.itroad.aace.eth.core.common.api.messaging.MessagingAPI;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

@Getter
@Builder
// TODO move the handler to a separate file
// TODO CREATE A FUNCTIONAL EXCEPTION / ERROR
// TODO work not finished
public class EthClient {

    private final MessagingAPI messaging;

    public static <T> T build(Class<T> clazz, String url, RestTemplate restTemplate) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ApiRestHandler(url, clazz, restTemplate));
    }

    @AllArgsConstructor
    @Slf4j
    static class ApiRestHandler implements InvocationHandler {
        private final String apiUrl;
        private final Class<?> clazz;
        private final RestTemplate restTemplate;

        @Async
        public Object invoke(Object proxy, Method method, Object[] args)
                throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, Exception {
            if (ArrayUtils.isNotEmpty(method.getAnnotations()) && Arrays.stream(method.getAnnotations()).anyMatch(e ->
                    e.annotationType().getTypeName().toLowerCase().contains("mapping"))) {
                Annotation mappingAnnotation = Arrays.stream(method.getAnnotations()).filter(e -> e.annotationType().getTypeName().toLowerCase().contains("mapping")).findFirst().orElseThrow(() -> new Exception("No request mapping found"));
                RequestMethod requestMethod = getRequestMethod(mappingAnnotation);
                StringBuilder sb = new StringBuilder(apiUrl);
                String requestEndpoint = getRequestMappingValue(mappingAnnotation);
                if (requestEndpoint == null) {
                    return null;
                }
                log.info("Calling {}:{}", requestMethod, apiUrl);
                Annotation requestMapping = clazz.getAnnotation(RequestMapping.class);
                if (requestMapping == null) {
                    log.error("Api class doesn't contain any request mapping");
                    return null;
                }
                Object requestMappingValue = requestMapping.getClass().getMethod("value").invoke(requestMapping);
                if (requestMappingValue instanceof String[]) {
                    String[] values = (String[]) requestMappingValue;
                    String endpoint = values[0];
                    if (!apiUrl.endsWith("/")) {
                        sb.append("/");
                    }
                    if (endpoint.startsWith("/")) {
                        endpoint = endpoint.substring(1);
                    }
                    sb.append(endpoint);
                    if (!endpoint.endsWith("/")) {
                        sb.append("/");
                    }
                    if (requestEndpoint.startsWith("/")) {
                        requestEndpoint = requestEndpoint.substring(1);
                    }
                    sb.append(requestEndpoint);
                    log.info("Full url : {}", sb.toString());
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<?> entity;
                    if (ArrayUtils.isNotEmpty(args)) {
                        entity = new HttpEntity<>(args[0], headers);
                    } else {
                        entity = new HttpEntity<>(headers);
                    }
                    return restTemplate.exchange(sb.toString(), HttpMethod.valueOf(requestMethod.name()), entity, Object.class, args);
                }
            }
            return null;
        }

        @SneakyThrows
        private RequestMethod getRequestMethod(Annotation annotation) {
            if (!annotation.annotationType().getTypeName().toLowerCase().contains("mapping"))
                return null;
            if (!(annotation instanceof RequestMapping)) {
                annotation = AnnotationUtils.findAnnotation(annotation.getClass(), RequestMapping.class);
            }
            return ((RequestMethod[]) AnnotationUtils.getValue(annotation, "method"))[0];
        }

        @SneakyThrows
        private String getRequestMappingValue(Annotation annotation) {
            if (!annotation.annotationType().getTypeName().toLowerCase().contains("mapping"))
                return null;
            return ((String[]) annotation.getClass().getMethod("value").invoke(annotation))[0];
        }
    }
}
