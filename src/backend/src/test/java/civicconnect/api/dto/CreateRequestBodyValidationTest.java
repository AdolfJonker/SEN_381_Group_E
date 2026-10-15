package civicconnect.api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CreateRequestBodyValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<ConstraintViolation<CreateRequestBody>> validate(String title, String description, String category) {
        return validator.validate(new CreateRequestBody(title, description, category));
    }

    private Set<String> fieldsWithViolations(Set<ConstraintViolation<CreateRequestBody>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("TC-E-07 Valid title, description and category are accepted")
    void validBodyIsAccepted() {
        var violations = validate("Broken tap", "Dripping since morning", "Maintenance");
        assertTrue(violations.isEmpty(), "Expected no violations but got: " + violations);
    }

    @Test
    @DisplayName("TC-E-11 Title of length 1 is accepted")
    void titleLengthOneIsAccepted() {
        var violations = validate("a", "Dripping tap", "Maintenance");
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("TC-E-11 Title of length 200 is accepted")
    void titleLength200IsAccepted() {
        var violations = validate("a".repeat(200), "Dripping tap", "Maintenance");
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("TC-E-11 Title of length 201 is rejected")
    void titleLength201IsRejected() {
        var violations = validate("a".repeat(201), "Dripping tap", "Maintenance");
        assertEquals(Set.of("title"), fieldsWithViolations(violations));
    }

    @Test
    @DisplayName("TC-E-11 Empty title is rejected")
    void emptyTitleIsRejected() {
        var violations = validate("", "Dripping tap", "Maintenance");
        assertEquals(Set.of("title"), fieldsWithViolations(violations));
    }

    @Test
    @DisplayName("TC-E-34 Title made only of spaces is rejected after trim")
    void spacesOnlyTitleIsRejected() {
        var violations = validate("     ", "Dripping tap", "Maintenance");
        assertEquals(Set.of("title"), fieldsWithViolations(violations));
    }

    @Test
    @DisplayName("TC-E-34 Spaces around a 200-character title are trimmed before the limit is checked")
    void paddedTitleIsTrimmedBeforeValidation() {
        var violations = validate("   " + "a".repeat(200) + "   ", "Dripping tap", "Maintenance");
        assertTrue(violations.isEmpty(), "Expected no violations but got: " + violations);
    }

    @Test
    @DisplayName("Empty description is rejected")
    void emptyDescriptionIsRejected() {
        var violations = validate("Broken tap", "", "Maintenance");
        assertEquals(Set.of("description"), fieldsWithViolations(violations));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("TC-E-08 / TC-E-09 Missing, null, empty or blank category is rejected")
    void blankCategoryIsRejected(String category) {
        var violations = validate("Broken tap", "Dripping tap", category);
        assertEquals(Set.of("category"), fieldsWithViolations(violations));
        assertEquals("Choose a category.", violations.iterator().next().getMessage());
    }
}
