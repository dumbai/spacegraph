package spacegraph.util.state;

interface Context {
    String id();

    default String[] tags() {
        return null;
    }

    Contexter parent();
}
