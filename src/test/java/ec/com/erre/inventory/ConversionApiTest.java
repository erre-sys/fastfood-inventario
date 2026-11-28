package ec.com.erre.inventory;

import static ec.com.erre.inventory.ApiTokens.cashier;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/** The conversions API end to end, each response checked against the contract. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class ConversionApiTest {

    @Autowired WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc =
                MockMvcBuilders.webAppContextSetup(context)
                        .apply(SecurityMockMvcConfigurers.springSecurity())
                        .build();
    }

    @Test
    void convertsWithinTheSameDimension() throws Exception {
        mvc.perform(
                        get("/api/conversions")
                                .param("from", "kg")
                                .param("to", "g")
                                .param("value", "2.5")
                                .with(cashier()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source.value").value("2.5"))
                .andExpect(jsonPath("$.source.unit").value("kg"))
                .andExpect(jsonPath("$.target.value").value("2500.000"))
                .andExpect(jsonPath("$.target.unit").value("g"))
                .andExpect(ApiContract.matches("Conversion"));
    }

    @Test
    void differentDimensionsAreA422() throws Exception {
        mvc.perform(
                        get("/api/conversions")
                                .param("from", "kg")
                                .param("to", "ml")
                                .param("value", "1")
                                .with(cashier()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("INVALID_CONVERSION"))
                .andExpect(
                        jsonPath("$.detail")
                                .value("No se puede convertir kg (MASS) a ml (VOLUME)."))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void anUnknownUnitIsA404() throws Exception {
        mvc.perform(
                        get("/api/conversions")
                                .param("from", "kg")
                                .param("to", "onza")
                                .param("value", "1")
                                .with(cashier()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNIT_NOT_FOUND"));
    }

    @Test
    void theValueIsRequired() throws Exception {
        mvc.perform(get("/api/conversions").param("from", "kg").param("to", "g").with(cashier()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("value"))
                .andExpect(jsonPath("$.errors[0].message").value("Es obligatorio."))
                .andExpect(ApiContract.matches("Problem"));
    }

    @Test
    void aNegativeValueIsRejected() throws Exception {
        mvc.perform(
                        get("/api/conversions")
                                .param("from", "kg")
                                .param("to", "g")
                                .param("value", "-1")
                                .with(cashier()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("value"))
                .andExpect(
                        jsonPath("$.errors[0].message")
                                .value("Debe ser un número no negativo con hasta 3 decimales."));
    }
}
