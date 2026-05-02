package br.com.raizesdonordeste.backend.api.controllers;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

	@GetMapping
	public ResponseEntity<HealthResponse> health() {
		HealthResponse response = new HealthResponse("UP", "raizes-backend", Instant.now().toString());
		return ResponseEntity.ok(response);
	}

	public record HealthResponse(String status, String service, String timestamp) {
	}
}