package spacegraph.space2d.phys.fracture.fragmentation;

import jcog.math.v2;

/**
 * Interface na definovanie toho, ktore ohniska budu zahrnute vo frakturacnej
 * elipse a ktore nie. V implementacii je definovany ako polelipsoid kombinovany
 * s opacnym polkruhom.
 *
 * @author Marek Benovic
 */
@FunctionalInterface
public interface IContains {
    /**
     * @param point
     * @return Vracia true, pokial sa bod nachadza v utvare.
     */
    boolean contains(v2 point);
}
