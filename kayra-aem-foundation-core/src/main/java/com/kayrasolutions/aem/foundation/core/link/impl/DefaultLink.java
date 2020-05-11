package com.kayrasolutions.aem.foundation.core.link.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.kayrasolutions.aem.foundation.api.link.Link;

public final class DefaultLink implements Link {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String path;

	private final String extension;

	private final String suffix;

	private final String href;

	private final List<String> selectors;

	private final String queryString;

	private final boolean external;

	private final String target;

	private final String title;

	private final Map<String, String> properties;

	private final boolean active;

	private final List<Link> children;

	public DefaultLink(final String path, final String extension, final String suffix, final String href,
			final List<String> selectors, final String queryString, final boolean external, final String target,
			final String title, final Map<String, String> properties, final boolean active, final List<Link> children) {
		this.path = path;
		this.extension = extension;
		this.suffix = suffix;
		this.href = href;
		this.selectors = selectors;
		this.queryString = queryString;
		this.external = external;
		this.target = target;
		this.title = title;
		this.properties = properties;
		this.active = active;
		this.children = children;
	}

	@Override
	public String getExtension() {
		return extension;
	}

	@Override
	public String getHref() {
		return href;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public List<String> getSelectors() {
		return selectors;
	}

	@Override
	public String getSuffix() {
		return suffix;
	}

	@Override
	public String getTarget() {
		return target;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public boolean isExternal() {
		return external;
	}

	@Override
	public List<Link> getChildren() {
		return children;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("path", path)
				.add("selectors", selectors)
				.add("extension", extension)
				.add("suffix", suffix)
				.add("queryString", queryString)
				.add("href", href)
				.add("title", title)
				.add("target", target)
				.add("isExternal", external)
				.add("isActive", active)
				.add("children", children.size())
				.add("properties", properties).toString();
	}
}
