/*
*				Applet.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

public class Applet extends java.applet.Applet {
    private QuasiUI ui;

    public void init() {
        setLayout(new java.awt.GridLayout(1, 1, 2, 2));
        ui = App.build(this);
    }

    public void start() {
        ui.recalc();
    }

    public void stop() {
        ui.interrupt();
    }
}
