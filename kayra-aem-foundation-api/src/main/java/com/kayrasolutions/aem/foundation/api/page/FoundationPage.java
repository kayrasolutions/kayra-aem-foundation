package com.kayrasolutions.aem.foundation.api.page;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.day.cq.wcm.api.Page;
import com.kayrasolutions.aem.foundation.api.Accessible;
import com.kayrasolutions.aem.foundation.api.Inheritable;
import com.kayrasolutions.aem.foundation.api.Linkable;
import com.kayrasolutions.aem.foundation.api.Traversable;
import com.kayrasolutions.aem.foundation.api.link.Link;
import com.kayrasolutions.aem.foundation.api.link.builders.LinkBuilder;
import com.kayrasolutions.aem.foundation.api.page.enums.TitleType;
import com.kayrasolutions.aem.foundation.api.resource.ComponentResource;

/**
 * Decorates the <code>Page</code> interface with additional convenience methods
 * for traversing the content hierarchy and getters for Kayra Solutions AEM Foundation
 * classes.
 */
public interface FoundationPage extends Page, Accessible, Inheritable, Linkable, Traversable<FoundationPage> {

	/**
	 * Get the child pages of the current page.
	 *
	 * @return all child pages of current page or empty list if none exist
	 */
	List<FoundationPage> getChildren();

	/**
	 * Get the child pages of the current page, excluding children that are not
	 * "displayable" (i.e. hidden in nav).
	 *
	 * @param displayableOnly if true, only pages that are not hidden in navigation
	 *                        will be returned
	 * @return child pages of current page or empty list if none exist
	 */
	List<FoundationPage> getChildren(boolean displayableOnly);

	/**
	 * Get the child pages of the current page filtered using the given predicate.
	 *
	 * @param predicate predicate to filter pages
	 * @return filtered list of child pages or empty list if none exist
	 */
	List<FoundationPage> getChildren(Predicate<FoundationPage> predicate);

	/**
	 * List children of the current page.
	 *
	 * @return iterator of child pages
	 */
	Iterator<FoundationPage> listChildPages();

	/**
	 * List child pages of the current page filtered using the given predicate.
	 *
	 * @param predicate predicate to filter pages
	 * @return filtered iterator of child pages
	 */
	Iterator<FoundationPage> listChildPages(Predicate<FoundationPage> predicate);

	/**
	 * List child pages of the current page filtered using the given predicate.
	 *
	 * @param predicate predicate to filter pages
	 * @param deep      if true, recursively iterate over all descendant pages
	 * @return filtered iterator of child pages
	 */
	Iterator<FoundationPage> listChildPages(Predicate<FoundationPage> predicate, boolean deep);

	/**
	 * Get the child page of the current page by name.
	 *
	 * @param name name of the child
	 * @return page or absent <code>Optional</code>
	 */
	Optional<FoundationPage> getChild(String name);

	/**
	 * Get the component node for the "jcr:content" node for this page. If the page
	 * does not have a content node, an "absent" Optional is returned.
	 *
	 * @return optional component node for page content
	 */
	Optional<ComponentResource> getComponentResource();

	/**
	 * Get the component node for the node at the given path relative to the
	 * "jcr:content" node for this page. If the node does not exist, an "absent"
	 * Optional is returned.
	 *
	 * @param relativePath relative path to resource
	 * @return optional component node for resource relative to page content
	 */
	Optional<ComponentResource> getComponentResource(String relativePath);

	/**
	 * Get a link with a specified title type for this item.
	 *
	 * @param titleType type of title to set on link
	 * @return link
	 */
	Link getLink(TitleType titleType);

	/**
	 * Get a link with a specified title type for this item.
	 *
	 * @param titleType type of title to set on link
	 * @param mapped    if true, the <code>Link</code> path will be routed through
	 *                  the resource resolver to determine the mapped path (e.g.
	 *                  without leading "/content").
	 * @return link
	 */
	Link getLink(TitleType titleType, boolean mapped);

	/**
	 * Get a link builder for the current resource path.
	 *
	 * @param titleType type of title to set on builder
	 * @return builder instance for this item
	 */
	LinkBuilder getLinkBuilder(TitleType titleType);

	/**
	 * Get a link builder for the current resource path.
	 *
	 * @param titleType type of title to set on builder
	 * @param mapped    if true, the <code>Link</code> path will be routed through
	 *                  the resource resolver to determine the mapped path (e.g.
	 *                  without leading "/content").
	 * @return builder instance for this item
	 */
	LinkBuilder getLinkBuilder(TitleType titleType, boolean mapped);

	/**
	 * Get a navigation link for this page containing an active state. The returned
	 * link will use the navigation title as the link title, defaulting to the JCR
	 * title if it does not exist.
	 *
	 * @param isActive active state to be set on returned link
	 * @return navigation link
	 */
	Link getNavigationLink(boolean isActive);

	/**
	 * Get a navigation link for this page containing an active state. The returned
	 * link will use the navigation title as the link title, defaulting to the JCR
	 * title if it does not exist.
	 *
	 * @param isActive active state to be set on returned link
	 * @param mapped   if true, the <code>NavigationLink</code> path will be routed
	 *                 through the resource resolver to determine the mapped path
	 *                 (e.g. without leading "/content").
	 * @return navigation link
	 */
	Link getNavigationLink(boolean isActive, boolean mapped);

	/**
	 * Get the template path for this page. This method is preferred over
	 * getTemplate().getPath(), which is dependent on access to /apps and will
	 * therefore fail in publish mode.
	 *
	 * @return value of cq:template property or empty string if none exists
	 */
	String getTemplatePath();

	/**
	 * Get the title with the given type for this page. If the title value is empty
	 * or non-existent, an absent <code>Optional</code> is returned.
	 *
	 * @param titleType type of title to retrieve
	 * @return title value or absent <code>Optional</code>
	 */
	Optional<String> getTitle(TitleType titleType);

	// overrides

	/**
	 * Returns the absolute parent page. If no page exists at that level,
	 * <code>null</code> is returned.
	 * <p>
	 * Example (this path == /content/geometrixx/en/products)
	 * <p>
	 * 
	 * <pre>
	 * | level | returned                        |
	 * |     0 | /content                        |
	 * |     1 | /content/geometrixx             |
	 * |     2 | /content/geometrixx/en          |
	 * |     3 | /content/geometrixx/en/products |
	 * |     4 | null                            |
	 * </pre>
	 *
	 * @param level hierarchy level of the parent page to retrieve
	 * @return the respective parent page or <code>null</code>
	 */
	@Override
	FoundationPage getAbsoluteParent(int level);

	/**
	 * Convenience method that returns the manager of this page.
	 *
	 * @return the page manager
	 */
	@Override
	FoundationPageManager getPageManager();

	/**
	 * Returns the parent page if it's resource adapts to page.
	 *
	 * @return the parent page or <code>null</code>
	 */
	@Override
	FoundationPage getParent();

	/**
	 * Returns the relative parent page. If no page exists at that level,
	 * <code>null</code> is returned.
	 * <p>
	 * Example (this path == /content/geometrixx/en/products)
	 * <p>
	 * 
	 * <pre>
	 * | level | returned                        |
	 * |     0 | /content/geometrixx/en/products |
	 * |     1 | /content/geometrixx/en          |
	 * |     2 | /content/geometrixx             |
	 * |     3 | /content                        |
	 * |     4 | null                            |
	 * </pre>
	 *
	 * @param level hierarchy level of the parent page to retrieve
	 * @return the respective parent page or <code>null</code>
	 */
	@Override
	FoundationPage getParent(int level);
}
