package com.example.jobalertbot.crawler;

import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.service.JobSearchCriteria;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Component
public class AmazonCrawler implements CompanyCrawler {

    /**
     *
     * URL : https://amazon.jobs/en/search.json?radius=24km&facets%5B%5D=normalized_country_code&facets%5B%5D=normalized_state_name&facets%5B%5D=normalized_city_name&facets%5B%5D=location&facets%5B%5D=business_category&facets%5B%5D=category&facets%5B%5D=schedule_type_id&facets%5B%5D=employee_class&facets%5B%5D=normalized_location&facets%5B%5D=job_function_id&facets%5B%5D=is_manager&facets%5B%5D=is_intern&offset=0&result_limit=10&sort=relevant&latitude=&longitude=&loc_group_id=&loc_query=India&base_query=software%20engineer&city=&country=IND&region=&county=&query_options=&
     * @return
     */
    @Override
    public String getCompanyName() {
        return "Amazon";
    }

    private static final String BASE_API_URL = "https://amazon.jobs/en/search.json";
    private static final int PAGE_SIZE = 10; // Larger page size to reduce number of requests
    private static final int MAX_RESULTS = 30; // Total limit
    private static final long POLITE_DELAY_MS = 1000; // 1 second delay between requests

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<JobPosting> fetchJobs(JobSearchCriteria criteria) {
        List<JobPosting> allJobs = new ArrayList<>();
        boolean firstRequest = true;

        try {
            for (String location : criteria.getLocations()) {
                int offset = 0;
                boolean hasMoreInLocation = true;

                while (hasMoreInLocation && allJobs.size() < MAX_RESULTS) {
                    // Be polite: wait before making requests, except the very first one
                    if (!firstRequest) {
                        TimeUnit.MILLISECONDS.sleep(POLITE_DELAY_MS);
                    }
                    firstRequest = false;

                    String url = String.format("%s?offset=%d&result_limit=%d&loc_query=India&country=IND&base_query=%s&sort=recent",
                            BASE_API_URL,
                            offset,
                            PAGE_SIZE,
                            URLEncoder.encode("software engineer", StandardCharsets.UTF_8));
                    System.out.println("fetching from URL: " +  url);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Accept", "application/json")
                            .GET()
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        JsonNode root = objectMapper.readTree(response.body());
                        JsonNode jobsNode = root.path("jobs");

                        if (jobsNode.isArray() && jobsNode.size() > 0) {
                            for (JsonNode node : jobsNode) {
                                if (allJobs.size() < MAX_RESULTS) {
                                    allJobs.add(mapToJobPosting(node));
                                }
                            }
                            offset += PAGE_SIZE;
                        } else {
                            hasMoreInLocation = false;
                        }
                    } else {
                        hasMoreInLocation = false;
                    }
                }
            }
        } catch (Exception e) {
            // In a real scenario, use a logger
            e.printStackTrace();
        }

        return allJobs;
    }

    private JobPosting mapToJobPosting(JsonNode node) {
        JobPosting job = new JobPosting();
        job.setTitle(node.path("title").asText());
        job.setCompany(getCompanyName());
        job.setLocation(node.path("location").asText());
        job.setExternalJobId(node.path("id").asText());
        job.setPostedDate(LocalDate.parse(node.path("posted_date").asText().replaceAll("\\s+", " ").trim(), DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)));
        // Construct full URL from the job_path
        job.setUrl("https://amazon.jobs" + node.path("job_path").asText());
        System.out.println("mapped job: " + job);
        return job;
    }
}
