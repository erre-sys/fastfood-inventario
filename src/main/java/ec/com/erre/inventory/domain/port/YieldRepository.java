package ec.com.erre.inventory.domain.port;

import ec.com.erre.inventory.domain.Yield;
import java.util.List;
import java.util.UUID;

/** Yield measurements of the items. They are only added, never changed. */
public interface YieldRepository {

    /** Every measurement of the item, newest first. */
    List<Yield> findByItem(UUID itemId);

    Yield save(Yield measured);
}
