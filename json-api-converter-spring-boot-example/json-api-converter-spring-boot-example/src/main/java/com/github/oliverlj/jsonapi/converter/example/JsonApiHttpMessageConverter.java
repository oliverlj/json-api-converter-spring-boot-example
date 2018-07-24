package com.github.oliverlj.jsonapi.converter.example;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.github.jasminb.jsonapi.DeserializationFeature;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

public class JsonApiHttpMessageConverter extends AbstractJackson2HttpMessageConverter {

	private final ResourceConverter resourceConverter;

	public JsonApiHttpMessageConverter(ObjectMapper objectMapper, List<String> basePackages) {
		super(objectMapper, new MediaType("application", "vnd.api+json", Charset.defaultCharset()));

		Objects.requireNonNull(objectMapper, "An ObjectMapper must be provided.");
		Objects.requireNonNull(basePackages,
				"Base packages to look for jsonapi-converter @Type annotations must be provided.");

		resourceConverter = new ResourceConverter(objectMapper);

		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(com.github.jasminb.jsonapi.annotations.Type.class));

		List<BeanDefinition> foundBeans = basePackages.stream().map(pkg -> provider.findCandidateComponents(pkg))
				.flatMap(beanDefinitions -> beanDefinitions.stream()).collect(Collectors.toList());

		foundBeans.forEach(bean -> {
			try {
				Class modelClazz = Class.forName(bean.getBeanClassName());
				resourceConverter.registerType(modelClazz);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});

		// Used for POST (create) as we don't have ID yet!
		resourceConverter.disableDeserializationOption(DeserializationFeature.REQUIRE_RESOURCE_ID);
	}

	@Override
	public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
		JavaType javaType = getJavaType(type, contextClass);
		return javaType.getRawClass().equals(JSONAPIDocument.class) && javaType.hasGenericTypes()
				&& super.canRead(type, contextClass, mediaType);
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return clazz.equals(JSONAPIDocument.class) && super.canWrite(clazz, mediaType);
	}

	@Override
	protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		JSONAPIDocument jsonapiDocument = (JSONAPIDocument) object;

		try {
			if (jsonapiDocument.get() != null && Iterable.class.isAssignableFrom(jsonapiDocument.get().getClass())) {
				outputMessage.getBody().write(resourceConverter.writeDocumentCollection(jsonapiDocument));
			} else {
				outputMessage.getBody().write(resourceConverter.writeDocument(jsonapiDocument));
			}
		} catch (DocumentSerializationException e) {
			throw new HttpMessageNotWritableException(e.getMessage(), e);
		}
	}

	@Override
	public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		JavaType javaType = getJavaType(type, contextClass);
		TypeBindings typeBindings = javaType.getBindings();

		// JSONAPIDocument has only 1 generic type e.g. JSONAPIDocument<User> or
		// JSONAPIDocument<List<User>>
		JavaType containerType = typeBindings.getBoundType(0);
		Class<?> containerTypeClass = containerType.getRawClass();

		if (Iterable.class.isAssignableFrom(containerTypeClass)) {
			TypeBindings iterableTypeBindings = containerType.getBindings();
			JavaType itemType = iterableTypeBindings.getBoundType(0);
			return resourceConverter.readDocumentCollection(inputMessage.getBody(), itemType.getRawClass());
		} else {
			return resourceConverter.readDocument(inputMessage.getBody(), containerTypeClass);
		}
	}

}
