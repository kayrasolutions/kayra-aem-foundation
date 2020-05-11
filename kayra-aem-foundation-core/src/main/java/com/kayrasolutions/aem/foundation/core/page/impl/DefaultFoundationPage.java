package com.kayrasolutions.aem.foundation.core.page.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.day.cq.commons.Filter;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.commons.DeepResourceIterator;
import com.google.common.base.Objects;
import com.kayrasolutions.aem.foundation.api.Accessible;
import com.kayrasolutions.aem.foundation.api.Inheritable;
import com.kayrasolutions.aem.foundation.api.link.Link;
import com.kayrasolutions.aem.foundation.api.link.builders.LinkBuilder;
import com.kayrasolutions.aem.foundation.api.page.FoundationPage;
import com.kayrasolutions.aem.foundation.api.page.FoundationPageManager;
import com.kayrasolutions.aem.foundation.api.page.enums.TitleType;
import com.kayrasolutions.aem.foundation.api.resource.ComponentResource;
import com.kayrasolutions.aem.foundation.core.link.builders.factory.LinkBuilderFactory;
import com.kayrasolutions.aem.foundation.core.resource.predicates.ComponentResourcePropertyExistsPredicate;
import com.kayrasolutions.aem.foundation.core.resource.predicates.ComponentResourcePropertyValuePredicate;

@Model(adaptables = Page.class, adapters = FoundationPage.class)
public final class DefaultFoundationPage implements FoundationPage {

	private static final Filter<Page> ALL_PAGES = page -> true;

	private static final Predicate<FoundationPage> DISPLAYABLE_ONLY = page -> page.getContentResource() != null
			&& !page.isHideInNav();

	@Self
	private Page page;

	private Optional<ComponentResource> componentResource;

