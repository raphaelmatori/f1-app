package com.f1.app.controller;

import com.f1.app.model.Champion;
import com.f1.app.service.ChampionService;
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

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Champions", description = "F1 World Champions API")
public class ChampionController {

    private final ChampionService championService;
    
    @Operation(
        summary = "Get world champion by year",
        description = "Retrieve the Formula 1 World Champion for a specific year"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Champion found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Champion.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No champion found for the specified year",
            content = @Content
        )
    })
    @GetMapping("/champions/{year}")
    public ResponseEntity<Champion> getChampion(
        @Parameter(description = "Year to get champion for (e.g. 2023)")
        @PathVariable Integer year
    ) {
        return championService.getChampion(year);
    }
} 
