package com.kayrasolutions.aem.foundation.injectors.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Optional;

import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.service.component.annotations.Component;

import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyManager;
import com.kayrasolutions.aem.foundation.injectors.utils.FoundationInjectorUtils;

/**
 * Injects the content policy for the current resource.
 */
@Component(service = Injector.class)
public final class ContentPolicyInjector implements Injector {

	@Override
	public String getName() {
		return "content-policy";
	}

	@Override
	public Object getValue(final Object adaptable, final String name, final Type type, final AnnotatedElement element,
			final DisposalCallbackRegistry registry) {
		Object value = null;

		if (type instanceof Class) {
			final Class clazz = (Class) type;

			if (clazz == ContentPolicy.class) {
				value = getContentPolicy(adaptable);
			}
		}

		return value;
	}

	private ContentPolicy getContentPolicy(final Object adaptable) {
		return Optional.ofNullable(FoundationInjectorUtils.getResource(adaptable)).map(resource -> {
			final ContentPolicyManager contentPolicyManager = resource.getResourceResolver()
					.adaptTo(ContentPolicyManager.class);

			return contentPolicyManager.getPolicy(resource);
		}).orElse(null);
	}
}
