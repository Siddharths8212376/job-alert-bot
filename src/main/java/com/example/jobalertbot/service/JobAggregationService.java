package com.example.jobalertbot.service;

import com.example.jobalertbot.crawler.CompanyCrawler;
import com.example.jobalertbot.model.JobNotification;
import com.example.jobalertbot.model.JobPosting;
import com.example.jobalertbot.model.Subscriber;
import com.example.jobalertbot.model.SubscriberStatus;
import com.example.jobalertbot.model.UserCriteria;
import com.example.jobalertbot.repository.JobNotificationRepository;
import com.example.jobalertbot.repository.JobPostingRepository;
import com.example.jobalertbot.repository.SubscriberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JobAggregationService {

    private final JobPostingRepository jobPostingRepository;
    private final SubscriberRepository subscriberRepository;
    private final JobNotificationRepository jobNotificationRepository;
    private final JobFilterService filterService;
    private final EmailService emailService;
    private final List<CompanyCrawler> crawlers;

    public JobAggregationService(
            List<CompanyCrawler> crawlers,
            JobPostingRepository jobPostingRepository,
            SubscriberRepository subscriberRepository,
            JobNotificationRepository jobNotificationRepository,
            JobFilterService filterService,
            EmailService emailService
    ) {
        this.crawlers = crawlers;
        this.jobPostingRepository = jobPostingRepository;
        this.subscriberRepository = subscriberRepository;
        this.jobNotificationRepository = jobNotificationRepository;
        this.filterService = filterService;
        this.emailService = emailService;
    }

    @Transactional
    public void runJobSearch() {
        // Step 1: Crawl jobs and update the database
        crawlAndStoreJobs();

        // Step 2: Send matching jobs to subscribers
        notifySubscribers();
    }

    @Transactional
    public void resendActiveJobs() {
        notifySubscribers();
    }

    @Transactional
    public void notifySubscribers() {

        // 1. Load all ACTIVE subscribers
        List<Subscriber> activeSubscribers = subscriberRepository.findByStatus(SubscriberStatus.ACTIVE);

        if (activeSubscribers.isEmpty()) {
            System.out.println("No active subscribers found.");
            return;
        }

        // 2. Load all currently active jobs
        List<JobPosting> activeJobs = jobPostingRepository.findByActiveTrue();

        if (activeJobs.isEmpty()) {
            System.out.println("No active jobs found.");
            return;
        }

        // 3. Process each subscriber independently
        for (Subscriber subscriber : activeSubscribers) {

            UserCriteria userCriteria = subscriber.getCriteria();

            if (userCriteria == null) {
                continue;
            }

            // Convert UserCriteria -> JobSearchCriteria
            JobSearchCriteria criteria = convertToJobSearchCriteria(userCriteria);

            // Jobs that should be sent to this subscriber
            List<JobPosting> jobsToSend = new ArrayList<>();

            for (JobPosting job : activeJobs) {

                // 4. Apply subscriber-specific criteria
                boolean relevant = filterService.isRelevant(
                        job,
                        criteria,
                        userCriteria.getMinimumScore()
                );

                if (!relevant) {
                    continue;
                }

                // 5. Check if already sent to this subscriber
                boolean alreadySent =
                        jobNotificationRepository
                                .existsBySubscriberIdAndJobPostingId(
                                        subscriber.getId(),
                                        job.getId()
                                );

                if (alreadySent) {
                    continue;
                }

                jobsToSend.add(job);
            }

            // 6. Send email if there are matching unsent jobs
            if (!jobsToSend.isEmpty()) {

                emailService.sendJobAlert(
                        jobsToSend,
                        List.of(subscriber.getEmail())
                );

                // 7. Insert notification records
                List<JobNotification> notifications = new ArrayList<>();

                for (JobPosting job : jobsToSend) {
                    JobNotification notification = new JobNotification();
                    notification.setSubscriber(subscriber);
                    notification.setJobPosting(job);

                    notifications.add(notification);
                }

                jobNotificationRepository.saveAll(notifications);

                System.out.println(
                        "Sent " + jobsToSend.size()
                                + " jobs to " + subscriber.getEmail()
                );
            }
        }
    }

    private void crawlAndStoreJobs() {
        JobSearchCriteria criteria = new JobSearchCriteria();

        List<JobPosting> jobsToSave = new ArrayList<>();

        // Iterate through each company crawler (UiPath, Microsoft, etc.)
        for (CompanyCrawler crawler : crawlers) {

            // Fetch all jobs from this company
            List<JobPosting> fetchedJobs = crawler.fetchJobs(criteria);

            // If API returned nothing, skip this company
            if (fetchedJobs.isEmpty()) {
                continue;
            }

            // All jobs returned by this crawler belong to the same company
            String company = fetchedJobs.get(0).getCompany();

            // ------------------------------------------------------------------
            // Step 1: Mark all existing jobs for this company as inactive.
            // If they appear again in the latest crawl, we will set them active=true.
            // Jobs that no longer appear remain inactive (expired/closed jobs).
            // ------------------------------------------------------------------
            List<JobPosting> existingCompanyJobs = jobPostingRepository.findByCompany(company);

            for (JobPosting existingJob : existingCompanyJobs) {
                existingJob.setActive(false);
                jobsToSave.add(existingJob);
            }

            // ------------------------------------------------------------------
            // Step 2: Process every job returned by the company API.
            // ------------------------------------------------------------------
            for (JobPosting fetchedJob : fetchedJobs) {

                Optional<JobPosting> existingOptional =
                        jobPostingRepository.findByCompanyAndExternalJobId(
                                fetchedJob.getCompany(),
                                fetchedJob.getExternalJobId()
                        );

                if (existingOptional.isPresent()) {
                    // ----------------------------------------------------------
                    // Existing job found → update latest details and reactivate it
                    // ----------------------------------------------------------
                    JobPosting existingJob = existingOptional.get();

                    existingJob.setTitle(fetchedJob.getTitle());
                    existingJob.setLocation(fetchedJob.getLocation());
                    existingJob.setUrl(fetchedJob.getUrl());
                    existingJob.setDescription(fetchedJob.getDescription());
                    existingJob.setRelevanceScore(fetchedJob.getRelevanceScore());
                    existingJob.setActive(true);

                    jobsToSave.add(existingJob);

                } else {
                    // ----------------------------------------------------------
                    // New job → insert into database as active
                    // ----------------------------------------------------------
                    fetchedJob.setActive(true);
                    fetchedJob.setNotified(false); // optional; no longer critical
                    jobsToSave.add(fetchedJob);
                }
            }
        }

        // ----------------------------------------------------------------------
        // Step 3: Persist all inserts and updates in bulk.
        // ----------------------------------------------------------------------
        if (!jobsToSave.isEmpty()) {
            jobPostingRepository.saveAll(jobsToSave);
        }

        System.out.println("Crawl completed. Total jobs inserted/updated: "
                + jobsToSave.size());
    }

    private JobSearchCriteria convertToJobSearchCriteria(UserCriteria userCriteria) {
        JobSearchCriteria criteria = new JobSearchCriteria();

        criteria.setTitles(split(userCriteria.getTitles()));
        criteria.setLocations(split(userCriteria.getLocations()));
        criteria.setMinExperience(userCriteria.getMinExperience());
        criteria.setMaxExperience(userCriteria.getMaxExperience());
        criteria.setRequiredSkills(split(userCriteria.getRequiredSkills()));
        criteria.setExcludedKeywords(split(userCriteria.getExcludedKeywords()));

        return criteria;
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}