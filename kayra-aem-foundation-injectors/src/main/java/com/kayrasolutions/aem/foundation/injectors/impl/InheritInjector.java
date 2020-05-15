package com.kayrasolutions.aem.foundation.injectors.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.apache.sling.models.spi.injectorspecific.AbstractInjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessorFactory2;
import org.osgi.service.component.annotations.Component;

import com.kayrasolutions.aem.foundation.api.resource.ComponentResource;
import com.kayrasolutions.aem.foundation.injectors.annotations.InheritInject;
import com.kayrasolutions.aem.foundation.injectors.utils.FoundationInjectorUtils;

@Component(service = Injector.class)
public final class InheritInjector extends AbstractComponentResourceInjector
		implements InjectAnnotationProcessorFactory2 {

	@Override
	public String getName() {
		return InheritInject.NAME;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object getValue(final ComponentResource componentResource, final String name, final Type declaredType,
			final AnnotatedElement element, final DisposalCallbackRegistry callbackRegistry) {
		Object value = null;

		if (element.getAnnotation(InheritInject.class) != null) {
			if (FoundationInjectorUtils.isParameterizedListType(declaredType)) {
				final Class<?> typeClass = FoundationInjectorUtils.getActualType((ParameterizedType) declaredType);

				value = componentResource.getComponentResourcesInherited(name).stream()
						.map(cr -> cr.getResource().adaptTo(typeClass)).collect(Collectors.toList());
			} else if (declaredType instanceof Class && ((Class) declaredType).isEnum()) {
				value = componentResource.getInherited(name, String.class)
						.map(enumString -> Enum.valueOf((Class) declaredType, enumString)).orElse(null);
			} else {
				value = componentResource.getInherited(name, (Class) declaredType).orElse(null);

				if (value == null) {
					value = componentResource.getComponentResourceInherited(name).map(ComponentResource::getResource)
							.map(resource -> resource.adaptTo((Class) declaredType)).orElse(null);
				}
			}
		}

		return value;
	}

	@Override
	public InjectAnnotationProcessor2 createAnnotationProcessor(final Object adaptable,
			final AnnotatedElement element) {
		final InheritInject annotation = element.getAnnotation(InheritInject.class);

		return annotation != null ? new InheritAnnotationProcessor(annotation) : null;
	}

	static class InheritAnnotationProcessor extends AbstractInjectAnnotationProcessor2 {

		private final InheritInject annotation;

		InheritAnnotationProcessor(final InheritInject annotation) {
			this.annotation = annotation;
		}

		@Override
		public InjectionStrategy getInjectionStrategy() {
			return annotation.injectionStrategy();
		}
	}
}
