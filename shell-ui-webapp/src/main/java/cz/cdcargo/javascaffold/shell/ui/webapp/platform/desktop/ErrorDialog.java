package cz.cdcargo.javascaffold.shell.ui.webapp.platform.desktop;

import java.awt.Dimension;
import java.awt.Font;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * Displays a modal error dialog with optional expandable exception details.
 *
 * The dialog must be created and used on the Swing event dispatch thread. Error
 * messages are displayed as literal text and line breaks are preserved. When an
 * exception is supplied, its stack trace is initially hidden and can be shown
 * or hidden by the user.
 */
public final class ErrorDialog extends JDialog {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a modal error dialog.
     *
     * @param owner       parent frame for the dialog, or null for no owner
     * @param title       dialog title, or null for no title
     * @param messageText primary error message, or null to display the text null
     * @param detailText  initial text for the button that expands the details
     * @param hideText    text for the button that hides the details
     * @param showText    text for the button that shows the details again
     * @param closeText   text for the button that closes the dialog
     * @param throwable   exception whose stack trace is displayed in the details,
     *                    or null to omit the details
     * @throws IllegalStateException if called outside the Swing event dispatch
     *                               thread
     */
    public ErrorDialog(JFrame owner, String title, String messageText, String detailText, String hideText,
            String showText, String closeText, Throwable throwable) {
        super(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
        checkEventDispatchThread();
        final Dimension[] collapsedSize = { null };

        setLayout(new MigLayout("fill, insets 15", "[grow]", "[][grow][]"));
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        var messageLabel = new JLabel(formatMessage(messageText), UIManager.getIcon("OptionPane.errorIcon"),
                SwingConstants.LEFT);
        messageLabel.setIconTextGap(10);
        add(messageLabel, "growx, wrap");

        JPanel detailsPanel = null;
        if (throwable != null) {
            var sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));

            var textArea = new JTextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            var scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 200));

            detailsPanel = new JPanel(new MigLayout("fill, insets 0"));
            detailsPanel.add(scrollPane, "grow");
            detailsPanel.setVisible(false);
            add(detailsPanel, "growx, growy, wrap, hidemode 3");
        }

        var buttonPanel = new JPanel(new MigLayout("insets 0", "push[][]"));

        if (detailsPanel != null) {
            var finalDetailsPanel = detailsPanel;
            var detailsButton = new JButton(detailText);
            detailsButton.addActionListener(event -> {
                var visible = !finalDetailsPanel.isVisible();
                finalDetailsPanel.setVisible(visible);
                detailsButton.setText(visible ? hideText : showText);
                pack();
                if (!visible) {
                    setSize(collapsedSize[0]);
                    setMinimumSize(collapsedSize[0]);
                }
            });
            buttonPanel.add(detailsButton);
        }

        var closeButton = new JButton(closeText);
        closeButton.addActionListener(event -> dispose());
        getRootPane().setDefaultButton(closeButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, "growx");
        pack();
        collapsedSize[0] = getSize();
        setMinimumSize(collapsedSize[0]);
        setLocationRelativeTo(owner);
    }

    static String formatMessage(String message) {
        return "<html>" + String.valueOf(message)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace("\n", "<br>")
                + "</html>";
    }

    private static void checkEventDispatchThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("ErrorDialog must be used on the Swing event dispatch thread");
        }
    }
}
