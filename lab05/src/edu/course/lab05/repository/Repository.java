package edu.course.lab05.repository;

import java.util.List;
import java.util.Optional;

/**
 * Обобщенный контракт репозитория доступа к данным.
 *
 * @param <T>  тип хранимой сущности
 * @param <ID> тип идентификатора сущности
 */
public interface Repository<T, ID> {

    /**
     * Сохраняет или обновляет сущность.
     *
     * @param entity объект для сохранения
     */
    void save(T entity);

    /**
     * Пакетное сохранение сущностей (демонстрация PECS: Producer Extends).
     * Принимает коллекцию объектов любого подтипа T.
     *
     * @param entities коллекция сущностей
     */
    void saveAll(Iterable<? extends T> entities);

    /**
     * Поиск сущности по идентификатору с возвратом Optional вместо null.
     *
     * @param id идентификатор
     * @return Optional с найденным объектом или Optional.empty()
     */
    Optional<T> findById(ID id);

    /**
     * Возвращает список всех хранящихся сущностей.
     *
     * @return список сущностей
     */
    List<T> findAll();

    /**
     * Удаляет сущность по ее идентификатору.
     *
     * @param id идентификатор
     * @return true, если сущность существовала и была удалена
     */
    boolean deleteById(ID id);

    /**
     * Возвращает общее количество сохраненных сущностей.
     */
    long count();
}
