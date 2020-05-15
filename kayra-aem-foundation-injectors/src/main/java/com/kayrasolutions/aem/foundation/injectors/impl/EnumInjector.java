package com.kayrasolutions.aem.foundation.injectors.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.service.component.annotations.Component;

import com.kayrasolutions.aem.foundation.api.resource.ComponentResource;

@Component(service = Injector.class)
public final class EnumInjector extends AbstractComponentResourceInjector {

	@Override
	public String getName() {
		return "enum";
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object getValue(final ComponentResource componentResource, final String name, final Type declaredType,
			final AnnotatedElement element, final DisposalCallbackRegistry callbackRegistry) {
		Object value = null;

		if (declaredType instanceof Class && ((Class) declaredType).isEnum()) {
			value = componentResource.get(name, String.class)
					.map(enumString -> Enum.valueOf((Class) declaredType, enumString)).orElse(null);
		}

		return value;
	}
}