package spacegraph.space2d.widget.textedit.keybind;

import jcog.Util;

class Stroke {
    private final SupportKey supportKey;
    private final int key;

    public Stroke(SupportKey supportKey, int key) {
        this.supportKey = supportKey;
        this.key = key;
    }

    @Override
    public String toString() {
        return String.format("Stroke[%s-%s]", supportKey, key);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Stroke stroke) {
            return (this.key == stroke.key) && (this.supportKey == stroke.supportKey);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Util.hashCombine(supportKey, key);
    }
}