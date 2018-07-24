package com.github.oliverlj.jsonapi.configuration;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.DeserializationFeature;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link org.springframework.http.converter.HttpMessageConverter} that can read and write JSON using <a href="http://jsonapi.org/">json
 * api</a>.
 * 
 * <p>
 * This converter can be used to bind to {@link com.github.jasminb.jsonapi.annotations.Type} beans, {@link JSONAPIDocument} beans, or untyped {@link Iterable}
 * instances.
 *
 * <p>
 * By default, this converter supports {@code application/vnd.api+json}.
 *
 * <p>
 * The default constructor uses the default configuration provided by {@link Jackson2ObjectMapperBuilder}.
 * 
 * @author Olivier LE JACQUES (o.le.jacques@gmail.com)
 *
 */
@Slf4j
public class JsonApiHttpMessageConverter extends AbstractJackson2HttpMessageConverter {

    @NonNull
    public static final String APPLICATION_JSON_API_VALUE = "application/vnd.api+json";

    public static final MediaType APPLICATION_JSON_API = MediaType.valueOf(APPLICATION_JSON_API_VALUE);

    private final ResourceConverter resourceConverter;

    public JsonApiHttpMessageConverter(String annotedElementPackage, EnableJsonApiTypes enableJsonApiTypes) {
        this(Jackson2ObjectMapperBuilder.json().build(), annotedElementPackage, enableJsonApiTypes);
    }

    private JsonApiHttpMessageConverter(ObjectMapper objectMapper, String annotedElementPackage, EnableJsonApiTypes enableJsonApiTypes) {
        super(objectMapper, APPLICATION_JSON_API);

        Objects.requireNonNull(objectMapper, "An ObjectMapper must be provided.");

        resourceConverter = new ResourceConverter(objectMapper);

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(com.github.jasminb.jsonapi.annotations.Type.class));

        Set<String> basePackageCandidates = getBasePackageCandidates(annotedElementPackage, enableJsonApiTypes);

        Set<BeanDefinition> jsonApiTypes = new HashSet<>();
        basePackageCandidates.stream().map(provider::findCandidateComponents).forEach(jsonApiTypes::addAll);

        jsonApiTypes.forEach(bean -> {
            try {
                Class<?> modelClazz = Class.forName(bean.getBeanClassName());
                resourceConverter.registerType(modelClazz);
                log.debug("Registered as json api type {}", modelClazz);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });

        // Used for POST (create) as we don't have ID yet!
        resourceConverter.disableDeserializationOption(DeserializationFeature.REQUIRE_RESOURCE_ID);
    }

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        JavaType javaType = getJavaType(type, contextClass);
        return super.canRead(type, contextClass, mediaType) && resourceConverter.isRegisteredType(javaType.getRawClass());
    }

    @Override
    public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
        return super.canWrite(clazz, mediaType)
                && (JSONAPIDocument.class.isAssignableFrom(clazz) || Iterable.class.isAssignableFrom(clazz) || resourceConverter.isRegisteredType(clazz));
    }

    @Override
    public Object read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        JavaType javaType = getJavaType(type, contextClass);

        if (Iterable.class.isAssignableFrom(javaType.getRawClass())) {
            JavaType itemType = javaType.getBindings().getBoundType(0);
            List<?> collection = resourceConverter.readDocumentCollection(inputMessage.getBody(), itemType.getRawClass()).get();
            return collection == null ? Collections.emptyList() : collection;
        } else {
            Object document = resourceConverter.readDocument(inputMessage.getBody(), javaType.getRawClass()).get();
            return document == null ? Optional.empty() : document;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void writeInternal(Object object, @Nullable Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

        try {
            JSONAPIDocument jsonApiDocument;
            if (JSONAPIDocument.class.isAssignableFrom(object.getClass())) {
                jsonApiDocument = Objects.requireNonNull(JSONAPIDocument.class.cast(object));
            } else {
                jsonApiDocument = new JSONAPIDocument<>(object);
            }

            Object document = jsonApiDocument.get();
            if (document != null && Iterable.class.isAssignableFrom(document.getClass())) {
                outputMessage.getBody().write(resourceConverter.writeDocumentCollection(jsonApiDocument));
            } else {
                outputMessage.getBody().write(resourceConverter.writeDocument(jsonApiDocument));
            }
        } catch (DocumentSerializationException ex) {
            String message = ex.getMessage() == null ? "" : ex.getMessage();
            throw new HttpMessageNotWritableException(message, ex);
        }
    }

    private Set<String> getBasePackageCandidates(String annotedElementPackage, EnableJsonApiTypes enableJsonApiTypes) {
        Set<String> basePackageCandidates = new HashSet<>();
        Arrays.asList(enableJsonApiTypes.basePackages()).forEach(basePackageCandidates::add);
        Arrays.asList(enableJsonApiTypes.value()).forEach(basePackageCandidates::add);
        Arrays.asList(enableJsonApiTypes.basePackageClasses()).stream().map(Class::getPackageName).forEach(basePackageCandidates::add);

        if (basePackageCandidates.isEmpty()) {
            basePackageCandidates.add(annotedElementPackage);
        }

        return basePackageCandidates;
    }

}
