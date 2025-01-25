package com.to.controller;

import com.to.service.ActionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/revert")
    @Operation(
            summary = "Revert an action",
            description = "Reverts an action based on the provided log ID in the request body."
    )
    public void revertAction(@RequestBody Map<String, String> request) {
        String logId = request.get("id");
        try {
            actionLogService.revertAction(logId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to revert action: " + e.getMessage(), e);
        }
    }

}
