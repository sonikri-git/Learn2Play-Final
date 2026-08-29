package com.learn2play.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class QuizAttemptRequest {
    private String userEmail;
    private List<AnswerSubmission> answers = new ArrayList<>();

    // =====================================
    // TIMED QUIZ MODE (ANALYTICS)
    // Sent by the frontend when the quiz was taken in Exam Mode.
    // timedMode: whether the timer was active for this attempt.
    // timeTakenSeconds: how long the user actually spent (works for
    //   both practice and exam mode).
    // remainingSeconds: time left on the clock at submission time
    //   (only meaningful when timedMode is true; 0 on auto-submit).
    // =====================================
    private Boolean timedMode;
    private Integer timeTakenSeconds;
    private Integer remainingSeconds;

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<AnswerSubmission> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerSubmission> answers) {
        this.answers = answers;
    }

    public Boolean getTimedMode() {
        return timedMode;
    }

    public void setTimedMode(Boolean timedMode) {
        this.timedMode = timedMode;
    }

    public Integer getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(Integer timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public Integer getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Integer remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}
