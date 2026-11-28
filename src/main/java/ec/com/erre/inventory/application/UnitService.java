package ec.com.erre.inventory.application;

import ec.com.erre.inventory.domain.Quantity;
import ec.com.erre.inventory.domain.Unit;
import ec.com.erre.inventory.domain.UnitAlreadyExistsException;
import ec.com.erre.inventory.domain.UnitNotFoundException;
import ec.com.erre.inventory.domain.port.ResultPage;
import ec.com.erre.inventory.domain.port.UnitRepository;
import ec.com.erre.inventory.domain.port.UnitSearch;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use cases of units of measure. Permissions are checked by the REST adapter. */
@Service
@Transactional(readOnly = true)
public class UnitService {

    private final UnitRepository units;

    public UnitService(UnitRepository units) {
        this.units = units;
    }

    public ResultPage<Unit> list(UnitSearch search) {
        return units.search(search);
    }

    public Unit get(String code) {
        return units.findByCode(code).orElseThrow(() -> new UnitNotFoundException(code));
    }

    @Transactional
    public Unit create(NewUnit newUnit) {
        if (units.existsByCode(newUnit.code())) {
            throw new UnitAlreadyExistsException(newUnit.code());
        }
        return units.save(newUnit.toUnit());
    }

    /** Applies the changes to any unit, standard or business-specific: only name and state. */
    @Transactional
    public Unit update(String code, UnitChanges changes) {
        var unit = get(code);
        if (changes.isEmpty()) {
            return unit;
        }
        if (changes.name() != null) {
            unit = unit.withName(changes.name());
        }
        if (changes.active() != null) {
            unit = unit.withActive(changes.active());
        }
        return units.save(unit);
    }

    /** Converts between units of the same dimension. Inactive units still convert. */
    public Conversion convert(String from, String to, BigDecimal value) {
        var source = new Quantity(value, get(from));
        return new Conversion(source, source.convertTo(get(to)));
    }
}
