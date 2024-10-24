package com.example.scheduler.custom_scheduler;

import com.example.scheduler.entity.ApiSchedule;
import com.example.scheduler.repository.ApiAuditLogRepository;
import com.example.scheduler.repository.ApiScheduleRepository;
import com.example.scheduler.service.ApiSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ApiSchedulerServiceTest {
}
