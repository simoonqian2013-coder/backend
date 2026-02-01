package com.qss.pet.service;

import com.qss.pet.dto.AdoptionCreateRequest;
import com.qss.pet.dto.AdoptionReviewRequest;
import com.qss.pet.entity.Adoption;

import java.time.LocalDateTime;
import java.util.List;

public interface AdoptionService {
    Adoption createAdoption(Long applicantUserId, AdoptionCreateRequest request);

    List<Adoption> listAdoptions(String keyword, Integer status, LocalDateTime startTime, LocalDateTime endTime);

    List<Adoption> listMyAdoptions(Long applicantUserId);

    Adoption reviewAdoption(Long adoptionId, Long reviewerId, AdoptionReviewRequest request);

    Adoption getAdoption(Long adoptionId);
}
