package com.qss.pet.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qss.pet.common.ApiResponse;
import com.qss.pet.dto.PetCreateRequest;
import com.qss.pet.dto.PetImageItem;
import com.qss.pet.dto.PetUpdateRequest;
import com.qss.pet.dto.PetView;
import com.qss.pet.entity.Pet;
import com.qss.pet.service.PetService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PetController {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PreAuthorize("hasAuthority('pet:read')")
    @GetMapping("/api/pets")
    public ApiResponse<List<PetView>> listPets(@RequestParam(value = "keyword", required = false) String keyword,
                                               @RequestParam(value = "type", required = false) String type,
                                               @RequestParam(value = "status", required = false) Integer status) {
        List<PetView> pets = petService.listPets(keyword, type, status).stream()
                .map(this::toView)
                .collect(Collectors.toList());
        return ApiResponse.ok(pets);
    }

    @PreAuthorize("hasAuthority('pet:read')")
    @GetMapping("/api/pets/{id}")
    public ApiResponse<PetView> getPet(@PathVariable("id") Long petId) {
        Pet pet = petService.getPet(petId);
        if (pet == null) {
            return ApiResponse.error(404, "Pet not found");
        }
        return ApiResponse.ok(toView(pet));
    }

    @PreAuthorize("hasAuthority('pet:create')")
    @PostMapping("/api/pets")
    public ApiResponse<PetView> createPet(@Valid @RequestBody PetCreateRequest request) {
        return ApiResponse.ok(toView(petService.createPet(request)));
    }

    @PreAuthorize("hasAuthority('pet:update')")
    @PutMapping("/api/pets/{id}")
    public ApiResponse<PetView> updatePet(@PathVariable("id") Long petId,
                                          @Valid @RequestBody PetUpdateRequest request) {
        Pet pet = petService.updatePet(petId, request);
        if (pet == null) {
            return ApiResponse.error(404, "Pet not found");
        }
        return ApiResponse.ok(toView(pet));
    }

    @PreAuthorize("hasAuthority('pet:delete')")
    @DeleteMapping("/api/pets/{id}")
    public ApiResponse<Void> deletePet(@PathVariable("id") Long petId) {
        if (!petService.deletePet(petId)) {
            return ApiResponse.error(404, "Pet not found");
        }
        return ApiResponse.ok(null);
    }

    private PetView toView(Pet pet) {
        PetView view = new PetView();
        view.setId(pet.getId());
        view.setNickname(pet.getNickname());
        view.setBreed(pet.getBreed());
        view.setType(pet.getType());
        view.setGender(pet.getGender());
        view.setAge(pet.getAge());
        view.setCity(pet.getCity());
        view.setAddress(pet.getAddress());
        view.setDetail(pet.getDetail());
        view.setImage(pet.getImage());
        view.setImageUrls(parseImageUrls(pet));
        view.setStatus(pet.getStatus());
        view.setCreatedAt(pet.getCreatedAt());
        view.setUpdatedAt(pet.getUpdatedAt());
        return view;
    }

    private List<PetImageItem> parseImageUrls(Pet pet) {
        if (pet == null) {
            return List.of();
        }
        if (StringUtils.hasText(pet.getImageUrls())) {
            try {
                return OBJECT_MAPPER.readValue(pet.getImageUrls(), new TypeReference<List<PetImageItem>>() {});
            } catch (Exception ignored) {
                // fallback below
            }
        }
        if (StringUtils.hasText(pet.getImage())) {
            PetImageItem item = new PetImageItem();
            item.setUrl(pet.getImage());
            item.setIsMain(true);
            return List.of(item);
        }
        return List.of();
    }
}
