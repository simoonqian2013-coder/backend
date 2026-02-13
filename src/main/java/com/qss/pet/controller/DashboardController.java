package com.qss.pet.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qss.pet.common.ApiResponse;
import com.qss.pet.dto.DashboardSummary;
import com.qss.pet.entity.Adoption;
import com.qss.pet.entity.Pet;
import com.qss.pet.mapper.AdoptionMapper;
import com.qss.pet.mapper.PetMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class DashboardController {
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;

    private static final int PET_STATUS_AVAILABLE = 1;
    private static final int PET_STATUS_ADOPTED = 2;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final AdoptionMapper adoptionMapper;
    private final PetMapper petMapper;

    public DashboardController(AdoptionMapper adoptionMapper, PetMapper petMapper) {
        this.adoptionMapper = adoptionMapper;
        this.petMapper = petMapper;
    }

    @GetMapping("/api/dashboard/summary")
    public ApiResponse<DashboardSummary> getSummary() {
        DashboardSummary summary = new DashboardSummary();
        summary.setStats(buildStats());
        summary.setPending(buildPending());
        summary.setReminders(buildReminders());
        summary.setNotice(buildNotice());
        summary.setTrend(buildTrend());
        summary.setCities(buildCities());
        summary.setActivity(buildActivity());
        summary.setStatusOverview(buildStatusOverview());
        return ApiResponse.ok(summary);
    }

    private DashboardSummary.Stats buildStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);

        long todayNewPets = petMapper.selectCount(
                Wrappers.lambdaQuery(Pet.class)
                        .ge(Pet::getCreatedAt, todayStart)
                        .lt(Pet::getCreatedAt, tomorrowStart)
        );
        long pendingAdoptions = adoptionMapper.selectCount(
                Wrappers.lambdaQuery(Adoption.class)
                        .eq(Adoption::getStatus, STATUS_PENDING)
        );
        long monthApproved = adoptionMapper.selectCount(
                Wrappers.lambdaQuery(Adoption.class)
                        .eq(Adoption::getStatus, STATUS_APPROVED)
                        .ge(Adoption::getCreatedAt, monthStart)
                        .lt(Adoption::getCreatedAt, nextMonthStart)
        );
        long monthRejected = adoptionMapper.selectCount(
                Wrappers.lambdaQuery(Adoption.class)
                        .eq(Adoption::getStatus, STATUS_REJECTED)
                        .ge(Adoption::getCreatedAt, monthStart)
                        .lt(Adoption::getCreatedAt, nextMonthStart)
        );
        long totalPets = petMapper.selectCount(Wrappers.lambdaQuery(Pet.class));
        long availablePets = petMapper.selectCount(
                Wrappers.lambdaQuery(Pet.class)
                        .eq(Pet::getStatus, PET_STATUS_AVAILABLE)
        );

        int approvalRate = 0;
        long reviewed = monthApproved + monthRejected;
        if (reviewed > 0) {
            approvalRate = (int) Math.round(monthApproved * 100.0 / reviewed);
        }

        DashboardSummary.Stats stats = new DashboardSummary.Stats();
        stats.setTodayNewPets(todayNewPets);
        stats.setPendingAdoptions(pendingAdoptions);
        stats.setMonthApprovedAdoptions(monthApproved);
        stats.setMonthApprovalRate(approvalRate);
        stats.setTotalPets(totalPets);
        stats.setAvailablePets(availablePets);
        return stats;
    }

    private List<DashboardSummary.PendingItem> buildPending() {
        List<Adoption> adoptions = adoptionMapper.selectList(
                Wrappers.lambdaQuery(Adoption.class)
                        .eq(Adoption::getStatus, STATUS_PENDING)
                        .orderByDesc(Adoption::getCreatedAt)
                        .last("limit 10")
        );
        Map<Long, Pet> petMap = loadPetMap(adoptions);
        return adoptions.stream()
                .map(adoption -> {
                    DashboardSummary.PendingItem item = new DashboardSummary.PendingItem();
                    item.setId(adoption.getId());
                    item.setName(adoption.getApplicantName());
                    item.setPhone(maskPhone(adoption.getPhone()));
                    Pet pet = petMap.get(adoption.getPetId());
                    item.setPet(pet == null ? "-" : pet.getNickname());
                    item.setTime(formatTime(adoption.getCreatedAt()));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private DashboardSummary.Reminders buildReminders() {
        LocalDateTime overdueTime = LocalDateTime.now().minusDays(7);
        long overduePending = adoptionMapper.selectCount(
                Wrappers.lambdaQuery(Adoption.class)
                        .eq(Adoption::getStatus, STATUS_PENDING)
                        .le(Adoption::getCreatedAt, overdueTime)
        );
        long missingMainImage = petMapper.selectCount(missingImageWrapper());

        DashboardSummary.Reminders reminders = new DashboardSummary.Reminders();
        reminders.setOverduePending(overduePending);
        reminders.setMissingMainImage(missingMainImage);
        reminders.setPeakTime("今日 14:00-17:00");
        return reminders;
    }

    private DashboardSummary.Notice buildNotice() {
        DashboardSummary.Notice notice = new DashboardSummary.Notice();
        notice.setContent("请每日 18:00 前完成当日申请审核，重要事项请在群内同步。");
        notice.setUpdatedBy("管理员");
        notice.setUpdatedAt(LocalDate.now().toString());
        return notice;
    }

    private List<DashboardSummary.TrendItem> buildTrend() {
        LocalDate today = LocalDate.now();
        LocalDate startDay = today.minusDays(6);
        LocalDateTime startTime = startDay.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay();

        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(created_at) AS day", "COUNT(*) AS count")
                .ge("created_at", startTime)
                .lt("created_at", endTime)
                .groupBy("DATE(created_at)");
        List<Map<String, Object>> rows = adoptionMapper.selectMaps(wrapper);
        Map<LocalDate, Long> dayCountMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object dayObj = row.get("day");
            Object countObj = row.get("count");
            if (dayObj != null && countObj != null) {
                LocalDate day = dayObj instanceof java.sql.Date
                        ? ((java.sql.Date) dayObj).toLocalDate()
                        : LocalDate.parse(dayObj.toString());
                dayCountMap.put(day, ((Number) countObj).longValue());
            }
        }
        List<DashboardSummary.TrendItem> items = new ArrayList<>();
        long max = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate day = startDay.plusDays(i);
            long count = dayCountMap.getOrDefault(day, 0L);
            max = Math.max(max, count);
            DashboardSummary.TrendItem item = new DashboardSummary.TrendItem();
            item.setDay(day.format(DATE_FORMATTER));
            item.setCount(count);
            items.add(item);
        }
        for (DashboardSummary.TrendItem item : items) {
            int percent = max > 0 ? (int) Math.round(item.getCount() * 100.0 / max) : 0;
            item.setPercent(percent);
        }
        return items;
    }

    private List<DashboardSummary.CityItem> buildCities() {
        QueryWrapper<Adoption> wrapper = new QueryWrapper<>();
        wrapper.select("city AS name", "COUNT(*) AS count")
                .isNotNull("city")
                .ne("city", "")
                .groupBy("city")
                .orderByDesc("count")
                .last("limit 5");
        List<Map<String, Object>> rows = adoptionMapper.selectMaps(wrapper);
        return rows.stream()
                .map(row -> {
                    DashboardSummary.CityItem item = new DashboardSummary.CityItem();
                    item.setName(row.get("name") == null ? "-" : row.get("name").toString());
                    item.setCount(((Number) row.get("count")).longValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<DashboardSummary.ActivityItem> buildActivity() {
        List<Pet> latestPets = petMapper.selectList(
                Wrappers.lambdaQuery(Pet.class)
                        .orderByDesc(Pet::getCreatedAt)
                        .last("limit 5")
        );
        List<Adoption> latestAdoptions = adoptionMapper.selectList(
                Wrappers.lambdaQuery(Adoption.class)
                        .orderByDesc(Adoption::getCreatedAt)
                        .last("limit 5")
        );

        Map<Long, Pet> petMap = loadPetMap(latestAdoptions);
        List<ActivityTemp> activity = new ArrayList<>();
        for (Pet pet : latestPets) {
            ActivityTemp temp = new ActivityTemp();
            temp.time = pet.getCreatedAt();
            String breed = pet.getBreed() == null ? "" : "（" + pet.getBreed() + "）";
            temp.title = "新增宠物 · " + pet.getNickname() + breed;
            activity.add(temp);
        }
        for (Adoption adoption : latestAdoptions) {
            ActivityTemp temp = new ActivityTemp();
            temp.time = adoption.getCreatedAt();
            Pet pet = petMap.get(adoption.getPetId());
            String petName = pet == null ? "-" : pet.getNickname();
            temp.title = "领养申请 · " + petName + " / " + adoption.getApplicantName();
            activity.add(temp);
        }

        return activity.stream()
                .sorted(Comparator.comparing((ActivityTemp a) -> a.time, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(6)
                .map(item -> {
                    DashboardSummary.ActivityItem result = new DashboardSummary.ActivityItem();
                    result.setTitle(item.title);
                    result.setTime(formatTime(item.time));
                    return result;
                })
                .collect(Collectors.toList());
    }

    private DashboardSummary.StatusOverview buildStatusOverview() {
        long adopted = petMapper.selectCount(
                Wrappers.lambdaQuery(Pet.class)
                        .eq(Pet::getStatus, PET_STATUS_ADOPTED)
        );
        long available = petMapper.selectCount(
                Wrappers.lambdaQuery(Pet.class)
                        .eq(Pet::getStatus, PET_STATUS_AVAILABLE)
        );
        long unavailable = petMapper.selectCount(
                Wrappers.lambdaQuery(Pet.class)
                        .notIn(Pet::getStatus, PET_STATUS_AVAILABLE, PET_STATUS_ADOPTED)
        );
        long needInfo = petMapper.selectCount(missingInfoWrapper());

        DashboardSummary.StatusOverview overview = new DashboardSummary.StatusOverview();
        overview.setAdopted(adopted);
        overview.setAvailable(available);
        overview.setUnavailable(unavailable);
        overview.setNeedInfo(needInfo);
        return overview;
    }

    private QueryWrapper<Pet> missingImageWrapper() {
        return new QueryWrapper<Pet>()
                .and(wrapper -> wrapper.isNull("image").or().eq("image", ""))
                .and(wrapper -> wrapper.isNull("image_urls").or().eq("image_urls", ""));
    }

    private QueryWrapper<Pet> missingInfoWrapper() {
        return new QueryWrapper<Pet>()
                .and(wrapper -> wrapper.isNull("detail").or().eq("detail", ""))
                .or(wrapper -> wrapper
                        .and(w -> w.isNull("image").or().eq("image", ""))
                        .and(w -> w.isNull("image_urls").or().eq("image_urls", ""))
                );
    }

    private Map<Long, Pet> loadPetMap(List<Adoption> adoptions) {
        List<Long> petIds = adoptions.stream()
                .map(Adoption::getPetId)
                .distinct()
                .collect(Collectors.toList());
        if (petIds.isEmpty()) {
            return Map.of();
        }
        return petMapper.selectBatchIds(petIds)
                .stream()
                .collect(Collectors.toMap(Pet::getId, pet -> pet));
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMATTER);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static class ActivityTemp {
        private String title;
        private LocalDateTime time;
    }
}
