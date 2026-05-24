package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GSCrawlerTest {

    private GSCrawler gsCrawler;

    @BeforeEach
    void setUp() {
        gsCrawler = new GSCrawler();
    }

    @Test
    void getCompanyName_shouldReturnGoldmanSachs() {
        assertEquals("Goldman Sachs", gsCrawler.getCompanyName());
    }

    @Test
    void fetchJobs_shouldReturnNonEmptyList() {
        // Given
        JobSearchCriteria criteria = new JobSearchCriteria(); // Criteria won't affect the hardcoded request body

        // When
        List<JobPosting> jobPostings = gsCrawler.fetchJobs(criteria);

        // Then
        assertNotNull(jobPostings);
        assertFalse(jobPostings.isEmpty(), "Expected job postings to be fetched, but the list was empty. Check API connectivity or response structure.");

        // Optional: Check some properties of the first job posting
        if (!jobPostings.isEmpty()) {
            JobPosting firstJob = jobPostings.get(0);
            assertNotNull(firstJob.getExternalJobId());
            assertFalse(firstJob.getExternalJobId().isEmpty());
            assertEquals("Goldman Sachs", firstJob.getCompany());
            assertNotNull(firstJob.getTitle());
            assertFalse(firstJob.getTitle().isEmpty());
            assertNotNull(firstJob.getLocation());
            assertFalse(firstJob.getLocation().isEmpty());
            assertNotNull(firstJob.getUrl());
            assertFalse(firstJob.getUrl().isEmpty());
            assertTrue(firstJob.getUrl().startsWith("https://www.goldmansachs.com/careers/students/our-programs/job-detail.html?id="));
            assertNotNull(firstJob.getPostedDate());
            assertNotNull(firstJob.getCreatedAt());
        }
    }

    @Test
    void fetchJobs_shouldHandleApiErrorsGracefully() {
        // This test is designed to catch unhandled exceptions during API calls.
        // It cannot truly simulate API errors without mocking HttpClient.
        // If the API is unreachable or returns an error, fetchJobs should return an empty list.
        // This test will pass if the API is up and returns jobs, or if it's down and returns empty,
        // as long as no unhandled exceptions are thrown.

        // Given
        JobSearchCriteria criteria = new JobSearchCriteria();

        // When
        List<JobPosting> jobPostings = gsCrawler.fetchJobs(criteria);
        jobPostings.forEach(System.out::println);
        // Then
        assertNotNull(jobPostings); // Should always return a list, even if empty
    }
}
