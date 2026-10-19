package midtermlabproject3;

import java.util.*;

/**
 * Builds a Huffman tree from a basis text and uses it to encode and decode text.
 *
 * <p>On construction the coder counts how often each character occurs in the
 * basis text, then repeatedly merges the two lowest-frequency nodes until a
 * single tree remains. The lower-frequency node becomes the left child
 * (bit {@code 0}) and the other becomes the right child (bit {@code 1}).
 * Each character's code is the path of bits from the root to its leaf.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * HuffmanCoder coder = new HuffmanCoder("abracadabra");
 * String bits = coder.encode("abra");
 * String text = coder.decode(bits);   // "abra"
 * }</pre>
 *
 * @see HuffmanNode
 */
public class HuffmanCoder {

    /** Bits used per character in uncompressed (fixed-length, ASCII-style) storage. */
    public static final int BITS_PER_CHAR = 8;

    /**
     * One row of the Huffman table.
     *
     * @param symbol    the character
     * @param frequency how many times the character occurs in the basis text
     * @param code      the character's Huffman code, as a string of {@code 0} and {@code 1}
     */
    public record Entry(char symbol, int frequency, String code) {

        /**
         * Returns the total bits this character takes up in the compressed text.
         *
         * @return {@code frequency * code.length()}
         */
        public int totalBits() { return frequency * code.length(); }
    }

    /** Character frequencies from the basis text, in order of first appearance. */
    private final Map<Character, Integer> frequencies = new LinkedHashMap<>();

    /** Lookup from each character to its Huffman code. */
    private final Map<Character, String> codes = new HashMap<>();

    /** The Huffman table rows, sorted for display. */
    private final List<Entry> entries = new ArrayList<>();

    /** Root of the Huffman tree. */
    private HuffmanNode root;

    /** Size of the basis text in bits when stored with {@link #BITS_PER_CHAR} bits per character. */
    private int originalBits;

    /** Size of the basis text in bits when stored with Huffman codes. */
    private int compressedBits;

    /**
     * Builds the Huffman tree, code table and size statistics for a basis text.
     *
     * <p>Entries are sorted by frequency (highest first), then by code length,
     * then by character. If the basis text contains only one distinct character,
     * that character is given the code {@code "0"}.</p>
     *
     * @param basis the text whose character frequencies determine the codes
     * @throws IllegalArgumentException if {@code basis} is {@code null} or empty
     */
    public HuffmanCoder(String basis) {
        if (basis == null || basis.isEmpty()) {
            throw new IllegalArgumentException("Please enter some text to build the Huffman code from.");
        }
        for (char c : basis.toCharArray()) frequencies.merge(c, 1, Integer::sum);

        int order = 0;
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> e : frequencies.entrySet()) {
            pq.add(new HuffmanNode(e.getKey(), e.getValue(), order++));
        }
        while (pq.size() > 1) {
            HuffmanNode a = pq.poll();   // smaller -> left (0)
            HuffmanNode b = pq.poll();   // larger  -> right (1)
            pq.add(new HuffmanNode(a, b, order++));
        }
        root = pq.poll();

        if (root.isLeaf()) {             // only one distinct character
            codes.put(root.symbol, "0");
        } else {
            assign(root, "");
        }

        for (Map.Entry<Character, Integer> e : frequencies.entrySet()) {
            entries.add(new Entry(e.getKey(), e.getValue(), codes.get(e.getKey())));
        }
        entries.sort(Comparator.comparingInt(Entry::frequency).reversed()
                .thenComparingInt(en -> en.code().length())
                .thenComparing(Entry::symbol));

