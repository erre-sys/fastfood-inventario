package ec.com.erre.inventory;

import static ec.com.erre.inventory.ApiTokens.admin;
import static ec.com.erre.inventory.ApiTokens.cashier;
import static ec.com.erre.inventory.ApiTokens.seller;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * The units API end to end (security, validation, use cases, PostgreSQL), each response checked
 * against the contract. Every test rolls back.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class UnitApiTest {

    private static final MediaType MERGE_PATCH =
            MediaType.parseMediaType("application/merge-patch+json");
    private static final String PORTION =
            """
            {"code": "portion180", "name": "Porción de 180 g", "dimension": "MASS", "baseFactor": "180"}
            """;

    @Autowired WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc =
                MockMvcBuilders.webAppContextSetup(context)
                        .apply(SecurityMockMvcConfigurers.springSecurity())
                        .build();
    }

    private static MockHttpServletRequestBuilder create(String body) {
        return post("/api/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf());
    }

    private static MockHttpServletRequestBuilder change(String code, String body) {
        return patch("/api/units/" + code).contentType(MERGE_PATCH).content(body).with(csrf());
    }

    // --- List -------------------------------------------------------------------------------

    @Test
    void anyRoleListsTheUnitsInPages() throws Exception {
        mvc.perform(get("/api/units").with(cashier()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(12))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].code").value("cup"))
                .andExpect(ApiContract.matches("UnitPage"));
    }

    @Test
    void listsFilteredSortedAndPaged() throws Exception {
        mvc.perform(
                        get("/api/units")
                                .param("dimension", "MASS")
                                .param("sort", "code,desc")
                                .param("size", "2")
                                .param("page", "1")
                                .with(seller()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].code", contains("lb", "kg")))
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(3))
                .andExpect(ApiContract.matches("UnitPage"));
    }

    @Test
    void searchesByTextAndState() throws Exception {
        mvc.perform(
                        get("/api/units")
                                .param("text", "litro")
                                .param("active", "true")
                                .with(cashier()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].code", contains("l", "ml")));
    }

    @Test
    void rejectsASortFieldOutsideTheContract() throws Exception {
        mvc.perform(get("/api/units").param("sort", "baseFactor").with(cashier()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.title").value("Datos no válidos"))
                .andExpect(jsonPath("$.instance").value("/api/units"))
                .andExpect(jsonPath("$.errors[0].field").value("sort"))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void rejectsAPageLargerThanTheMaximum() throws Exception {
        mvc.perform(get("/api/units").param("size", "101").with(cashier()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("size"))
                .andExpect(jsonPath("$.errors[0].message").value("Debe estar entre 1 y 100."));
    }

    @Test
    void rejectsAnUnknownDimension() throws Exception {
        mvc.perform(get("/api/units").param("dimension", "LENGTH").with(cashier()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("dimension"))
                .andExpect(ApiContract.matches("Problem"));
    }

    // --- Get --------------------------------------------------------------------------------

    @Test
    void getsAStandardUnitWithItsFactorAsText() throws Exception {
        mvc.perform(get("/api/units/kg").with(cashier()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("kg"))
                .andExpect(jsonPath("$.name").value("Kilogramo"))
                .andExpect(jsonPath("$.baseFactor").value("1000"))
                .andExpect(jsonPath("$.standard").value(true))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdBy").value("system"))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(ApiContract.matches("Unit"));
    }

    @Test
    void aMissingUnitIsA404WithItsCode() throws Exception {
        mvc.perform(get("/api/units/onza").with(cashier()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNIT_NOT_FOUND"))
                .andExpect(jsonPath("$.title").value("Unidad no encontrada"))
                .andExpect(jsonPath("$.detail").value("No existe una unidad con el código «onza»."))
                .andExpect(jsonPath("$.instance").value("/api/units/onza"))
                .andExpect(ApiContract.matches("Problem"));
    }

    // --- Create -----------------------------------------------------------------------------

    @Test
    void anAdminCreatesABusinessUnit() throws Exception {
        mvc.perform(create(PORTION).with(admin()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/units/portion180"))
                .andExpect(jsonPath("$.code").value("portion180"))
                .andExpect(jsonPath("$.baseFactor").value("180"))
                .andExpect(jsonPath("$.standard").value(false))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdBy").value("eromero"))
                .andExpect(jsonPath("$.updatedBy", nullValue()))
                .andExpect(ApiContract.matches("Unit"));
    }

    @Test
    void aCashierCannotCreateUnits() throws Exception {
        mvc.perform(create(PORTION).with(cashier()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.title").value("Acceso denegado"))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void anExistingCodeIsAConflict() throws Exception {
        mvc.perform(
                        create(
                                        """
                                        {"code": "kg", "name": "Kilo", "dimension": "MASS", "baseFactor": "1000"}
                                        """)
                                .with(admin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("UNIT_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.detail").value("Ya existe una unidad con el código «kg»."))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void invalidFieldsAreListedOneByOne() throws Exception {
        mvc.perform(
                        create(
                                        """
                                        {"code": "KG!", "name": "", "dimension": "MASS", "baseFactor": "0"}
                                        """)
                                .with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("code")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("name")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("baseFactor")))
                .andExpect(
                        jsonPath(
                                "$.errors[?(@.field == 'baseFactor')].message",
                                hasItem("Debe ser mayor que cero y tener hasta 6 decimales.")))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void aMissingFieldIsRequired() throws Exception {
        mvc.perform(
                        create("{\"code\": \"x1\", \"name\": \"X\", \"dimension\": \"MASS\"}")
                                .with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("baseFactor"))
                .andExpect(jsonPath("$.errors[0].message").value("Es obligatorio."));
    }

    @Test
    void unknownFieldsAreRejected() throws Exception {
        mvc.perform(
                        create(
                                        """
                                        {"code": "x1", "name": "X", "dimension": "MASS", "baseFactor": "1", "standard": true}
                                        """)
                                .with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("standard"))
                .andExpect(ApiContract.matches("Problem"));
    }

    // --- Update (merge patch) ---------------------------------------------------------------

    @Test
    void anAdminRenamesAStandardUnitAndTheChangeIsAudited() throws Exception {
        mvc.perform(change("kg", "{\"name\": \"Kilo\"}").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kilo"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.baseFactor").value("1000"))
                .andExpect(jsonPath("$.createdBy").value("system"))
                .andExpect(jsonPath("$.updatedBy").value("eromero"))
                .andExpect(jsonPath("$.updatedAt", notNullValue()))
                .andExpect(ApiContract.matches("Unit"));
    }

    @Test
    void anAdminInactivatesAUnit() throws Exception {
        mvc.perform(change("cup", "{\"active\": false}").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Taza"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void theImmutableFieldsCannotBePatched() throws Exception {
        mvc.perform(change("kg", "{\"baseFactor\": \"1\"}").with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("baseFactor"))
                .andExpect(
                        jsonPath("$.errors[0].message")
                                .value("Este campo no existe o no se puede cambiar."))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void anEmptyPatchIsRejected() throws Exception {
        mvc.perform(change("kg", "{}").with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(
                        jsonPath("$.detail")
                                .value("Envía al menos un campo para cambiar: name o active."))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void aNameCannotBeRemoved() throws Exception {
        mvc.perform(change("kg", "{\"name\": null}").with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("No puede ser nulo."));
    }

    @Test
    void aBlankNameIsRejected() throws Exception {
        mvc.perform(change("kg", "{\"name\": \"   \"}").with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void aPatchMustBeAMergePatch() throws Exception {
        mvc.perform(
                        patch("/api/units/kg")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \"Kilo\"}")
                                .with(csrf())
                                .with(admin()))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void aSellerCannotChangeUnits() throws Exception {
        mvc.perform(change("kg", "{\"name\": \"Kilo\"}").with(seller()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void patchingAMissingUnitIsA404() throws Exception {
        mvc.perform(change("onza", "{\"name\": \"Onza\"}").with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNIT_NOT_FOUND"));
    }
}
