package com.example.jobalertbot.crawler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OracleCrawler extends OracleHCMCrawler {
    public OracleCrawler(@Value("${app.oracle.base-api-url}") String baseAPIUrl, @Value("${app.oracle.job-detail-base}") String jobDetailBase, @Value("${app.oracle.company-name}") String companyName, @Value("${app.oracle.selected-category-facet}") String selectedCategoryFacet, @Value("${app.oracle.site-number}") String siteNumber) {
        super(baseAPIUrl, jobDetailBase, companyName, selectedCategoryFacet, siteNumber);
    }
}
