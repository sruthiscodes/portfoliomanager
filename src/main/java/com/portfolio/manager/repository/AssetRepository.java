package com.portfolio.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portfolio.manager.model.Asset;
import com.portfolio.manager.model.AssetType;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByAssetType(AssetType assetType);
}
