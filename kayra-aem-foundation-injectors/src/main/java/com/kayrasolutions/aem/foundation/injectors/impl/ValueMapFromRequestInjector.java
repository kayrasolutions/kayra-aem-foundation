package com.kayrasolutions.aem.foundation.injectors.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.service.component.annotations.Component;

import com.kayrasolutions.aem.foundation.injectors.utils.FoundationInjectorUtils;

@Component(service = Injector.class)
public final class ValueMapFromRequestInjector implements Injector {

	@Override
	public String getName() {
		return "valuemap-request";
	}

	@Override
	public Object getValue(final Object adaptable, final String name, final Type type, final AnnotatedElement element,
			final DisposalCallbackRegistry registry) {
		Object value = null;

		final SlingHttpServletRequest request = FoundationInjectorUtils.getRequest(adaptable);

		if (type instanceof Class<?> && request != null && request.getResource() != null) {
			value = request.getResource().getValueMap().get(name, (Class<?>) type);
		}

		return value;
	}
}