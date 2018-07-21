/*
*				Closer.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

public class Closer extends WindowAdapter {
    public Closer(Frame win) {
        win.addWindowListener(this);
    }

    /**
     *** WindowAdapter extension.
     **/

    public void windowClosing(WindowEvent ev) {
        System.exit(0);
    }
}
