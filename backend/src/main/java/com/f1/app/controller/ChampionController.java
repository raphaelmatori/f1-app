package com.f1.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f1.app.dto.ChampionDTO;
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

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://f1-world-champions.vercel.app"})
@Tag(name = "Champions", description = "F1 World Champions API")
public class ChampionController {

    private final ChampionService championService;

    @Operation(
            summary = "Get all world champions (from 2005 to now)",
            description = "Retrieve all Formula 1 World Champion (from 2005 to now) mapped by year"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Champions found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChampionDTO.class)
                    )
            )
    })
    @GetMapping("/champions")
    public ResponseEntity<List<ChampionDTO>> getChampions() {
        return championService.getChampions();
    }

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
                            schema = @Schema(implementation = ChampionDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No champion found for the specified year",
                    content = @Content
            )
    })
    @GetMapping("/champions/{year}")
    public ResponseEntity<ChampionDTO> getChampion(
            @Parameter(description = "Year to get champion for (e.g. 2023)")
            @PathVariable Integer year
    ) {
        return championService.getChampion(year);
    }
}
