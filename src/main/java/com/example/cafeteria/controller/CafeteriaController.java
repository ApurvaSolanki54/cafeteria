package com.example.cafeteria.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cafeteria.dto.ApiResponse;
import com.example.cafeteria.dto.TableStatusResponse;
import com.example.cafeteria.entity.Cafeteria;
import com.example.cafeteria.entity.CafeteriaTable;
import com.example.cafeteria.service.CafeteriaService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


/*
 * Public APIs that ALL logged-in employees can use.
 * View cafeterias, see available tables.
 */
@RestController
@RequestMapping("/api/cafeterias")
@RequiredArgsConstructor
public class CafeteriaController {
    private final CafeteriaService cafeteriaService;

    // GET /api/cafeterias — see all active cafeterias
    @GetMapping
    public ResponseEntity<ApiResponse<List<Cafeteria>>> getCafeterias() {
        return ResponseEntity.ok(ApiResponse.success("Cafeterias", cafeteriaService.getActivCafeterias()));
    }

    /*
     * GET /api/cafeterias/{id}/available-tables?startTime=2024-01-15T13:00:00
     * See which tables are free at a given time in a cafeteria.
     *
     * @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) tells Spring
     * how to parse the datetime from the URL query parameter.
     */
    @GetMapping("/{cafeteriaId}/available-tables")
    public ResponseEntity<ApiResponse<List<CafeteriaTable>>> getAvailableTables(
        @PathVariable Long cafeteriaId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime
    ) {
        System.out.println("getAvailableTables "+startTime);
        List<CafeteriaTable> tables = cafeteriaService.getAvailableTables(cafeteriaId, startTime);
        return ResponseEntity.ok(ApiResponse.success("Available tables", tables)); 
    }

    /*
    * GET /api/cafeterias/{id}/all-tables
    * Returns ALL tables in a cafeteria regardless of booking status.
    * Employees need this to SEE which tables are booked (shown in red).
    * Different from /available-tables which only returns free ones.
    */
    @GetMapping("/{cafeteriaId}/all-tables")
    public ResponseEntity<ApiResponse<List<CafeteriaTable>>> getAllTables(
        @PathVariable Long cafeteriaId
    ) {
        List<CafeteriaTable> tables = cafeteriaService.getTablesByCafeteria(cafeteriaId);
        return ResponseEntity.ok(ApiResponse.success("All tables", tables));
    }
    
    /*
    * GET /api/cafeterias/{cafeId}/table-statuses?startTime=2024-01-15T17:00:00
    *
    * Returns ALL tables with their status at the requested time.
    * For BOOKED tables, also returns:
    *   - actual booking start time (not the viewer's selected time!)
    *   - number of members sitting (occupied seats)
    *
    * This is what powers the detailed red tile with correct time + seat dots.
    */
    @GetMapping("/{cafeteriaId}/table-statuses")
    public ResponseEntity<ApiResponse<List<TableStatusResponse>>> getTableStatuses(
        @PathVariable Long cafeteriaId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime
    ) {
        List<TableStatusResponse> statuses = cafeteriaService.getTableStatuses(cafeteriaId, startTime);
        return ResponseEntity.ok(ApiResponse.success("Table statuses", statuses));
    }
}
