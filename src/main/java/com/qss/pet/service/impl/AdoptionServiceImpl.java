package com.qss.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qss.pet.dto.AdoptionCreateRequest;
import com.qss.pet.dto.AdoptionReviewRequest;
import com.qss.pet.entity.Adoption;
import com.qss.pet.entity.Pet;
import com.qss.pet.mapper.AdoptionMapper;
import com.qss.pet.mapper.PetMapper;
import com.qss.pet.service.AdoptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdoptionServiceImpl implements AdoptionService {
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;

    private static final int PET_STATUS_AVAILABLE = 1;
    private static final int PET_STATUS_ADOPTED = 2;

    private final AdoptionMapper adoptionMapper;
    private final PetMapper petMapper;

    public AdoptionServiceImpl(AdoptionMapper adoptionMapper, PetMapper petMapper) {
        this.adoptionMapper = adoptionMapper;
        this.petMapper = petMapper;
    }

    @Override
    @Transactional
    public Adoption createAdoption(Long applicantUserId, AdoptionCreateRequest request) {
        Pet pet = petMapper.selectById(request.getPetId());
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found");
        }
        if (pet.getStatus() == null || pet.getStatus() != PET_STATUS_AVAILABLE) {
            throw new IllegalArgumentException("Pet is not available");
        }
        Adoption adoption = new Adoption();
        adoption.setPetId(request.getPetId());
        adoption.setApplicantUserId(applicantUserId);
        adoption.setApplicantName(request.getApplicantName());
        adoption.setPhone(request.getPhone());
        adoption.setIdNumber(request.getIdNumber());
        adoption.setCity(request.getCity());
        adoption.setAddress(request.getAddress());
        adoption.setExperience(request.getExperience());
        adoption.setRemark(request.getRemark());
        adoption.setStatus(STATUS_PENDING);
        adoption.setCreatedAt(LocalDateTime.now());
        adoption.setUpdatedAt(LocalDateTime.now());
        adoptionMapper.insert(adoption);
        return adoption;
    }

    @Override
    public List<Adoption> listAdoptions(String keyword, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Adoption> wrapper = Wrappers.lambdaQuery();
        if (status != null) {
            wrapper.eq(Adoption::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(Adoption::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(Adoption::getCreatedAt, endTime);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(query -> query.like(Adoption::getApplicantName, keyword)
                    .or()
                    .like(Adoption::getPhone, keyword));
        }
        wrapper.orderByDesc(Adoption::getCreatedAt);
        return adoptionMapper.selectList(wrapper);
    }

    @Override
    public List<Adoption> listMyAdoptions(Long applicantUserId) {
        if (applicantUserId == null) {
            return java.util.Collections.emptyList();
        }
        return adoptionMapper.selectList(
                Wrappers.lambdaQuery(Adoption.class)
                        .eq(Adoption::getApplicantUserId, applicantUserId)
                        .orderByDesc(Adoption::getCreatedAt)
        );
    }

    @Override
    @Transactional
    public Adoption reviewAdoption(Long adoptionId, Long reviewerId, AdoptionReviewRequest request) {
        Adoption adoption = adoptionMapper.selectById(adoptionId);
        if (adoption == null) {
            return null;
        }
        if (adoption.getStatus() == null || adoption.getStatus() != STATUS_PENDING) {
            throw new IllegalArgumentException("Adoption already reviewed");
        }
        if (request.getStatus() == null || (request.getStatus() != STATUS_APPROVED && request.getStatus() != STATUS_REJECTED)) {
            throw new IllegalArgumentException("Invalid status");
        }
        if (request.getStatus() == STATUS_REJECTED && !StringUtils.hasText(request.getRemark())) {
            throw new IllegalArgumentException("Reject reason required");
        }
        adoption.setStatus(request.getStatus());
        adoption.setReviewerId(reviewerId);
        adoption.setReviewRemark(request.getRemark());
        adoption.setReviewedAt(LocalDateTime.now());
        adoption.setUpdatedAt(LocalDateTime.now());
        adoptionMapper.updateById(adoption);

        if (request.getStatus() == STATUS_APPROVED) {
            Pet pet = petMapper.selectById(adoption.getPetId());
            if (pet != null) {
                pet.setStatus(PET_STATUS_ADOPTED);
                pet.setUpdatedAt(LocalDateTime.now());
                petMapper.updateById(pet);
            }
        }
        return adoption;
    }

    @Override
    public Adoption getAdoption(Long adoptionId) {
        return adoptionMapper.selectById(adoptionId);
    }
}
