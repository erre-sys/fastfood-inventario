package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.application.Conversion;
import ec.com.erre.inventory.domain.Quantity;

/** A quantity and its equivalent (contract: {@code Conversion}). */
record ConversionResponse(QuantityResponse source, QuantityResponse target) {

    record QuantityResponse(String value, String unit) {

        static QuantityResponse from(Quantity quantity) {
            return new QuantityResponse(Decimals.plain(quantity.value()), quantity.unit().code());
        }
    }

    static ConversionResponse from(Conversion conversion) {
        return new ConversionResponse(
                QuantityResponse.from(conversion.source()),
                QuantityResponse.from(conversion.target()));
    }
}
