package spacegraph.util.state;

public interface Contexter extends Context {

    class ContextBranch implements Contexter {

        private final String id;
        private Contexter parent;

        ContextBranch(String id) {
            this.id = id;
        }

        ContextBranch start(Contexter parent) {
            synchronized (this) {
                Contexter prevParent = this.parent;
                if (prevParent!=parent) {
                    if (prevParent!=null)
                        stop();

                    if (parent!=null)
                        start(this.parent = parent);
                }
            }
            return this;
        }

        void stop() {

        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Contexter parent() {
            return parent;
        }
    }
}