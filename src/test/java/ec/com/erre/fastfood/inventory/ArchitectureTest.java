package ec.com.erre.fastfood.inventory;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Architecture rules: if someone breaks them, the build fails and the PR does not get in. */
@AnalyzeClasses(
        packages = "ec.com.erre.fastfood.inventory",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule hexagonalLayers =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .withOptionalLayers(true)
                    .layer("Domain")
                    .definedBy("..domain..")
                    .layer("Application")
                    .definedBy("..application..")
                    .layer("Infrastructure")
                    .definedBy("..infrastructure..")
                    .whereLayer("Infrastructure")
                    .mayNotBeAccessedByAnyLayer()
                    .whereLayer("Application")
                    .mayOnlyBeAccessedByLayers("Infrastructure")
                    .whereLayer("Domain")
                    .mayOnlyBeAccessedByLayers("Application", "Infrastructure");

    @ArchTest
    static final ArchRule domainWithoutFrameworks =
            noClasses()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..", "jakarta.persistence..", "lombok..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule lombokOnlyInPersistence =
            noClasses()
                    .that()
                    .resideOutsideOfPackage("..infrastructure.out.persistence..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("lombok..")
                    .allowEmptyShould(true);
}
