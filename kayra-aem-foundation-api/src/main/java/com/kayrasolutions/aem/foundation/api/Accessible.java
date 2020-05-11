package com.kayrasolutions.aem.foundation.api;

import java.util.List;
import java.util.Optional;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.tagging.Tag;
import com.kayrasolutions.aem.foundation.api.link.Link;
import com.kayrasolutions.aem.foundation.api.page.FoundationPage;

/**
 * Definition of accessor methods for content resources for <code>Node</code>
 * and <code>Page</code> instances.
 */
public interface Accessible {

	/**
	 * @return map of property names to values, or empty map if underlying resource
	 *         is null or nonexistent
	 */
	ValueMap asMap();

	/**
	 * Get a property value from the current node, returning the default value if
	 * the property does not exist.
	 *
	 * @param <T>          property type
	 * @param propertyName property name
	 * @param defaultValue default value
	 * @return property value or default value if it does not exist
	 */
	<T> T get(String propertyName, T defaultValue);

	/**
	 * Get a property value from the current node. This returns the same value as
	 * the underlying <code>ValueMap</code> wrapped in an <code>Optional</code>
	 * instance instead of returning null.
	 *
	 * @param propertyName property name
	 * @param type         property type
	 * @param <T>          property type
	 * @return <code>Optional</code> of the given type containing the property value
	 *         or absent if the property does not exist
	 */
	<T> Optional<T> get(String propertyName, Class<T> type);

	/**
	 * Given a property on this resource containing the path of another resource,
	 * get an <code>Optional</code> containing the href to the resource (i.e. the
	 * content path with ".html" appended).
	 *
	 * @param propertyName name of property containing a valid content path
	 * @return href value wrapped in an <code>Optional</code>
	 */
	Optional<String> getAsHref(String propertyName);

	/**
	 * Given a property on this resource containing the path of another resource,
	 * get an <code>Optional</code> containing the href to the resource. Use this
	 * method with a <code>true</code> argument when appending ".html" to the
	 * resource path is desired only for valid CQ pages and not external paths.
	 *
	 * @param propertyName name of property containing a valid content path
	 * @param strict       if true, strict resource resolution will be applied and
	 *                     only valid CQ content paths will have ".html" appended
	 * @return href value wrapped in an <code>Optional</code>
	 */
	Optional<String> getAsHref(String propertyName, boolean strict);

	/**
	 * Given a property on this resource containing the path of another resource,
	 * get an <code>Optional</code> containing the href to the resource. Use this
	 * method with a <code>true</code> argument when appending ".html" to the
	 * resource path is desired only for valid CQ pages and not external paths.
	 * Setting <code>mapped</code> to <code>true</code> will map the path value, if
	 * it exists, through the Sling Resource Resolver.
	 *
	 * @param propertyName name of property containing a valid content path
	 * @param strict       if true, strict resource resolution will be applied and
	 *                     only valid CQ content paths will have ".html" appended
	 * @param mapped       if true, the property value will be routed through the
	 *                     Resource Resolver to determine the mapped path for the
	 *                     value. For example, if a mapping from "/content/" to "/"
	 *                     exists in the Apache Sling Resource Resolver Factory OSGi
	 *                     configuration, getting the mapped href for the path
	 *                     "/content/avionos" will return "/avionos.html".
	 * @return href value wrapped in an <code>Optional</code>
	 */
	Optional<String> getAsHref(String propertyName, boolean strict, boolean mapped);

	/**
	 * Given a property on this resource containing the path of another resource,
	 * get a link to the resource.
	 *
	 * @param propertyName name of property containing a valid content path
	 * @return <code>Optional</code> link object, absent if property does not
	 *         contain a valid content path
	 */
	Optional<Link> getAsLink(String propertyName);

	/**
	 * Given a property on this resource containing the path of another resource,
	 * get a link to the resource. Use this method with a <code>true</code> argument
	 * when including an extension for the link is desired only for valid CQ pages
	 * and not external paths.
	 *
	 * @param propertyName name of property containing a valid content path
	 * @param strict       if true, strict resource resolution will be applied and
	 *                     only valid CQ content paths will have an extension
	 * @return <code>Optional</code> link object, absent if property does not
	 *         contain a valid content path
	 */
	Optional<Link> getAsLink(String propertyName, boolean strict);

	/**
	 * Given a property on this resource containing the path of another resource,
	 * get a link to the resource. Use this method with a <code>true</code> argument
	 * when including an extension for the link is desired only for valid CQ pages
	 * and not external paths. Setting <code>mapped</code> to <code>true</code> will
	 * map the path value, if it exists, through the Sling Resource Resolver.
	 *
	 * @param propertyName name of property containing a valid content path
	 * @param strict       if true, strict resource resolution will be applied and
	 *                     only valid CQ content paths will have an extension
	 * @param mapped       if true, the property value will be routed through the
	 *                     Resource Resolver to determine the mapped path for the
	 *                     value. For example, if a mapping from "/content/" to "/"
	 *                     exists in the Apache Sling Resource Resolver Factory OSGi
	 *                     configuration, the <code>Link</code> path will be
	 *                     "/avionos" rather than "/content/avionos".
	 * @return <code>Optional</code> link object, absent if property does not
	 *         contain a valid content path
	 */
	Optional<Link> getAsLink(String propertyName, boolean strict, boolean mapped);

