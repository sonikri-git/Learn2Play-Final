package com.learn2play.backend.dashboard;

import com.learn2play.backend.dto.ReviewResponse;
import com.learn2play.backend.service.ReviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(
            ReviewService reviewService
    ) {

        this.reviewService = reviewService;

    }

    @GetMapping("/{attemptId}")
    public ReviewResponse getReview(

            @PathVariable String attemptId

    ) {

        return reviewService.getReview(

                attemptId

        );

    }

}