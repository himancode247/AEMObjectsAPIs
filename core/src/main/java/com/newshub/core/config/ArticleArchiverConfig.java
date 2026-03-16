package com.newshub.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Newshub Article Archiver Configuration")
public @interface ArticleArchiverConfig {

    @AttributeDefinition(name = "Cron Expression")
    String scheduler_expression() default "0 0 3 * * ?";

    @AttributeDefinition(name = "News Root Path")
    String news_root() default "/content/newshub/en/news";

    @AttributeDefinition(name = "Archive After Days")
    int days_limit() default 365;

    @AttributeDefinition(name = "Batch Size")
    int batch_size() default 20;
}