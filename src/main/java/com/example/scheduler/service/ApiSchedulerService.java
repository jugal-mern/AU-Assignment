package com.example.scheduler.service;

import com.example.scheduler.entity.ApiAuditLog;
import com.example.scheduler.entity.ApiSchedule;
import com.example.scheduler.repository.ApiAuditLogRepository;
import com.example.scheduler.repository.ApiScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class ApiSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(ApiSchedulerService.class);

    @Autowired
    private ApiScheduleRepository apiScheduleRepository;
    @Autowired
    private ApiAuditLogRepository apiAuditLogRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Initialize and schedule all APIs at startup
        List<ApiSchedule> allApis = apiScheduleRepository.findAll();
        allApis.forEach(this::scheduleApi);
    }

    @Cacheable(value = "apiSchedules", key = "#apiSchedule.id")
    public void scheduleApi(ApiSchedule apiSchedule) {
        long initialDelay = calculateInitialDelay(apiSchedule);
        long interval = apiSchedule.getScheduleInterval() * 1000L;

        // Create a Date for the initial delay by adding the delay in milliseconds to the current time
        Date startTime = new Date(System.currentTimeMillis() + initialDelay);

        // Schedule the task with a fixed rate
        ScheduledFuture<?> scheduledTask = taskScheduler.scheduleAtFixedRate(() -> executeApi(apiSchedule), startTime, interval);
        scheduledTasks.put(apiSchedule.getId(), scheduledTask);
    }


    public void executeApi(ApiSchedule apiSchedule) {
        try {
            logger.info("Making request to API: {}, Method: {}", apiSchedule.getApiUrl(), apiSchedule.getHttpMethod());

            HttpEntity<String> entity = new HttpEntity<>(null);
            HttpMethod method = HttpMethod.valueOf(apiSchedule.getHttpMethod());

            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(apiSchedule.getApiUrl(), method, entity, String.class);
            long responseTime = System.currentTimeMillis() - startTime;

            logger.info("Response Status: {} and Response Time is: {}", response.getStatusCode(), responseTime);
            logExecution(apiSchedule, response.getBody(), true, responseTime, null);

            // Update next execution time
            updateNextExecutionTime(apiSchedule);
        } catch (RestClientException e) {
            logExecution(apiSchedule, null, false, null, e.getMessage());
            logger.error("RestClientException occurred: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage());
        }
    }

    private void logExecution(ApiSchedule apiSchedule, String body, boolean success, Long responseTime, String errorMessage) {
        ApiAuditLog auditLog = new ApiAuditLog();
        auditLog.setApiSchedule(apiSchedule);
        auditLog.setExecutionTime(new Timestamp(System.currentTimeMillis()));
        auditLog.setResponseMessage(success ? body : "Failed: " + errorMessage);
        auditLog.setSuccess(success);
        auditLog.setResponseTimeMillis(responseTime);

        apiAuditLogRepository.save(auditLog);

        apiSchedule.setLastExecutionTime(new Timestamp(System.currentTimeMillis()));
        apiScheduleRepository.save(apiSchedule);
    }

    private void updateNextExecutionTime(ApiSchedule apiSchedule) {
        long nextExecution = System.currentTimeMillis() + apiSchedule.getScheduleInterval() * 1000L;
        apiSchedule.setNextExecutionTime(new Timestamp(nextExecution));
        apiScheduleRepository.save(apiSchedule);
    }

    private long calculateInitialDelay(ApiSchedule apiSchedule) {
        long now = System.currentTimeMillis();
        Timestamp lastExecution = apiSchedule.getLastExecutionTime();
        return lastExecution != null ? Math.max(0, lastExecution.getTime() + apiSchedule.getScheduleInterval() * 1000L - now) : 0;
    }

    @CachePut(value = "apiSchedules", key = "#apiSchedule.id")
    public ApiSchedule addApi(ApiSchedule apiSchedule) {
        ApiSchedule addedSchedule = apiScheduleRepository.save(apiSchedule);
        scheduleApi(addedSchedule);
        logger.info("Scheduled API: {} to run every {} seconds", apiSchedule.getApiUrl(), apiSchedule.getScheduleInterval());
        return addedSchedule;
    }

    @CachePut(value = "apiSchedules", key = "#updatedApi.id")
    public Optional<ApiSchedule> updateApi(Long id, ApiSchedule updatedApi) {
        ApiSchedule existingApi = apiScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API not found"));
        existingApi.setApiUrl(updatedApi.getApiUrl());
        existingApi.setHttpMethod(updatedApi.getHttpMethod());
        existingApi.setScheduleInterval(updatedApi.getScheduleInterval());
        apiScheduleRepository.save(existingApi);

        // Cancel and reschedule updated API
        cancelScheduledTask(id);
        scheduleApi(existingApi);
        return Optional.of(existingApi);
    }

    @CacheEvict(value = "apiSchedules", key = "#id")
    public void deleteApi(Long id) {
        apiScheduleRepository.deleteById(id);
        cancelScheduledTask(id);
    }

    private void cancelScheduledTask(Long id) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.get(id);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTasks.remove(id);
        }
    }

    public List<ApiSchedule> getAllScheduledApis() {
        return apiScheduleRepository.findAll();
    }
}
