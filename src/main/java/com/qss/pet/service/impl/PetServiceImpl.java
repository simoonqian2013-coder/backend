package com.qss.pet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qss.pet.dto.PetCreateRequest;
import com.qss.pet.dto.PetImageItem;
import com.qss.pet.dto.PetUpdateRequest;
import com.qss.pet.entity.Pet;
import com.qss.pet.mapper.PetMapper;
import com.qss.pet.service.PetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PetServiceImpl implements PetService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final PetMapper petMapper;

    public PetServiceImpl(PetMapper petMapper) {
        this.petMapper = petMapper;
    }

    @Override
    public List<Pet> listPets(String keyword, String type, Integer status) {
        LambdaQueryWrapper<Pet> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(query -> query.like(Pet::getNickname, keyword)
                    .or()
                    .like(Pet::getBreed, keyword)
                    .or()
                    .like(Pet::getCity, keyword));
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Pet::getType, type);
        }
        if (status != null) {
            wrapper.eq(Pet::getStatus, status);
        }
        wrapper.orderByDesc(Pet::getCreatedAt);
        return petMapper.selectList(wrapper);
    }

    @Override
    public Pet getPet(Long petId) {
        return petMapper.selectById(petId);
    }

    @Override
    @Transactional
    public Pet createPet(PetCreateRequest request) {
        Pet pet = new Pet();
        pet.setNickname(request.getNickname());
        pet.setBreed(request.getBreed());
        pet.setType(request.getType());
        pet.setGender(request.getGender());
        pet.setAge(request.getAge());
        pet.setCity(request.getCity());
        pet.setAddress(request.getAddress());
        pet.setDetail(request.getDetail());
        List<PetImageItem> imageItems = normalizeImageUrls(request.getImageUrls(), request.getImage());
        pet.setImage(resolveMainImage(imageItems, request.getImage()));
        pet.setImageUrls(toJson(imageItems));
        pet.setStatus(request.getStatus());
        pet.setCreatedAt(LocalDateTime.now());
        pet.setUpdatedAt(LocalDateTime.now());
        petMapper.insert(pet);
        return pet;
    }

    @Override
    @Transactional
    public Pet updatePet(Long petId, PetUpdateRequest request) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            return null;
        }
        if (request.getNickname() != null) {
            pet.setNickname(request.getNickname());
        }
        if (request.getBreed() != null) {
            pet.setBreed(request.getBreed());
        }
        if (request.getType() != null) {
            pet.setType(request.getType());
        }
        if (request.getGender() != null) {
            pet.setGender(request.getGender());
        }
        if (request.getAge() != null) {
            pet.setAge(request.getAge());
        }
        if (request.getCity() != null) {
            pet.setCity(request.getCity());
        }
        if (request.getAddress() != null) {
            pet.setAddress(request.getAddress());
        }
        if (request.getDetail() != null) {
            pet.setDetail(request.getDetail());
        }
        if (request.getImageUrls() != null) {
            List<PetImageItem> imageItems = normalizeImageUrls(request.getImageUrls(), request.getImage());
            pet.setImage(resolveMainImage(imageItems, request.getImage()));
            pet.setImageUrls(toJson(imageItems));
        } else if (request.getImage() != null) {
            pet.setImage(request.getImage());
            pet.setImageUrls(toJson(normalizeImageUrls(null, request.getImage())));
        }
        pet.setStatus(request.getStatus());
        pet.setUpdatedAt(LocalDateTime.now());
        petMapper.updateById(pet);
        return pet;
    }

    @Override
    @Transactional
    public boolean deletePet(Long petId) {
        Pet existing = petMapper.selectById(petId);
        if (existing == null) {
            return false;
        }
        petMapper.deleteById(petId);
        return true;
    }

    private List<PetImageItem> normalizeImageUrls(List<PetImageItem> items, String fallbackImage) {
        List<PetImageItem> normalized = new ArrayList<>();
        if (items != null) {
            for (PetImageItem item : items) {
                if (item == null || !StringUtils.hasText(item.getUrl())) {
                    continue;
                }
                PetImageItem safe = new PetImageItem();
                safe.setUrl(item.getUrl());
                safe.setIsMain(Boolean.TRUE.equals(item.getIsMain()));
                normalized.add(safe);
            }
        }
        if (normalized.isEmpty() && StringUtils.hasText(fallbackImage)) {
            PetImageItem single = new PetImageItem();
            single.setUrl(fallbackImage);
            single.setIsMain(true);
            normalized.add(single);
        }
        boolean hasMain = normalized.stream().anyMatch(item -> Boolean.TRUE.equals(item.getIsMain()));
        if (!normalized.isEmpty() && !hasMain) {
            normalized.get(0).setIsMain(true);
        }
        return normalized;
    }

    private String resolveMainImage(List<PetImageItem> items, String fallbackImage) {
        if (items != null) {
            for (PetImageItem item : items) {
                if (item != null && Boolean.TRUE.equals(item.getIsMain()) && StringUtils.hasText(item.getUrl())) {
                    return item.getUrl();
                }
            }
            if (!items.isEmpty() && StringUtils.hasText(items.get(0).getUrl())) {
                return items.get(0).getUrl();
            }
        }
        return StringUtils.hasText(fallbackImage) ? fallbackImage : null;
    }

    private String toJson(List<PetImageItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize pet image urls", e);
        }
    }
}
