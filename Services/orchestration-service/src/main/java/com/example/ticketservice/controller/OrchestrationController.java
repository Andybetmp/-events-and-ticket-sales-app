package com.example.ticketservice.controller;

import com.example.ticketservice.client.TicketServiceClient;
import com.example.ticketservice.dto.PurchaseTicketRequest;
import com.example.ticketservice.orchestrator.EventCreationOrchestrator;
import com.example.ticketservice.orchestrator.TicketPurchaseOrchestrator;
import com.example.ticketservice.orchestrator.UserRegistrationOrchestrator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orchestration")
@Validated
public class OrchestrationController {

    @Autowired
    private TicketPurchaseOrchestrator purchaseOrchestrator;

    @Autowired
    private UserRegistrationOrchestrator userRegistrationOrchestrator;

    @Autowired
    private EventCreationOrchestrator eventCreationOrchestrator;

    @Autowired
    private TicketServiceClient ticketClient;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody Map<String, Object> userData) {
        try {
            Map<String, Object> response = userRegistrationOrchestrator.orchestrateRegistration(userData);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("exitoso", false);
            errorResponse.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/create-event")
    public ResponseEntity<?> createEvent(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @Valid @RequestBody Map<String, Object> eventData) {
        
        if (userEmail == null || userEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("Header X-User-Email es requerido");
        }

        try {
            Map<String, Object> response = eventCreationOrchestrator.orchestrateEventCreation(userEmail, eventData);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/purchase-ticket")
    public ResponseEntity<?> purchaseTicket(
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @Valid @RequestBody PurchaseTicketRequest request) {
        
        if (userId == null) {
            return ResponseEntity.badRequest().body("Header X-User-ID es requerido");
        }
        
        if (userEmail == null || userEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("Header X-User-Email es requerido");
        }

        try {
            Map<String, Object> response = purchaseOrchestrator.orchestratePurchase(userId, userEmail, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<?> getMyTickets(
            @RequestHeader(value = "X-User-ID", required = false) Long userId) {
        
        if (userId == null) {
            return ResponseEntity.badRequest().body("Header X-User-ID es requerido");
        }

        List<Map<String, Object>> tickets = ticketClient.obtenerTicketsPorUsuario(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Orchestration Service OK");
    }
}
