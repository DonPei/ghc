package org.ucdm.tracedisplay;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ucdm.common.CommonDialog;
import org.ucdm.common.VeconUtils;
import org.ucdm.common.util.ArrowIcon;
import org.ucdm.mti.Seg2View;
import org.ucdm.resource.MathArrowIcon;

public class TraceDisplayToolBar extends JToolBar {
	//CommonDialog 	_dialog		= null;
	private TracePanel _tracePanel = null;
	private boolean [][] 	_state 	= null;
	String 		_cwd 				= null;
	JTextField 	_t0TF 				= new JTextField("0", 5);
	CheckCombo 	_compIndexCB 		= null;
	
	public TraceDisplayToolBar(TracePanel tracePanel, int mode, String cwd) {
		super(SwingConstants.HORIZONTAL);
		_tracePanel 	= tracePanel;
		_cwd 		= cwd;
		if(_cwd==null) _cwd = System.getProperty("user.dir");
		
		_state		= new boolean [6][3];
		for(int i=0; i<_state.length; i++) {
			if(i==1) { //observed picks
				for(int j=0; j<_state[i].length; j++) {_state[i][j] = true;}
			} else if(i==5) { //calculated picks
				for(int j=0; j<_state[i].length; j++) {_state[i][j] = false;}
			} else if(i==4) {
				if(mode==2){ 	_state[i][0] = false; _state[i][1] = true; _state[i][2] = false;}
				else {			_state[i][0] = true; _state[i][1] = false; _state[i][2] = false; }
			} else {
				//0 component
				for(int j=0; j<_state[i].length; j++) { _state[i][j] = true; }
			}
		}
		setRollover(false);

		JCheckBox jCheckBox = null;
	
		add(new JLabel(" Component:", JLabel.RIGHT));
		jCheckBox = new JCheckBox("Northing", _state[0][0]);
		jCheckBox.setForeground(Color.BLACK);
		jCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final TraceView traceView = _tracePanel.getTraceView();
				_state[0][0] = ((JCheckBox)event.getSource()).isSelected();
				traceView.setCompZSelected(_state[0][0]);
			}
		});
		add(jCheckBox);
		jCheckBox = new JCheckBox("Easting", _state[0][1]);
		jCheckBox.setForeground(new Color(139, 0, 0));
		jCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final TraceView traceView = _tracePanel.getTraceView();
				_state[0][1] = ((JCheckBox)event.getSource()).isSelected();
				traceView.setCompYSelected(_state[0][1]);
			}
		});
		add(jCheckBox);
		jCheckBox = new JCheckBox("Down", _state[0][2]);
		jCheckBox.setForeground(Color.BLUE);
		jCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final TraceView traceView = _tracePanel.getTraceView();
				_state[0][2] = ((JCheckBox)event.getSource()).isSelected();
				traceView.setCompXSelected(_state[0][2]);
			}
		});
		add(jCheckBox);

		addSeparator();
		
		add(new JLabel(" Picks:", JLabel.RIGHT));

		jCheckBox = new JCheckBox("P", _state[1][0]);
		jCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final TraceView traceView = _tracePanel.getTraceView();
				_state[1][0] = ((JCheckBox)event.getSource()).isSelected();
				traceView.setPickPSelected(_state[1][0]);
			}
		});
		add(jCheckBox);
		jCheckBox = new JCheckBox("SH", _state[1][1]);
		jCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final TraceView traceView = _tracePanel.getTraceView();
				_state[1][1] = ((JCheckBox)event.getSource()).isSelected();
				traceView.setPickSHSelected(_state[1][1]);
			}
		});
		add(jCheckBox);
		jCheckBox = new JCheckBox("SV", _state[1][2]);
		jCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final TraceView traceView = _tracePanel.getTraceView();
				_state[1][2] = ((JCheckBox)event.getSource()).isSelected();
				traceView.setPickSVSelected(_state[1][2]);
			}
		});
		add(jCheckBox);
		
		addSeparator();
		
		add(new JLabel("Equilibrium:"));
		int n = 3;
		ButtonGroup moduleRadioGroup = new ButtonGroup();
		JRadioButton [] moduleRadioButton = new JRadioButton[n];
		int k = 0;
		moduleRadioButton[k] = new JRadioButton("Comp", _state[4][0]);
		moduleRadioButton[k].setToolTipText("scale to peak value of each component");
		moduleRadioButton[k].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_state[4][0] = ((JRadioButton)event.getSource()).isSelected();
				_state[4][1] = !_state[4][0];
				_state[4][2] = !_state[4][0];
				final TraceView traceView = _tracePanel.getTraceView();
				traceView.setEquilibrium(_state[4][0], _state[4][1], _state[4][2]);
			}
		});
		moduleRadioGroup.add(moduleRadioButton[k]);
		add(moduleRadioButton[k]);

		k++;
		moduleRadioButton[k] = new JRadioButton("Receiver", _state[4][1]);
		moduleRadioButton[k].setToolTipText("scale to peak value of each row");
		moduleRadioButton[k].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_state[4][1] = ((JRadioButton)event.getSource()).isSelected();
				_state[4][0] = !_state[4][1];
				_state[4][2] = !_state[4][1];
				final TraceView traceView = _tracePanel.getTraceView();
				traceView.setEquilibrium(_state[4][0], _state[4][1], _state[4][2]);
			}
		});
		moduleRadioGroup.add(moduleRadioButton[k]);
		add(moduleRadioButton[k]);

		k++;
		moduleRadioButton[k] = new JRadioButton("Event", _state[4][2]);
		moduleRadioButton[k].setToolTipText("scale to peak value of entire record");
		moduleRadioButton[k].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_state[4][2] = ((JRadioButton)event.getSource()).isSelected();
				_state[4][0] = !_state[4][2];
				_state[4][1] = !_state[4][2];
				final TraceView traceView = _tracePanel.getTraceView();
				traceView.setEquilibrium(_state[4][0], _state[4][1], _state[4][2]);
			}
		});
		moduleRadioGroup.add(moduleRadioButton[k]);
		add(moduleRadioButton[k]);

		addSeparator();
		
		JButton button = new JButton("smooth");
		button.setBorder(new CompoundBorder(BorderFactory.createRaisedBevelBorder(), new EmptyBorder(1, 1, 0, 0)));
		button.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) { 
				final TraceView traceView = _tracePanel.getTraceView();
				traceView.exponentiallySmooth(true, true, 1);
			}
		});
		//add(button);
		
		add(new JLabel("Amp:"));
		JButton ampUp = new JButton(getMathArrowIcon(ArrowIcon.SOUTH, 1));
		ampUp.setToolTipText("scale up amplitude");
		ampUp.addActionListener(new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				final TraceView traceView = _tracePanel.getTraceView();
				traceView.setAmpScale(1.1f);
			}
		});
		add(ampUp);
		JButton ampDown = new JButton(getMathArrowIcon(ArrowIcon.NORTH, 1));
		ampDown.setToolTipText("scale down amplitude");
		ampDown.addActionListener(new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				final TraceView traceView = _tracePanel.getTraceView();
				traceView.setAmpScale(0.9f);
			}
		});
		add(ampDown);

	}
	
	public MathArrowIcon getMathArrowIcon(int direction, int type) {
		int sizeW = 24;
		int sizeH = 20;
		int style = 2;
		double headSize = 1.5;
		double arrowAngle = 1.5;
		int lineWidth = 2;
		//Color color = new Color(75, 125,0);
		Color color = Color.RED;

		return new MathArrowIcon(direction, sizeW, sizeH, 
				style, type, arrowAngle, headSize, lineWidth, color);
	}
	
	public boolean [][] getState() { return _state; }
	
	private class CheckCombo extends AbstractAction	{
		//private class CheckCombo implements ActionListener	{

		String [] 		_names 			= null;
		boolean [] 		_state 			= null;
		CheckComboStore[] 	_stores 	= null;
		@SuppressWarnings("rawtypes")
		JComboBox 			_combo 		= null;
		ActionListener _actionListener = null;

		public CheckCombo(String [] names, boolean [] state) {
			_names 	= names;
			_state 	= state;
		}
		public void addActionListener(ActionListener actionListener) {
			_actionListener = actionListener;
		}
		public void actionPerformed(ActionEvent e) {}
		public void setState(boolean v) {
			for(int j = 1; j <_names.length; j++) {
				_stores[j].state=v;
			}
		}
		public void setState(boolean [] v) {
			for(int j = 1; j <_names.length; j++) {
				_stores[j].state=v[j-1];
			}
		}
		private JPanel getContent() {
			_stores = new CheckComboStore[_names.length];
			for(int j = 0; j <_names.length; j++) {_stores[j] = new CheckComboStore(_names[j], _state[j]); }
			_combo = new JComboBox(_stores);
			_combo.setRenderer(new CheckComboRenderer());
			//combo.addActionListener(this);
			_combo.addActionListener(_actionListener);
			//combo.setPrototypeDisplayValue("XXXXX"); // Set a desired width
			//combo.setMaximumSize(combo.getMinimumSize());
			JPanel panel = new JPanel();
			panel.add(_combo);
			panel.setMaximumSize(_combo.getMinimumSize());
			return panel;
		}
	}

	/** adapted from comment section of ListCellRenderer api */
	private class CheckComboRenderer implements ListCellRenderer {
		JCheckBox checkBox;

		public CheckComboRenderer() {
			checkBox = new JCheckBox();
		}
		public Component getListCellRendererComponent(JList list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus)
		{
			CheckComboStore store = (CheckComboStore)value;
			checkBox.setText(store.id);
			checkBox.setSelected(store.state);
			checkBox.setBackground(isSelected ? Color.blue : Color.white);
			checkBox.setForeground(isSelected ? Color.white : Color.black);
			return checkBox;
		}
	}

	private class CheckComboStore {
		String id;
		boolean state;

		public CheckComboStore(String id, boolean state) {
			this.id = id;
			this.state = state;
		}
		public String getName() { return id; }
		public boolean getState() { return state; }
	}
	
	private class PreviewButton extends JButton { 
		public PreviewButton(int w, int h) { 
			setPreferredSize(new Dimension(w,h));
		} 
		
		public PreviewButton(int w, int h, Color color) { 
			setPreferredSize(new Dimension(w,h));
			setColor(color);
		} 
		
		public void setColor(Color color){
			setForeground(color);
			setBackground(color);
		}
		
		@Override 
		protected void paintComponent(Graphics g) { 
			g.fillRect(0, 0, getWidth(), getHeight());
		} 
	}

}
