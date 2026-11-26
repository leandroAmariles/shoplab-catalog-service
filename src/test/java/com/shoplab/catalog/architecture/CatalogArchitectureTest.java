package com.shoplab.catalog.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class CatalogArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.shoplab.catalog");

    @Test
    void domainIsPlainJava() {
        noClasses().that().resideInAPackage("com.shoplab.catalog.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..", "..infrastructure..", "..api..", "..mcp..", "org.springframework..",
                        "reactor..")
                .check(CLASSES);
    }

    @Test
    void entryPointsReachTheDatabaseOnlyThroughUseCases() {
        noClasses().that().resideInAnyPackage("..api..", "..mcp..", "..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(CLASSES);
    }

    @Test
    void noCodeComesFromTheMonolithOrAnotherService() {
        noClasses().should().dependOnClassesThat().resideInAnyPackage(
                        "com.shoplab.helpdesk..", "com.shoplab.chat..", "com.shoplab.retrieval..",
                        "com.shoplab.eval..", "com.shoplab.orders..")
                .check(CLASSES);
    }
}
