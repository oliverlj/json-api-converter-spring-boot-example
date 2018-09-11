package com.github.oliverlj.jsonapi.configuration;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.jasminb.jsonapi.ResourceConverter;

/**
 * Configuration for HTTP Message converters that use json api.
 * 
 * @author Olivier LE JACQUES (o.le.jacques@gmail.com)
 *
 */
@Configuration
@ConditionalOnClass(ResourceConverter.class)
@Import({ JsonApiResponseEntityExceptionHandler.class, FilterParametersConfiguration.class })
public class JsonApiHttpMessageConvertersConfiguration {

    @Autowired
    private ListableBeanFactory listableBeanFactory;

    @Bean
    public HttpMessageConverters jsonApiHttpMessageConverter() {
        Map<String, Object> annotedElements = listableBeanFactory.getBeansWithAnnotation(EnableJsonApiTypes.class);
        if (annotedElements.size() != 1) {
            throw new IllegalStateException("One and only one EnableJsonApiTypes must be present on the classpath");
        }
        String bean = requireNonNull(annotedElements.keySet().iterator().next());
        String packageName = requireNonNull(annotedElements.values().iterator().next().getClass().getPackageName());
        return new HttpMessageConverters(new JsonApiHttpMessageConverter(packageName, findEnableJsonApiTypesAnnotation(bean)));
    }

    private EnableJsonApiTypes findEnableJsonApiTypesAnnotation(String bean) {
        return requireNonNull(listableBeanFactory.findAnnotationOnBean(bean, EnableJsonApiTypes.class));
    }

}
