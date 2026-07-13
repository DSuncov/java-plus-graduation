package ru.practicum.compilation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationDto> saveNewCompilation(@Valid @RequestBody @NotNull NewCompilationDto dto) {
        CompilationDto created = compilationService.createNewCompilation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable @NotNull @Positive Long compId) {
        compilationService.deleteCompilation(compId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> patchCompilation(
            @PathVariable @NotNull @Positive Long compId,
            @Valid @RequestBody @NotNull UpdateCompilationRequest updateCompilationRequest) {
        CompilationDto updated = compilationService.patchCompilation(compId, updateCompilationRequest);
        return ResponseEntity.ok(updated);
    }
}