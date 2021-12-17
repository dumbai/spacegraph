package net.propero.rdp.rdp5.disk;

import net.propero.rdp.RdpPacket;
import net.propero.rdp.rdp5.VChannel;

public interface Device {

    int getType();

    String getName();

    void setChannel(VChannel channel);

    int process(RdpPacket data, IRP irp);

}
