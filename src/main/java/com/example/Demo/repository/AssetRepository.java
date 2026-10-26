package com.example.Demo.repository;

import com.example.Demo.entity.Asset;
import com.example.Demo.entity.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByStatus(AssetStatus status);

    List<Asset> findByAssignedToUsername(String username);

    boolean existsBySerialNumber(String serialNumber);
}
