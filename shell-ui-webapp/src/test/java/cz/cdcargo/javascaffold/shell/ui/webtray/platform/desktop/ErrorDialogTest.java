package cz.cdcargo.javascaffold.shell.ui.webapp.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ErrorDialogTest {

    @Test
    void testFormatMessageEscapesHtmlCharacters() {
        assertEquals("<html>&lt;Failure&gt; &amp; &quot;details&quot;</html>",
                ErrorDialog.formatMessage("<Failure> & \"details\""));
    }

    @Test
    void testFormatMessagePreservesLineBreaks() {
        assertEquals("<html>First<br>Second<br>Third</html>",
                ErrorDialog.formatMessage("First\r\nSecond\rThird"));
    }

    @Test
    void testFormatMessageWithNullMessage() {
        assertEquals("<html>null</html>", ErrorDialog.formatMessage(null));
    }
}
