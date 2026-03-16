package com.newshub.core.jobs;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.newshub.core.services.ArticleArchiveService;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import java.util.*;

@Component(service = JobConsumer.class, immediate = true, property = {
        JobConsumer.PROPERTY_TOPICS + "=" + ArticleArchiveJobConsumer.JOB_TOPIC
})
public class ArticleArchiveJobConsumer implements JobConsumer {

    public static final String JOB_TOPIC = "com/newshub/archive/articles";
    private static final String SUBSERVICE = "content-writer";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private ArticleArchiveService archiveService;

    @Override
    public JobResult process(Job job) {
        String newsRoot = job.getProperty("newsRoot", String.class);
        int daysLimit = job.getProperty("daysLimit", Integer.class);
        int batchSize = job.getProperty("batchSize", Integer.class);

        Map<String, Object> auth = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SUBSERVICE);

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(auth)) {
            Session session = resolver.adaptTo(Session.class);
            List<String> paths = findOldArticles(session, newsRoot, daysLimit);

            for (String path : paths) {
                // Pass the existing resolver to the service for efficiency
                archiveService.archiveArticle(path, resolver);
            }
            return JobResult.OK;
        } catch (Exception e) {
            return JobResult.FAILED;
        }
    }

    private List<String> findOldArticles(Session session, String root, int days) {
        List<String> results = new ArrayList<>();
        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.DAY_OF_YEAR, -days);

        Map<String, String> map = new HashMap<>();
        map.put("path", root);
        map.put("type", "cq:Page");
        map.put("1_property", "jcr:content/cq:lastReplicated");
        map.put("1_property.operation", "exists");
        map.put("2_daterange.property", "jcr:content/cq:lastReplicated");
        map.put("2_daterange.upperBound", String.valueOf(limit.getTimeInMillis()));
        map.put("p.limit", "-1");

        for (Hit hit : queryBuilder.createQuery(PredicateGroup.create(map), session).getResult().getHits()) {
            try { results.add(hit.getPath()); } catch (Exception ignored) {}
        }
        return results;
    }
}