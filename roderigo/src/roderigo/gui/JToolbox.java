package roderigo.gui;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import roderigo.Controller;

/**
 * Toolbox, part of main window
 * 
 * @author Federico Ferri
 *
 */
public class JToolbox extends JPanel {
	private static final long serialVersionUID = -3897492433424480914L;
	
	private Controller controller;
	
	private static final String sdLabelFmt = "Search Depth: (%d)";
	public final JLabel searchDepthLabel = new JLabel();
	public final JSlider searchDepth = new JSlider(JSlider.HORIZONTAL);
	
	public JToolbox(Controller controller) {
		this.controller = controller;
		
		searchDepth.setMinimum(1);
		searchDepth.setMaximum(10);
		searchDepth.setPaintTicks(true);
		searchDepth.setSnapToTicks(true);
		searchDepth.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int sd = searchDepth.getValue();
				searchDepthLabel.setText(String.format(sdLabelFmt, sd));
				JToolbox.this.controller.setSearchDepth(sd);
			}
		});
		searchDepth.setValue(3);
		
		setLayout(new FlowLayout());
		add(searchDepthLabel);
		add(searchDepth);
	}
}
