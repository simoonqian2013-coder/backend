package com.qss.pet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qss.pet.dto.PetCreateRequest;
import com.qss.pet.dto.PetUpdateRequest;
import com.qss.pet.entity.Pet;
import com.qss.pet.mapper.PetMapper;
import com.qss.pet.service.PetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PetServiceImpl implements PetService {
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
        pet.setImage(request.getImage());
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
        if (request.getImage() != null) {
            pet.setImage(request.getImage());
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
}
