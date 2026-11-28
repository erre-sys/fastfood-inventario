package ec.com.erre.inventory.application;

import ec.com.erre.inventory.domain.Unit;
import ec.com.erre.inventory.domain.port.ResultPage;
import ec.com.erre.inventory.domain.port.UnitRepository;
import ec.com.erre.inventory.domain.port.UnitSearch;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** A fake repository for the use case tests: no Spring, no database. Same rules as the adapter. */
class InMemoryUnitRepository implements UnitRepository {

    private final Map<String, Unit> units = new LinkedHashMap<>();

    InMemoryUnitRepository with(Unit... initial) {
        for (Unit unit : initial) {
            units.put(unit.code(), unit);
        }
        return this;
    }

    @Override
    public List<Unit> findAll() {
        return List.copyOf(units.values());
    }

    @Override
    public ResultPage<Unit> search(UnitSearch search) {
        var matching = new ArrayList<>(units.values());
        matching.sort(Comparator.comparing(Unit::code));
        var from = Math.min(search.page() * search.size(), matching.size());
        var to = Math.min(from + search.size(), matching.size());
        return new ResultPage<>(
                matching.subList(from, to), search.page(), search.size(), matching.size());
    }

    @Override
    public Optional<Unit> findByCode(String code) {
        return Optional.ofNullable(units.get(code));
    }

    @Override
    public boolean existsByCode(String code) {
        return units.containsKey(code);
    }

    @Override
    public Unit save(Unit unit) {
        var stored = units.get(unit.code());
        var saved = stored == null ? unit : stored.withName(unit.name()).withActive(unit.active());
        units.put(saved.code(), saved);
        return saved;
    }
}
