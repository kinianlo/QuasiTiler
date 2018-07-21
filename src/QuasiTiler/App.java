/*
*				App.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

import java.awt.Container;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Label;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

public class App {
	static private int default_dim = 5;
	static private float default_zoom = 10;
	static private float[][] default_bounds = { { 0, 0, 0, 0, 0 }, { 10, 10, 10, 10, 10 } };
	static private float[] default_offsets = { 0.0f, 0.0f, (float) Math.sqrt(5.0) / 2.0f, 0.001f, 0.001f };

	public static void main(String[] args) {
		// Parse arguments.
		//
		// First arg: dimension.
		// Second arg: zoom.
		// Next dimension args: bounds.
		// Next dimension args: relation offsets.
		//
		// Note: argument indexes are reported starting at one, for user
		// friendliness.

		if (args.length > 0)
			try {
				default_dim = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Argument #1 error: " + e.getMessage());
			}
		if (args.length > 1)
			try {
				default_zoom = Float.valueOf(args[1]).floatValue();
			} catch (NumberFormatException e) {
				System.err.println("Argument #2 error: " + e.getMessage());
			}
		for (int i = 0; i < default_dim * 2; ++i)
			if (args.length > i + 2)
				try {
					default_bounds[i % 2][i / 2] = Float.valueOf(args[i + 2]).floatValue();
				} catch (NumberFormatException e) {
					System.err.println("Argument #" + (i + 3) + " error: " + e.getMessage());
				}
		;
		for (int i = 0; i < default_dim; ++i)
			if (args.length > i + 2 + default_dim * 2)
				try {
					default_offsets[i] = Float.valueOf(args[i + 2 + default_dim * 2]).floatValue();
				} catch (NumberFormatException e) {
					System.err.println("Argument #" + (i + 3 + default_dim * 2) + " error: " + e.getMessage());
				}
		;

		Frame frame = new Frame("QuasiTiler");
		frame.setLayout(new BorderLayout(0, 0));
		new Closer(frame);
		QuasiUI ui = build(frame);
		frame.pack();
		frame.setBounds(20, 20, 800, 600);
		frame.setVisible(true);
		ui.recalc();
	}

	public static QuasiUI build(Container cont) {
		// Create data.

		QuasiTiler quasi = new QuasiTiler();
		quasi.setDimension(default_dim);
		quasi.setBounds(default_bounds);
		quasi.setOffsets(default_offsets);

		// Build application views and UI.

		TileView tileView = new TileView(quasi);
		ZoomUI tileZoom = new ZoomUI(tileView);
		tileZoom.setZoom((int) default_zoom);
		TileUI tileUI = new TileUI(tileView);

		ColorView colorView = new ColorView(quasi);
		ZoomUI colorZoom = new ZoomUI(colorView);

		ColorUI colorUI = new ColorUI(quasi);

		OffsetView offsetView = new OffsetView(quasi);
		ZoomUI offsetZoom = new ZoomUI(offsetView);

		GenView genView = new GenView(quasi);
		ZoomUI genZoom = new ZoomUI(genView);

		QuasiUI ui = new QuasiUI(quasi);
		quasi.setInterruptor(ui);

		// Organize UI.
		{
			Panel panel = new Panel(new BorderLayout(0, 0));
			panel.add(tileView, BorderLayout.CENTER);
			panel.add(tileZoom, BorderLayout.SOUTH);
			cont.add(panel, BorderLayout.CENTER);
		}
		{
			Panel panel = new Panel(new BorderLayout(0, 2));
			{
				Panel panel2 = new Panel(new BorderLayout(2, 2));
				panel2.add(ui, BorderLayout.NORTH);
				panel2.add(tileUI, BorderLayout.CENTER);
				panel2.add(colorUI, BorderLayout.SOUTH);
				panel.add(panel2, BorderLayout.NORTH);
			}
			{
				Panel panel2 = new Panel(new GridLayout(2, 1, 2, 2));
				{
					Panel panel3 = new Panel3D(new BorderLayout(0, 0));
					panel3.add(new Label("Offset"), BorderLayout.NORTH);
					panel3.add(offsetView, BorderLayout.CENTER);
					panel3.add(offsetZoom, BorderLayout.SOUTH);
					panel2.add(panel3);
				}
				{
					Panel panel3 = new Panel3D(new BorderLayout(0, 0));
					panel3.add(new Label("Generator"), BorderLayout.NORTH);
					panel3.add(genView, BorderLayout.CENTER);
					panel3.add(genZoom, BorderLayout.SOUTH);
					panel2.add(panel3);
				}
				panel.add(panel2, BorderLayout.CENTER);
			}
			cont.add(panel, BorderLayout.EAST);
		}

		return ui;
	}
}
