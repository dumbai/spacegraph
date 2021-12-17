package spacegraph.util.state;


/** root of a semantic spacegraph
 *      -provides matched state values for path keys
 *      -saves state by leaf requests
 *      -updates matching leafs on state changes
 */
public abstract class Root implements Contexter {

    public abstract Iterable get(StatePath p);

    /** the 'id' of a root node corresponds to the overall user context, which
     * the user controls to switch between global states
     */
    private String id = MatchPath.STAR;

    public Root id(String mode) {
        this.id = mode;
        return this;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Contexter parent() {
        return null;
    }

    public static void main(String[] args) {
        
        DummyRoot root = new DummyRoot();

        ContextBranch x = new ContextBranch("x");
        ContextBranch y = new ContextBranch("y");
        x.start(root);
        y.start(x);
        StatePath xr = StatePath.toRoot(x);
        StatePath yr = StatePath.toRoot(y);
        System.out.println(xr);
        System.out.println(yr);
        System.out.println(xr.ids(false));
        System.out.println(xr.ids(true));
        System.out.println(yr.ids(true));
        System.out.println(xr.types(false));
        System.out.println(xr.types(true));
        System.out.println(yr.types(true));


//        root.user.put("x", "abc");
//        root.user.get("x", 3, (d)->{
//            System.out.println(d);
//            return true;
//        });

    }

    private static class DummyRoot extends Root {

//        final User user = new User();



        @Override
        public Iterable get(StatePath p) {
            return null;
        }
    }










}
