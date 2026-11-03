package cz.cdcargo.javascaffold.core.platform.naming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TextsTest {

    @Test
    void testToPascalCase() {
        // Arrange + Act + Assert
        assertEquals("FirstSecondThird", Texts.toPascalCase("fIrSt_second third"));
        assertEquals("FirstSecondThird", Texts.toPascalCase("-first__second.third-"));
        assertEquals("Book42V2", Texts.toPascalCase("book42 v2"));
        assertEquals("ČeskýŽluťoučký", Texts.toPascalCase("ČESKÝ ŽLUŤOUČKÝ"));
        assertNull(Texts.toPascalCase(null));
        assertEquals(" \t", Texts.toPascalCase(" \t"));
    }
}
