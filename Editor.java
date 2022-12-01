import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.stream.Stream;


import javax.swing.*;

/**
 * Client-server graphical editor
 * @author Nicolas and Shaamil
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Integer currId = null;					// current shape id (if any) being handled
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged
	private Point firstMoveFrom = null;				// where object is as it starts being dragged


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 * Adds shape to local sketch
	 */
	public void addShape(int id, Shape shape) {
		sketch.clientAddShape(id, shape);
		System.out.println("adding " + shape.toString());
		repaint();
	}

	/**
	 * deletes shape from local sketch
	 * @param id Shape ID
	 */
	public void deleteShape(int id)	{
		sketch.deleteShape(id);
		System.out.println("deleting shape ID" + id);
		repaint();
	}

	/**
	 * recolors shape from local sketch
	 * @param id Shape ID
	 * @param color Color to apply
	 */
	public void recolorShape(int id, Color color) {
		sketch.getShapeById(id).setColor(color);
		System.out.println("recoloring shape ID to" + color);
		repaint();
	}

	/**
	 * Moves shape from local sketch
	 * @param id Shape ID
	 * @param dx differencial in x
	 * @param dy differencial in y
	 */
	public void moveShape(int id, int dx, int dy) {
		System.out.printf("moving %d %d %d \n", id, dx, dy);
		Shape shape = sketch.getShapeById(id);
		System.out.println(shape.toString());
		shape.moveBy(dx, dy);
		System.out.println(shape.toString());
		repaint();
	}
	private JComponent setupCanvas() {

		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		List<Shape> shapes = sketch.getShapesOrdered();
		for (Shape shape: shapes) {
			if (shape != null)
				shape.draw(g);
		}

		if (curr != null) {
			curr.draw(g);
		}
	}


	// Helpers for event handlers

	/**
	 * returns ID of the highest (front to back) shape drawn that's under a point
	 * @param p point to check
	 * @return ID of the shape
	 */
	private int getClickedShapeId(Point p) {
		List<Integer> shapes = sketch.getShapesIdsOrdered();
		int lastId = -1;
		for (Integer id : shapes) {
			Shape shape = sketch.getShapeById(id);
			if (shape.contains(p.x, p.y)) {
				lastId = id;
			}
		}

		return lastId;
	}
	
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		switch (mode) {
			case DRAW:
				drawFrom = p;
				switch (shapeType) {
					case "ellipse":
						curr = new Ellipse(p.x, p.y, color);
						break;
					case "rectangle":
						curr = new Rectangle(p.x, p.y, color);
						break;
					case "freehand":
						curr = new Polyline(p, color);
						break;
					case "segment":
						curr = new Segment(p.x, p.y, color);
						break;
				}
				break;
			case MOVE:
				int shapeId = getClickedShapeId(p);
				if (shapeId == -1) break;
				moveFrom = p;
				firstMoveFrom = p;
				currId = shapeId;
				curr = sketch.getShapeById(currId);
				break;
			case DELETE:
				shapeId = getClickedShapeId(p);
				if(shapeId ==-1) break;
				comm.sendShapeDelete(shapeId);
			case RECOLOR:
				shapeId = getClickedShapeId(p);
				comm.sendShapeRecolor(shapeId, color.getRGB());

		}
		repaint();
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		switch (mode) {
			case DRAW:
				switch (shapeType) {
					case "ellipse":
						Ellipse ell = (Ellipse) curr;
						ell.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
						break;
					case "rectangle":
						Rectangle rect = (Rectangle) curr;
						rect.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
						break;
					case "freehand":
						Polyline pol = (Polyline) curr;
						pol.addPoint(p);
						break;
					case "segment":
						Segment seg = (Segment) curr;
						seg.setEnd(p.x, p.y);
						break;
				}
				break;
			case MOVE:
				if (curr == null) break;
				comm.sendShapeMove(currId, moveFrom, p);
				moveFrom = p;
		}
		repaint();
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease() {
		switch (mode) {
			case DRAW:
				comm.sendShapeCreate(curr);
				curr = null;
				break;
			case MOVE:
				if (curr == null) break;
				System.out.println("SHAPE " + currId);
				curr = null;
				currId = null;
				break;
		}

		repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}
}
