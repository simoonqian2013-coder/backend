package com.qss.pet.controller;

import com.qss.pet.common.ApiResponse;
import com.qss.pet.dto.AdoptionCreateRequest;
import com.qss.pet.dto.AdoptionReviewRequest;
import com.qss.pet.dto.AdoptionView;
import com.qss.pet.entity.Adoption;
import com.qss.pet.entity.Pet;
import com.qss.pet.security.SecurityUser;
import com.qss.pet.service.AdoptionService;
import com.qss.pet.mapper.PetMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AdoptionController {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AdoptionService adoptionService;
    private final PetMapper petMapper;

    public AdoptionController(AdoptionService adoptionService, PetMapper petMapper) {
        this.adoptionService = adoptionService;
        this.petMapper = petMapper;
    }

    @PreAuthorize("hasAuthority('adoption:read')")
    @GetMapping("/api/adoptions")
    public ApiResponse<List<AdoptionView>> listAdoptions(@RequestParam(value = "keyword", required = false) String keyword,
                                                         @RequestParam(value = "status", required = false) Integer status,
                                                         @RequestParam(value = "startTime", required = false) String startTime,
                                                         @RequestParam(value = "endTime", required = false) String endTime) {
        LocalDateTime start = parseTime(startTime);
        LocalDateTime end = parseTime(endTime);
        List<Adoption> adoptions = adoptionService.listAdoptions(null, status, start, end);
        List<AdoptionView> views = toViews(adoptions);
        if (keyword != null && !keyword.isBlank()) {
            String key = keyword.trim().toLowerCase();
            views = views.stream()
                    .filter(view -> contains(view.getApplicantName(), key)
                            || contains(view.getPhone(), key)
                            || contains(view.getPetNickname(), key))
                    .collect(Collectors.toList());
        }
        views.sort(Comparator.comparing(AdoptionView::getCreatedAt).reversed());
        return ApiResponse.ok(views);
    }

    @PreAuthorize("hasAuthority('adoption:read')")
    @GetMapping("/api/adoptions/{id}")
    public ApiResponse<AdoptionView> getAdoption(@PathVariable("id") Long adoptionId) {
        Adoption adoption = adoptionService.getAdoption(adoptionId);
        if (adoption == null) {
            return ApiResponse.error(404, "Adoption not found");
        }
        return ApiResponse.ok(toViews(java.util.Collections.singletonList(adoption)).get(0));
    }

    @PreAuthorize("hasAuthority('adoption:create')")
    @PostMapping("/api/adoptions")
    public ApiResponse<AdoptionView> createAdoption(@Valid @RequestBody AdoptionCreateRequest request) {
        Long userId = getCurrentUserId();
        Adoption adoption = adoptionService.createAdoption(userId, request);
        return ApiResponse.ok(toViews(java.util.Collections.singletonList(adoption)).get(0));
    }

    @PreAuthorize("hasAuthority('adoption:create')")
    @GetMapping("/api/adoptions/my")
    public ApiResponse<List<AdoptionView>> listMyAdoptions() {
        Long userId = getCurrentUserId();
        List<Adoption> adoptions = adoptionService.listMyAdoptions(userId);
        return ApiResponse.ok(toViews(adoptions));
    }

    @PreAuthorize("hasAuthority('adoption:review')")
    @PostMapping("/api/adoptions/{id}/review")
    public ApiResponse<AdoptionView> reviewAdoption(@PathVariable("id") Long adoptionId,
                                                    @Valid @RequestBody AdoptionReviewRequest request) {
        Long reviewerId = getCurrentUserId();
        Adoption adoption = adoptionService.reviewAdoption(adoptionId, reviewerId, request);
        if (adoption == null) {
            return ApiResponse.error(404, "Adoption not found");
        }
        return ApiResponse.ok(toViews(java.util.Collections.singletonList(adoption)).get(0));
    }

    private List<AdoptionView> toViews(List<Adoption> adoptions) {
        List<Long> petIds = adoptions.stream()
                .map(Adoption::getPetId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Pet> petMap = petIds.isEmpty()
                ? Map.of()
                : petMapper.selectBatchIds(petIds)
                .stream()
                .collect(Collectors.toMap(Pet::getId, pet -> pet));

        return adoptions.stream()
                .map(adoption -> {
                    AdoptionView view = new AdoptionView();
                    view.setId(adoption.getId());
                    view.setPetId(adoption.getPetId());
                    Pet pet = petMap.get(adoption.getPetId());
                    if (pet != null) {
                        view.setPetNickname(pet.getNickname());
                        view.setPetBreed(pet.getBreed());
                        view.setPetType(pet.getType());
                        view.setPetGender(pet.getGender());
                        view.setPetAge(pet.getAge());
                        view.setPetCity(pet.getCity());
                        view.setPetImage(pet.getImage());
                    }
                    view.setApplicantName(adoption.getApplicantName());
                    view.setPhone(adoption.getPhone());
                    view.setIdNumber(adoption.getIdNumber());
                    view.setCity(adoption.getCity());
                    view.setAddress(adoption.getAddress());
                    view.setExperience(adoption.getExperience());
                    view.setRemark(adoption.getRemark());
                    view.setStatus(adoption.getStatus());
                    view.setReviewerId(adoption.getReviewerId());
                    view.setReviewRemark(adoption.getReviewRemark());
                    view.setReviewedAt(adoption.getReviewedAt());
                    view.setCreatedAt(adoption.getCreatedAt());
                    return view;
                })
                .collect(Collectors.toList());
    }

    private LocalDateTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value, TIME_FORMATTER);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser)) {
            return null;
        }
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        return user.getUser() == null ? null : user.getUser().getId();
    }

    private boolean contains(String value, String keyword) {
        if (value == null) return false;
        return value.toLowerCase().contains(keyword);
    }
}
