package com.kayrasolutions.aem.foundation.core.page.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.RowIterator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.RangeIterator;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Revision;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.api.msm.Blueprint;
import com.google.common.base.Stopwatch;
import com.kayrasolutions.aem.foundation.api.page.FoundationPage;
import com.kayrasolutions.aem.foundation.api.page.FoundationPageManager;
import com.kayrasolutions.aem.foundation.core.page.predicates.TemplatePredicate;

public final class DefaultFoundationPageManager implements FoundationPageManager {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultFoundationPageManager.class);

	private final ResourceResolver resourceResolver;

	private final PageManager pageManager;

	public DefaultFoundationPageManager(final ResourceResolver resourceResolver) {
		this.resourceResolver = resourceResolver;

		pageManager = resourceResolver.adaptTo(PageManager.class);
	}

	@Override
	public FoundationPage copy(final Page page, final String destination, final String beforeName,
			final boolean shallow, final boolean resolveConflict) throws WCMException {
		return getPage(pageManager.copy(page, destination, beforeName, shallow, resolveConflict));
	}

	@Override
	public FoundationPage copy(final Page page, final String destination, final String beforeName,
			final boolean shallow, final boolean resolveConflict, final boolean autoSave) throws WCMException {
		return getPage(pageManager.copy(page, destination, beforeName, shallow, resolveConflict, autoSave));
	}

	@Override
	public FoundationPage create(final String parentPath, final String pageName, final String template,
			final String title) throws WCMException {
		return getPage(pageManager.create(parentPath, pageName, template, title));
	}

	@Override
	public FoundationPage create(final String parentPath, final String pageName, final String template,
			final String title, final boolean autoSave) throws WCMException {
		return getPage(pageManager.create(parentPath, pageName, template, title, autoSave));
	}

	@Override
	public List<FoundationPage> findPages(final String rootPath, final Collection<String> tagIds,
			final boolean matchOne) {
		checkNotNull(rootPath);
		checkNotNull(tagIds);

		LOG.debug("path = {}, tag IDs = {}", rootPath, tagIds);

		final Stopwatch stopwatch = Stopwatch.createStarted();

		final RangeIterator<Resource> iterator = resourceResolver.adaptTo(TagManager.class).find(rootPath,
				tagIds.toArray(new String[0]), matchOne);

		final List<FoundationPage> pages = new ArrayList<>();

		while (iterator.hasNext()) {
			final Resource resource = iterator.next();

			if (JcrConstants.JCR_CONTENT.equals(resource.getName())) {
				final FoundationPage page = getPage(resource.getParent().getPath());

				if (page != null) {
					pages.add(page);
				}
			}
		}

		LOG.debug("found {} result(s) in {}ms", pages.size(), stopwatch.elapsed(MILLISECONDS));

		return pages;
	}

	@Override
	public List<FoundationPage> findPages(final String rootPath, final String templatePath) {
		return findPages(rootPath, new TemplatePredicate(templatePath));
	}

	@Override
	public List<FoundationPage> findPages(final String rootPath, final Predicate<FoundationPage> predicate) {
		final Stopwatch stopwatch = Stopwatch.createStarted();

		final FoundationPage page = getPage(checkNotNull(rootPath));
		final List<FoundationPage> pages = page != null ? page.findDescendants(predicate) : Collections.emptyList();

		stopwatch.stop();

		LOG.debug("found {} result(s) in {}ms", pages.size(), stopwatch.elapsed(MILLISECONDS));

		return pages;
	}

	@Override
	public FoundationPage getContainingPage(final Resource resource) {
		return getPage(pageManager.getContainingPage(resource));
	}

	@Override
	public FoundationPage getContainingPage(final String path) {
		return getPage(pageManager.getContainingPage(path));
	}

	@Override
	public FoundationPage getPage(final Page page) {
		return page != null ? page.adaptTo(FoundationPage.class) : null;
	}

	@Override
	public FoundationPage getPage(final String path) {
		return getPage(pageManager.getPage(path));
	}

	@Override
	public FoundationPage move(final Page page, final String destination, final String beforeName,
			final boolean shallow, final boolean resolveConflict, final String[] adjustRefs) throws WCMException {
		return getPage(pageManager.move(page, destination, beforeName, shallow, resolveConflict, adjustRefs));
	}

	@Override
	public FoundationPage move(final Page page, final String destination, final String beforeName,
			final boolean shallow, final boolean resolveConflict, final String[] adjustRefs, final String[] publishRefs)
			throws WCMException {
		return getPage(
				pageManager.move(page, destination, beforeName, shallow, resolveConflict, adjustRefs, publishRefs));
	}

	@Override
	public FoundationPage restore(final String path, final String revisionId) throws WCMException {
		return getPage(pageManager.restore(path, revisionId));
	}

	@Override
	public FoundationPage restoreTree(final String path, final Calendar date) throws WCMException {
		return getPage(pageManager.restoreTree(path, date));
	}

	@Override
	public List<FoundationPage> search(final Query query) {
		return search(query, -1);
	}

	@Override
	public List<FoundationPage> search(final Query query, final int limit) {
		checkNotNull(query);

		LOG.debug("query statement = {}", query.getStatement());

		final Stopwatch stopwatch = Stopwatch.createStarted();

		final List<FoundationPage> pages = new ArrayList<>();

		int count = 0;

		try {
			final Set<String> paths = new HashSet<>();

			final RowIterator rows = query.execute().getRows();

			while (rows.hasNext()) {
				final String path = rows.nextRow().getPath();

				if (limit == -1 || count < limit) {
					LOG.debug("result path = {}", path);

					final FoundationPage page = getContainingPage(path);

					// ensure no duplicate pages are added
					if (!paths.contains(page.getPath())) {
						paths.add(page.getPath());

						if (page != null) {
							pages.add(page);
							count++;
						} else {
							LOG.error("result is null for path : {}", path);
						}
					}
				}
			}

			stopwatch.stop();

			LOG.debug("found {} result(s) in {}ms", pages.size(), stopwatch.elapsed(MILLISECONDS));
		} catch (RepositoryException re) {
			LOG.error("error finding pages for query : " + query.getStatement(), re);
		}

		return pages;
	}

	// delegate methods

	@Override
	public Resource move(final Resource resource, final String s, final String s1, final boolean b, final boolean b1,
			final String[] strings) throws WCMException {
		return pageManager.move(resource, s, s1, b, b1, strings);
	}

	@Override
	public Resource move(final Resource resource, final String s, final String s1, final boolean b, final boolean b1,
			final String[] strings, final String[] strings1) throws WCMException {
		return pageManager.move(resource, s, s1, b, b1, strings, strings1);
	}

	@Override
	public Resource copy(final CopyOptions copyOptions) throws WCMException {
		return pageManager.copy(copyOptions);
	}

	@Override
	public Resource copy(final Resource resource, final String s, final String s1, final boolean b, final boolean b1)
			throws WCMException {
		return pageManager.copy(resource, s, s1, b, b1);
	}

	@Override
	public Resource copy(final Resource resource, final String s, final String s1, final boolean b, final boolean b1,
			final boolean b2) throws WCMException {
		return pageManager.copy(resource, s, s1, b, b1, b2);
	}

	@Override
	public void delete(final Page page, final boolean b) throws WCMException {
		pageManager.delete(page, b);
	}

	@Override
	public void delete(final Page page, final boolean b, final boolean b1) throws WCMException {
		pageManager.delete(page, b, b1);
	}

	@Override
	public void delete(final Resource resource, final boolean b) throws WCMException {
		pageManager.delete(resource, b);
	}

	@Override
	public void delete(final Resource resource, final boolean b, final boolean b1) throws WCMException {
		pageManager.delete(resource, b, b1);
	}

	@Override
	public void order(final Page page, final String s) throws WCMException {
		pageManager.order(page, s);
	}

	@Override
	public void order(final Page page, final String s, final boolean b) throws WCMException {
		pageManager.order(page, s, b);
	}

	@Override
	public void order(final Resource resource, final String s) throws WCMException {
		pageManager.order(resource, s);
	}

	@Override
	public void order(final Resource resource, final String s, final boolean b) throws WCMException {
		pageManager.order(resource, s, b);
	}

	@Override
	public Template getTemplate(final String s) {
		return pageManager.getTemplate(s);
	}

	@Override
	public Collection<Template> getTemplates(final String s) {
		return pageManager.getTemplates(s);
	}

	@Override
	@Deprecated
	public Collection<Blueprint> getBlueprints(final String s) {
		return pageManager.getBlueprints(s);
	}

	@Override
	public Revision createRevision(final Page page) throws WCMException {
		return pageManager.createRevision(page);
	}

	@Override
	public Revision createRevision(final Page page, final String s, final String s1) throws WCMException {
		return pageManager.createRevision(page, s, s1);
	}

	@Override
	public Collection<Revision> getRevisions(final String s, final Calendar calendar) throws WCMException {
		return pageManager.getRevisions(s, calendar);
	}

	@Override
	public Collection<Revision> getRevisions(final String s, final Calendar calendar, final boolean b)
			throws WCMException {
		return pageManager.getRevisions(s, calendar, b);
	}

	@Override
	public Collection<Revision> getChildRevisions(final String s, final Calendar calendar) throws WCMException {
		return pageManager.getChildRevisions(s, calendar);
	}

	@Override
	public Collection<Revision> getChildRevisions(final String s, final Calendar calendar, final boolean b)
			throws WCMException {
		return pageManager.getChildRevisions(s, calendar, b);
	}

	@Override
	public Collection<Revision> getChildRevisions(final String s, final String s1, final Calendar calendar)
			throws WCMException {
		return pageManager.getChildRevisions(s, s1, calendar);
	}

	@Override
	public Page restoreTree(final String s, final Calendar calendar, final boolean b) throws WCMException {
		return pageManager.restoreTree(s, calendar, b);
	}

	@Override
	public void touch(final Node node, final boolean b, final Calendar calendar, final boolean b1) throws WCMException {
		pageManager.touch(node, b, calendar, b1);
	}
}
