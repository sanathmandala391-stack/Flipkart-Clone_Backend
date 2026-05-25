//package com.flipkart.clone.controller;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/tts")
//@Slf4j
//public class SarvamTTSController {
//
//    private static final String SARVAM_API_KEY =
//            "sk_suohs20h_t8SMoubQSrkthHirSsKdkDk0";
//
//    private static final String SARVAM_URL =
//            "https://api.sarvam.ai/text-to-speech";
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    // ── POST /api/tts/speak ────────────────────────────────────
//    // Frontend calls this → backend calls Sarvam → returns audio
//    @PostMapping("/speak")
//    public ResponseEntity<Map> speak(
//            @RequestBody Map<String, String> body) {
//        try {
//            String text = body.get("text");
//            if (text == null || text.isBlank()) {
//                return ResponseEntity.badRequest()
//                        .body(Map.of("error", "text is required"));
//            }
//
//            // Build Sarvam AI request
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("api-subscription-key", SARVAM_API_KEY);
//
//            Map<String, Object> sarvamBody = Map.of(
//                    "inputs",               List.of(text),
//                    "target_language_code", "te-IN",
//                    "speaker",              "anushka",
//                    "pitch",                0.1,
//                    "pace",                 0.9,
//                    "loudness",             1.5,
//                    "enable_preprocessing", true,
//                    "model",                "bulbul:v1"
//            );
//
//            HttpEntity<Map<String, Object>> request =
//                    new HttpEntity<>(sarvamBody, headers);
//
//            ResponseEntity<Map> response = restTemplate.postForEntity(
//                    SARVAM_URL, request, Map.class);
//
//            // Return the Sarvam response directly to frontend
//            return ResponseEntity.ok(response.getBody());
//
//        } catch (Exception e) {
//            log.error("Sarvam TTS error: {}", e.getMessage());
//            return ResponseEntity.status(500)
//                    .body(Map.of("error", e.getMessage()));
//        }
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//


package com.flipkart.clone.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
        import org.springframework.web.bind.annotation.*;
        import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tts")
@Slf4j
public class SarvamTTSController {

    // ── DIRECT API KEY ─────────────────────────────────────────
    private static final String SARVAM_API_KEY =
            "sk_suohs20h_t8SMoubQSrkthHirSsKdkDk0";

    private static final String SARVAM_URL =
            "https://api.sarvam.ai/text-to-speech";

    private final RestTemplate restTemplate =
            new RestTemplate();

    // ── POST /api/tts/speak ────────────────────────────────────
    @PostMapping("/speak")
    public ResponseEntity<?> speak(
            @RequestBody Map<String, String> body) {

        try {

            // ── Validate input ─────────────────────────────────
            String text = body.get("text");

            if (text == null || text.isBlank()) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "error", "text is required"
                        ));
            }

            // ── Headers ────────────────────────────────────────
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.set(
                    "api-subscription-key",
                    SARVAM_API_KEY
            );

            // ── Sarvam Request Body ────────────────────────────
            Map<String, Object> sarvamBody = Map.of(

                    "inputs", List.of(text),

                    "target_language_code", "te-IN",

                    "speaker", "kavya",

                    "pace", 0.88,

                    "enable_preprocessing", true,

                    "model", "bulbul:v3"
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(sarvamBody, headers);

            // ── Call Sarvam API ────────────────────────────────
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            SARVAM_URL,
                            request,
                            Map.class
                    );

            log.info("Sarvam TTS Success");

            // ── Return Response ────────────────────────────────
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", response.getBody()
                    )
            );

        }

        // ── Sarvam API Errors ─────────────────────────────────
        catch (HttpClientErrorException e) {

            log.error(
                    "Sarvam API Error: {}",
                    e.getResponseBodyAsString()
            );

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(
                            Map.of(
                                    "success", false,
                                    "error",
                                    e.getResponseBodyAsString()
                            )
                    );
        }

        // ── General Errors ────────────────────────────────────
        catch (Exception e) {

            log.error("TTS Server Error", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "success", false,
                                    "error", e.getMessage()
                            )
                    );
        }
    }
}