package com.kayrasolutions.aem.foundation.injectors.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.scripting.WCMBindingsConstants;
import com.google.common.collect.ImmutableList;
import com.kayrasolutions.aem.foundation.api.page.FoundationPage;
import com.kayrasolutions.aem.foundation.api.page.FoundationPageManager;
import com.kayrasolutions.aem.foundation.api.resource.ComponentResource;
import com.kayrasolutions.aem.foundation.injectors.utils.FoundationInjectorUtils;

/**
 * Injector for objects derived from the current component context.
 */
@Component(service = Injector.class)
@ServiceRanking(Integer.MIN_VALUE)
public final class ComponentInjector implements Injector {

	private static final Logger LOG = LoggerFactory.getLogger(ComponentInjector.class);

	private static final List<Class> REQUEST_INJECTABLES = ImmutableList.of(Page.class, FoundationPage.class,
			WCMMode.class);

	private static final List<Class> RESOURCE_INJECTABLES = ImmutableList.of(ResourceResolver.class, ValueMap.class,
			ComponentResource.class, Page.class, FoundationPage.class);

	@Override
	public String getName() {
		return "component";
	}

	@Override
	public Object getValue(final Object adaptable, final String name, final Type type, final AnnotatedElement element,
			final DisposalCallbackRegistry registry) {
		Object value = null;

		if (type instanceof Class) {
			final Class clazz = (Class) type;

			if (adaptable instanceof SlingHttpServletRequest) {
				// get request adaptable
				if (REQUEST_INJECTABLES.contains(clazz)) {
					value = getValueForRequest(clazz, adaptable);
				} else if (RESOURCE_INJECTABLES.contains(clazz)) {
					value = getValueForResource(clazz, adaptable);
				} else {
					LOG.debug("class : {} is not supported by this injector for request", clazz.getName());
				}
			} else if (adaptable instanceof Resource) {
				// get resource adaptable
				if (RESOURCE_INJECTABLES.contains(clazz)) {
					value = getValueForResource(clazz, adaptable);
				} else {
					LOG.debug("class : {} is not supported by this injector for resource", clazz.getName());
				}
			}
		}

		return value;
	}

	private Object getValueForRequest(final Class clazz, final Object adaptable) {
		final SlingHttpServletRequest request = FoundationInjectorUtils.getRequest(adaptable);

		Object value = null;

		if (clazz == WCMMode.class) {
			value = WCMMode.fromRequest(request);
		} else if (clazz == FoundationPage.class || clazz == Page.class) {
			value = Optional.ofNullable((SlingBindings) request.getAttribute(SlingBindings.class.getName()))
					.map(bindings -> (Page) bindings.get(WCMBindingsConstants.NAME_CURRENT_PAGE))
					.map(currentPage -> request.getResourceResolver().adaptTo(FoundationPageManager.class)
							.getPage(currentPage))
					.orElse(null);
		}

		LOG.debug("injecting class : {} with instance : {}", clazz.getName(), value);

		return value;
	}

	private Object getValueForResource(final Class clazz, final Object adaptable) {
		final Resource resource = FoundationInjectorUtils.getResource(adaptable);

		Object value = null;

		if (clazz == ResourceResolver.class) {
			value = resource.getResourceResolver();
		} else if (clazz == ValueMap.class) {
			value = resource.getValueMap();
		} else if (clazz == ComponentResource.class) {
			value = resource.adaptTo(ComponentResource.class);
		} else if (clazz == FoundationPage.class || clazz == Page.class) {
			value = resource.getResourceResolver().adaptTo(FoundationPageManager.class).getContainingPage(resource);
		}

		LOG.debug("injecting class : {} with instance : {}", clazz.getName(), value);

		return value;
	}
}
