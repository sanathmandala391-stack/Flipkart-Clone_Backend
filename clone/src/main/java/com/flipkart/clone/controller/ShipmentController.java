package com.flipkart.clone.controller;

import com.flipkart.clone.entity.Shipment;
import com.flipkart.clone.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    // PUBLIC — track by number
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<Shipment> track(
            @PathVariable String trackingNumber) {
        return ResponseEntity.ok(
                shipmentService.trackByNumber(trackingNumber));
    }

    // USER — get shipment for their order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Shipment> getByOrder(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
                shipmentService.getByOrderId(orderId));
    }

    // VENDOR — create shipment
    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Shipment> create(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(
                shipmentService.createShipment(orderId, body));
    }

    // ADMIN — update status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Shipment> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                shipmentService.updateStatus(
                        id, body.get("status")));
    }
}