package org.ucdm.launcher.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;

import com.sun.awt.AWTUtilities;

//http://java-demos.blogspot.com/2013/11/using-glasspane-to-block-input-in-jframe.html

/** An application that requires no other files. */
public class GlassPaneDemo {
	static private MyGlassPane myGlassPane;

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("GlassPaneDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setUndecorated(true);
		//AWTUtilities.setWindowOpacity(frame, 0.5f);

		//Start creating and adding components.
		JCheckBox changeButton = new JCheckBox("Glass pane \"visible\"");
		changeButton.setSelected(false);

		//Set up the content pane, where the "main GUI" lives.
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new FlowLayout());
		contentPane.add(changeButton);
		contentPane.add(new JButton("Button 1"));
		contentPane.add(new JButton("Button 2"));

		//Set up the menu bar, which appears above the content pane.
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Menu");
		menu.add(new JMenuItem("Do nothing"));
		menuBar.add(menu);
		frame.setJMenuBar(menuBar);

		//Set up the glass pane, which appears over both menu bar
		//and content pane and is an item listener on the change
		//button.
		myGlassPane = new MyGlassPane(changeButton, menuBar, frame.getContentPane());
		changeButton.addItemListener(myGlassPane);
		frame.setGlassPane(myGlassPane);

		//Show the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}

/**
 * We have to provide our own glass pane so that it can paint.
 */
class MyGlassPane extends JComponent
implements ItemListener {
	Point point;

	//React to change button clicks.
	public void itemStateChanged(ItemEvent e) {
		setVisible(e.getStateChange() == ItemEvent.SELECTED);
	}

	protected void paintComponent(Graphics g) {
		if (point != null) {
			Graphics2D g2d = (Graphics2D) g.create();
			float alpha = 0.1f;
            AlphaComposite alcom = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(alcom);
			g2d.setColor(Color.red);
			g2d.fillOval(point.x - 10, point.y - 10, 20, 20);
			g2d.dispose();
		}
	}

	public void setPoint(Point p) {
		point = p;
	}

	public MyGlassPane(AbstractButton aButton,
			JMenuBar menuBar,
			Container contentPane) {
		CBListener listener = new CBListener(aButton, menuBar,
				this, contentPane);
		addMouseListener(listener);
		addMouseMotionListener(listener);
	}
}

/**
 * Listen for all events that our check box is likely to be
 * interested in.  Redispatch them to the check box.
 */
class CBListener extends MouseInputAdapter {
	Toolkit toolkit;
	Component liveButton;
	JMenuBar menuBar;
	MyGlassPane glassPane;
	Container contentPane;

	public CBListener(Component liveButton, JMenuBar menuBar,
			MyGlassPane glassPane, Container contentPane) {
		toolkit = Toolkit.getDefaultToolkit();
		this.liveButton = liveButton;
		this.menuBar = menuBar;
		this.glassPane = glassPane;
		this.contentPane = contentPane;
	}

	public void mouseMoved(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseDragged(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseClicked(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseEntered(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseExited(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mousePressed(MouseEvent e) {
		System.out.println(e.getX()+", "+e.getY());
		PointerInfo inf = MouseInfo.getPointerInfo();
		Point p = inf.getLocation();
		System.out.println(p.x+", "+p.y);
		//redispatchMouseEvent(e, false);
	}

	public void mouseReleased(MouseEvent e) {
		redispatchMouseEvent(e, true);
	}

	//A basic implementation of redispatching events.
	private void redispatchMouseEvent(MouseEvent e,
			boolean repaint) {
		Point glassPanePoint = e.getPoint();
		Container container = contentPane;
		Point containerPoint = SwingUtilities.convertPoint(
				glassPane,
				glassPanePoint,
				contentPane);
		if (containerPoint.y < 0) { //we're not in the content pane
			if (containerPoint.y + menuBar.getHeight() >= 0) { 
				//The mouse event is over the menu bar.
				//Could handle specially.
			} else { 
				//The mouse event is over non-system window 
				//decorations, such as the ones provided by
				//the Java look and feel.
				//Could handle specially.
			}
		} else {
			//The mouse event is probably over the content pane.
			//Find out exactly which component it's over.  
			Component component = 
					SwingUtilities.getDeepestComponentAt(
							container,
							containerPoint.x,
							containerPoint.y);

			if ((component != null) 
					&& (component.equals(liveButton))) {
				//Forward events over the check box.
				Point componentPoint = SwingUtilities.convertPoint(
						glassPane,
						glassPanePoint,
						component);
				component.dispatchEvent(new MouseEvent(component,
						e.getID(),
						e.getWhen(),
						e.getModifiers(),
						componentPoint.x,
						componentPoint.y,
						e.getClickCount(),
						e.isPopupTrigger()));
			}
		}

		//Update the glass pane if requested.
		if (repaint) {
			glassPane.setPoint(glassPanePoint);
			glassPane.repaint();
		}
	}
}