	/**
	 * Get a multi-valued property from the current node as a list of the given
	 * type.
	 *
	 * @param propertyName name of multi-valued property
	 * @param type         property type
	 * @param <T>          property type, must be supported by <code>ValueMap</code>
	 * @return list of property values or an empty list if the property does not
	 *         exist
	 */
	<T> List<T> getAsList(String propertyName, Class<T> type);

	/**
	 * Get a page instance from the value of the given property. Will return an
	 * absent <code>Optional</code> if the path value for the given property name
	 * does not resolve to a valid CQ page.
	 *
	 * @param propertyName property name
	 * @return <code>Optional</code> page for property value
	 */
	Optional<FoundationPage> getAsPage(String propertyName);

	/**
	 * Get a list of pages from the value of the given property. Pages will only be
	 * returned in the list if the paths resolve to a non-null page.
	 *
	 * @param propertyName property name
	 * @return list of pages for property value
	 */
	List<FoundationPage> getAsPageList(String propertyName);

	/**
	 * Get an <code>Optional</code> resource instance for a property on this
	 * resource containing the path of another <code>Resource</code>.
	 *
	 * @param propertyName name of property containing a resource path
	 * @return <code>Optional</code> instance of the resource, or absent if either
	 *         the property does not exist or does not resolve to a resource
	 */
	Optional<Resource> getAsResource(String propertyName);

	/**
	 * Get a list of resource instances for a property on this resource containing
	 * an array of paths to other <code>Resource</code>s.
	 *
	 * @param propertyName name of property containing resource paths
	 * @return list of resources, or empty list if either the property does not
	 *         exist or the resources for the paths do not resolve
	 */
	List<Resource> getAsResourceList(String propertyName);

	/**
	 * Get an <code>Optional</code> type instance for a property on this resource
	 * containing the path of another <code>Resource</code>.
	 *
	 * @param propertyName  name of property containing a resource path
	 * @param type          type to adapt from resource
	 * @param <AdapterType> adapter class that is adaptable from
	 *                      <code>Resource</code>
	 * @return <code>Optional</code> instance of the specified type, or absent if
	 *         either the property does not exist or the resource does not adapt to
	 *         the provided type
	 */
	<AdapterType> Optional<AdapterType> getAsType(String propertyName, Class<AdapterType> type);

	/**
	 * Get a list of type instances for a property on this resource containing an
	 * array of paths to other <code>Resource</code>s.
	 *
	 * @param propertyName  name of property containing resource paths
	 * @param type          type to adapt from resource
	 * @param <AdapterType> adapter class that is adaptable from
	 *                      <code>Resource</code>
	 * @return list of instances of the specified type, or empty list if either the
	 *         property does not exist or the resources for the paths do not adapt
	 *         to the provided type
	 */
	<AdapterType> List<AdapterType> getAsTypeList(String propertyName, Class<AdapterType> type);

	/**
	 * Get the referenced DAM asset path for the default image (named "image") for
	 * this component.
	 *
	 * @param isSelf if true, image reference property will be accessed from the
	 *               current resource rather than a child
	 * @return <code>Optional</code> image reference path
	 */
	Optional<String> getImageReference(boolean isSelf);

	/**
	 * Get the referenced DAM asset path for the default image (named "image") for
	 * this component.
	 *
	 * @return <code>Optional</code> image reference path
	 */
	Optional<String> getImageReference();

	/**
	 * @param name image name
	 * @return <code>Optional</code> image reference path
	 */
	Optional<String> getImageReference(String name);

	/**
	 * Get the DAM asset rendition path for the default image (named "image") for
	 * this component.
	 *
	 * @param renditionName rendition name for this asset (e.g.
	 *                      "cq5dam.thumbnail.140.100.png")
	 * @return <code>Optional</code> image rendition path
	 */
	Optional<String> getImageRendition(String renditionName);

	/**
	 * @param name          image name
	 * @param renditionName rendition name for this asset
	 * @return <code>Optional</code> image rendition path
	 */
	Optional<String> getImageRendition(String name, String renditionName);

	/**
	 * Check if the current resource has a default image.
	 *
	 * @return true if image has content
	 */
	boolean isHasImage();

	/**
	 * Check if the current resource has a named image.
	 *
	 * @param name image name (name of image as defined in dialog)
	 * @return true if image has content
	 */
	boolean isHasImage(String name);

	/**
	 * Get a list of tags for the given property name.
	 *
	 * @param propertyName name of property containing an array of tag IDs
	 * @return list of tags or empty list if not found
	 */
	List<Tag> getTags(String propertyName);
}
