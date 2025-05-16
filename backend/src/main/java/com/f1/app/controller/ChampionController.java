package com.f1.app.controller;

import com.f1.app.model.Champion;
import com.f1.app.service.ChampionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChampionController {

    private final ChampionCacheService championCacheService;
    
    @GetMapping("/champions/{year}")
    public ResponseEntity<Champion> getChampion(@PathVariable Integer year) {
        return championCacheService.getChampion(year);
    }
} 
