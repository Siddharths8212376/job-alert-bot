package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.http.HttpRange;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AvalaraCrawler implements CompanyCrawler {
    /**
     * URL: https://api.careerpuck.com/v1/public/job-boards/avalara
     * Avalara doesn't have a lot of openings, and their rest api is not offering any filters (as far as I can see)
     * So fetching all data (100 records?) and filtering done after -
     * Location filter: India
     * Team Filter: Engineering / Information Technology
     * @returns relavant jobs list
     */
    private final String URL = "https://api.careerpuck.com/v1/public/job-boards/avalara";
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Override
    public String getCompanyName() {
        return "Avalara";
    }

    @Override
    public List<JobPosting> fetchJobs(JobSearchCriteria criteria) {
        List<JobPosting> postings = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .GET()
                .build();
        try {
            HttpResponse<String>  response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                JsonNode jobs = root.path("jobs");
                if (jobs.isArray() && jobs.size() > 0) {
                   for (JsonNode job: jobs)  {
                       if (job.path("location").asString().contains("India") && (job.path("team").asString().contains("Engineering") || job.path("team").asString().contains("Information"))) {
                           JobPosting posting = mapToJobPosting(job);
                           postings.add(posting);
                       }
                   }
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        } catch (InterruptedException e) {
            System.out.println("RuntimeException: " + e);
        }
        return postings;
    }
    private JobPosting mapToJobPosting(JsonNode node) {
        JobPosting job = new JobPosting();
        job.setTitle(node.path("title").asString());
        job.setCompany(getCompanyName());
        job.setLocation(node.path("location").asString());
        job.setExternalJobId(node.path("permalink").asString());
        job.setDescription(node.path("content").asString());;
        job.setUrl(node.path("applyUrl").asString());
        return job;
    }

    public static void main(String[] args) {
        AvalaraCrawler crawler = new AvalaraCrawler();
        crawler.fetchJobs(new JobSearchCriteria());
    }
}
