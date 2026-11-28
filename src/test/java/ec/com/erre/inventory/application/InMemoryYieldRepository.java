package ec.com.erre.inventory.application;

import ec.com.erre.inventory.domain.Yield;
import ec.com.erre.inventory.domain.port.YieldRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A fake repository for the use case tests. Measurements are only added. */
class InMemoryYieldRepository implements YieldRepository {

    private final List<Yield> yields = new ArrayList<>();

    @Override
    public List<Yield> findByItem(UUID itemId) {
        return yields.reversed().stream().filter(y -> y.itemId().equals(itemId)).toList();
    }

    @Override
    public Yield save(Yield measured) {
        yields.add(measured);
        return measured;
    }
}
