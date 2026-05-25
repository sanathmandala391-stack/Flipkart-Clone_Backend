package com.flipkart.clone.service;

import com.flipkart.clone.entity.PlatformConfig;
import com.flipkart.clone.entity.User;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.PlatformConfigRepository;
import com.flipkart.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlatformConfigService {

    private final PlatformConfigRepository configRepository;
    private final UserRepository userRepository;

    // ── GET all configs ───────────────────────────────────────────
    public List<PlatformConfig> getAllConfigs() {
        return configRepository.findAll();
    }

    // ── GET single config by key ──────────────────────────────────
    public PlatformConfig getConfig(String key) {
        return configRepository.findByConfigKey(key)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Config not found: " + key));
    }

    // ── GET config value directly ─────────────────────────────────
    public String getConfigValue(String key,
                                 String defaultValue) {
        return configRepository.findByConfigKey(key)
                .map(PlatformConfig::getConfigValue)
                .orElse(defaultValue);
    }

    // ── SET config (create or update) ─────────────────────────────
    @Transactional
    public PlatformConfig setConfig(Long adminId,
                                    Map<String, Object> body) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Admin not found"));

        String key   = body.get("configKey").toString();
        String value = body.get("configValue").toString();

        PlatformConfig config = configRepository
                .findByConfigKey(key)
                .orElse(PlatformConfig.builder()
                        .configKey(key)
                        .build());

        config.setConfigValue(value);
        config.setDescription(body.containsKey("description")
                ? body.get("description").toString()
                : config.getDescription());
        config.setUpdatedBy(admin);

        return configRepository.save(config);
    }

    // ── DELETE config ─────────────────────────────────────────────
    @Transactional
    public void deleteConfig(String key) {
        PlatformConfig config = getConfig(key);
        configRepository.delete(config);
    }
}