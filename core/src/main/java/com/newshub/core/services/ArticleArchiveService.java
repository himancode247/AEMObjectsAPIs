package com.newshub.core.services;

import org.apache.sling.api.resource.ResourceResolver;

public interface ArticleArchiveService {

    boolean archiveArticle(String articlePath);


    boolean archiveArticle(String articlePath, ResourceResolver resolver);
}