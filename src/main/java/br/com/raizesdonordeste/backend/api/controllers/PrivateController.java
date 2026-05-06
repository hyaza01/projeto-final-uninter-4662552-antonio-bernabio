package br.com.raizesdonordeste.backend.api.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/private")
public class PrivateController {

	@GetMapping("/ping")
	public ResponseEntity<Map<String, String>> ping(Authentication authentication) {
		return ResponseEntity.ok(Map.of(
			"message", "pong",
			"usuario", authentication.getName()
		));
	}
}
