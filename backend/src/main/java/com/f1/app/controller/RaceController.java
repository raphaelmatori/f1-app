package com.f1.app.controller;

import com.f1.app.model.Race;
import com.f1.app.service.RaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RaceController {

    private final RaceService raceService;
    
    @GetMapping("/races/{year}")
    public ResponseEntity<List<Race>> getRacesByYear(@PathVariable Integer year) {
        List<Race> races = raceService.getRacesByYear(year);
        return races.isEmpty() 
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(races);
    }
} 