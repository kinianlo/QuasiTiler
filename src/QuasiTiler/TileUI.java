/*
*				TileUI.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

import java.awt.Panel;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.GridLayout;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class TileUI extends Panel3D implements ItemListener {
    /**
     *** Constructors.
     **/

    public TileUI(TileView aView) {
        super(new GridLayout(3, 2));
        view = aView;
        addCB(displayTiles);
        add(new Panel());
        addCB(displayEdges);
        add(width);
        width.addItemListener(this);
        addCB(displayCentroids);
        add(new Panel());
        view.setEdgeWidth(width.getSelectedIndex() + 1);
    }

    private void addCB(Checkbox cb) {
        cb.addItemListener(this);
        add(cb);
    }

    /**
     *** ItemListener implementation.
     **/

    public void itemStateChanged(ItemEvent ev) {
        final Object source = ev.getSource();
        if (source == displayTiles) {
            view.displayTiles(displayTiles.getState());
            view.repaint();
            return;
        }
        if (source == displayEdges) {
            view.displayEdges(displayEdges.getState());
            view.repaint();
            return;
        }
        if (source == displayCentroids) {
            view.displayCentroids(displayCentroids.getState());
            view.repaint();
            return;
        }
        if (source == width) {
            view.setEdgeWidth(width.getSelectedIndex() + 1);
            view.repaint();
            return;
        }
    }

    /**
     *** Data.
     **/

    private TileView view;
    private Checkbox displayTiles = new Checkbox("Display Tiles", true);
    private Checkbox displayEdges = new Checkbox("Display Edges", true);
    private Checkbox displayCentroids = new Checkbox("Display Centroids", false);
    private Choice width = new Choice();

    {
        for (int i = 1; i <= 10; ++i) {
            width.add("" + i);
        }
    }
}
