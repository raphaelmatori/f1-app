package com.f1.app.controller;

import com.f1.app.dto.DriverDTO;
import com.f1.app.dto.RaceDTO;
import com.f1.app.dto.SeasonDTO;
import com.f1.app.service.F1ChampionService;
import com.f1.app.service.DataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "F1 Champions API", description = "API endpoints for F1 World Champions data")
@CrossOrigin(origins = "http://localhost:4200")
public class F1ChampionController {
    
    private final F1ChampionService f1ChampionService;
    private final DataSyncService dataSyncService;
    
    @PostMapping("/sync")
    @Operation(summary = "Manually trigger data synchronization")
    public ResponseEntity<String> syncData() {
        dataSyncService.syncData();
        return ResponseEntity.ok("Data synchronization started");
    }
    
    @GetMapping("/seasons")
    @Operation(summary = "Get all F1 seasons with champions")
    public ResponseEntity<List<SeasonDTO>> getAllSeasons() {
        return ResponseEntity.ok(f1ChampionService.getAllSeasons());
    }
    
    @GetMapping("/seasons/{year}")
    @Operation(summary = "Get specific F1 season details")
    public ResponseEntity<SeasonDTO> getSeasonByYear(@PathVariable Integer year) {
        return f1ChampionService.getSeasonByYear(year)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/seasons/{year}/races")
    @Operation(summary = "Get all races for a specific season")
    public ResponseEntity<List<RaceDTO>> getRacesBySeason(@PathVariable Integer year) {
        List<RaceDTO> races = f1ChampionService.getRacesBySeason(year);
        return races.isEmpty() 
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(races);
    }
    
    @GetMapping("/champions")
    @Operation(summary = "Get all F1 World Champions")
    public ResponseEntity<List<DriverDTO>> getWorldChampions() {
        return ResponseEntity.ok(f1ChampionService.getWorldChampions());
    }
} 