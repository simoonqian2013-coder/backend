package com.qss.pet.service;

import com.qss.pet.dto.PetCreateRequest;
import com.qss.pet.dto.PetUpdateRequest;
import com.qss.pet.entity.Pet;

import java.util.List;

public interface PetService {
    List<Pet> listPets(String keyword, String type, Integer status);

    Pet getPet(Long petId);

    Pet createPet(PetCreateRequest request);

    Pet updatePet(Long petId, PetUpdateRequest request);

    boolean deletePet(Long petId);
}
