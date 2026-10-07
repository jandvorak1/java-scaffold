package cz.cdcargo.javascaffold.core.platform.naming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TextsTest {

    @Test
    void testToPascalCaseConvertsSeparatedWords() {
        assertEquals("FirstSecondThird", Texts.toPascalCase("fIrSt_second third"));
    }

    @Test
    void testToPascalCaseTreatsNonAlphanumericCharactersAsSeparators() {
        assertEquals("FirstSecondThird", Texts.toPascalCase("-first__second.third-"));
    }

    @Test
    void testToPascalCasePreservesNumbersWithinWords() {
        assertEquals("Book42V2", Texts.toPascalCase("book42 v2"));
    }

    @Test
    void testToPascalCaseConvertsUnicodeWords() {
        assertEquals("ČeskýŽluťoučký", Texts.toPascalCase("ČESKÝ ŽLUŤOUČKÝ"));
    }

    @Test
    void testToPascalCaseReturnsNull() {
        assertNull(Texts.toPascalCase(null));
    }

    @Test
    void testToPascalCaseReturnsBlankValueUnchanged() {
        assertEquals(" \t", Texts.toPascalCase(" \t"));
    }
}
