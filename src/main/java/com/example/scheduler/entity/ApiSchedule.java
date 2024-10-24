package com.example.scheduler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
@Table(name = "api_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "API URL cannot be null")
    private String apiUrl;
    @NotNull(message = "HTTP Method cannot be null")
    private String httpMethod;
    @Min(value = 1, message = "Schedule interval must be at least 1 second")
    private Long scheduleInterval;
    private Timestamp lastExecutionTime;
    private Timestamp nextExecutionTime;


}
