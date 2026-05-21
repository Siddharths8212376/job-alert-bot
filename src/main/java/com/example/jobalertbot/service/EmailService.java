package com.example.jobalertbot.service;

import com.example.jobalertbot.exception.MailgunRegistrationException;
import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.model.Subscriber;
import com.example.jobalertbot.model.SubscriberStatus;
import com.example.jobalertbot.repository.SubscriberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class EmailService {

    private final WebClient webClient;
    private final SubscriberRepository subscriberRepository;

    @Value("${mailgun.api-key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${mailgun.from}")
    private String from;

    public EmailService(WebClient webClient, SubscriberRepository subscriberRepository) {
        this.webClient = webClient;
        this.subscriberRepository = subscriberRepository;
    }

    // TODO: refactor below functions
    //  sendJobAlert(List<JobPosting> jobs),
    //  sendJobAlert(List<JobPosting> jobs, List<String> recipients) and
    //  sendEmail(List<JobPosting> jobs, String recipient)
    public void sendJobAlert(List<JobPosting> jobs) {
        String subject = "Job Alert Bot - New Matching Jobs (" + jobs.size() + ")";
        String html = buildHtml(jobs);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("from", from);

        List<Subscriber> activeSubscribers = subscriberRepository.findByStatus(SubscriberStatus.ACTIVE);

        for (Subscriber subscriber : activeSubscribers) {
            String recipient = subscriber.getEmail();
            form.add("to", recipient);
        }

        form.add("subject", subject);
        form.add("html", html);

        String auth = Base64.getEncoder()
                .encodeToString(("api:" + apiKey)
                        .getBytes(StandardCharsets.UTF_8));

        String response = webClient.post()
                .uri("https://api.mailgun.net/v3/" + domain + "/messages")
                .header("Authorization", "Basic " + auth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Email sent successfully. " + response);
    }

    public void sendJobAlert(List<JobPosting> jobs, List<String> recipients) {
        for (String recipient : recipients) {
            sendEmail(jobs, recipient);
        }
    }

    private void sendEmail(List<JobPosting> jobs, String recipient) {
        String subject = "Job Alert Bot - New Matching Jobs (" + jobs.size() + ")";
        String html = buildHtml(jobs);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("from", from);
        form.add("to", recipient);
        form.add("subject", subject);
        form.add("html", html);

        String auth = Base64.getEncoder()
                .encodeToString(("api:" + apiKey)
                        .getBytes(StandardCharsets.UTF_8));

        String response = webClient.post()
                .uri("https://api.mailgun.net/v3/" + domain + "/messages")
                .header("Authorization", "Basic " + auth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Email sent successfully. " + response);
    }

    private String buildHtml(List<JobPosting> jobs) {
        StringBuilder html = new StringBuilder();
        html.append("<h2>New Matching Jobs Found</h2>");
        html.append("<ul>");

        for (JobPosting job : jobs) {
            html.append("<li>")
                    .append("<b>").append(job.getTitle()).append("</b>")
                    .append(" - ").append(job.getCompany())
                    .append("<br/>")
                    .append(job.getLocation())
                    .append("<br/>")
                    .append("<a href='").append(job.getUrl()).append("'>Apply</a>")
                    .append("</li><br/>");
        }

        html.append("</ul>");
        return html.toString();
    }

    public void addSubscriberToMailgun(String email) throws IOException, InterruptedException {
        String auth = Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mailgun.net/v5/sandbox/auth_recipients?email=" + email))
                .header("Authorization", "Basic " + auth)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            log.info("Subscriber added to Mailgun sandbox successfully: {}", email);
            return;
        }

        log.error("Failed to add subscriber to Mailgun. Status: {}, Body: {}", response.statusCode(), response.body());
        throw new MailgunRegistrationException("Failed to add Subscriber to Mailgun.");
    }
}