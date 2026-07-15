package ru.practicum.category.service;


import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryRequestDto;

public interface CategoryAdminService {

    CategoryDto create(CategoryRequestDto categoryRequestDto);

    CategoryDto update(Long id, CategoryRequestDto categoryRequestDto);

    void delete(Long id);
}
