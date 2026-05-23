package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AvalaraCrawlerTest {
    @Test
    void testAvalaraCrawlerLiveApi() {
        AvalaraCrawler crawler = new AvalaraCrawler();
        System.out.println("Starting live GET request to Avalara Careers...");
        List<JobPosting> results = crawler.fetchJobs(new JobSearchCriteria());
        System.out.println("--- Avalara SEARCH RESULTS ---");
        System.out.println("Jobs found: " + results.size());
        results.forEach(System.out::println);
        assertNotNull(results);
    }
}
