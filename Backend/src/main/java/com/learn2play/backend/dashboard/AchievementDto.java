package com.learn2play.backend.dashboard;

public class AchievementDto {

    private String icon;

    private String title;

    private String description;

    private boolean unlocked;

    public AchievementDto() {
    }

    public AchievementDto(
            String icon,
            String title,
            String description,
            boolean unlocked
    ) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.unlocked = unlocked;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

}