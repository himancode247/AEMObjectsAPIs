package com.newshub.core.services.impl;

import com.newshub.core.services.ArticleArchiveService;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;
import java.util.Collections;
import java.util.Map;

@Component(service = ArticleArchiveService.class)
public class ArticleArchiveServiceImpl implements ArticleArchiveService {

    private static final Logger LOG = LoggerFactory.getLogger(ArticleArchiveServiceImpl.class);
    private static final String ARCHIVE_ROOT = "/content/newshub/en/archive";
    private static final String SUBSERVICE = "content-writer";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public boolean archiveArticle(String articlePath) {
        Map<String, Object> auth = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SUBSERVICE);
        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(auth)) {
            return archiveArticle(articlePath, resolver);
        } catch (Exception e) {
            LOG.error("Manual archive failed for {}", articlePath, e);
            return false;
        }
    }

    @Override
    public boolean archiveArticle(String articlePath, ResourceResolver resolver) {
        try {
            Session session = resolver.adaptTo(Session.class);
            if (session == null || !session.nodeExists(articlePath)) return false;

            ensureArchiveFolder(session);

            Node contentNode = session.getNode(articlePath + "/jcr:content");
            contentNode.setProperty("originalPath", articlePath);

            String articleName = articlePath.substring(articlePath.lastIndexOf("/") + 1);
            session.move(articlePath, ARCHIVE_ROOT + "/" + articleName);

            session.save();
            return true;
        } catch (Exception e) {
            LOG.error("Error moving article {}", articlePath, e);
            return false;
        }
    }

    private void ensureArchiveFolder(Session session) throws Exception {
        if (!session.nodeExists(ARCHIVE_ROOT)) {
            Node parent = session.getNode("/content/newshub/en");
            parent.addNode("archive", "sling:OrderedFolder");
            session.save();
        }
    }
}