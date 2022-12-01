import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class Sketch {
    int currentId;

    /**
     * returns list of shapes ordered by ID
     * @return list of the shapes
     */
    public List<Shape> getShapesOrdered() {
        return shapes.navigableKeySet().stream().map((Integer key) -> shapes.get(key)).toList();
    }

    /**
     * returns list of IDs of the shapes in ascending order
     * @return list of integer IDs
     */
    public List<Integer> getShapesIdsOrdered() {
        return shapes.navigableKeySet().stream().toList();
    }

    /**
     * returns a shape by its ID
     * @param id ID to look for
     * @return corresponding shape
     */
    public Shape getShapeById(int id) {
        return shapes.get(id);
    }

    /**
     * should only be called by server, adds a new shape and returns its assigned ID
     * @param shape shape to add
     * @return new ID
     */
    public int serverAddShape(Shape shape) {
       int id = ++currentId;
       shapes.put(id, shape);
       return id;
    }

    /**
     * should only be called by client, adds a new shape with an existing ID
     * @param id ID of the shape
     * @param shape shape to add
     */
    public void clientAddShape(int id, Shape shape) {
        shapes.put(id, shape);
    }

    /**
     * deletes a shape from the map by a given ID
     * @param id ID to delete
     */
    public void deleteShape(int id) {
        shapes.remove(id);
    }

    private TreeMap<Integer, Shape> shapes;

    /**
     * parses a CREATE command and returns a Shape. Internally calls corresponding shape.parseShapeInfo
     * @param command command to parse
     * @return new Shape
     */
    public static Shape parseCreateCommand(String command) {
        String[] parts = command.split(" ");
        String[] shapeInfo = command.split("\\|")[1].split(" ");
        switch (parts[1]) {
            case "ellipse":
                return Ellipse.parseShapeInfo(shapeInfo);
            case "rectangle":
                return Rectangle.parseShapeInfo(shapeInfo);
            case "polyline":
                return Polyline.parseShapeInfo(shapeInfo);
            case "segment":
                return Segment.parseShapeInfo(shapeInfo);
        }

        return null;
    }

    public Sketch() {
        shapes = new TreeMap<>();
        currentId = 0;
    }

}
