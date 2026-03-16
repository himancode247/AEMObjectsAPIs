package com.newshub.core.schedulers;

import com.newshub.core.config.ArticleArchiverConfig;
import com.newshub.core.jobs.ArticleArchiveJobConsumer;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.HashMap;
import java.util.Map;

@Component(service = Runnable.class, immediate = true)
@Designate(ocd = ArticleArchiverConfig.class)
public class ArticleArchiveScheduler implements Runnable {

    @Reference
    private JobManager jobManager;

    private ArticleArchiverConfig config;

    @Activate @Modified
    protected void activate(ArticleArchiverConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        Map<String, Object> props = new HashMap<>();
        props.put("newsRoot", config.news_root());
        props.put("daysLimit", config.days_limit());
        props.put("batchSize", config.batch_size());

        jobManager.addJob(ArticleArchiveJobConsumer.JOB_TOPIC, props);
    }
}