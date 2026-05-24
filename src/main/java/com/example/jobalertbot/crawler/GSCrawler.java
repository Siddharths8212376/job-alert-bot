package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GSCrawler implements CompanyCrawler {

    private static final String API_URL = "https://api-higher.gs.com/gateway/api/v1/graphql";
    private static final String COMPANY_NAME = "Goldman Sachs";
    private static final String JOB_DETAIL_BASE_URL = "https://www.goldmansachs.com/careers/students/our-programs/job-detail.html?id=";

    // The exact raw JSON body from your curl command, with pageSize changed to 100
    private static final String RAW_REQUEST_BODY = "{\n" +
            "    \"operationName\":\"GetRoles\",\n" +
            "    \"variables\":{\n" +
            "      \"searchQueryInput\":{\n" +
            "        \"page\":{\n" +
            "          \"pageSize\":100,\n" +
            "          \"pageNumber\":0\n" +
            "        },\n" +
            "        \"sort\":{\n" +
            "          \"sortStrategy\":\"RELEVANCE\",\n" +
            "          \"sortOrder\":\"DESC\"\n" +
            "        },\n" +
            "        \"filters\":[\n" +
            "          {\n" +
            "            \"filterCategoryType\":\"EXPERIENCE_LEVEL\",\n" +
            "            \"filters\":[\n" +
            "              {\n" +
            "                \"filter\":\"Associate\",\n" +
            "                \"subFilters\":[]\n" +
            "              }\n" +
            "            ]\n" +
            "          },\n" +
            "          {\n" +
            "            \"filterCategoryType\":\"JOB_FUNCTION\",\n" +
            "            \"filters\":[\n" +
            "              {\n" +
            "                \"filter\":\"Software Engineering\",\n" +
            "                \"subFilters\":[]\n" +
            "              }\n" +
            "            ]\n" +
            "          },\n" +
            "          {\n" +
            "            \"filterCategoryType\":\"LOCATION\",\n" +
            "            \"filters\":[\n" +
            "              {\n" +
            "                \"filter\":\"India\",\n" +
            "                \"subFilters\":[\n" +
            "                  {\n" +
            "                    \"filter\":\"Karnataka\",\n" +
            "                    \"subFilters\":[\n" +
            "                      {\n" +
            "                        \"filter\":\"Bengaluru\",\n" +
            "                        \"subFilters\":[]\n" +
            "                      }\n" +
            "                    ]\n" +
            "                  },\n" +
            "                  {\n" +
            "                    \"filter\":\"Maharashtra\",\n" +
            "                    \"subFilters\":[\n" +
            "                      {\n" +
            "                        \"filter\":\"Mumbai\",\n" +
            "                        \"subFilters\":[]\n" +
            "                      }\n" +
            "                    ]\n" +
            "                  },\n" +
            "                  {\n" +
            "                    \"filter\":\"Telangana\",\n" +
            "                    \"subFilters\":[\n" +
            "                      {\n" +
            "                        \"filter\":\"Hyderabad\",\n" +
            "                        \"subFilters\":[]\n" +
            "                      }\n" +
            "                    ]\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        ],\n" +
            "        \"experiences\":[\n" +
            "          \"EARLY_CAREER\",\n" +
            "          \"PROFESSIONAL\"\n" +
            "        ],\n" +
            "        \"searchTerm\":\"\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"query\":\"query GetRoles($searchQueryInput: RoleSearchQueryInput!) { roleSearch(searchQueryInput: $searchQueryInput) { totalCount items { roleId corporateTitle jobTitle jobFunction locations { primary state country city __typename } status division skills jobType { code description __typename } externalSource { sourceId __typename } __typename } __typename } }\"\n" +
            "  }";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GSCrawler() {
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getCompanyName() {
        return COMPANY_NAME;
    }

    @Override
    public List<JobPosting> fetchJobs(JobSearchCriteria criteria) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(RAW_REQUEST_BODY))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                System.err.println("Failed to fetch jobs from GS API. Status code: " + response.statusCode() + ", Body: " + response.body());
                return Collections.emptyList();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error fetching jobs from GS API: " + e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    private List<JobPosting> parseResponse(String responseBody) throws IOException {
        List<JobPosting> jobPostings = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(responseBody);

        JsonNode itemsNode = rootNode.path("data").path("roleSearch").path("items");

        if (itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                JobPosting jobPosting = new JobPosting();
                jobPosting.setCompany(COMPANY_NAME);
                jobPosting.setExternalJobId(itemNode.path("roleId").asText());
                jobPosting.setTitle(String.format("%s - %s",
                        itemNode.path("corporateTitle").asText(),
                        itemNode.path("jobTitle").asText()));

                // Extract primary location
                String location = "N/A";
                JsonNode locationsNode = itemNode.path("locations");
                if (locationsNode.isArray()) {
                    for (JsonNode locNode : locationsNode) {
                        if (locNode.path("primary").asBoolean()) {
                            location = String.format("%s, %s, %s",
                                    locNode.path("city").asText(),
                                    locNode.path("state").asText(),
                                    locNode.path("country").asText());
                            break;
                        }
                    }
                }
                jobPosting.setLocation(location);

                // Construct a basic description
                String description = String.format("Corporate Title: %s, Job Title: %s, Function: %s, Division: %s",
                        itemNode.path("corporateTitle").asText(),
                        itemNode.path("jobTitle").asText(),
                        itemNode.path("jobFunction").asText(),
                        itemNode.path("division").asText());
                jobPosting.setDescription(description);

                jobPosting.setUrl(JOB_DETAIL_BASE_URL + itemNode.path("roleId").asText());
                jobPosting.setPostedDate(LocalDate.now()); // API response doesn't have posted date, using current date
                jobPosting.setCreatedAt(LocalDateTime.now());
                jobPosting.setActive(true);
                jobPosting.setNotified(false);

                jobPostings.add(jobPosting);
            }
        }
        return jobPostings;
    }
}
