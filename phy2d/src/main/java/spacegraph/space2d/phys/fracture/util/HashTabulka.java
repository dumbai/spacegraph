package spacegraph.space2d.phys.fracture.util;

import jcog.TODO;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Genericka optimalizovana hashovacia tabulka. Implementuje abstraktnu triedu
 * AbstractSet a interface Set, ktore su v standardnej kniznici. Vnutorne datove
 * struktury su protected, takze pri dedeni je ich mozne pouzivat.
 * Princip - separate chaining.
 *
 * @param <T>
 * @author Marek Benovic
 */

public class HashTabulka<T> extends AbstractSet<T> {
    /**
     * Pocet vlozenych vrcholov
     */
    private int count;

    /**
     * hash & n zaruci, ze vysledny hash sa zmesti do hashovacej tabulky.
     * Velkost tabulky je vzdy n + 1 a n je vzdy cislo R^n - 1.
     */
    protected int n;

    /**
     * Hashovacia tabulka
     */
    protected Node<T>[] hashtable;

    /**
     * Inicializuje hashovaciu tabulku.
     */
    public HashTabulka() {
        alocate();
    }

    /**
     * Vytvori hashovaciu tabulku a vlozi do nej hodnoty z parametra.
     *
     * @param array Vkladane hodnoty
     */
    public HashTabulka(T[] array) {
        this();
        addAll(Arrays.asList(array));
    }

    /**
     * Vrati pocet vlozenych prvkov.
     *
     * @return Vrati pocet prvkov v type {@code int}.
     */
    @Override
    public int size() {
        return count;
    }

    /**
     * Prida prvok z parametra do struktury. Neosetruje sa, ci uz bol rovnaky
     * prvok vlozeny. Na hashovanie hodnot sa pouziva funkcia {@code hashCode}.
     * Casova zlozitost: O(c).
     *
     * @param value
     * @return True
     */
    @Override
    public final boolean add(T value) {


        if (count >= n) { 
            realocate();
        }
        Node<T> node = new Node<>(value); 
        int code = node.hash & this.n;
        node.next = hashtable[code];
        hashtable[code] = node; 
        count++; 
        return true;
    }

    /**
     * Odstrani prvok z tabulky. Pri hladani prvku zhoda nastane v pripade, ak sa rovnaju
     * referencie prvkov - pouziva sa na porovnavanie operator {@code ==}.
     * Casova zlozitost: O(c).
     *
     * @param value Hodnota, ktoru treba z tabulky odstranit.
     * @return <tt>true</tt>, ak sa objekt podarilo vymazat, <tt>false</tt> ak
     * sa v strukture nenachadzal.
     */
    @Override
    public boolean remove( Object value) {
        int code = value.hashCode() & n;
        Node<T> zaznam = hashtable[code];
        if (zaznam != null) {
            if (zaznam.value.equals(value)) { 
                hashtable[code] = zaznam.next;
                count--;
                return true;
            } else { 
                for (Node<T> dalsi = zaznam.next; dalsi != null; zaznam = dalsi, dalsi = dalsi.next) {
                    if (dalsi.value.equals(value)) {
                        zaznam.next = dalsi.next;
                        count--;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param o Porovnavany objekt
     * @return Vrati objekt, ktory vracia pri porovnavani funkciou equals true
     */
    public T get(Object o) {
        int oh = o.hashCode();
        return Stream.iterate(hashtable[oh & n], Objects::nonNull, chain -> chain.next).filter(chain -> chain.hash == oh).map(chain -> chain.value).filter(v -> v.equals(o)).findFirst().orElse(null);
    }

    /**
     * @param o
     * @return Vrati true, pokial sa v strukture nachadza dany objekt. Objekty
     * porovnava pomocou funkcie equal.
     */
    @Override
    public boolean contains(Object o) {
        return get(o) != null;
    }

    /**
     * Vymaze vsetky prvky zo struktury.
     */
    @Override
    public void clear() {
        alocate();
    }

    /**
     * Prekopiruje hodnoty do pola z parametra. Poradie je pseudonahodne a prvky
     * mozu byt vo vystupe rozhadzane akokolvek. V pripade, ze ma pole
     * z parametra vacsiu velkost, ako pocet vlozenych prvkov, hodnoty sa
     * zapisuju od zaciatku pola od indexu 0.
     *
     * @param <U> Typ pola
     * @param a   Pole, do ktoreho sa prekopiruju hodnoty.
     */
    @Override
    public <U> U[] toArray(U[] a) {
        if (a == null) {
            throw new NullPointerException();
        }
        int ln = a.length;
        if (ln < count) {
            a = (U[]) Array.newInstance(a.getClass().getComponentType(), count);
        }
        int index = 0;
        for (Node<T> n : hashtable) {
            while (n != null) {
                a[index++] = (U) n.value;
                n = n.next;
            }
        }
        if (ln > count) {
            a[count] = null;
        }
        return a;
    }

    @Override public void forEach(Consumer<? super T> action) {
        for (Node<T> n : hashtable) {
            while (n!=null) {
                action.accept(n.value);
                n = n.next;
            }
        }
    }

    /**
     * Zvacsi hashovaciu tabulku na dvojnasobok a prehashuje vsetky hodnoty v nej.
     */
    private void realocate() {
        n = (n << 1) | 1; 
        Node<T>[] newTable = new Node[n + 1]; 
        for (Node<T> chain : hashtable) { 
            while (chain != null) { 
                Node<T> next = chain.next;
                int code = chain.hash & n;
                chain.next = newTable[code];
                newTable[code] = chain;
                chain = next;
            }
        }
        hashtable = newTable; 
    }

    private void alocate() {
        count = 0;
        n = 1;
        hashtable = new Node[n + 1];
    }

    @Override
    public Iterator<T> iterator() {
        throw new TODO();
    }

    @Override
    public String toString() {
        return getClass() + "@" + hashCode();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}