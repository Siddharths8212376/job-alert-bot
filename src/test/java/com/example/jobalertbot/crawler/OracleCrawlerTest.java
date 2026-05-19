package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OracleCrawlerTest {

    @Test
    void testOracleCrawlerLiveApi() {
        // Setup
        OracleCrawler crawler = new OracleCrawler();
        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setLocations(List.of("India"));

        // Execute
        System.out.println("Starting live API call to Oracle Jobs...");
        List<JobPosting> results = crawler.fetchJobs(criteria);

        // Print results to console
        System.out.println("--- ORACLE JOB SEARCH RESULTS ---");
        System.out.println("Total Jobs Found: " + results.size());
        results.forEach(job -> {
            System.out.println(job);
            System.out.println("URL: " + job.getUrl());
            System.out.println("---------------------------------");
        });

        // Verification
        assertNotNull(results, "The result list should not be null");
        assertFalse(results.isEmpty(), "The crawler should have found at least one job for common keywords");
    }
}