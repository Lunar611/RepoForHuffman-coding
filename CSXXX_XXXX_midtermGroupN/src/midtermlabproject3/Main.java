package midtermlabproject3;

import javax.swing.*;

/**
 * Entry point of the Huffman Coding application.
 *
 * <p>Launches the Swing GUI ({@link MainFrame}) on the Event Dispatch Thread,
 * using the operating system's native look and feel when available.</p>
 *
 * @see MainFrame
 */
public class Main {

    /**
     * Starts the application.
     *
     * <p>The window is created inside {@link SwingUtilities#invokeLater(Runnable)}
     * so that all Swing components are built on the Event Dispatch Thread. If the
     * system look and feel cannot be applied, the default look and feel is used.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
            new MainFrame().setVisible(true);
        });
    }
}
