/* ImageSelection.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * (See gpl.txt for details of the GNU General Public License.)
 *
 */
package net.propero.rdp.rdp5.cliprdr;

import net.propero.rdp.Utilities_Localised;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

class ImageSelection implements Transferable {
    
    private final Image image;

    ImageSelection(Image image) {
        this.image = image;
    }

    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{Utilities_Localised.imageFlavor};
    }

    
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Utilities_Localised.imageFlavor.equals(flavor);
    }

    
    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {
        if (!Utilities_Localised.imageFlavor.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        
        return image;
    }
}