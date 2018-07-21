/*
*				ColorUI.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright (c) 1999-2010 by Pierre Baillargeon.
*/

package QuasiTiler;

import java.awt.Color;
import java.awt.Container;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;

public class ColorUI extends Panel3D implements MouseListener, View {
    /*
     ** Constructors.
     */

    public ColorUI(QuasiTiler aQuasi) {
        quasi = aQuasi;
        quasi.addView(this);
        setLayout(new GridLayout(2, colors.length / 2));
        for (int i = 0; i < colors.length; ++i) {
            colors[i] = new Canvas();
            colors[i].setSize(18, 18);
            add(colors[i]);
            colors[i].addMouseListener(this);
            Color c = quasi.getColor(i % 16 - 1, i / 16);
            colors[i].setBackground(c);
            colors[i].setVisible(false);
        }
        colors[0].setVisible(true);
        colors[1].setVisible(true);
        updateDimension();
    }

    /*
     ** MouseListener implementation.
     */

    public void mouseClicked(MouseEvent ev) {
    }

    public void mouseEntered(MouseEvent ev) {
    }

    public void mouseExited(MouseEvent ev) {
    }

    public void mousePressed(MouseEvent ev) {
        pressed = ev.getSource();
    }

    public void mouseReleased(MouseEvent ev) {
        Object source = ev.getSource();
        if (source == pressed) {
            for (int i = 0; i < colors.length; ++i) {
                if (colors[i] == source) {
                    Color c = JColorChooser.showDialog(this, "Choose Color", colors[i].getBackground());
                    if (c != null) {
                        if (i % 16 == 0) {
                            colors[0].setBackground(c);
                            colors[16].setBackground(c);
                        } else {
                            colors[i].setBackground(c);
                        }
                        quasi.setColor(i % 16 - 1, i / 16, c);
                        quasi.repaint();
                        break;
                    }
                }
            }
        }
        pressed = null;
    }

    /*
     ** View implementation.
     */

    public void setZoom(double aZoom) {
    }

    public void paint(Graphics gfx) {
        updateDimension();
        super.paint(gfx);
    }

    /*
     ** Internal details.
     */

    private static int getColorFromDim(int dim) {
        return 1 + (dim / 2);
    }

    private void setColorVisibility(int color, boolean isVisible) {
        colors[0 + color].setVisible(isVisible);
        colors[16 + color].setVisible(isVisible);
    }

    private void updateDimension() {
        int newDim = quasi.getDimension();
        int newLimitColor = getColorFromDim(newDim);
        int oldLimitColor = getColorFromDim(dimension);
        if (oldLimitColor > newLimitColor) {
            for (int i = newLimitColor; i < oldLimitColor; ++i)
                setColorVisibility(i, false);
            dimension = newDim;
        } else if (oldLimitColor < newLimitColor) {
            for (int i = oldLimitColor; i < newLimitColor; ++i)
                setColorVisibility(i, true);
            dimension = newDim;
        }
    }

    /*
     ** Data.
     */

    private QuasiTiler quasi;
    private Canvas colors[] = new Canvas[32];
    private Object pressed;
    private int dimension;
}

/* vim: set sw=3 ts=3 : */
