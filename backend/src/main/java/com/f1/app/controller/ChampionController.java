package com.f1.app.controller;

import com.f1.app.model.Champion;
import com.f1.app.repository.ChampionRepository;
import com.f1.app.service.ErgastApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChampionController {

    private final ChampionRepository championRepository;
    private final RestTemplate restTemplate;
    private final ErgastApiService ergastApiService;
    
    @GetMapping("/champions/{year}")
    public ResponseEntity<Champion> getChampion(@PathVariable Integer year) {
        return championRepository.findById(year)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ergastApiService.fetchWorldChampion(year));
    }
} 
