package com.example.defi.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    @DisplayName("Status enum should have UNPAID value")
    void status_shouldHaveUnpaidValue() {
        assertEquals("UNPAID", Status.UNPAID.name());
    }

    @Test
    @DisplayName("Status enum should have PAID value")
    void status_shouldHavePaidValue() {
        assertEquals("PAID", Status.PAID.name());
    }

    @Test
    @DisplayName("Status values should be retrievable")
    void statusValues_shouldBeRetrievable() {
        Status[] values = Status.values();

        assertTrue(values.length >= 2);
    }

    @Test
    @DisplayName("Status valueOf should work correctly")
    void statusValueOf_shouldWorkCorrectly() {
        assertEquals(Status.UNPAID, Status.valueOf("UNPAID"));
        assertEquals(Status.PAID, Status.valueOf("PAID"));
    }

    @Test
    @DisplayName("Status valueOf with invalid value should throw exception")
    void statusValueOf_withInvalidValue_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> Status.valueOf("INVALID"));
    }
}
