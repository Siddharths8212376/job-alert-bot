package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class AdobeCrawler implements CompanyCrawler {

    private static final String API_URL = "https://careers.adobe.com/widgets";
    private static final int PAGE_SIZE = 10;
    private static final int MAX_RESULTS = 50;
    private static final long POLITE_DELAY_MS = 1000;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getCompanyName() {
        return "Adobe";
    }

    @Override
    public List<JobPosting> fetchJobs(JobSearchCriteria criteria) {
        List<JobPosting> allJobs = new ArrayList<>();
        Set<String> processedJobIds = new HashSet<>();
        boolean firstRequest = true;

        try {
            for (String location : criteria.getLocations()) {
                int offset = 0;
                boolean hasMore = true;

                while (hasMore && allJobs.size() < MAX_RESULTS) {
                    if (!firstRequest) {
                        TimeUnit.MILLISECONDS.sleep(POLITE_DELAY_MS);
                    }
                    firstRequest = false;

                    String payload = buildPayload(location, offset);

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(API_URL))
                            .header("Content-Type", "application/json")
                            .header("Accept", "*/*")
                            .POST(HttpRequest.BodyPublishers.ofString(payload))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        JsonNode root = objectMapper.readTree(response.body());
                        // Adobe's widget response puts data under the ddoKey provided in request
                        JsonNode jobsNode = root.path("refineSearch").path("data").path("jobs");

                        if (jobsNode.isArray() && jobsNode.size() > 0) {
                            for (JsonNode node : jobsNode) {
                                String jobId = node.path("jobId").asText();
                                if (!processedJobIds.contains(jobId) && allJobs.size() < MAX_RESULTS) {
                                    allJobs.add(mapToJobPosting(node));
                                    processedJobIds.add(jobId);
                                }
                            }
                            offset += PAGE_SIZE;
                        } else {
                            hasMore = false;
                        }
                    } else {
                        hasMore = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allJobs;
    }

    private String buildPayload(String location, int offset) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("lang", "en_us");
        payload.put("deviceType", "desktop");
        payload.put("country", "us");
        payload.put("pageName", "Engineering and Product jobs");
        payload.put("ddoKey", "refineSearch");
        payload.put("sortBy", "Most recent");
        payload.put("subsearch", "");
        payload.put("from", offset);
        payload.put("irs", false);
        payload.put("jobs", true);
        payload.put("counts", true);
        payload.put("all_fields", List.of("remote", "country", "state", "city", "experienceLevel", "category", "profession", "employmentType", "jobLevel"));
        payload.put("pageType", "category");
        payload.put("size", PAGE_SIZE);
        payload.put("clearAll", false);
        payload.put("jdsource", "facets");
        payload.put("isSliderEnable", false);
        payload.put("pageId", "page62-ds");
        payload.put("siteType", "external");
        payload.put("keywords", "");
        payload.put("global", true);

        Map<String, Object> selectedFields = new HashMap<>();
        selectedFields.put("category", List.of("Engineering and Product"));
        selectedFields.put("country", List.of(location));
        payload.put("selected_fields", selectedFields);

        Map<String, String> sort = new HashMap<>();
        sort.put("order", "desc");
        sort.put("field", "postedDate");
        payload.put("sort", sort);

        payload.put("locationData", new HashMap<>());

        return objectMapper.writeValueAsString(payload);
    }

    private JobPosting mapToJobPosting(JsonNode node) {
        JobPosting job = new JobPosting();
        job.setTitle(node.path("title").asText());
        job.setCompany(getCompanyName());
        job.setLocation(node.path("location").asText());
        job.setExternalJobId(node.path("jobId").asText());
        // Adobe's widget usually provides a full URL in 'jobSeqNo' or 'applyUrl', 
        // but standard Phenom path is usually careers.adobe.com/global/en/job/ID
        job.setUrl("https://careers.adobe.com/global/en/job/" + node.path("jobId").asText());
        return job;
    }
}
