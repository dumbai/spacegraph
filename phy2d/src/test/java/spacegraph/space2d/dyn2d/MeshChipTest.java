//package spacegraph.space2d.dyn2d;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import jcog.Util;
//import jcog.event.Off;
//import jcog.signal.IntRange;
//import jcog.net.UDPeer;
//import spacegraph.SpaceGraph;
//import spacegraph.space2d.SurfaceBase;
//import spacegraph.space2d.container.Gridding;
//import spacegraph.space2d.container.Splitting;
//import spacegraph.space2d.widget.console.TextEdit;
//import spacegraph.space2d.widget.windo.Port;
//import spacegraph.space2d.widget.windo.GraphEdit;
//
//import java.io.IOException;
//
//public class MeshChipTest {
//
//    /**
//     * encapsulates a Mesh node end-point with ports for send and recv
//     */
//    public static class MeshChip extends Gridding {
//
//        final IntRange ttl = new IntRange(3, 1, 5);
//
//        final UDPeer peer;
//        private final Port in, out;
////        private final BagChart<UDPeer.UDProfile> themChart;
////        private final Every display;
//        private Off recv;
//
//        public MeshChip(UDPeer peer) {
//            this.peer = peer;
//            peer.setFPS(5f);
//            this.in = new Port().on(x->{
//                try {
//                    peer.tellSome(Util.toBytes(x), ttl.intValue());
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                }
//            });
//            this.out = new Port();
//
////            this.themChart = new BagChart<>(peer.them);
////            setAt(
////                new Gridding(
////                        new Label(peer.name()),
////                        new LabeledPane("I", in),
////                        new LabeledPane("O", out),
////                        new LabeledPane("them", themChart)
////                )
////            );
////            this.display = new Every(themChart::update, 100);
//        }
//
//        @Override
//        public boolean start(SurfaceBase parent) {
//            if (super.start(parent)){
//                recv = peer.receive.on(this::receive);
//                return true;
//            }
//            return false;
//        }
//
//        protected void receive(UDPeer.MsgReceived x) {
//            try {
//                out.out(Util.fromBytes(x.data(), Object.class));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public boolean stop() {
//            if (super.stop()) {
//                recv.off();
//                recv = null;
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public void prePaint(int dtMS) {
//            super.prePaint(dtMS);
////            display.next();
//        }
//    }
//
//    public static void main(String[] args) throws IOException {
//
//        GraphEdit p = SpaceGraph.wall(800, 800);
//        p.put(new MessageChip(), 1, 1);
//        p.put(new MeshChip(new UDPeer()), 1, 1);
//        p.put(new MeshChip(new UDPeer()), 1, 1);
//    }
//
//    public static class MessageChip extends Splitting {
//
//        final Port out = new Port();
//
//        final TextEdit.TextEditUI t = new TextEdit.TextEditUI(24, 3) {
//            @Override
//            protected void onKeyCtrlEnter() {
//                String t = text();
//                text("");
//                out.out(t);
//            }
//        };
//
//        public MessageChip() {
//            super();
//            split(0.1f);
//            setAt(new TextEdit(t), out);
//        }
//    }
//
//}