package com.example.scheduler.controller;

import com.example.scheduler.entity.ApiSchedule;
import com.example.scheduler.exception.ResourceNotFoundException;
import com.example.scheduler.service.ApiSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
public class ApiSchedulerController {

    @Autowired
    private ApiSchedulerService apiSchedulerService;

    @PostMapping
    public ResponseEntity<ApiSchedule> addApi(@Valid @RequestBody ApiSchedule apiSchedule) {
        ApiSchedule createdApi = apiSchedulerService.addApi(apiSchedule);
        return new ResponseEntity<>(createdApi, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ApiSchedule>> getAllScheduledApis() {
        List<ApiSchedule> apis = apiSchedulerService.getAllScheduledApis();
        return new ResponseEntity<>(apis, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSchedule> updateApi(@PathVariable Long id, @Valid @RequestBody ApiSchedule apiSchedule) {
        ApiSchedule updatedApi = apiSchedulerService.updateApi(id, apiSchedule)
                .orElseThrow(() -> new ResourceNotFoundException("API Schedule with id " + id + " not found"));
        return ResponseEntity.ok(updatedApi);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApi(@PathVariable Long id) {
        apiSchedulerService.deleteApi(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

