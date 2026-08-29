package com.learn2play.backend.dashboard;

import com.learn2play.backend.dashboard.DashboardResponse;
import com.learn2play.backend.dashboard.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{email}")
    public DashboardResponse getDashboard(

            @PathVariable String email

    ) {

        return dashboardService.getDashboard(email);

    }

}