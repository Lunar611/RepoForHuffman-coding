package midtermlabproject3;

/**
 * A node of the Huffman tree.
 *
 * <p>Leaf nodes hold a character and its frequency. Internal nodes hold no
 * character; their frequency is the sum of their two children's frequencies.
 * Nodes are ordered by frequency, with the creation {@linkplain #order order}
 * used to break ties so that the resulting tree is deterministic.</p>
 *
 * @see HuffmanCoder
 */
public class HuffmanNode implements Comparable<HuffmanNode> {

    /** The character stored in this node, or {@code null} for internal nodes. */
    final Character symbol;

    /** The number of times the symbol occurs, or the combined count of both children. */
    final int frequency;

    /** Creation order of this node, used to break ties between equal frequencies deterministically. */
    final int order;

    /** Left child (edge labelled {@code 0}); {@code null} for leaves. */
    HuffmanNode left;

    /** Right child (edge labelled {@code 1}); {@code null} for leaves. */
    HuffmanNode right;

    /**
     * Creates a leaf node.
     *
     * @param symbol    the character this leaf represents
     * @param frequency how many times the character occurs in the basis text
     * @param order     creation order, used as a tie-breaker when comparing nodes
     */
    HuffmanNode(char symbol, int frequency, int order) {
        this.symbol = symbol;
        this.frequency = frequency;
        this.order = order;
    }

    /**
     * Creates an internal node that joins two subtrees.
     *
     * <p>The new node's frequency is {@code left.frequency + right.frequency}
     * and it holds no symbol.</p>
     *
     * @param left  the left subtree (assigned bit {@code 0})
     * @param right the right subtree (assigned bit {@code 1})
     * @param order creation order, used as a tie-breaker when comparing nodes
     */
    HuffmanNode(HuffmanNode left, HuffmanNode right, int order) {
        this.symbol = null;
        this.frequency = left.frequency + right.frequency;
        this.order = order;
        this.left = left;
        this.right = right;
    }

    /**
     * Tells whether this node is a leaf.
     *
     * @return {@code true} if this node stores a character, {@code false} if it is an internal node
     */
    boolean isLeaf() {
        return symbol != null;
    }

    /**
     * Compares this node with another by frequency, then by creation order.
     *
     * <p>This ordering lets a {@link java.util.PriorityQueue} always return the
     * lowest-frequency node first, and keeps results consistent when frequencies tie.</p>
     *
     * @param o the node to compare against
     * @return a negative integer, zero, or a positive integer if this node is
     *         less than, equal to, or greater than {@code o}
     */
    @Override
    public int compareTo(HuffmanNode o) {
        if (frequency != o.frequency) return Integer.compare(frequency, o.frequency);
        return Integer.compare(order, o.order);
    }

    /**
     * Returns a readable label for a character, making whitespace visible.
     *
     * <p>Space becomes {@code [space]}, and newline, carriage return and tab
     * become {@code \n}, {@code \r} and {@code \t}. Any other character is
     * returned as-is.</p>
     *
     * @param c the character to describe
     * @return a printable label for {@code c}
     */
    static String display(char c) {
        switch (c) {
            case ' ':  return "[space]";
            case '\n': return "\\n";
            case '\r': return "\\r";
            case '\t': return "\\t";
            default:   return String.valueOf(c);
        }
    }
}
