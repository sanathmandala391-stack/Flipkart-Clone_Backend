package com.flipkart.clone.service;

import com.flipkart.clone.entity.Banner;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    // PUBLIC
    public List<Banner> getActiveBanners() {
        return bannerRepository
                .findByIsActiveTrueOrderBySortOrderAsc();
    }

    public List<Banner> getByPosition(String position) {
        return bannerRepository
                .findByPositionAndIsActiveTrue(
                        Banner.BannerPosition.valueOf(
                                position.toUpperCase()));
    }

    // ADMIN
    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }

    @Transactional
    public Banner createBanner(Map<String, Object> body) {
        Banner banner = Banner.builder()
                .title(body.get("title").toString())
                .imageUrl(body.get("imageUrl").toString())
                .redirectUrl(body.containsKey("redirectUrl")
                        ? body.get("redirectUrl").toString()
                        : null)
                .position(Banner.BannerPosition.valueOf(
                        body.getOrDefault(
                                "position", "TOP").toString()))
                .isActive(true)
                .sortOrder(body.containsKey("sortOrder")
                        ? Integer.valueOf(
                        body.get("sortOrder").toString())
                        : 0)
                .startDate(body.containsKey("startDate")
                        ? LocalDateTime.parse(
                        body.get("startDate").toString())
                        : null)
                .endDate(body.containsKey("endDate")
                        ? LocalDateTime.parse(
                        body.get("endDate").toString())
                        : null)
                .build();

        return bannerRepository.save(banner);
    }

    @Transactional
    public Banner updateBanner(Long id,
                               Map<String, Object> body) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Banner not found"));

        if (body.containsKey("title"))
            banner.setTitle(body.get("title").toString());
        if (body.containsKey("imageUrl"))
            banner.setImageUrl(
                    body.get("imageUrl").toString());
        if (body.containsKey("redirectUrl"))
            banner.setRedirectUrl(
                    body.get("redirectUrl").toString());
        if (body.containsKey("isActive"))
            banner.setIsActive(
                    (Boolean) body.get("isActive"));
        if (body.containsKey("sortOrder"))
            banner.setSortOrder(Integer.valueOf(
                    body.get("sortOrder").toString()));

        return bannerRepository.save(banner);
    }

    @Transactional
    public void deleteBanner(Long id) {
        bannerRepository.deleteById(id);
    }
}