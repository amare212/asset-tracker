package com.example.Demo.service;

import com.example.Demo.dto.AssetRequest;
import com.example.Demo.dto.AssetResponse;
import com.example.Demo.entity.Asset;
import com.example.Demo.entity.AssetStatus;
import com.example.Demo.entity.Category;
import com.example.Demo.entity.User;
import com.example.Demo.exception.BadRequestException;
import com.example.Demo.exception.ConflictException;
import com.example.Demo.exception.ResourceNotFoundException;
import com.example.Demo.repository.AssetRepository;
import com.example.Demo.repository.CategoryRepository;
import com.example.Demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public AssetService(AssetRepository assetRepository,
                        CategoryRepository categoryRepository,
                        UserRepository userRepository) {
        this.assetRepository = assetRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> findAll(AssetStatus status) {
        List<Asset> assets = (status == null)
                ? assetRepository.findAll()
                : assetRepository.findByStatus(status);
        return assets.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> findMyAssets(String username) {
        return assetRepository.findByAssignedToUsername(username).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public AssetResponse create(AssetRequest request) {
        if (assetRepository.existsBySerialNumber(request.serialNumber().trim())) {
            throw new ConflictException("Serial number already exists: " + request.serialNumber());
        }

        AssetStatus status = (request.status() == null) ? AssetStatus.AVAILABLE : request.status();
        if (status == AssetStatus.ASSIGNED) {
            throw new BadRequestException(
                    "A new asset cannot be created as ASSIGNED; use the assign endpoint instead");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id " + request.categoryId()));

        Asset asset = new Asset();
        asset.setName(request.name().trim());
        asset.setSerialNumber(request.serialNumber().trim());
        asset.setStatus(status);
        asset.setCategory(category);
        asset.setAssignedTo(null);

        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public AssetResponse assign(Long assetId, Long userId) {
        Asset asset = findAssetOrThrow(assetId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        // Business rule: only AVAILABLE assets can be assigned
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new BadRequestException(
                    "Asset cannot be assigned because its status is " + asset.getStatus());
        }

        asset.setStatus(AssetStatus.ASSIGNED);
        asset.setAssignedTo(user);
        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public AssetResponse returnAsset(Long assetId) {
        Asset asset = findAssetOrThrow(assetId);

        if (asset.getStatus() != AssetStatus.ASSIGNED) {
            throw new BadRequestException(
                    "Only ASSIGNED assets can be returned (current status: " + asset.getStatus() + ")");
        }

        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setAssignedTo(null);
        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public void delete(Long assetId) {
        assetRepository.delete(findAssetOrThrow(assetId));
    }

    private Asset findAssetOrThrow(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id " + id));
    }

    // Entity -> Response DTO (entities are never exposed by controllers)
    private AssetResponse toResponse(Asset a) {
        User u = a.getAssignedTo();
        return new AssetResponse(
                a.getId(),
                a.getName(),
                a.getSerialNumber(),
                a.getStatus(),
                a.getCategory().getId(),
                a.getCategory().getName(),
                u == null ? null : u.getId(),
                u == null ? null : u.getUsername(),
                a.getCreatedAt());
    }
}
