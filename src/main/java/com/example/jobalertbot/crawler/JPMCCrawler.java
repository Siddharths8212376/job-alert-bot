package com.example.jobalertbot.crawler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JPMCCrawler extends OracleHCMCrawler {
    public JPMCCrawler(@Value("${app.jpmc.base-api-url}") String baseAPIUrl, @Value("${app.jpmc.job-detail-base}") String jobDetailBase, @Value("${app.jpmc.company-name}") String companyName,  @Value("${app.jpmc.selected-category-facet}") String selectedCategoryFacet, @Value("${app.jpmc.site-number}") String siteNumber) {
        super(baseAPIUrl, jobDetailBase, companyName, selectedCategoryFacet, siteNumber);
    }
}
