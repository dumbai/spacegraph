package spacegraph.space2d.phys.fracture.util;

/**
 * Obecny uzol spojoveho zoznamu
 *
 * @param <T>
 * @author Marek Benovic
 */
public class Node<T> {
    /**
     * Hodnota.
     */
    public final T value;
    public final int hash;

    /**
     * Susedne prvky spojoveho zoznamu.
     */
    public Node<T> next;

    /**
     * Inicializuje uzol.
     *
     * @param value
     */
    public Node(T value) {
        this.value = value;
        this.hash = value.hashCode();
    }

    /**
     * Inicializuje vrchol a nastavi referencie na susedne prvky.
     *
     * @param p
     * @param left
     * @param right
     */
    public Node(T p, Node<T> left, Node<T> right) {
        this(p);
        this.next = left;
    }
}