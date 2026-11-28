package ec.com.erre.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.networknt.schema.InputFormat;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import java.nio.charset.StandardCharsets;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Checks that a response body matches its schema in the contract ({@code
 * openapi/inventory-api.yaml}), so the code and the contract cannot drift apart unnoticed.
 */
final class ApiContract {

    private static final String CONTRACT = "classpath:openapi/inventory-api.yaml";
    private static final SchemaRegistry SCHEMAS =
            SchemaRegistry.withDialect(Dialects.getOpenApi31());

    private ApiContract() {}

    /** The response body is valid against {@code #/components/schemas/<schema>}. */
    static ResultMatcher matches(String schema) {
        return result -> {
            var body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
            var errors =
                    SCHEMAS.getSchema(
                                    SchemaLocation.of(CONTRACT + "#/components/schemas/" + schema))
                            .validate(body, InputFormat.JSON);
            assertThat(errors)
                    .as("Response against the contract schema %s: %s", schema, body)
                    .isEmpty();
        };
    }
}
