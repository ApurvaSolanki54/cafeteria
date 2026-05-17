package com.example.cafeteria.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cafeteria.dto.ApiResponse;
import com.example.cafeteria.entity.Cafeteria;
import com.example.cafeteria.entity.CafeteriaTable;
import com.example.cafeteria.service.CafeteriaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final CafeteriaService cafeteriaService;

    // ==================== CAFETERIA MANAGEMENT ====================

    // POST /api/admin/cafeterias — add a new cafeteria
    @PostMapping("/cafeterias")
    public ResponseEntity<ApiResponse<Cafeteria>>addCafeteria(@RequestBody Cafeteria cafeteria) {
        Cafeteria saved = cafeteriaService.addCafeteria(cafeteria);
        return ResponseEntity.ok(ApiResponse.success("Cafeteria added!", saved));
    }

    // GET /api/admin/cafeterias — list all cafeterias
    @GetMapping("/cafeterias")
    public ResponseEntity<ApiResponse<List<Cafeteria>>> getAllCafeterias() {
        return ResponseEntity.ok(ApiResponse.success("All cafeterias", cafeteriaService.getAllCafeterias()));
    }

    // PUT /api/admin/cafeterias/{id} — update a cafeteria
    @PutMapping("/cafeterias/{id}")
    public ResponseEntity<ApiResponse<Cafeteria>> updateCafeteria(
        @PathVariable Long id,
        @RequestBody Cafeteria cafeteria
    ) {
        return ResponseEntity.ok(ApiResponse.success("Updated!", cafeteriaService.updateCafeteria(id, cafeteria)));
    }

    // ==================== TABLE MANAGEMENT ====================

    // POST /api/admin/tables — add a table to a cafeteria
    @PostMapping("/tables")
    public ResponseEntity<ApiResponse<CafeteriaTable>> addTable(@RequestBody CafeteriaTable table) {
        System.out.println("table" +  table);
        CafeteriaTable saved = cafeteriaService.addTable(table);
        return ResponseEntity.ok(ApiResponse.success("Table added!", saved));
    }

    // GET /api/admin/cafeterias/{cafeteriaId}/tables — list all tables in a cafeteria
    @GetMapping("/cafeterias/{cafeteriaId}/tables")
    public ResponseEntity<ApiResponse<List<CafeteriaTable>>> getTablesInCafeteria(
        @PathVariable Long cafeteriaId
    ) {
        return ResponseEntity.ok(ApiResponse.success("Tables", cafeteriaService.getTablesByCafeteria(cafeteriaId)));
    }
}
