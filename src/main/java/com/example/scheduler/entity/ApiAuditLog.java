package com.example.scheduler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "api_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "api_schedule_id")
    private ApiSchedule apiSchedule;

    private boolean success;
    @Lob
    private String responseMessage;
    private Timestamp executionTime;
    private Long responseTimeMillis;
}
