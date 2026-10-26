package com.example.Demo.controller;

import com.example.Demo.dto.AssetRequest;
import com.example.Demo.dto.AssetResponse;
import com.example.Demo.entity.AssetStatus;
import com.example.Demo.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    // GET /api/assets  or  GET /api/assets?status=AVAILABLE
    @GetMapping
    public ResponseEntity<List<AssetResponse>> list(@RequestParam(required = false) AssetStatus status) {
        return ResponseEntity.ok(assetService.findAll(status));
    }

    @GetMapping("/my-assets")
    public ResponseEntity<List<AssetResponse>> myAssets(Authentication authentication) {
        return ResponseEntity.ok(assetService.findMyAssets(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody AssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.create(request));
    }

    @PatchMapping("/{id}/assign/{userId}")
    public ResponseEntity<AssetResponse> assign(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(assetService.assign(id, userId));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<AssetResponse> returnAsset(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.returnAsset(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
