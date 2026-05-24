package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
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

public class AtlassianCrawler implements CompanyCrawler {
    /**
     * URL: https://www.atlassian.com/endpoint/careers/listings
     * Atlassian doesn't have a lot of openings, and their rest api is not offering any filters (as far as I can see)
     * so fetching all records (~120) and filtering done after
     * location filter : India (Very few roles)
     * @return relavant jobs list
     */
    private final String URL = "https://www.atlassian.com/endpoint/careers/listings";
    private final JsonMapper mapper = JsonMapper.builder().build();
    @Override
    public String getCompanyName() {
        return "Atlassian";
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
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode jobs = mapper.readTree(response.body());
                if (jobs.isArray() && jobs.size() > 0) {
                    for (JsonNode job: jobs)  {
                        JsonNode locations = job.path("locations");
                        boolean isLocIndia = false;
                        if (locations.isArray()) {
                            for (JsonNode location: locations) {
                                if (location.asString().contains("India")) isLocIndia = true;
                            }
                        }
                        if (isLocIndia) {
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
        job.setLocation(node.path("locations").get(0).asString());
        job.setExternalJobId(node.path("id").asString());
        job.setDescription(node.path("overview").asString());;
        job.setUrl(node.path("portalJobPost").path("portalUrl").asString());
        return job;
    }
}
