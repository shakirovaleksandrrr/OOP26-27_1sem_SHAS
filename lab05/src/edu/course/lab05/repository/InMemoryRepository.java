package edu.course.lab05.repository;

import edu.course.lab05.model.Identifiable;

import java.util.*;

/**
 * Обобщенная реализация репозитория в оперативной памяти (In-Memory).
 * Использует Map<ID, T> для обеспечения константной сложности поиска O(1).
 *
 * @param <T>  тип сущности, обязанный реализовывать Identifiable<ID>
 * @param <ID> тип идентификатора
 */
public class InMemoryRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID> {

    // LinkedHashMap сохраняет порядок вставки и гарантирует O(1) для get, put, remove
    private final Map<ID, T> storage = new LinkedHashMap<>();

    @Override
    public void save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Сущность для сохранения не может быть null");
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Идентификатор сущности не может быть null");
        }
        storage.put(entity.getId(), entity);
    }

    @Override
    public void saveAll(Iterable<? extends T> entities) {
        if (entities == null) {
            return;
        }
        for (T entity : entities) {
            save(entity);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(ID id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }

    @Override
    public long count() {
        return storage.size();
    }
}
