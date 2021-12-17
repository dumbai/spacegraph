package spacegraph.space2d.meta;

import jcog.data.list.Lst;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.space2d.widget.textedit.TextEdit;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** representative interaction surface for a function call + set of parameters
 *  configured optionally by reflection and annotation analysis
 * */
public class MethodSurface extends Gridding {

    private final List<Supplier<?>> parameterSuppliers;

    public MethodSurface(Method m, Map<String,Object> defaults) {
        add(new VectorLabel(m.getName()));

        parameterSuppliers = new Lst<>(m.getParameterCount());
        Parameter[] p = m.getParameters();

        for (int i = 0, pLength = p.length; i < pLength; i++) {
            Parameter c = p[i];
            edit(c, defaults);
            if (parameterSuppliers.size()!=i+1) {
                System.err.println("warning: edit for " + c+ " was not constructed"); //TODO logger
                parameterSuppliers.add(() -> null);
            }
        }
    }

    protected void edit(Parameter p, Map<String, Object> defaults) {
        Gridding pp = new Gridding();
        String pName = p.getName();
        pp.add(new VectorLabel(pName));
        Class<?> t = p.getType();
        if (t == float.class) {
            //..
        } else if (t == String.class) {
            TextEdit te = new TextEdit(8, 1);
            pp.add(te);
            Object d = defaults.get(pName);
            if (d!=null) {
                te.text(d.toString());
            }
            parameterSuppliers.add(te::text);
        } //..

        add(pp);
    }

}