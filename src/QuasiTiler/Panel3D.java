/*
*				Panel3D.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

import java.awt.Panel;
import java.awt.LayoutManager;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;

public class Panel3D extends Panel {
    /**
     *** Constructors.
     **/

    public Panel3D() {
    }

    public Panel3D(LayoutManager mgr) {
        super(mgr);
    }

    public Insets getInsets() {
        return new Insets(4, 4, 4, 4);
    }

    public void paint(Graphics gfx) {
        super.paint(gfx);
        gfx.setColor(Color.gray);
        final Dimension dim = getSize();
        gfx.drawRect(0, 0, dim.width - 1, dim.height - 1);
        gfx.drawRect(1, 1, dim.width - 3, dim.height - 3);
    }
}
