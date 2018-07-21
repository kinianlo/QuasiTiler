/*
*				ZoomUI.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.Label;
import java.awt.BorderLayout;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;

public class ZoomUI extends Panel implements AdjustmentListener {
    /**
     *** Constructors.
     **/

    public ZoomUI(View aView) {
        view = aView;
        setLayout(new BorderLayout());
        add(text, BorderLayout.WEST);
        add(zoom, BorderLayout.CENTER);
        zoom.setBlockIncrement(scale_factor);
        zoom.addAdjustmentListener(this);
    }

    /**
     *** Modifiers.
     **/

    public void setZoom(double aZoom) {
        zoom.setValue((int) (aZoom * scale_factor));
        updateZoom(aZoom);
    }

    /**
     *** Computers.
     **/

    private void updateZoom(double aZoom) {
        text.setText("Zoom: " + aZoom);
        view.setZoom(aZoom);
        view.repaint();
    }

    /**
     *** AdjustmentListener implementation.
     **/

    public void adjustmentValueChanged(AdjustmentEvent ev) {
        final Object source = ev.getSource();
        if (source == zoom) {
            updateZoom(ev.getValue() / (double) scale_factor);
            return;
        }
    }

    /**
     *** Data.
     **/

    private View view;
    private static final int default_zoom = 30;
    private static final int scale_factor = 1000;
    private Label text = new Label("Zoom: " + default_zoom);
    private Scrollbar zoom = new Scrollbar(Scrollbar.HORIZONTAL, default_zoom * scale_factor, 1 * scale_factor, 1,
            100 * scale_factor);
}
