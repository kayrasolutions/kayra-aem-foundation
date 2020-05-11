package com.kayrasolutions.aem.foundation.core.resource.predicates;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Predicate;

import com.kayrasolutions.aem.foundation.api.resource.ComponentResource;

public final class ComponentResourcePropertyExistsPredicate implements Predicate<ComponentResource> {

	private final String propertyName;

	public ComponentResourcePropertyExistsPredicate(final String propertyName) {
		this.propertyName = checkNotNull(propertyName);
	}

	@Override
	public boolean test(final ComponentResource componentResource) {
		return componentResource.getResource().getValueMap().containsKey(propertyName);
	}
}
