package midtermlabproject3;

import javax.swing.*;
import java.awt.*;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A Swing panel that draws a Huffman tree graphically.
 *
 * <p>Left edges are labelled {@code 0} and right edges {@code 1}. Leaves are
 * drawn as green circles showing the character and its frequency; internal
 * nodes are drawn as lavender circles showing the combined frequency.</p>
 *
 * @see HuffmanNode
 */
public class HuffmanTreePanel extends JPanel {

    /** Node radius, horizontal gap between leaves, vertical gap between levels, and outer margin (all in pixels). */
    private static final int R = 20, X_GAP = 56, Y_GAP = 74, MARGIN = 40;

    /** Root of the tree being displayed, or {@code null} if nothing is shown yet. */
    private HuffmanNode root;

    /** Pixel position of each node's centre, keyed by node identity. */
    private final Map<HuffmanNode, Point> pos = new IdentityHashMap<>();

    /** Number of leaves placed so far during layout; gives each leaf its x position. */
    private int leafCounter;

    /** Creates an empty panel with a white background. */
    public HuffmanTreePanel() {
        setBackground(Color.WHITE);
    }

    /**
     * Sets the tree to display, recomputes the layout, and repaints.
     *
     * <p>The panel's preferred size is adjusted to fit the whole tree so that a
     * surrounding scroll pane can scroll it.</p>
     *
     * @param root the root of the tree to draw, or {@code null} to show nothing
     */
    public void setTree(HuffmanNode root) {
        this.root = root;
        pos.clear();
        leafCounter = 0;
        if (root != null) {
            int depth = layout(root, 0);
            setPreferredSize(new Dimension(Math.max(300, leafCounter * X_GAP + 2 * MARGIN),
                                           (depth + 1) * Y_GAP + 2 * MARGIN));
        }
        revalidate();
        repaint();
    }

    /**
     * Computes the position of every node.
     *
     * <p>Leaves are placed left to right; each parent sits above the midpoint of
     * its two children.</p>
     *
     * @param n     the node to place
     * @param depth the depth of {@code n} (the root is at depth 0)
     * @return the maximum depth reached in the subtree rooted at {@code n}
     */
    private int layout(HuffmanNode n, int depth) {
        int y = MARGIN + depth * Y_GAP;
        if (n.isLeaf()) {
            pos.put(n, new Point(MARGIN + leafCounter++ * X_GAP, y));
            return depth;
        }
        int d = Math.max(layout(n.left, depth + 1), layout(n.right, depth + 1));
        pos.put(n, new Point((pos.get(n.left).x + pos.get(n.right).x) / 2, y));
        return d;
    }

    /**
     * Paints the tree: edges first, so that nodes are drawn on top of them.
     *
     * @param g0 the graphics context supplied by Swing
     */
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (root == null) return;
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        drawEdges(g, root);
        drawNodes(g, root);
    }

    /**
     * Recursively draws the lines from a node to its children, each labelled with its bit.
     *
     * @param g the graphics context
     * @param n the parent node whose outgoing edges are drawn
     */
    private void drawEdges(Graphics2D g, HuffmanNode n) {
        if (n.isLeaf()) return;
        Point p = pos.get(n);
        for (int i = 0; i < 2; i++) {
            HuffmanNode c = (i == 0) ? n.left : n.right;
            Point q = pos.get(c);
            g.setColor(Color.GRAY);
            g.drawLine(p.x, p.y, q.x, q.y);
            g.setColor(new Color(0, 90, 170));
            g.drawString(String.valueOf(i), (p.x + q.x) / 2 + (i == 0 ? -12 : 5), (p.y + q.y) / 2);
            drawEdges(g, c);
        }
    }

    /**
     * Recursively draws a node and its descendants as labelled circles.
     *
     * <p>A leaf shows its character above its frequency; a space is shortened to
     * {@code sp} to fit. An internal node shows only its frequency.</p>
     *
     * @param g the graphics context
     * @param n the node to draw
     */
    private void drawNodes(Graphics2D g, HuffmanNode n) {
        Point p = pos.get(n);
        g.setColor(n.isLeaf() ? new Color(200, 235, 200) : new Color(225, 225, 245));
        g.fillOval(p.x - R, p.y - R, 2 * R, 2 * R);
        g.setColor(Color.DARK_GRAY);
        g.drawOval(p.x - R, p.y - R, 2 * R, 2 * R);
        FontMetrics fm = g.getFontMetrics();
        String top = n.isLeaf() ? HuffmanNode.display(n.symbol) : "";
        String freq = String.valueOf(n.frequency);
        if (n.isLeaf()) {
            if (top.length() > 3) top = top.equals("[space]") ? "sp" : top;
            g.drawString(top, p.x - fm.stringWidth(top) / 2, p.y - 2);
            g.drawString(freq, p.x - fm.stringWidth(freq) / 2, p.y + 12);
        } else {
            g.drawString(freq, p.x - fm.stringWidth(freq) / 2, p.y + 5);
        }
        if (!n.isLeaf()) {
            drawNodes(g, n.left);
            drawNodes(g, n.right);
        }
    }
}
