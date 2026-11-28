package ec.com.erre.inventory;

import static ec.com.erre.inventory.ApiTokens.admin;
import static ec.com.erre.inventory.ApiTokens.cashier;
import static ec.com.erre.inventory.ApiTokens.seller;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/** The items API end to end, each response checked against the contract. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class ItemApiTest {

    private static final MediaType MERGE_PATCH =
            MediaType.parseMediaType("application/merge-patch+json");
    private static final String MISSING = "0b6f8c3e-4a51-4d1b-9a0e-6f2d3c1b7a10";

    @Autowired WebApplicationContext context;

    private MockMvc mvc;
    private String dairy;
    private String vegetables;

    @BeforeEach
    void setUp() throws Exception {
        mvc =
                MockMvcBuilders.webAppContextSetup(context)
                        .apply(SecurityMockMvcConfigurers.springSecurity())
                        .build();
        dairy = idOf(category("Lácteos"));
        vegetables = idOf(category("Vegetales"));
    }

    private static String idOf(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private ResultActions category(String name) throws Exception {
        return mvc.perform(
                post("/item-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\"}")
                        .with(csrf())
                        .with(admin()));
    }

    private ResultActions create(String json) throws Exception {
        return mvc.perform(
                post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf())
                        .with(admin()));
    }

    private ResultActions egg() throws Exception {
        return create(
                """
                {"name": "Huevo", "kind": "PURCHASED", "categoryId": "%s", "stockUnit": "unit",
                 "minimumStock": "30", "gramsPerUnit": "60"}
                """
                        .formatted(dairy));
    }

    private ResultActions onion() throws Exception {
        return create(
                """
                {"name": "Cebolla", "kind": "PURCHASED", "categoryId": "%s", "stockUnit": "g",
                 "minimumStock": "2000"}
                """
                        .formatted(vegetables));
    }

    private ResultActions change(String id, String json) throws Exception {
        return mvc.perform(
                patch("/items/" + id)
                        .contentType(MERGE_PATCH)
                        .content(json)
                        .with(csrf())
                        .with(admin()));
    }

    // --- Create and read ----------------------------------------------------------------------

    @Test
    void anAdminCreatesAnItem() throws Exception {
        egg().andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost/items/")))
                .andExpect(jsonPath("$.name").value("Huevo"))
                .andExpect(jsonPath("$.kind").value("PURCHASED"))
                .andExpect(jsonPath("$.category.name").value("Lácteos"))
                .andExpect(jsonPath("$.stockUnit").value("unit"))
                .andExpect(jsonPath("$.dimension").value("COUNT"))
                .andExpect(jsonPath("$.minimumStock").value("30"))
                .andExpect(jsonPath("$.gramsPerUnit").value("60"))
                .andExpect(jsonPath("$.yieldPercent").value("100"))
                .andExpect(jsonPath("$.createdBy").value("eromero"))
                .andExpect(ApiContract.matches("Item"));
    }

    @Test
    void anyRoleReadsItems() throws Exception {
        var id = idOf(egg());
        onion();

        mvc.perform(get("/items/" + id).with(seller()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Huevo"))
                .andExpect(ApiContract.matches("Item"));
        mvc.perform(get("/items").with(cashier()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", contains("Cebolla", "Huevo")))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(ApiContract.matches("ItemPage"));
        mvc.perform(get("/items").param("category", vegetables).with(cashier()))
                .andExpect(jsonPath("$.content[*].name", contains("Cebolla")));
        mvc.perform(get("/items").param("text", "hue").param("kind", "PURCHASED").with(cashier()))
                .andExpect(jsonPath("$.content[*].name", contains("Huevo")));
    }

    @Test
    void aMissingItemIsA404() throws Exception {
        mvc.perform(get("/items/" + MISSING).with(cashier()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ITEM_NOT_FOUND"))
                .andExpect(jsonPath("$.title").value("Insumo no encontrado"))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void aRepeatedNameIsAConflict() throws Exception {
        egg();

        egg().andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ITEM_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.detail").value("Ya existe un insumo con el nombre «Huevo»."))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void aCashierCannotCreateItems() throws Exception {
        mvc.perform(
                        post("/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .with(csrf())
                                .with(cashier()))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"name": "X", "kind": "PURCHASED", "categoryId": "%s", "stockUnit": "g", "minimumStock": "0"}
                                        """
                                                .formatted(dairy))
                                .with(csrf())
                                .with(cashier()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void anUnknownUnitOrCategoryIsAFieldError() throws Exception {
        create(
                        """
                        {"name": "X", "kind": "PURCHASED", "categoryId": "%s", "stockUnit": "saco", "minimumStock": "0"}
                        """
                                .formatted(dairy))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("stockUnit"))
                .andExpect(jsonPath("$.errors[0].message").value("No existe esa unidad."))
                .andExpect(ApiContract.matches("Problem"));
        create(
                        """
                        {"name": "X", "kind": "PURCHASED", "categoryId": "%s", "stockUnit": "g", "minimumStock": "0"}
                        """
                                .formatted(MISSING))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("categoryId"))
                .andExpect(jsonPath("$.errors[0].message").value("No existe esa categoría."));
    }

    @Test
    void gramsPerUnitOnlyForItemsCountedInUnits() throws Exception {
        create(
                        """
                        {"name": "Harina", "kind": "PURCHASED", "categoryId": "%s", "stockUnit": "g",
                         "minimumStock": "0", "gramsPerUnit": "1"}
                        """
                                .formatted(dairy))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("gramsPerUnit"))
                .andExpect(
                        jsonPath("$.errors[0].message")
                                .value("Solo aplica a insumos que se cuentan por unidades."));
    }

    @Test
    void invalidFieldsAreListed() throws Exception {
        create(
                        """
                        {"name": "", "kind": "BOUGHT", "categoryId": "%s", "stockUnit": "g", "minimumStock": "0"}
                        """
                                .formatted(dairy))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("kind"));
        create(
                        """
                        {"name": "", "kind": "PURCHASED", "categoryId": "%s", "stockUnit": "g", "minimumStock": "-1"}
                        """
                                .formatted(dairy))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItems("name", "minimumStock")))
                .andExpect(ApiContract.matches("Problem"));
    }

    // --- Update -------------------------------------------------------------------------------

    @Test
    void anAdminChangesAnItemAndRemovesItsWeight() throws Exception {
        var id = idOf(egg());

        change(
                        id,
                        "{\"name\": \"Huevo de campo\", \"categoryId\": \"%s\", \"gramsPerUnit\": null}"
                                .formatted(vegetables))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Huevo de campo"))
                .andExpect(jsonPath("$.category.name").value("Vegetales"))
                .andExpect(jsonPath("$.gramsPerUnit", nullValue()))
                .andExpect(jsonPath("$.minimumStock").value("30"))
                .andExpect(jsonPath("$.updatedBy").value("eromero"))
                .andExpect(jsonPath("$.updatedAt", notNullValue()))
                .andExpect(ApiContract.matches("Item"));
    }

    @Test
    void theKindAndTheStockUnitCannotBePatched() throws Exception {
        var id = idOf(egg());

        change(id, "{\"stockUnit\": \"g\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("stockUnit"))
                .andExpect(
                        jsonPath("$.errors[0].message")
                                .value("Este campo no existe o no se puede cambiar."));
        change(id, "{\"kind\": \"PREPARED\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("kind"));
    }

    @Test
    void theNameCannotBeRemoved() throws Exception {
        var id = idOf(egg());

        change(id, "{\"name\": null}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("No puede ser nulo."));
    }

    @Test
    void anInactiveCategoryCannotBeAssigned() throws Exception {
        var id = idOf(egg());
        var old = idOf(category("Antigua"));
        mvc.perform(
                        patch("/item-categories/" + old)
                                .contentType(MERGE_PATCH)
                                .content("{\"active\": false}")
                                .with(csrf())
                                .with(admin()))
                .andExpect(status().isOk());

        change(id, "{\"categoryId\": \"%s\"}".formatted(old))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("categoryId"))
                .andExpect(jsonPath("$.errors[0].message").value("La categoría está inactiva."));
    }

    @Test
    void aSellerCannotChangeItems() throws Exception {
        var id = idOf(egg());

        mvc.perform(
                        patch("/items/" + id)
                                .contentType(MERGE_PATCH)
                                .content("{\"minimumStock\": \"1\"}")
                                .with(csrf())
                                .with(seller()))
                .andExpect(status().isForbidden());
    }

    // --- Yields -------------------------------------------------------------------------------

    @Test
    void aMeasuredYieldBecomesTheCurrentOne() throws Exception {
        var id = idOf(onion());

        mvc.perform(
                        post("/items/" + id + "/yields")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"grossQuantity": "1000", "usableQuantity": "910", "note": "Cebolla paiteña pelada"}
                                        """)
                                .with(csrf())
                                .with(admin()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.yieldPercent").value("91"))
                .andExpect(jsonPath("$.wastePercent").value("9"))
                .andExpect(jsonPath("$.note").value("Cebolla paiteña pelada"))
                .andExpect(jsonPath("$.createdBy").value("eromero"))
                .andExpect(ApiContract.matches("Yield"));

        mvc.perform(get("/items/" + id).with(cashier()))
                .andExpect(jsonPath("$.yieldPercent").value("91"));
        mvc.perform(get("/items/" + id + "/yields").with(cashier()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].yieldPercent", contains("91")));
    }

    @Test
    void theUsablePartCannotExceedTheGross() throws Exception {
        var id = idOf(onion());

        mvc.perform(
                        post("/items/" + id + "/yields")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"grossQuantity\": \"100\", \"usableQuantity\": \"101\"}")
                                .with(csrf())
                                .with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("usableQuantity"))
                .andExpect(
                        jsonPath("$.errors[0].message")
                                .value("No puede ser mayor que la cantidad bruta."))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void theYieldsOfAMissingItemAreA404() throws Exception {
        mvc.perform(get("/items/" + MISSING + "/yields").with(cashier()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ITEM_NOT_FOUND"));
    }
}
