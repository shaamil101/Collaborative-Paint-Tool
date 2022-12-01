import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.util.List;

/**
 * A server to handle sketches: getting requests from the clients,
 * updating the overall state, and passing them on to the clients
 *
 */
public class SketchServer {
	private ServerSocket listen;						// for accepting connections
	private ArrayList<SketchServerCommunicator> comms;	// all the connections with clients
	private Sketch sketch;								// the state of the world
	
	public SketchServer(ServerSocket listen) {
		this.listen = listen;
		sketch = new Sketch();
		comms = new ArrayList<SketchServerCommunicator>();
	}

	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * creates an "ADD" command for every current shape to update the new client
	 * @return String with one line per shape to be drawn
	 */
	public String getWorldDescription() {
		List<Integer> ids = sketch.getShapesIdsOrdered();
		StringBuilder sb = new StringBuilder();
		ids.stream().forEach((id) -> {
			sb.append("ADD ");
			sb.append(id);
			sb.append(" ");
			sb.append(sketch.getShapeById(id).toString());
			sb.append("\n");
		});
		return sb.toString();
	}

	/**
	 * atomically handles every incoming edit command
	 * it is synchronized so there is always a single state in the sketch
	 * which represents ground truth
	 * @param command incomming command to parse and handle
	 */
	public synchronized void handleEdit(String command) {
		String[] parts = command.split(" ");
		switch (parts[0]) {
			case "CREATE":
				Shape shape = Sketch.parseCreateCommand(command);
				int id = sketch.serverAddShape(shape);
				broadcast(String.format("ADD %d %s", id, shape.toString()));
				break;
			case "DELETE":
				id = Integer.parseInt(parts[1]);
				sketch.deleteShape(id);
				broadcast(command);
				break;
			case "MOVE":
				id = Integer.parseInt(parts[1]);
				shape = sketch.getShapeById(id);
				if (shape == null) break;
				shape.moveBy(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
				broadcast(command);
				break;
			case "RECOLOR":
				id = Integer.parseInt(parts[1]);
				shape = sketch.getShapeById(id);
				if (shape == null) break;
				Color color = new Color(Integer.parseInt(parts[2]));
				shape.setColor(color);
				broadcast(command);
				break;
		}
	}

	/**
	 * The usual loop of accepting connections and firing off new threads to handle them
	 */
	public void getConnections() throws IOException {
		System.out.println("server ready for connections");
		while (true) {
			SketchServerCommunicator comm = new SketchServerCommunicator(listen.accept(), this);
			comm.setDaemon(true);
			comm.start();
			addCommunicator(comm);
		}
	}

	/**
	 * Adds the communicator to the list of current communicators
	 */
	public synchronized void addCommunicator(SketchServerCommunicator comm) {
		comms.add(comm);
	}

	/**
	 * Removes the communicator from the list of current communicators
	 */
	public synchronized void removeCommunicator(SketchServerCommunicator comm) {
		comms.remove(comm);
	}

	/**
	 * Sends the message from the one communicator to all (including the originator)
	 */
	public synchronized void broadcast(String msg) {
		for (SketchServerCommunicator comm : comms) {
			comm.send(msg);
		}
	}
	
	public static void main(String[] args) throws Exception {
		new SketchServer(new ServerSocket(4242)).getConnections();
	}
}
