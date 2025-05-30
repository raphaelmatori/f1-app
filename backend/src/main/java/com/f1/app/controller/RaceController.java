package com.f1.app.controller;

import com.f1.app.dto.RaceDTO;
import com.f1.app.service.RaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://f1-world-champions.vercel.app"})
@Tag(name = "Races", description = "F1 Race Results API")
public class RaceController {

    private final RaceService raceService;

    @Operation(
            summary = "Get races by year",
            description = "Retrieve all Formula 1 races and their results for a specific year"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Races found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RaceDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No races found for the specified year",
                    content = @Content
            )
    })
    @GetMapping("/races/{year}")
    public ResponseEntity<List<RaceDTO>> getRacesByYear(
            @Parameter(description = "Year to get races for (e.g. 2023)")
            @PathVariable Integer year
    ) {
        List<RaceDTO> races = raceService.getRacesByYear(year);
        return races.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(races);
    }
} 
