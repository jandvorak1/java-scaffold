package cz.cdcargo.javascaffold.core.platform.naming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TextsTest {

    @Test
    void testToPascalCaseConvertsSeparatedWords() {
        // Arrange + Act + Assert
        assertEquals("FirstSecondThird", Texts.toPascalCase("fIrSt_second third"));
    }

    @Test
    void testToPascalCaseTreatsNonAlphanumericCharactersAsSeparators() {
        // Arrange + Act + Assert
        assertEquals("FirstSecondThird", Texts.toPascalCase("-first__second.third-"));
    }

    @Test
    void testToPascalCasePreservesNumbersWithinWords() {
        // Arrange + Act + Assert
        assertEquals("Book42V2", Texts.toPascalCase("book42 v2"));
    }

    @Test
    void testToPascalCaseConvertsUnicodeWords() {
        // Arrange + Act + Assert
        assertEquals("ČeskýŽluťoučký", Texts.toPascalCase("ČESKÝ ŽLUŤOUČKÝ"));
    }

    @Test
    void testToPascalCaseReturnsNull() {
        // Arrange + Act + Assert
        assertNull(Texts.toPascalCase(null));
    }

    @Test
    void testToPascalCaseReturnsBlankValueUnchanged() {
        // Arrange + Act + Assert
        assertEquals(" \t", Texts.toPascalCase(" \t"));
    }
}