        originalBits = basis.length() * BITS_PER_CHAR;
        for (Entry en : entries) compressedBits += en.totalBits();
    }

    /**
     * Walks the tree recursively and records the code of every leaf.
     *
     * @param n      the current node
     * @param prefix the bits collected on the path from the root to {@code n}
     */
    private void assign(HuffmanNode n, String prefix) {
        if (n.isLeaf()) { codes.put(n.symbol, prefix); return; }
        assign(n.left, prefix + "0");
        assign(n.right, prefix + "1");
    }

    // ---------- results ----------

    /**
     * Returns the Huffman table.
     *
     * @return an unmodifiable list of entries, sorted by frequency (highest first)
     */
    public List<Entry> getEntries()  { return Collections.unmodifiableList(entries); }

    /**
     * Returns the root of the Huffman tree.
     *
     * @return the root node; it is a leaf if the basis text had only one distinct character
     */
    public HuffmanNode getRoot()     { return root; }

    /**
     * Returns the size of the basis text without compression.
     *
     * @return the number of characters multiplied by {@link #BITS_PER_CHAR}
     */
    public int getOriginalBits()     { return originalBits; }

    /**
     * Returns the size of the basis text when stored with Huffman codes.
     *
     * @return the sum of {@link Entry#totalBits()} over all entries
     */
    public int getCompressedBits()   { return compressedBits; }

    /**
     * Returns how much storage Huffman coding saves compared with fixed-length storage.
     *
     * @return the savings as a percentage: {@code (original - compressed) * 100 / original}
     */
    public double getSavingsPercent() {
        return (originalBits - compressedBits) * 100.0 / originalBits;
    }

    // ---------- encode ----------

    /**
     * Converts text into a string of Huffman code bits.
     *
     * @param text the text to encode; every character must appear in the Huffman table
     * @return the concatenated codes of each character, as {@code 0}s and {@code 1}s
     * @throws IllegalArgumentException if {@code text} is {@code null} or empty, or if it
     *         contains characters that are not in the Huffman table
     */
    public String encode(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Please enter the text to convert.");
        }
        StringBuilder sb = new StringBuilder();
        Set<Character> missing = new LinkedHashSet<>();
        for (char c : text.toCharArray()) {
            String code = codes.get(c);
            if (code == null) missing.add(c); else sb.append(code);
        }
        if (!missing.isEmpty()) {
            StringJoiner sj = new StringJoiner(", ");
            for (char c : missing) sj.add("'" + HuffmanNode.display(c) + "'");
            throw new IllegalArgumentException(
                "Cannot convert the text. These character(s) are not in the Huffman table: " + sj);
        }
        return sb.toString();
    }

    // ---------- decode ----------

    /**
     * Converts a string of Huffman code bits back into text.
     *
     * <p>Whitespace in the input is ignored. The bits are read one at a time,
     * moving left on {@code 0} and right on {@code 1} from the root; each time a
     * leaf is reached its character is output and the walk restarts at the root.</p>
     *
     * @param bits the code to decode, made up of {@code 0}s and {@code 1}s
     * @return the decoded text
     * @throws IllegalArgumentException if the input is empty, contains characters other
     *         than {@code 0} and {@code 1}, uses a bit the table does not use (single-symbol
     *         tree), or ends in the middle of a character's code
     */
    public String decode(String bits) {
        if (bits == null) bits = "";
        String clean = bits.replaceAll("\\s+", "");
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Please enter the Huffman code to convert.");
        }
        StringBuilder out = new StringBuilder();
        HuffmanNode node = root;
        for (int i = 0; i < clean.length(); i++) {
            char b = clean.charAt(i);
            if (b != '0' && b != '1') {
                throw new IllegalArgumentException(
                    "Invalid character '" + b + "' at position " + (i + 1) + ". A Huffman code may only contain 0 and 1.");
            }
            if (root.isLeaf()) {         // single-symbol tree: every code is "0"
                if (b != '0') throw new IllegalArgumentException("Invalid code: only '0' is used by this table.");
                out.append(root.symbol);
                continue;
            }
            node = (b == '0') ? node.left : node.right;
            if (node.isLeaf()) {
                out.append(node.symbol);
                node = root;
            }
        }
        if (node != root) {
            throw new IllegalArgumentException(
                "Cannot convert the code. It ends in the middle of a character's code (incomplete bits).");
        }
        return out.toString();
    }

    // ---------- text rendering of the tree ----------

    /**
     * Draws the Huffman tree as indented text.
     *
     * <p>Internal nodes are shown as {@code * (frequency)}, leaves as
     * {@code 'c' (frequency)} followed by their code, and each edge is labelled
     * with its bit.</p>
     *
     * @return a multi-line outline of the tree
     */
    public String treeAsText() {
        StringBuilder sb = new StringBuilder();
        sb.append(label(root)).append('\n');
        if (!root.isLeaf()) {
            draw(root.left, "", false, "0", sb);
            draw(root.right, "", true, "1", sb);
        } else {
            sb.append("(single character: code \"0\")\n");
        }
        return sb.toString();
    }

    /**
     * Recursively appends one node, and then its subtrees, to the text outline.
     *
     * @param n      the node to draw
     * @param indent the indentation inherited from the node's ancestors
     * @param last   whether {@code n} is the last (right) child of its parent
     * @param edge   the bit on the edge leading to {@code n}
     * @param sb     the buffer the outline is appended to
     */
    private void draw(HuffmanNode n, String indent, boolean last, String edge, StringBuilder sb) {
        sb.append(indent).append(last ? "└─" : "├─").append('[').append(edge).append("] ").append(label(n));
        if (n.isLeaf()) sb.append("   code=").append(codes.get(n.symbol));
        sb.append('\n');
        if (!n.isLeaf()) {
            String next = indent + (last ? "   " : "│  ");
            draw(n.left, next, false, "0", sb);
            draw(n.right, next, true, "1", sb);
        }
    }

    /**
     * Builds the short label used for a node in the text outline.
     *
     * @param n the node to label
     * @return {@code 'c' (frequency)} for a leaf, or {@code * (frequency)} for an internal node
     */
    private String label(HuffmanNode n) {
        return n.isLeaf() ? "'" + HuffmanNode.display(n.symbol) + "' (" + n.frequency + ")"
                          : "* (" + n.frequency + ")";
    }
}
