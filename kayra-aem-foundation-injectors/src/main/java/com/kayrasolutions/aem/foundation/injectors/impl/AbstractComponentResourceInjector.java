package com.kayrasolutions.aem.foundation.injectors.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Optional;

import javax.inject.Named;

import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;

import com.kayrasolutions.aem.foundation.api.resource.ComponentResource;
import com.kayrasolutions.aem.foundation.injectors.utils.FoundationInjectorUtils;

public abstract class AbstractComponentResourceInjector implements Injector {

	@Override
	public Object getValue(final Object adaptable, final String name, final Type declaredType,
			final AnnotatedElement element, final DisposalCallbackRegistry callbackRegistry) {
		return Optional.ofNullable(FoundationInjectorUtils.getResource(adaptable))
				.map(resource -> resource.adaptTo(ComponentResource.class)).map(componentResource -> {
					final Named namedAnnotation = element.getAnnotation(Named.class);

					return getValue(componentResource,
							Optional.ofNullable(namedAnnotation).map(Named::value).orElse(name), declaredType, element,
							callbackRegistry);
				}).orElse(null);
	}

	protected abstract Object getValue(final ComponentResource componentResource, final String name,
			final Type declaredType, final AnnotatedElement element, final DisposalCallbackRegistry callbackRegistry);
}
