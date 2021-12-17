package spacegraph.space2d.phys.fracture.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.stream.IntStream;

/**
 * Optializovany ArrayList.
 *
 * @param <T>
 * @author Marek Benovic
 */
public class MyList<T> extends AbstractList<T> {
    private T[] array;
    private int count;

    /**
     * Inicializuje prazdny list.
     */
    @SuppressWarnings("unchecked")
    public MyList() {
        array = (T[]) new Object[8];
        count = 0;
    }

    /**
     * Inicializuje list z parametra (kopiruje prvky).
     *
     * @param ar
     */
    public MyList(T[] ar) {
        count = ar.length;
        array = (T[]) new Object[count];
        System.arraycopy(ar, 0, array, 0, count);
    }

    /**
     * @return Pocet prvkov v liste.
     */
    @Override
    public int size() {
        return count;
    }

    /**
     * Prida prvok na koniec listu.
     *
     * @param o Vkladany prvok.
     * @return Vrati vzdy true.
     */
    @Override
    public boolean add(T o) {
        if (array.length == count) {
            T[] newArray = (T[]) new Object[count * 2];
            addToArray(newArray);
            array = newArray;
        }
        array[count++] = o;
        return true;
    }

    /**
     * Vlozi do listu prvky z pola.
     *
     * @param ar Pole vkladanych prvkov.
     */
    public void add(T[] ar) {
        int ln = ar.length;
        if (count + ln > array.length) {
            T[] newArray = (T[]) new Object[array.length + ln];
            System.arraycopy(array, 0, newArray, 0, count);
            array = newArray;
        }
        System.arraycopy(ar, 0, array, count, ln);
        count += ln;
    }

    /**
     * Vymaze vsetky prvky zo struktury. Casova zozitost: O(c).
     */
    @Override
    public void clear() {
        count = 0;
    }

    /**
     * Vymaze vsetky prvky v poli a rozsiri ho na velkost n. Velkost pola bude n
     * a vsetky hodnoty budu {@code null}. Casova zlozitost: O(n), kde n je
     * hodnota parametra.
     *
     * @param n Velkost rozsirenia po vyprazdneni pola.
     */
    public void clear(int n) {
        count = n;
    }

    /**
     * @param v Hladany objekt.
     * @return Vrati true, pokial sa v liste nachadza referencia na objekt z parametra.
     * Porovnava referencie.
     */
    @Override
    public boolean contains(Object v) {
        int bound = count;
        return IntStream.range(0, bound).anyMatch(i -> v == array[i]);
    }

    /**
     * Vrati list v poli. Zkopiruje prvky do pola. Casova zlozitost: O(n).
     *
     * @param newArray Referencia na pole, do ktoreho sa nakopiruju hodnoty
     *                 zo struktury.
     * @throws NullPointerException Ak ma parameter hodnotu null.
     */
    public void addToArray(T[] newArray) {
        System.arraycopy(array, 0, newArray, 0, Math.min(count, newArray.length));
    }

    /**
     * Vrati prvok na zadanom indexi.
     *
     * @param index Index vyberaneho prvku.
     * @return Vrati hodnotu na danom indexi.
     * @throws IndexOutOfBoundsException Ak je index mimo rozsah naalokovaneho
     *                                   pola.
     */
    @Override
    public T get(int index) {
        return array[index];
    }

    /**
     * Nastavi prvok na indexi {@code index} hodnotou {@code value}. Casova
     * zlozitost: O(c).
     *
     * @param index Index, na ktorom sa meni hodnota.
     * @param value Hodnota, za aku sa nahradza.
     * @throws IndexOutOfBoundsException Ak je index mimo rozsah naalokovaneho
     *                                   pola. Uzivatel nema prehlad nad tym, ako je pole naalokovane,
     *                                   takze vyhadzovanie vynimky sa neda presne popisat.
     */
    @Override
    public T set(int index, T value) {
        return array[index] = value;
    }

    /**
     * Vymeni prvky na indexi {@code i} a {@code j}. Casova zlozitost: O(c).
     *
     * @param i Index 1.
     * @param j Index 2.
     * @throws IndexOutOfBoundsException Ak je index mimo rozsah naalokovaneho
     *                                   pola. Uzivatel nema prehlad nad tym, ako je pole naalokovane,
     *                                   takze vyhadzovanie vynimky sa neda presne popisat.
     */
    public void swap(int i, int j) {
        T item = array[i];
        array[i] = array[j];
        array[j] = item;
    }

    /**
     * Vymaze prvok na indexi {@code index} tym sposobom, ze do neho presunie
     * posledny prvok a strukturu zkrati o jednu hodnotu. Funkcia preto
     * nezachovava poradie prvkov. Casova zlozitost: O(c).
     *
     * @param index Index mazaneho prvku.
     */
    public void removeAt(int index) {
        array[index] = array[--count];
    }

    /**
     * Zmaze posledny prvok. V pripade, ze je struktura prazdna, zavolanie
     * tejto funkcie sposobi nekonzistenciu dat v strukture, co skor ci neskor
     * vyvola pad programu. To si musi preto programator osetrit explicitne.
     * Casova zlozitost: O(c).
     *
     * @return Vrati posledny prvok, ktory bol zmazany.
     */
    public T removeLast() {
        return array[--count];
    }

    /**
     * Vrati iterator typu T.
     *
     * @return Vrati iterator na 1. prvok listu.
     */
    @Override
    public Iterator<T> iterator() {
        return new MyIterator();
    }

    private class MyIterator implements Iterator<T> {
        private int index;

        MyIterator() {
            index = 0;
        }

        MyIterator(int index) {
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index < count;
        }

        @Override
        public T next() {
            return get(index++);
        }

        @Override
        public void remove() {
            MyList.this.removeAt(index);
        }
    }
}