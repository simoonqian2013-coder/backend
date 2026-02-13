package com.qss.pet.dto;

import java.util.List;

public class DashboardSummary {
    private Stats stats;
    private Reminders reminders;
    private Notice notice;
    private List<PendingItem> pending;
    private List<TrendItem> trend;
    private List<CityItem> cities;
    private List<ActivityItem> activity;
    private StatusOverview statusOverview;

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Reminders getReminders() {
        return reminders;
    }

    public void setReminders(Reminders reminders) {
        this.reminders = reminders;
    }

    public Notice getNotice() {
        return notice;
    }

    public void setNotice(Notice notice) {
        this.notice = notice;
    }

    public List<PendingItem> getPending() {
        return pending;
    }

    public void setPending(List<PendingItem> pending) {
        this.pending = pending;
    }

    public List<TrendItem> getTrend() {
        return trend;
    }

    public void setTrend(List<TrendItem> trend) {
        this.trend = trend;
    }

    public List<CityItem> getCities() {
        return cities;
    }

    public void setCities(List<CityItem> cities) {
        this.cities = cities;
    }

    public List<ActivityItem> getActivity() {
        return activity;
    }

    public void setActivity(List<ActivityItem> activity) {
        this.activity = activity;
    }

    public StatusOverview getStatusOverview() {
        return statusOverview;
    }

    public void setStatusOverview(StatusOverview statusOverview) {
        this.statusOverview = statusOverview;
    }

    public static class Stats {
        private long todayNewPets;
        private long pendingAdoptions;
        private long monthApprovedAdoptions;
        private int monthApprovalRate;
        private long totalPets;
        private long availablePets;

        public long getTodayNewPets() {
            return todayNewPets;
        }

        public void setTodayNewPets(long todayNewPets) {
            this.todayNewPets = todayNewPets;
        }

        public long getPendingAdoptions() {
            return pendingAdoptions;
        }

        public void setPendingAdoptions(long pendingAdoptions) {
            this.pendingAdoptions = pendingAdoptions;
        }

        public long getMonthApprovedAdoptions() {
            return monthApprovedAdoptions;
        }

        public void setMonthApprovedAdoptions(long monthApprovedAdoptions) {
            this.monthApprovedAdoptions = monthApprovedAdoptions;
        }

        public int getMonthApprovalRate() {
            return monthApprovalRate;
        }

        public void setMonthApprovalRate(int monthApprovalRate) {
            this.monthApprovalRate = monthApprovalRate;
        }

        public long getTotalPets() {
            return totalPets;
        }

        public void setTotalPets(long totalPets) {
            this.totalPets = totalPets;
        }

        public long getAvailablePets() {
            return availablePets;
        }

        public void setAvailablePets(long availablePets) {
            this.availablePets = availablePets;
        }
    }

    public static class PendingItem {
        private Long id;
        private String name;
        private String phone;
        private String pet;
        private String time;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPet() {
            return pet;
        }

        public void setPet(String pet) {
            this.pet = pet;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    public static class TrendItem {
        private String day;
        private long count;
        private int percent;

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }
    }

    public static class CityItem {
        private String name;
        private long count;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    public static class ActivityItem {
        private String title;
        private String time;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    public static class StatusOverview {
        private long adopted;
        private long available;
        private long unavailable;
        private long needInfo;

        public long getAdopted() {
            return adopted;
        }

        public void setAdopted(long adopted) {
            this.adopted = adopted;
        }

        public long getAvailable() {
            return available;
        }

        public void setAvailable(long available) {
            this.available = available;
        }

        public long getUnavailable() {
            return unavailable;
        }

        public void setUnavailable(long unavailable) {
            this.unavailable = unavailable;
        }

        public long getNeedInfo() {
            return needInfo;
        }

        public void setNeedInfo(long needInfo) {
            this.needInfo = needInfo;
        }
    }

    public static class Reminders {
        private long overduePending;
        private long missingMainImage;
        private String peakTime;

        public long getOverduePending() {
            return overduePending;
        }

        public void setOverduePending(long overduePending) {
            this.overduePending = overduePending;
        }

        public long getMissingMainImage() {
            return missingMainImage;
        }

        public void setMissingMainImage(long missingMainImage) {
            this.missingMainImage = missingMainImage;
        }

        public String getPeakTime() {
            return peakTime;
        }

        public void setPeakTime(String peakTime) {
            this.peakTime = peakTime;
        }
    }

    public static class Notice {
        private String content;
        private String updatedBy;
        private String updatedAt;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
