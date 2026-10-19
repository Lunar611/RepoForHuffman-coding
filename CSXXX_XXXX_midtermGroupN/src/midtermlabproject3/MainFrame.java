package midtermlabproject3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * The main window of the Huffman Coding application.
 *
 * <p>The window has four tabs:</p>
 * <ol>
 *   <li><b>Build Huffman Code</b> – enter a basis text and view its Huffman table
 *       and storage savings.</li>
 *   <li><b>Text → Huffman Code</b> – convert text into Huffman code bits.</li>
 *   <li><b>Huffman Code → Text</b> – convert Huffman code bits back into text.</li>
 *   <li><b>Huffman Tree</b> – view the tree as a diagram and as a text outline.</li>
 * </ol>
 *
 * <p>Tabs 2 to 4 need a code to be built in tab 1 first.</p>
 *
 * @see HuffmanCoder
 * @see HuffmanTreePanel
 */
public class MainFrame extends JFrame {

    /** The coder built from the current basis text, or {@code null} until the code is built. */
    private HuffmanCoder coder;

    /** Input area for the basis text (tab 1). */
    private final JTextArea basisInput = area(5, true);

    /** Table model for the Huffman table (tab 1); cells are read-only. */
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Character", "Frequency", "Huffman Code", "Code Length", "Total Bits"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    /** Label showing original size, Huffman size and storage savings (tab 1). */
    private final JLabel summary = new JLabel(" ");

    /** Input area for text to encode (tab 2). */
    private final JTextArea encodeInput = area(5, true);

    /** Read-only output area for the encoded bits (tab 2). */
    private final JTextArea encodeOutput = area(5, false);

    /** Input area for bits to decode (tab 3). */
    private final JTextArea decodeInput = area(5, true);

    /** Read-only output area for the decoded text (tab 3). */
    private final JTextArea decodeOutput = area(5, false);

    /** Read-only text outline of the tree (tab 4). */
    private final JTextArea treeText = area(10, false);

    /** Graphical diagram of the tree (tab 4). */
    private final HuffmanTreePanel treePanel = new HuffmanTreePanel();

    /**
     * Creates the window, adds the four tabs, and centres it on screen.
     *
     * <p>Closing the window exits the application.</p>
     */
    public MainFrame() {
        super("Huffman Coding");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Build Huffman Code", buildTab());
        tabs.addTab("2. Text → Huffman Code", encodeTab());
        tabs.addTab("3. Huffman Code → Text", decodeTab());
        tabs.addTab("4. Huffman Tree", treeTab());
        add(tabs);
        setSize(900, 640);
        setLocationRelativeTo(null);
    }

    /**
     * Creates a wrapped, monospaced text area.
     *
     * @param rows     the number of visible rows
     * @param editable whether the user can type in the area
     * @return the new text area
     */
    private static JTextArea area(int rows, boolean editable) {
        JTextArea a = new JTextArea(rows, 40);
        a.setLineWrap(true);
        a.setWrapStyleWord(false);
        a.setEditable(editable);
        a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        return a;
    }

    /**
     * Wraps a component in a panel with a titled border.
     *
     * @param title the border title
     * @param c     the component to wrap
     * @return the new panel
     */
    private static JPanel titled(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }

    // ---------------- tab 1 ----------------

    /**
     * Builds tab 1: the basis text input, the build button, the summary line and the Huffman table.
     *
     * @return the tab's panel
     */
    private JPanel buildTab() {
        JButton build = new JButton("Build Huffman Code");
        build.addActionListener(e -> buildCode());
        JTable table = new JTable(tableModel);
        table.setRowHeight(22);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel top = titled("Basis text (used to create the Huffman code)", new JScrollPane(basisInput));
        JPanel south = new JPanel(new BorderLayout());
        south.add(summary, BorderLayout.CENTER);
        south.add(build, BorderLayout.EAST);
        top.add(south, BorderLayout.SOUTH);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(top, BorderLayout.NORTH);
        p.add(titled("Huffman table", new JScrollPane(table)), BorderLayout.CENTER);
        return p;
    }