	@Override
	public boolean equals(final Object other) {
		return new EqualsBuilder().append(getPath(), ((FoundationPage) other).getPath()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getPath()).hashCode();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("path", getPath()).add("title", getTitle()).toString();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <AdapterType> AdapterType adaptTo(final Class<AdapterType> type) {
		final AdapterType result;

		if (type == Page.class) {
			result = (AdapterType) page;
		} else if (type == ComponentResource.class) {
			result = (AdapterType) getComponentResource().orElse(null);
		} else {
			result = page.adaptTo(type);
		}

		return result;
	}

	@Override
	public ValueMap asMap() {
		return getComponentResource().map(Accessible::asMap).orElse(ValueMap.EMPTY);
	}

	@Override
	public Optional<FoundationPage> findAncestor(final Predicate<FoundationPage> predicate) {
		return findAncestor(predicate, false);
	}

	@Override
	public Optional<FoundationPage> findAncestor(final Predicate<FoundationPage> predicate,
			final boolean excludeCurrentResource) {
		FoundationPage page = excludeCurrentResource ? getParent() : this;
		FoundationPage ancestorPage = null;

		while (page != null) {
			if (predicate.test(page)) {
				ancestorPage = page;
				break;
			} else {
				page = page.getParent();
			}
		}

		return Optional.ofNullable(ancestorPage);
	}

	@Override
	public Optional<FoundationPage> findAncestorWithProperty(final String propertyName) {
		return findAncestorForPredicate(new ComponentResourcePropertyExistsPredicate(propertyName), false);
	}

	@Override
	public Optional<FoundationPage> findAncestorWithProperty(final String propertyName,
			final boolean excludeCurrentResource) {
		return findAncestorForPredicate(new ComponentResourcePropertyExistsPredicate(propertyName),
				excludeCurrentResource);
	}

	@Override
	public <V> Optional<FoundationPage> findAncestorWithPropertyValue(final String propertyName,
			final V propertyValue) {
		return findAncestorForPredicate(new ComponentResourcePropertyValuePredicate<>(propertyName, propertyValue),
				false);
	}

	@Override
	public <V> Optional<FoundationPage> findAncestorWithPropertyValue(final String propertyName, final V propertyValue,
			final boolean excludeCurrentResource) {
		return findAncestorForPredicate(new ComponentResourcePropertyValuePredicate<>(propertyName, propertyValue),
				excludeCurrentResource);
	}

	@Override
	public List<FoundationPage> findDescendants(final Predicate<FoundationPage> predicate) {
		final List<FoundationPage> pages = new ArrayList<>();

		final FoundationPageManager pageManager = getPageManager();

		final Iterator<Page> iterator = page.listChildren(ALL_PAGES, true);

		while (iterator.hasNext()) {
			final FoundationPage page = pageManager.getPage(iterator.next());

			if (predicate.test(page)) {
				pages.add(page);
			}
		}

		return pages;
	}

	@Override
	public <T> T get(final String propertyName, final T defaultValue) {
		return getComponentResource().map(componentResource -> componentResource.get(propertyName, defaultValue))
				.orElse(defaultValue);
	}

	@Override
	public <T> Optional<T> get(final String propertyName, final Class<T> type) {
		return getComponentResource().flatMap(componentResource -> componentResource.get(propertyName, type));
	}

	@Override
	public Optional<String> getAsHref(final String propertyName) {
		return getComponentResource().flatMap(componentResource -> componentResource.getAsHref(propertyName));
	}

	@Override
	public Optional<String> getAsHref(final String propertyName, final boolean strict) {
		return getComponentResource().flatMap(componentResource -> componentResource.getAsHref(propertyName, strict));
	}

	@Override
	public Optional<String> getAsHref(final String propertyName, final boolean strict, final boolean mapped) {
		return getComponentResource()
				.flatMap(componentResource -> componentResource.getAsHref(propertyName, strict, mapped));
	}

	@Override
	public Optional<String> getAsHrefInherited(final String propertyName) {
		return getComponentResource().flatMap(componentResource -> componentResource.getAsHrefInherited(propertyName));
	}

	@Override
	public Optional<String> getAsHrefInherited(final String propertyName, final boolean strict) {
		return getComponentResource()
				.flatMap(componentResource -> componentResource.getAsHrefInherited(propertyName, strict));
	}

	@Override
	public Optional<String> getAsHrefInherited(final String propertyName, final boolean strict, final boolean mapped) {
		return getComponentResource()
				.flatMap(componentResource -> componentResource.getAsHrefInherited(propertyName, strict, mapped));
	}

	@Override
	public Optional<Link> getAsLinkInherited(final String propertyName) {
		return getComponentResource().flatMap(componentResource -> componentResource.getAsLinkInherited(propertyName));
	}

	@Override
	public Optional<Link> getAsLinkInherited(final String propertyName, final boolean strict) {
		return getComponentResource()
				.flatMap(componentResource -> componentResource.getAsLinkInherited(propertyName, strict));
	}

	@Override
	public Optional<Link> getAsLinkInherited(final String propertyName, final boolean strict, final boolean mapped) {
		return getComponentResource()
				.flatMap(componentResource -> componentResource.getAsLinkInherited(propertyName, strict, mapped));
	}

	@Override
	public <T> List<T> getAsListInherited(final String propertyName, final Class<T> type) {
		return getComponentResource().map(componentResource -> componentResource.getAsListInherited(propertyName, type))
				.orElse(Collections.emptyList());
	}

	@Override
	public Optional<FoundationPage> getAsPageInherited(final String propertyName) {
		return getComponentResource().flatMap(componentResource -> componentResource.getAsPageInherited(propertyName));
	}

	@Override
	public List<FoundationPage> getAsPageListInherited(final String propertyName) {
		return getComponentResource().map(componentResource -> componentResource.getAsPageListInherited(propertyName))
				.orElse(Collections.emptyList());
	}

	@Override
	public Optional<Resource> getAsResourceInherited(final String propertyName) {
		return getComponentResource()
				.flatMap(componentResource -> componentResource.getAsResourceInherited(propertyName));
	}

	@Override
	public List<Resource> getAsResourceListInherited(final String propertyName) {
		return getComponentResource()
				.map(componentResource -> componentResource.getAsResourceListInherited(propertyName))
				.orElse(Collections.emptyList());
	}

	@Override
	public <AdapterType> Optional<AdapterType> getAsTypeInherited(final String propertyName,
			final Class<AdapterType> type) {
		return getComponentResource()
				.flatMap(componentResource -> componentResource.getAsTypeInherited(propertyName, type));
	}

	@Override
	public <AdapterType> List<AdapterType> getAsTypeListInherited(final String propertyName,
			final Class<AdapterType> type) {
		return getComponentResource()
				.map(componentResource -> componentResource.getAsTypeListInherited(propertyName, type))
				.orElse(Collections.emptyList());
	}

	@Override
	public String getHref() {
		return getHref(false);
	}

	@Override
	public String getHref(final boolean mapped) {
		return getLink(mapped).getHref();
	}

	@Override
	public Link getLink() {
		return getLink(false);
	}

	@Override
	public Link getLink(final boolean mapped) {
		return getLinkBuilder(mapped).build();
	}

	@Override
	public LinkBuilder getLinkBuilder() {
		return getLinkBuilder(false);
	}

	@Override
	public LinkBuilder getLinkBuilder(final boolean mapped) {
		return LinkBuilderFactory.forPage(this, mapped, TitleType.TITLE);
	}

	@Override
	public Optional<String> getImageReferenceInherited(final boolean isSelf) {
		return getComponentResource()
				.flatMap(componentResource -> componentResource.getImageReferenceInherited(isSelf));
	}

	@Override
	public Optional<String> getImageReferenceInherited() {
		return getComponentResource().flatMap(Inheritable::getImageReferenceInherited);
	}

	@Override
	public Optional<String> getImageReferenceInherited(final String name) {
		return getComponentResource().flatMap(componentResource -> componentResource.getImageReferenceInherited(name));
	}

	@Override
	public <T> T getInherited(final String propertyName, final T defaultValue) {
		return getComponentResource()
				.map(componentResource -> componentResource.getInherited(propertyName, defaultValue))
				.orElse(defaultValue);
	}

	@Override
	public <T> Optional<T> getInherited(final String propertyName, final Class<T> type) {
		return getComponentResource().flatMap(componentResource -> componentResource.getInherited(propertyName, type));
	}

	@Override
	public List<Tag> getTagsInherited(final String propertyName) {
		return getComponentResource().map(componentResource -> componentResource.getTagsInherited(propertyName))
				.orElse(Collections.emptyList());
	}

	@Override
	public Optional<ComponentResource> getComponentResourceInherited(final String relativePath) {
		return getComponentResource()
				.flatMap(componentResource -> componentResource.getComponentResourceInherited(relativePath));
	}

	@Override
	public List<ComponentResource> getComponentResourcesInherited() {
		return getComponentResource().map(Inheritable::getComponentResourcesInherited).orElse(Collections.emptyList());
	}

	@Override
	public List<ComponentResource> getComponentResourcesInherited(final Predicate<ComponentResource> predicate) {
		return getComponentResource()
				.map(componentResource -> componentResource.getComponentResourcesInherited(predicate))
				.orElse(Collections.emptyList());
	}

	@Override
	public List<ComponentResource> getComponentResourcesInherited(final String relativePath) {
		return getComponentResource()
				.map(componentResource -> componentResource.getComponentResourcesInherited(relativePath))
				.orElse(Collections.emptyList());
	}

	@Override
	public List<ComponentResource> getComponentResourcesInherited(final String relativePath,
			final Predicate<ComponentResource> predicate) {
		return getComponentResource()
				.map(componentResource -> componentResource.getComponentResourcesInherited(relativePath, predicate))
				.orElse(Collections.emptyList());
	}

	@Override
	public Optional<Link> getAsLink(final String propertyName) {
		return getComponentResource().flatMap(componentResource -> componentResource.getAsLink(propertyName));
	}

	@Override
	public Optional<Link> getAsLink(final String propertyName, final boolean strict) {
		return getComponentResource().flatMap(componentResource -> componentResource.getAsLink(propertyName, strict));
	}

	@Override
	public Optional<Link> getAsLink(final String propertyName, final boolean strict, final boolean mapped) {
		return getComponentResource()
				.flatMap(componentResource -> componentResource.getAsLink(propertyName, strict, mapped));
	}

	@Override
	public <T> List<T> getAsList(final String propertyName, final Class<T> type) {
		return getComponentResource().map(componentResource -> componentResource.getAsList(propertyName, type))
				.orElse(Collections.emptyList());
	}

	@Override
	public Optional<FoundationPage> getAsPage(final String propertyName) {
		return getComponentResource().flatMap(componentResource -> componentResource.getAsPage(propertyName));
	}

	@Override
	public List<FoundationPage> getAsPageList(final String propertyName) {
		return getComponentResource().map(componentResource -> componentResource.getAsPageList(propertyName))
				.orElse(Collections.emptyList());
	}

	@Override
	public Optional<Resource> getAsResource(final String propertyName) {
		return getComponentResource().flatMap(componentResource -> componentResource.getAsResource(propertyName));
	}

	@Override
	public List<Resource> getAsResourceList(final String propertyName) {
		return getComponentResource().map(componentResource -> componentResource.getAsResourceList(propertyName))
				.orElse(Collections.emptyList());
	}

	@Override
	public <AdapterType> Optional<AdapterType> getAsType(final String propertyName, final Class<AdapterType> type) {
		return getComponentResource().flatMap(componentResource -> componentResource.getAsType(propertyName, type));
	}

	@Override
	public <AdapterType> List<AdapterType> getAsTypeList(final String propertyName, final Class<AdapterType> type) {
		return getComponentResource().map(componentResource -> componentResource.getAsTypeList(propertyName, type))
				.orElse(Collections.emptyList());
	}

	@Override
	public Optional<String> getImageReference(final boolean isSelf) {
		return getComponentResource().flatMap(componentResource -> componentResource.getImageReference(isSelf));
	}

	@Override
	public Optional<String> getImageReference() {
		return getComponentResource().flatMap(Accessible::getImageReference);
	}

	@Override
	public Optional<String> getImageReference(final String name) {
		return getComponentResource().flatMap(componentResource -> componentResource.getImageReference(name));
	}

	@Override
	public Optional<String> getImageRendition(final String renditionName) {
		return getComponentResource().flatMap(componentResource -> componentResource.getImageRendition(renditionName));
	}

	@Override
	public Optional<String> getImageRendition(final String name, final String renditionName) {
		return getComponentResource()
				.flatMap(componentResource -> componentResource.getImageRendition(name, renditionName));
	}

	@Override
	public List<Tag> getTags(final String propertyName) {
		return getComponentResource().map(componentResource -> componentResource.getTags(propertyName))
				.orElse(Collections.emptyList());
	}

	@Override
	public List<FoundationPage> getChildren() {
		return filterChildren(page -> true, false);
	}

	@Override
	public List<FoundationPage> getChildren(final boolean displayableOnly) {
		return displayableOnly ? filterChildren(DISPLAYABLE_ONLY, false) : getChildren();
	}

	@Override
	public List<FoundationPage> getChildren(final Predicate<FoundationPage> predicate) {
		return filterChildren(checkNotNull(predicate), false);
	}

	@Override
	public boolean isHasImage() {
		return getComponentResource().map(Accessible::isHasImage).orElse(false);
	}

	@Override
	public boolean isHasImage(final String name) {
		return getComponentResource().map(componentResource -> componentResource.isHasImage(name)).orElse(false);
	}

	@Override
	public Iterator<FoundationPage> listChildPages() {
		return listChildPages(page -> true);
	}

	@Override
	public Iterator<FoundationPage> listChildPages(final Predicate<FoundationPage> predicate) {
		return listChildPages(predicate, false);
	}

	@Override
	public Iterator<FoundationPage> listChildPages(final Predicate<FoundationPage> predicate, final boolean deep) {
		final Resource resource = page.adaptTo(Resource.class);
		final Iterator<Resource> iterator = deep ? new DeepResourceIterator(resource) : resource.listChildren();

		return new FoundationPageIterator(iterator, predicate);
	}

	@Override
	public Optional<FoundationPage> getChild(final String name) {
		Optional<FoundationPage> child = Optional.empty();

		if (hasChild(name)) {
			child = Optional.of(page.adaptTo(Resource.class).getChild(name).adaptTo(FoundationPage.class));
		}

		return child;
	}

	@Override
	public Optional<ComponentResource> getComponentResource() {
		if (componentResource == null) {
			componentResource = Optional.ofNullable(page.getContentResource())
					.map(resource -> resource.adaptTo(ComponentResource.class));
		}

		return componentResource;
	}

	@Override
	public Optional<ComponentResource> getComponentResource(final String relativePath) {
		return Optional.ofNullable(page.getContentResource(relativePath))
				.map(resource -> resource.adaptTo(ComponentResource.class));
	}

	@Override
	public Link getLink(final TitleType titleType) {
		return getLinkBuilder(titleType).build();
	}

	@Override
	public Link getLink(final TitleType titleType, final boolean mapped) {
		return getLinkBuilder(titleType, mapped).build();
	}

	@Override
	public LinkBuilder getLinkBuilder(final TitleType titleType) {
		return getLinkBuilder(titleType, false);
	}

	@Override
	public LinkBuilder getLinkBuilder(final TitleType titleType, final boolean mapped) {
		return LinkBuilderFactory.forPage(this, mapped, titleType);
	}

	@Override
	public Link getNavigationLink(final boolean isActive) {
		return getNavigationLink(isActive, false);
	}

	@Override
	public Link getNavigationLink(final boolean isActive, final boolean mapped) {
		return LinkBuilderFactory.forPage(this, mapped, TitleType.NAVIGATION_TITLE).setActive(isActive).build();
	}

	@Override
	public String getTemplatePath() {
		return getProperties().get(NameConstants.NN_TEMPLATE, String.class);
	}

	@Override
	public Optional<String> getTitle(final TitleType titleType) {
		return get(titleType.getPropertyName(), String.class);
	}

	// overridden methods

	@Override
	public FoundationPage getParent() {
		return Optional.ofNullable(page.getParent()).map(parent -> parent.adaptTo(FoundationPage.class)).orElse(null);
	}

	@Override
	public FoundationPage getParent(final int level) {
		return Optional.ofNullable(page.getParent(level)).map(parent -> parent.adaptTo(FoundationPage.class))
				.orElse(null);
	}

	@Override
	public FoundationPage getAbsoluteParent(final int level) {
		return Optional.ofNullable(page.getAbsoluteParent(level)).map(parent -> parent.adaptTo(FoundationPage.class))
				.orElse(null);
	}

	@Override
	public FoundationPageManager getPageManager() {
		return page.getContentResource().getResourceResolver().adaptTo(FoundationPageManager.class);
	}

	// delegate methods

	@Override
	public ValueMap getProperties() {
		return page.getProperties();
	}

	@Override
	public ValueMap getProperties(final String s) {
		return page.getProperties(s);
	}

	@Override
	public String getName() {
		return page.getName();
	}

	@Override
	public String getTitle() {
		return page.getTitle();
	}

	@Override
	public String getDescription() {
		return page.getDescription();
	}

	@Override
	public String getPageTitle() {
		return Optional.ofNullable(StringUtils.trimToNull(page.getPageTitle())).orElse(page.getTitle());
	}

	@Override
	public String getNavigationTitle() {
		return Optional.ofNullable(StringUtils.trimToNull(page.getNavigationTitle())).orElse(getPageTitle());
	}

	@Override
	public boolean isHideInNav() {
		return page.isHideInNav();
	}

	@Override
	public boolean hasContent() {
		return page.hasContent();
	}

	@Override
	public boolean isValid() {
		return page.isValid();
	}

	@Override
	public long timeUntilValid() {
		return page.timeUntilValid();
	}

	@Override
	public Calendar getOnTime() {
		return page.getOnTime();
	}

	@Override
	public Calendar getOffTime() {
		return page.getOffTime();
	}

	@Override
	public Calendar getDeleted() {
		return page.getDeleted();
	}

	@Override
	public String getDeletedBy() {
		return page.getDeletedBy();
	}

	@Override
	public String getLastModifiedBy() {
		return page.getLastModifiedBy();
	}

	@Override
	public Calendar getLastModified() {
		return page.getLastModified();
	}

	@Override
	public String getVanityUrl() {
		return page.getVanityUrl();
	}

	@Override
	public Tag[] getTags() {
		return page.getTags();
	}

	@Override
	public void lock() throws WCMException {
		page.lock();
	}

	@Override
	public boolean isLocked() {
		return page.isLocked();
	}

	@Override
	public String getLockOwner() {
		return page.getLockOwner();
	}

	@Override
	public boolean canUnlock() {
		return page.canUnlock();
	}

	@Override
	public void unlock() throws WCMException {
		page.unlock();
	}

	@Override
	public Template getTemplate() {
		return page.getTemplate();
	}

	@Override
	public Locale getLanguage(final boolean b) {
		return page.getLanguage(b);
	}

	@Override
	public Locale getLanguage() {
		return page.getLanguage();
	}

	@Override
	public String getPath() {
		return page.getPath();
	}

	@Override
	public Resource getContentResource() {
		return page.getContentResource();
	}

	@Override
	public Resource getContentResource(final String s) {
		return page.getContentResource(s);
	}

	@Override
	public Iterator<Page> listChildren() {
		return page.listChildren();
	}

	@Override
	public Iterator<Page> listChildren(final Filter<Page> filter) {
		return page.listChildren(filter);
	}

	@Override
	public Iterator<Page> listChildren(final Filter<Page> filter, final boolean b) {
		return page.listChildren(filter, b);
	}

	@Override
	public boolean hasChild(final String s) {
		return page.hasChild(s);
	}

	@Override
	public int getDepth() {
		return page.getDepth();
	}

	// internals

	private Optional<FoundationPage> findAncestorForPredicate(final Predicate<ComponentResource> predicate,
			final boolean excludeCurrentResource) {
		FoundationPage page = excludeCurrentResource ? getParent() : this;
		FoundationPage ancestorPage = null;

		while (page != null) {
			final Optional<ComponentResource> optionalComponentResource = page.getComponentResource();

			if (optionalComponentResource.isPresent() && predicate.test(optionalComponentResource.get())) {
				ancestorPage = page;
				break;
			} else {
				page = page.getParent();
			}
		}

		return Optional.ofNullable(ancestorPage);
	}

	private List<FoundationPage> filterChildren(final Predicate<FoundationPage> predicate, final boolean deep) {
		final List<FoundationPage> pages = new ArrayList<>();

		final FoundationPageManager pageManager = getPageManager();

		final Iterator<Page> iterator = page.listChildren(ALL_PAGES, deep);

		while (iterator.hasNext()) {
			final FoundationPage page = pageManager.getPage(iterator.next());

			if (page != null && predicate.test(page)) {
				pages.add(page);
			}
		}

		return pages;
	}
}