package cz.cdcargo.javascaffold.shell.ui.webapp.platform.desktop;

import java.io.Serial;
import java.util.Objects;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

/**
 * Displays a modal dialog with indeterminate progress while a background task
 * is running.
 *
 * Instances must be created and all methods must be invoked on the Swing event
 * dispatch thread. The dialog cannot be closed by the user and is disposed
 * automatically when its associated worker finishes.
 */
public final class BusyDialog extends JDialog {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String STATE_PROPERTY = "state";
    private static final String MESSAGE_PROPERTY = "message";

    private final JLabel label;

    /**
     * Creates a modal busy dialog.
     *
     * @param owner       parent frame for the dialog, or null for no owner
     * @param title       dialog title, or null for no title
     * @param messageText initial message displayed in the dialog, or null for no
     *                    message
     * @throws IllegalStateException if called outside the Swing event dispatch
     *                               thread
     */
    public BusyDialog(JFrame owner, String title, String messageText) {
        super(owner, true);
        checkEventDispatchThread();
        setResizable(false);
        setTitle(title);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        var panel = new JPanel(new MigLayout("insets 24 32 24 32, gap 8", "[grow]", "[][][][]"));
        label = new JLabel(messageText);
        label.setHorizontalAlignment(JLabel.CENTER);
        panel.add(label, "growx, wrap");

        var progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
        progressBar.putClientProperty("JProgressBar.largeHeight", true);
        panel.add(progressBar, "growx, wrap");

        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Replaces the message displayed in the dialog.
     *
     * @param message new message text, or null to clear the message
     * @throws IllegalStateException if called outside the Swing event dispatch
     *                               thread
     */
    public void setMessage(String message) {
        checkEventDispatchThread();
        label.setText(message);
        pack();
        setLocationRelativeTo(getOwner());
    }

    /**
     * Executes the supplied worker and displays a busy dialog until the worker
     * reaches the done state. Message property changes published by the worker
     * replace the text displayed in the dialog.
     *
     * @param owner       parent frame for the dialog, or null for no owner
     * @param title       dialog title, or null for no title
     * @param messageText initial message displayed in the dialog, or null for no
     *                    message
     * @param worker      background worker whose completion closes the dialog
     * @throws NullPointerException  if worker is null
     * @throws IllegalStateException if called outside the Swing event dispatch
     *                               thread
     */
    public static void run(JFrame owner, String title, String messageText, SwingWorker<?, ?> worker) {
        checkEventDispatchThread();
        Objects.requireNonNull(worker, "Worker must not be null");
        var dialog = new BusyDialog(owner, title, messageText);
        worker.addPropertyChangeListener(event -> {
            switch (event.getPropertyName()) {
                case STATE_PROPERTY -> {
                    if (SwingWorker.StateValue.DONE.equals(event.getNewValue())) {
                        dialog.dispose();
                    }
                }
                case MESSAGE_PROPERTY -> dialog.setMessage((String) event.getNewValue());
            }
        });
        worker.execute();
        dialog.setVisible(true);
    }

    private static void checkEventDispatchThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Busy dialog must be used on the Swing event dispatch thread");
        }
    }
}
