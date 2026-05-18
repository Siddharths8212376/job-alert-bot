package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AmazonCrawlerTest {

    @Test
    void testAmazonCrawlerLiveApi() {
        // Setup
        AmazonCrawler crawler = new AmazonCrawler();
        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setExcludedKeywords(List.of("Director", "Staff", "Principal", "Embedded"));
        criteria.setLocations(List.of("IN", "Bengaluru"));
        
        // Execute
        System.out.println("Starting live API call to Amazon Jobs...");
        List<JobPosting> results = crawler.fetchJobs(criteria);

        // Print results to console
        System.out.println("--- AMZON JOB SEARCH RESULTS ---");
        System.out.println("Total Jobs Found: " + results.size());
        results.forEach(job -> {
            System.out.println(String.format("Title: %s | Location: %s", job.getTitle(), job.getLocation()));
            System.out.println("URL: " + job.getUrl());
            System.out.println("---------------------------------");
        });

        // Verification
        assertNotNull(results, "The result list should not be null");
        assertFalse(results.isEmpty(), "The crawler should have found at least one job");
    }
}