    /**
     * Builds the Huffman code from the basis text and refreshes the whole window.
     *
     * <p>On success, fills the table, the storage summary and the tree views, and
     * clears the old results in tabs 2 and 3. If the basis text is invalid, an
     * error dialog is shown and nothing else changes.</p>
     */
    private void buildCode() {
        try {
            coder = new HuffmanCoder(basisInput.getText());
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
            return;
        }
        tableModel.setRowCount(0);
        for (HuffmanCoder.Entry en : coder.getEntries()) {
            tableModel.addRow(new Object[]{HuffmanNode.display(en.symbol()), en.frequency(),
                    en.code(), en.code().length(), en.totalBits()});
        }
        summary.setText(String.format(
                "<html>Original: %d bits (%d chars × %d bits) &nbsp;|&nbsp; Huffman: %d bits &nbsp;|&nbsp; "
                + "<b>Storage savings: %.2f%%</b></html>",
                coder.getOriginalBits(), coder.getOriginalBits() / HuffmanCoder.BITS_PER_CHAR,
                HuffmanCoder.BITS_PER_CHAR, coder.getCompressedBits(), coder.getSavingsPercent()));
        treeText.setText(coder.treeAsText());
        treeText.setCaretPosition(0);
        treePanel.setTree(coder.getRoot());
        encodeOutput.setText("");
        decodeOutput.setText("");
    }

    // ---------------- tab 2 ----------------

    /**
     * Builds tab 2: converts text into Huffman code bits.
     *
     * @return the tab's panel
     */
    private JPanel encodeTab() {
        JButton go = new JButton("Convert to Huffman Code");
        go.addActionListener(e -> {
            if (!ready()) return;
            try {
                encodeOutput.setText(coder.encode(encodeInput.getText()));
            } catch (IllegalArgumentException ex) {
                encodeOutput.setText("");
                error(ex.getMessage());
            }
        });
        return ioTab("Text to convert", encodeInput, go, "Huffman code", encodeOutput);
    }

    // ---------------- tab 3 ----------------

    /**
     * Builds tab 3: converts Huffman code bits back into text.
     *
     * @return the tab's panel
     */
    private JPanel decodeTab() {
        JButton go = new JButton("Convert to Text");
        go.addActionListener(e -> {
            if (!ready()) return;
            try {
                decodeOutput.setText(coder.decode(decodeInput.getText()));
            } catch (IllegalArgumentException ex) {
                decodeOutput.setText("");
                error(ex.getMessage());
            }
        });
        return ioTab("Huffman code to convert (0s and 1s)", decodeInput, go, "Text equivalent", decodeOutput);
    }

    /**
     * Lays out an input/output tab: an input area with a button above a read-only output area.
     *
     * @param inTitle  the title of the input area
     * @param in       the input text area
     * @param go       the button that runs the conversion
     * @param outTitle the title of the output area
     * @param out      the output text area
     * @return the tab's panel
     */
    private JPanel ioTab(String inTitle, JTextArea in, JButton go, String outTitle, JTextArea out) {
        JPanel p = new JPanel(new GridLayout(2, 1, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel top = titled(inTitle, new JScrollPane(in));
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btn.add(go);
        top.add(btn, BorderLayout.SOUTH);
        p.add(top);
        p.add(titled(outTitle, new JScrollPane(out)));
        return p;
    }

    // ---------------- tab 4 ----------------

    /**
     * Builds tab 4: the tree diagram above the text outline, in a resizable split pane.
     *
     * @return the tab's panel
     */
    private JPanel treeTab() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                titled("Tree diagram (left edge = 0, right edge = 1)", new JScrollPane(treePanel)),
                titled("Tree outline  —  * = internal node, number = frequency", new JScrollPane(treeText)));
        split.setResizeWeight(0.7);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(split);
        return p;
    }

    // ---------------- helpers ----------------

    /**
     * Checks that a Huffman code has been built, and shows an error dialog if not.
     *
     * @return {@code true} if a code is available, {@code false} otherwise
     */
    private boolean ready() {
        if (coder == null) {
            error("Please build the Huffman code first (tab 1).");
            return false;
        }
        return true;
    }

    /**
     * Shows an error dialog.
     *
     * @param msg the message to display
     */
    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
