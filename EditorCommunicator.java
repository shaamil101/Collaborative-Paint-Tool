import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 * Handles communication to/from the server for the editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * parses command and executes a corresponding action on the local sketch, through the editor
	 * @param command
	 */
	private void parseCommand(String command) {
		String[] parts = command.split(" ");
		switch (parts[0]) {
			case "ADD":
				Shape shape = null;
				String[] shapeInfo = command.split("\\|")[1].split(" ");
				// We cannot really match string->type in java, so a polymorphic solution is not possible
				switch (parts[2]) {
					case "ellipse":
						shape = Ellipse.parseShapeInfo(shapeInfo);
						break;
					case "rectangle":
						shape = Rectangle.parseShapeInfo(shapeInfo);
						break;
					case "polyline":
						shape = Polyline.parseShapeInfo(shapeInfo);
						break;
					case "segment":
						shape = Segment.parseShapeInfo(shapeInfo);
						break;
				}
				
				editor.addShape(Integer.parseInt(parts[1]), shape);
				break;
			case "DELETE":
				editor.deleteShape(Integer.parseInt(parts[1]));
				break;
			case "MOVE":
				editor.moveShape(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
				break;
			case "RECOLOR":
				editor.recolorShape(Integer.parseInt(parts[1]),new Color(Integer.parseInt(parts[2])));
				break;
		}
	}
	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			// Handle messages
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				parseCommand(line);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}

	/**
	 * method for sending a shape creation command to the server
	 * @param shape shape to encode
	 */
	public void sendShapeCreate(Shape shape) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE ");
		// to string serializes the shape
		sb.append(shape.toString());
		send(sb.toString());
	}

	/**
	 * method for sending a shape movement command to the server
	 * @param shapeId ID of the moved shape
	 * @param p1 original position of the shape (only consider mouse position at start of drag)
	 * @param p2 new position of the shape (only consider mouse position at end of drag)
	 */
	public void sendShapeMove(Integer shapeId, Point p1, Point p2) {
		StringBuilder sb = new StringBuilder();
		sb.append("MOVE ");
		sb.append(shapeId);
		sb.append(" ");
		sb.append(p2.x - p1.x);
		sb.append(" ");
		sb.append(p2.y - p1.y);

		send(sb.toString());
	}

	/**
	 * method for sending shape deletion command to the server
	 * @param shapeId ID of the deleted shape
	 */
	public void sendShapeDelete(Integer shapeId) {
		send("DELETE "+shapeId);
	}

	/**
	 *  method for sending shape recoloring command to the server
	 * @param shapeId ID of the recolored shape
	 * @param color new color of the shape
	 */
	public void sendShapeRecolor(Integer shapeId, int color) {
		send("RECOLOR "+ shapeId + " " + color);
	}

}
