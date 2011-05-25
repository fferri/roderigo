package roderigo.gui;

import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JToolbox extends JPanel {
	private static final long serialVersionUID = -3897492433424480914L;
	
	private static final String sdLabelFmt = "Search Depth: (%d)";
	public final JLabel searchDepthLabel = new JLabel();
	public final JSlider searchDepth = new JSlider(JSlider.HORIZONTAL);
	public final JCheckBox showReasoning = new JCheckBox("Show reasoning");
	public final JCheckBox dontMakeMoves = new JCheckBox("Don't move");
	
	public JToolbox() {
		searchDepth.setMinimum(1);
		searchDepth.setMaximum(9);
		searchDepth.setPaintTicks(true);
		searchDepth.setSnapToTicks(true);
		searchDepth.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				searchDepthLabel.setText(String.format(sdLabelFmt, searchDepth.getValue()));
			}
		});
		searchDepth.setValue(3);
		
		showReasoning.setSelected(true);
		
		setLayout(new FlowLayout());
		add(searchDepthLabel);
		add(searchDepth);
		add(showReasoning);
		add(dontMakeMoves);
	}
}
