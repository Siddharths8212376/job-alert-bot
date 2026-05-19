package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JPMCCrawlerTest {

    @Test
    void testJPMCCrawlerLiveApi() {
        // Setup
        JPMCCrawler crawler = new JPMCCrawler("https://jpmc.fa.oraclecloud.com/hcmRestApi/resources/latest/recruitingCEJobRequisitions","https://jpmc.fa.oraclecloud.com/hcmUI/CandidateExperience/en/sites/CX_1001/job/","JPMC","300000086152753", "CX_1001");

        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setLocations(List.of("India"));

        // Execute
        System.out.println("Starting live API call to JPMC Jobs...");

        List<JobPosting> results = crawler.fetchJobs(criteria);

        // Print results to console
        System.out.println("--- JPMC JOB SEARCH RESULTS ---");
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