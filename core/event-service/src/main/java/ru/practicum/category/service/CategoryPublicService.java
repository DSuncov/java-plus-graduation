package ru.practicum.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.category.CategoryDto;

import java.util.List;

public interface CategoryPublicService {

    List<CategoryDto> getListCategories(Pageable pageable);

    CategoryDto getCategoryById(Long id);
}
