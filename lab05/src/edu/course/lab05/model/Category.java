package edu.course.lab05.model;

import java.util.Objects;

/**
 * Категория финансовой операции (например, "Продукты", "Транспорт", "Зарплата").
 * Корректно переопределяет equals и hashCode для использования в Set и Map.
 */
public final class Category implements Comparable<Category> {

    private final String id;
    private final String name;

    public Category(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Идентификатор категории не может быть пустым");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Наименование категории не может быть пустым");
        }
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Category o) {
        return this.name.compareToIgnoreCase(o.name);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, id);
    }
}
