/*
*				QuasiUI.java
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
import java.awt.Choice;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class QuasiUI extends Panel implements AdjustmentListener, ItemListener, Runnable, Interruptor {
    /**
     *** Constructors.
     **/

    public QuasiUI(QuasiTiler aQuasi) {
        super(new BorderLayout(2, 2));

        quasi = aQuasi;

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY - 2);
        thread.start();

        selected_dim = quasi.getDimension() - dim_base;
        dim.select(selected_dim);
        mod_dim.select(selected_dim + dim_base - mod_dim_base);
        updateSelectedDim();

        dim.addItemListener(this);
        mod_dim.addItemListener(this);
        low.addAdjustmentListener(this);
        high.addAdjustmentListener(this);
        offset.addAdjustmentListener(this);

        low.setBlockIncrement(scale_factor);
        high.setBlockIncrement(scale_factor);
        offset.setBlockIncrement(scale_factor);

        {
            Panel panel = new Panel3D(new BorderLayout(2, 2));
            panel.add(dim_label, BorderLayout.WEST);
            panel.add(dim, BorderLayout.CENTER);
            add(panel, BorderLayout.NORTH);
        }
        {
            Panel panel = new Panel3D(new BorderLayout(2, 2));
            {
                Panel panel2 = new Panel(new BorderLayout());
                panel2.add(mod_dim_label, BorderLayout.WEST);
                panel2.add(mod_dim, BorderLayout.CENTER);
                panel.add(panel2, BorderLayout.NORTH);
            }
            {
                Panel panel2 = new Panel(new GridLayout(3, 1, 2, 2));
                {
                    Panel panel3 = new Panel(new BorderLayout());
                    panel3.add(low_label, BorderLayout.WEST);
                    panel3.add(low, BorderLayout.CENTER);
                    panel2.add(panel3);
                }
                {
                    Panel panel3 = new Panel(new BorderLayout());
                    panel3.add(high_label, BorderLayout.WEST);
                    panel3.add(high, BorderLayout.CENTER);
                    panel2.add(panel3);
                }
                {
                    Panel panel3 = new Panel(new BorderLayout());
                    panel3.add(offset_label, BorderLayout.WEST);
                    panel3.add(offset, BorderLayout.CENTER);
                    panel2.add(panel3);
                }
                panel.add(panel2, BorderLayout.CENTER);
            }
            add(panel, BorderLayout.CENTER);
        }
    }

    /**
     *** Computers.
     **/

    public synchronized void recalc() {
        stopNow = true;
        please_recalc = true;
        notifyAll();
    }

    public synchronized void interrupt() {
        stopNow = true;
    }

    /**
     *** ItemListener implementation.
     **/

    public void itemStateChanged(ItemEvent ev) {
        final Object source = ev.getSource();
        if (source == dim) {
            quasi.setDimension(dim.getSelectedIndex() + dim_base);
            recalc();
            return;
        }
        if (source == mod_dim) {
            selected_dim = mod_dim.getSelectedIndex();
            updateSelectedDim();
            return;
        }
    }

    private void updateSelectedDim() {
        float value = quasi.getBound(0, selected_dim);
        low.setValue((int) (value * scale_factor));
        low_label.setText(low_text + value);

        value = quasi.getBound(1, selected_dim);
        high.setValue((int) (value * scale_factor));
        high_label.setText(high_text + value);

        value = quasi.getOffset(selected_dim);
        offset.setValue((int) (value * scale_factor));
        offset_label.setText(offset_text + value);
    }

    /**
     *** AdjustmentListener implementation.
     **/

    public void adjustmentValueChanged(AdjustmentEvent ev) {
        final Object source = ev.getSource();
        float value = ev.getValue() / (float) scale_factor;
        if (source == low) {
            low_label.setText(low_text + value);
            quasi.setBound(value, 0, selected_dim);
            recalc();
            return;
        }
        if (source == high) {
            high_label.setText(high_text + value);
            quasi.setBound(value, 1, selected_dim);
            recalc();
            return;
        }
        if (source == offset) {
            offset_label.setText(offset_text + value);
            quasi.setOffset(value, selected_dim);
            recalc();
            return;
        }
    }

    /**
     *** Runnable implementation.
     **/

    public void run() {
        while (true) {
            synchronized (this) {
                while (!please_recalc) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                    }
                }
                please_recalc = false;
            }
            stopNow = false;
            quasi.recalc();
            quasi.repaint();
            try {
                Thread.sleep(33);
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     *** Interruptor implementation.
     **/

    public boolean interrupted() {
        return stopNow;
    }

    /**
     *** Data.
     **/

    private static final int default_dim = 5;

    private boolean stopNow;
    private boolean please_recalc;
    private QuasiTiler quasi;
    private int selected_dim = default_dim;

    private int scale_factor = 1000;

    private static final int dim_base = 3;
    private static final String dim_text = "Number of dimensions: ";
    private Label dim_label = new Label(dim_text);
    private Choice dim = new Choice();

    private static final int mod_dim_base = 1;
    private Label mod_dim_label = new Label("Manipulate dimension: ");
    private Choice mod_dim = new Choice();

    private static final int default_low = 0;
    private static final String low_text = "Lower boundary: ";
    private Label low_label = new Label(low_text + default_low);
    private Scrollbar low = new Scrollbar(Scrollbar.HORIZONTAL, default_low, 1 * scale_factor, -100 * scale_factor,
            100 * scale_factor);

    private static final int default_high = 20;
    private static final String high_text = "Higher boundary: ";
    private Label high_label = new Label(high_text + default_high);
    private Scrollbar high = new Scrollbar(Scrollbar.HORIZONTAL, default_high, 1 * scale_factor, 0, 100 * scale_factor);

    private static final int default_offset = 0;
    private static final String offset_text = "Offset: ";
    private Label offset_label = new Label(offset_text + default_offset);
    private Scrollbar offset = new Scrollbar(Scrollbar.HORIZONTAL, default_offset, 1 * scale_factor, 0,
            100 * scale_factor);

    {
        for (int i = 3; i <= 15; ++i) {
            dim.add("" + i);
        }

        for (int i = 1; i <= 15; ++i) {
            mod_dim.add("" + i);
        }
    }
}
