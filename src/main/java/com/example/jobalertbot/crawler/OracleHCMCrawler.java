package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public abstract class OracleHCMCrawler implements CompanyCrawler {

    private String baseAPIUrl;
    private String jobDetailBase;
    private String companyName;
    private String selectedCategoryFacet;
    private String siteNumber;
    private static final int PAGE_SIZE = 25;
    private static final int MAX_RESULTS = 100;
    private static final long POLITE_DELAY_MS = 1000;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * URL: https://eeho.fa.us2.oraclecloud.com/hcmRestApi/resources/latest/recruitingCEJobRequisitions
     * @return job list
     */
    public OracleHCMCrawler(String baseAPIUrl, String jobDetailBase, String companyName, String selectedCategoryFacet, String siteNumber) {
        this.companyName = companyName;
        this.baseAPIUrl = baseAPIUrl;
        this.jobDetailBase = jobDetailBase;
        this.selectedCategoryFacet = selectedCategoryFacet;
        this.siteNumber = siteNumber;
    }
    @Override
    public String getCompanyName() {
        return this.companyName;
    }

    @Override
    public List<JobPosting> fetchJobs(JobSearchCriteria criteria) {
        List<JobPosting> allJobs = new ArrayList<>();
        Set<String> processedIds = new HashSet<>();
        boolean firstRequest = true;

        try {
            for (String location : criteria.getLocations()) {
                int offset = 0;
                boolean hasMoreInLocation = true;

                while (hasMoreInLocation && allJobs.size() < MAX_RESULTS) {
                    if (!firstRequest) {
                        TimeUnit.MILLISECONDS.sleep(POLITE_DELAY_MS);
                    }
                    firstRequest = false;

                    // Oracle HCM REST uses a semicolon-separated finder parameter
                    // here selectedCategoriesFacet is the category for Product Development (associated facet has to be selected)
                    // there is no keyword search, and the keyword search oracle offers sux
                    String finderValue = String.format("findReqs;siteNumber=%s,location=%s,selectedCategoriesFacet=%s,limit=%d,offset=%d,sortBy=POSTING_DATES_DESC",
                             siteNumber, location, selectedCategoryFacet, PAGE_SIZE, offset);

                    String url = String.format("%s?onlyData=true&expand=requisitionList.workLocation&finder=%s",
                            baseAPIUrl, URLEncoder.encode(finderValue, StandardCharsets.UTF_8));

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Accept", "application/json")
                            .GET()
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        JsonNode root = objectMapper.readTree(response.body());
                        // Oracle returns results inside items[0].requisitionList
                        JsonNode items = root.path("items");
                        if (items.isArray() && items.size() > 0) {
                            JsonNode reqList = items.get(0).path("requisitionList");
                            if (reqList.isArray() && reqList.size() > 0) {
                                for (JsonNode node : reqList) {
                                    JobPosting job = mapToJobPosting(node);
                                    String jobId = node.path("Id").asText();
                                    
                                    if (!processedIds.contains(jobId) && allJobs.size() < MAX_RESULTS) {
                                        allJobs.add(job);
                                        processedIds.add(jobId);
                                    }
                                }
                                offset += PAGE_SIZE;
                            } else {
                                hasMoreInLocation = false;
                            }
                        } else {
                            hasMoreInLocation = false;
                        }
                    } else {
                        hasMoreInLocation = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allJobs;
    }

    private JobPosting mapToJobPosting(JsonNode node) {
        JobPosting job = new JobPosting();
        job.setTitle(node.path("Title").asText());
        job.setCompany(getCompanyName());
        job.setLocation(node.path("PrimaryLocation").asText());
        job.setExternalJobId(node.path("Id").asText());
        job.setUrl(jobDetailBase + node.path("Id").asText());
        return job;
    }
}
