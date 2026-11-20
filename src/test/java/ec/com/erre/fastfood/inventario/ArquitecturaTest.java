package ec.com.erre.fastfood.inventario;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Reglas de arquitectura: si alguien las rompe, el build falla y el PR no entra. */
@AnalyzeClasses(
        packages = "ec.com.erre.fastfood.inventario",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    @ArchTest
    static final ArchRule capasHexagonales =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .withOptionalLayers(true)
                    .layer("Dominio")
                    .definedBy("..dominio..")
                    .layer("Aplicacion")
                    .definedBy("..aplicacion..")
                    .layer("Infraestructura")
                    .definedBy("..infraestructura..")
                    .whereLayer("Infraestructura")
                    .mayNotBeAccessedByAnyLayer()
                    .whereLayer("Aplicacion")
                    .mayOnlyBeAccessedByLayers("Infraestructura")
                    .whereLayer("Dominio")
                    .mayOnlyBeAccessedByLayers("Aplicacion", "Infraestructura");

    @ArchTest
    static final ArchRule dominioSinFrameworks =
            noClasses()
                    .that()
                    .resideInAPackage("..dominio..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..", "jakarta.persistence..", "lombok..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule lombokSoloEnPersistencia =
            noClasses()
                    .that()
                    .resideOutsideOfPackage("..infraestructura.salida.bd..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("lombok..")
                    .allowEmptyShould(true);
}
