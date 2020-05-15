package com.kayrasolutions.aem.foundation.injectors.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.apache.sling.models.spi.injectorspecific.AbstractInjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessorFactory2;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kayrasolutions.aem.foundation.api.resource.ComponentResource;
import com.kayrasolutions.aem.foundation.injectors.annotations.ReferenceInject;
import com.kayrasolutions.aem.foundation.injectors.utils.FoundationInjectorUtils;

@Component(service = Injector.class)
public final class ReferenceInjector extends AbstractComponentResourceInjector
		implements InjectAnnotationProcessorFactory2 {

	private static final Logger LOG = LoggerFactory.getLogger(ReferenceInjector.class);

	@Override
	public Object getValue(final ComponentResource componentResource, final String name, final Type declaredType,
			final AnnotatedElement element, final DisposalCallbackRegistry callbackRegistry) {
		final ReferenceInject annotation = element.getAnnotation(ReferenceInject.class);
		Object value = null;

		if (annotation != null) {
			final List<String> references = annotation.inherit()
					? componentResource.getAsListInherited(name, String.class)
					: componentResource.getAsList(name, String.class);

			final List<Object> referencedObjects = getReferencedObjects(componentResource, declaredType, references);

			if (!referencedObjects.isEmpty()) {
				if (FoundationInjectorUtils.isDeclaredTypeCollection(declaredType)) {
					value = referencedObjects;
				} else {
					value = referencedObjects.get(0);
				}
			}
		}

		return value;
	}

	@Override
	public InjectAnnotationProcessor2 createAnnotationProcessor(final Object adaptable,
			final AnnotatedElement element) {
		final ReferenceInject annotation = element.getAnnotation(ReferenceInject.class);

		return annotation != null ? new ReferenceAnnotationProcessor(annotation) : null;
	}

	@Override
	public String getName() {
		return ReferenceInject.NAME;
	}

	private List<Object> getReferencedObjects(final ComponentResource componentResource, final Type declaredType,
			final List<String> references) {
		final Class<?> declaredClass = FoundationInjectorUtils.getDeclaredClassForDeclaredType(declaredType);

		final List<Object> referencedObjects = new ArrayList<>();

		final ResourceResolver resourceResolver = componentResource.getResource().getResourceResolver();

		for (final String reference : references) {
			final Resource referencedResource = reference.startsWith("/") ? resourceResolver.getResource(reference)
					: resourceResolver.getResource(componentResource.getResource(), reference);

			if (referencedResource == null) {
				LOG.warn("reference {} did not resolve to an accessible resource", reference);
			} else {
				if (declaredClass != Resource.class) {
					final Object adaptedObject = referencedResource.adaptTo(declaredClass);

					if (adaptedObject == null) {
						LOG.warn("resource at {} could not be adapted to an instance of {}",
								referencedResource.getPath(), declaredClass.getName());
					} else {
						referencedObjects.add(adaptedObject);
					}
				} else {
					referencedObjects.add(referencedResource);
				}
			}
		}

		return referencedObjects;
	}

	static class ReferenceAnnotationProcessor extends AbstractInjectAnnotationProcessor2 {

		private final ReferenceInject annotation;

		ReferenceAnnotationProcessor(final ReferenceInject annotation) {
			this.annotation = annotation;
		}

		@Override
		public InjectionStrategy getInjectionStrategy() {
			return annotation.injectionStrategy();
		}
	}
}
