package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AtlassianCrawlerTest {
    @Test
    void testAtlassianCrawlerLiveApi() {
        AtlassianCrawler crawler = new AtlassianCrawler();
        System.out.println("Starting live GET request to Atlassian Careers...");
        List<JobPosting> results = crawler.fetchJobs(new JobSearchCriteria());
        System.out.println("--- Atlassian SEARCH RESULTS ---");
        System.out.println("Jobs found: " + results.size());
        results.forEach(System.out::println);
        assertNotNull(results);
    }
}
