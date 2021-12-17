package spacegraph.space2d.widget.chip;

import jcog.exe.Exe;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.ContainerSurface;
import spacegraph.space2d.container.graph.GraphEdit2D;
import spacegraph.space2d.widget.port.Wire;

import java.util.Map;
import java.util.function.Supplier;

public class HubMenuChip extends Bordering {

    private final Map<String, Supplier<Surface>> menu;

    public HubMenuChip(Surface the, Map<String,Supplier<Surface>> items) {
        super(the);
        this.menu = items;

        resize(100,100); //temporar
    }

    @Override
    protected void starting() {
        super.starting();

        GraphEdit2D g = parentOrSelf(GraphEdit2D.class); //TODO generic Spawnable interface
        for (Map.Entry<String, Supplier<Surface>> entry : menu.entrySet()) {
            String name = entry.getKey();
            Supplier<Surface> i = entry.getValue();
            ExpandingChip ii = new ExpandingChip(name, i);
            ContainerSurface iii = g.add(ii);//.posRel(bounds, 1, 1, 0.1f, 0.1f); //TODO radial layout

            Exe.runLater(() -> g.addWire(new Wire(HubMenuChip.this, ii)));

        }

    }
}
