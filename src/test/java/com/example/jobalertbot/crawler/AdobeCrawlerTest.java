package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AdobeCrawlerTest {

    @Test
    void testAdobeCrawlerLiveApi() {
        // Setup
        AdobeCrawler crawler = new AdobeCrawler();
        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setLocations(List.of("India"));

        // Execute
        System.out.println("Starting live POST request to Adobe Careers...");
        List<JobPosting> results = crawler.fetchJobs(criteria);

        // Print results
        System.out.println("--- ADOBE SEARCH RESULTS ---");
        System.out.println("Jobs found: " + results.size());
        results.forEach(job -> {
            System.out.println("Title: " + job.getTitle() + " | Location: " + job.getLocation());
            System.out.println("URL: " + job.getUrl());
            System.out.println("---------------------------------");
        });

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find at least one Adobe job for 'Software Engineer' in India");
    }
}