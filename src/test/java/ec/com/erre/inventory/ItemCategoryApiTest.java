package ec.com.erre.inventory;

import static ec.com.erre.inventory.ApiTokens.admin;
import static ec.com.erre.inventory.ApiTokens.cashier;
import static org.hamcrest.Matchers.contains;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/** The item categories API end to end, each response checked against the contract. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class ItemCategoryApiTest {

    private static final MediaType MERGE_PATCH =
            MediaType.parseMediaType("application/merge-patch+json");

    @Autowired WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc =
                MockMvcBuilders.webAppContextSetup(context)
                        .apply(SecurityMockMvcConfigurers.springSecurity())
                        .build();
    }

    private String create(String name) throws Exception {
        var body =
                mvc.perform(
                                post("/item-categories")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"name\": \"" + name + "\"}")
                                        .with(csrf())
                                        .with(admin()))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    void anAdminCreatesACategory() throws Exception {
        mvc.perform(
                        post("/item-categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \" Lácteos \"}")
                                .with(csrf())
                                .with(admin()))
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        "Location",
                                        startsWith("http://localhost/item-categories/")))
                .andExpect(jsonPath("$.name").value("Lácteos"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(ApiContract.matches("ItemCategory"));
    }

    @Test
    void listsByNameAndFiltersByState() throws Exception {
        create("Vegetales");
        var groceries = create("Abarrotes");
        create("Lácteos");
        mvc.perform(
                        patch("/item-categories/" + groceries)
                                .contentType(MERGE_PATCH)
                                .content("{\"active\": false}")
                                .with(csrf())
                                .with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(ApiContract.matches("ItemCategory"));

        mvc.perform(get("/item-categories").with(cashier()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains("Abarrotes", "Lácteos", "Vegetales")));
        mvc.perform(get("/item-categories").param("active", "true").with(cashier()))
                .andExpect(jsonPath("$[*].name", contains("Lácteos", "Vegetales")));
    }

    @Test
    void aRepeatedNameIsAConflict() throws Exception {
        create("Lácteos");

        mvc.perform(
                        post("/item-categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \"LÁCTEOS\"}")
                                .with(csrf())
                                .with(admin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_ALREADY_EXISTS"))
                .andExpect(
                        jsonPath("$.detail")
                                .value("Ya existe una categoría con el nombre «LÁCTEOS»."))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void aCashierCannotCreateCategories() throws Exception {
        mvc.perform(
                        post("/item-categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \"Lácteos\"}")
                                .with(csrf())
                                .with(cashier()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void aMissingCategoryIsA404() throws Exception {
        mvc.perform(
                        patch("/item-categories/0b6f8c3e-4a51-4d1b-9a0e-6f2d3c1b7a10")
                                .contentType(MERGE_PATCH)
                                .content("{\"name\": \"X\"}")
                                .with(csrf())
                                .with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void anEmptyPatchIsRejected() throws Exception {
        var dairy = create("Lácteos");

        mvc.perform(
                        patch("/item-categories/" + dairy)
                                .contentType(MERGE_PATCH)
                                .content("{}")
                                .with(csrf())
                                .with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Envía al menos un campo para cambiar."));
    }
}
