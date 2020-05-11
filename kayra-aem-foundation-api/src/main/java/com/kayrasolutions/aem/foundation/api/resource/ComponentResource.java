package com.kayrasolutions.aem.foundation.api.resource;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.sling.api.resource.Resource;

import com.kayrasolutions.aem.foundation.api.Accessible;
import com.kayrasolutions.aem.foundation.api.Inheritable;
import com.kayrasolutions.aem.foundation.api.Linkable;
import com.kayrasolutions.aem.foundation.api.Traversable;

/**
 * Represents a "component" resource in the JCR, typically an unstructured
 * resource in the hierarchy of an AEM page.
 */
public interface ComponentResource extends Linkable, Accessible, Inheritable, Traversable<ComponentResource> {

	/**
	 * Get the unique ID for this resource based on the path. If this resource is
	 * the descendant of a page, the page path will be removed from the identifier,
	 * since the relative path of a component resource is always unique for a page.
	 *
	 * @return unique ID
	 */
	String getId();

	/**
	 * Get the index of this resource in relation to sibling resources.
	 *
	 * @return index in sibling resources or -1 if resource is null or has null
	 *         parent resource
	 */
	int getIndex();

	/**
	 * Get the index of this resource in relation to sibling resources, ignoring
	 * resource types that do not match the specified value.
	 *
	 * @param resourceType sling:resourceType to filter on
	 * @return index in sibling resources or -1 if resource is null or has null
	 *         parent resource
	 */
	int getIndex(String resourceType);

	/**
	 * Shortcut for getting the current resource path.
	 *
	 * @return resource path
	 */
	String getPath();

	/**
	 * Get the underlying resource for this instance.
	 *
	 * @return current resource
	 */
	Resource getResource();

	/**
	 * Get the component resource for the resource at the given path relative to the
	 * current resource.
	 *
	 * @param relativePath relative path to component
	 * @return <code>Optional</code> resource for component
	 */
	Optional<ComponentResource> getComponentResource(String relativePath);

	/**
	 * Get a list of child resources for the current resource.
	 *
	 * @return list of component resources or empty list if none exist
	 */
	List<ComponentResource> getComponentResources();

	/**
	 * Get a predicate-filtered list of child resources for the current resource.
	 *
	 * @param predicate predicate used to filter resources
	 * @return list of component resources that meet the predicate criteria or empty
	 *         list if none exist
	 */
	List<ComponentResource> getComponentResources(Predicate<ComponentResource> predicate);

	/**
	 * Get a list of child resources for the resource at the given path relative to
	 * this resource.
	 *
	 * @param relativePath relative path to parent of desired resources
	 * @return list of component resources below the specified relative path or
	 *         empty list if none exist
	 */
	List<ComponentResource> getComponentResources(String relativePath);

	/**
	 * Get a list of child resources for the resource at the given path relative to
	 * this resource, returning only the resources that have the specified resource
	 * type.
	 *
	 * @param relativePath relative path to parent of desired resources
	 * @param resourceType sling:resourceType of resources to get from parent
	 *                     resource
	 * @return list of component resources matching the given resource type below
	 *         the specified relative path or empty list if none exist
	 */
	List<ComponentResource> getComponentResources(String relativePath, String resourceType);

	/**
	 * Get a list of child resources for the resource at the given path relative to
	 * this resource, returning only the resources that meet the predicate criteria.
	 *
	 * @param relativePath relative path to parent of desired resources
	 * @param predicate    predicate used to filter resources
	 * @return list of component resources that meet the predicate criteria below
	 *         the specified relative path or empty list if none exist
	 */
	List<ComponentResource> getComponentResources(String relativePath, Predicate<ComponentResource> predicate);

	/**
	 * Get the parent of this resource.
	 *
	 * @return parent component resource or absent optional if resource has no
	 *         parent
	 */
	Optional<ComponentResource> getParent();
}
