package ru.practicum.explore.main.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.main.category.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
