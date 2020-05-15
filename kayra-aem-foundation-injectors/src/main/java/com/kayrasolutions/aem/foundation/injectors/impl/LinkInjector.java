package com.kayrasolutions.aem.foundation.injectors.impl;

import java.lang.reflect.AnnotatedElement;

import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.spi.AcceptsNullName;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.apache.sling.models.spi.injectorspecific.AbstractInjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessorFactory2;
import org.osgi.service.component.annotations.Component;

import com.kayrasolutions.aem.foundation.api.link.Link;
import com.kayrasolutions.aem.foundation.api.resource.ComponentResource;
import com.kayrasolutions.aem.foundation.core.link.builders.factory.LinkBuilderFactory;
import com.kayrasolutions.aem.foundation.injectors.annotations.LinkInject;

@Component(service = Injector.class)
public final class LinkInjector extends AbstractTypedComponentResourceInjector<Link>
		implements Injector, InjectAnnotationProcessorFactory2, AcceptsNullName {

	@Override
	public String getName() {
		return LinkInject.NAME;
	}

	@Override
	public Object getComponentResourceValue(final ComponentResource componentResource, final String name,
			final Class<Link> declaredType, final AnnotatedElement element,
			final DisposalCallbackRegistry callbackRegistry) {
		final LinkInject annotation = element.getAnnotation(LinkInject.class);

		final Link link;

		if (annotation != null) {
			final String title = getTitle(componentResource, annotation);

			link = (annotation.inherit() ? componentResource.getInherited(name, String.class)
					: componentResource.get(name, String.class))
							.map(path -> LinkBuilderFactory.forPath(path).setTitle(title).build()).orElse(null);
		} else {
			link = componentResource.get(name, String.class).map(path -> LinkBuilderFactory.forPath(path).build())
					.orElse(null);
		}

		return link;
	}

	@Override
	public InjectAnnotationProcessor2 createAnnotationProcessor(final Object adaptable,
			final AnnotatedElement element) {
		// check if the element has the expected annotation
		final LinkInject annotation = element.getAnnotation(LinkInject.class);

		return annotation != null ? new LinkAnnotationProcessor(annotation) : null;
	}

	private String getTitle(final ComponentResource componentResource, final LinkInject annotation) {
		return annotation.inherit() ? componentResource.getInherited(annotation.titleProperty(), "")
				: componentResource.get(annotation.titleProperty(), "");
	}

	static class LinkAnnotationProcessor extends AbstractInjectAnnotationProcessor2 {

		private final LinkInject annotation;

		LinkAnnotationProcessor(final LinkInject annotation) {
			this.annotation = annotation;
		}

		@Override
		public InjectionStrategy getInjectionStrategy() {
			return annotation.injectionStrategy();
		}
	}
}
