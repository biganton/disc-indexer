package com.to.controller;

import com.to.service.ActionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
@Tag(name = "Log Operations", description = "Endpoints for managing logs")
public class LogController {
    private final ActionLogService actionLogService;

    public LogController(ActionLogService actionLogService) {
        this.actionLogService = actionLogService;
    }

    @GetMapping("/all")
    @Operation(
            summary = "Get all logs",
            description = "Returns a list of all logs stored in the database."
    )
    public List<?> getAllLogs() {
        return actionLogService.getAllActionLogs();
    }

}
