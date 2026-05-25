package com.flipkart.clone.service;

import com.flipkart.clone.entity.Category;
import com.flipkart.clone.entity.CommissionRule;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.CategoryRepository;
import com.flipkart.clone.repository.CommissionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommissionRuleService {

    private final CommissionRuleRepository commissionRuleRepository;
    private final CategoryRepository categoryRepository;

    // ── GET all rules ─────────────────────────────────────────────
    public List<CommissionRule> getAllRules() {
        return commissionRuleRepository.findAll();
    }

    // ── GET rule for a category ───────────────────────────────────
    public CommissionRule getRuleForCategory(Long categoryId) {
        return commissionRuleRepository
                .findByCategoryIdAndIsActiveTrue(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No commission rule for this category"));
    }

    // ── CREATE rule (admin) ───────────────────────────────────────
    @Transactional
    public CommissionRule createRule(Map<String, Object> body) {

        Long categoryId = Long.valueOf(
                body.get("categoryId").toString());

        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"));

        // Deactivate old rule for this category
        commissionRuleRepository
                .findByCategoryIdAndIsActiveTrue(categoryId)
                .ifPresent(old -> {
                    old.setIsActive(false);
                    commissionRuleRepository.save(old);
                });

        CommissionRule rule = CommissionRule.builder()
                .category(category)
                .commissionPct(new BigDecimal(
                        body.get("commissionPct").toString()))
                .fixedFee(body.containsKey("fixedFee")
                        ? new BigDecimal(
                        body.get("fixedFee").toString())
                        : BigDecimal.ZERO)
                .minOrderValue(body.containsKey("minOrderValue")
                        ? new BigDecimal(
                        body.get("minOrderValue").toString())
                        : BigDecimal.ZERO)
                .effectiveFrom(body.containsKey("effectiveFrom")
                        ? LocalDate.parse(
                        body.get("effectiveFrom").toString())
                        : LocalDate.now())
                .isActive(true)
                .build();

        return commissionRuleRepository.save(rule);
    }

    // ── DEACTIVATE rule ───────────────────────────────────────────
    @Transactional
    public void deactivateRule(Long ruleId) {
        CommissionRule rule = commissionRuleRepository
                .findById(ruleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Rule not found"));
        rule.setIsActive(false);
        commissionRuleRepository.save(rule);
    }
}