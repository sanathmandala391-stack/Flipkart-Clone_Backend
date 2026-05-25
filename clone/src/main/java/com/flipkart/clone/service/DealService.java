package com.flipkart.clone.service;

import com.flipkart.clone.entity.Deal;
import com.flipkart.clone.entity.ProductVariant;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.DealRepository;
import com.flipkart.clone.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;
    private final ProductVariantRepository variantRepository;

    // PUBLIC
    public List<Deal> getActiveDeals() {
        return dealRepository
                .findActiveDeals(LocalDateTime.now());
    }

    public List<Deal> getDealsByType(String type) {
        return dealRepository
                .findByDealTypeAndIsActiveTrue(
                        Deal.DealType.valueOf(
                                type.toUpperCase()));
    }

    // ADMIN
    @Transactional
    public Deal createDeal(Map<String, Object> body) {

        Long variantId = Long.valueOf(
                body.get("productVariantId").toString());

        ProductVariant variant = variantRepository
                .findById(variantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product variant not found"));

        BigDecimal dealPrice = new BigDecimal(
                body.get("dealPrice").toString());

        if (dealPrice.compareTo(variant.getMrp()) >= 0) {
            throw new BadRequestException(
                    "Deal price must be less than MRP");
        }

        Deal deal = Deal.builder()
                .productVariant(variant)
                .dealPrice(dealPrice)
                .dealType(Deal.DealType.valueOf(
                        body.get("dealType").toString()))
                .maxQtyPerUser(
                        body.containsKey("maxQtyPerUser")
                                ? Integer.valueOf(
                                body.get("maxQtyPerUser").toString())
                                : 5)
                .totalQty(body.containsKey("totalQty")
                        ? Integer.valueOf(
                        body.get("totalQty").toString())
                        : 100)
                .soldQty(0)
                .isActive(true)
                .startAt(LocalDateTime.parse(
                        body.get("startAt").toString()))
                .endAt(LocalDateTime.parse(
                        body.get("endAt").toString()))
                .build();

        return dealRepository.save(deal);
    }

    @Transactional
    public Deal toggleDeal(Long dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Deal not found"));
        deal.setIsActive(!deal.getIsActive());
        return dealRepository.save(deal);
    }

    @Transactional
    public void deleteDeal(Long dealId) {
        dealRepository.deleteById(dealId);
    }
}