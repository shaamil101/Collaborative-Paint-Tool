import java.awt.Color;
import java.awt.Graphics;

/**
 * A rectangle-shaped Shape
 * Defined by an upper-left corner (x1,y1) and a lower-right corner (x2,y2)
 * with x1<=x2 and y1<=y2
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, updated Fall 2016
 */
public class Rectangle implements Shape {
	Color color;
	int x1;
	int x2;
	int y1;
	int y2;

	public Rectangle(int x1, int y1, int x2, int y2, Color color) {
		setCorners(x1, y1, x2, y2);
		this.color = color;
	}

	public Rectangle(int x1, int y1, Color color) {
		this.x1 = x1; this.x2 = x1;
		this.y1 = y1; this.y2 = y1;
		this.color = color;
	}

	/**
	 * this serves the purpose of setting our local co-ordinate variables to the parameters
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void setCorners(int x1, int y1, int x2, int y2) {
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
	}

	// TODO: YOUR CODE HERE
	@Override
	public void moveBy(int dx, int dy) {
		x1 += dx; y1 += dy;
		x2 += dx; y2 += dy;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
		
	@Override
	public boolean contains(int x, int y) {
		double a = (x2-x1)/2.0, b = (y2-y1)/2.0;
		double dx = x - (x1 + a); // horizontal distance from center
		double dy = y - (y1 + b); // vertical distance from center
		// Apply the standard geometry formula. (See CRC, 29th edition, p. 178.)
		return Math.pow(dx / a, 2) + Math.pow(dy / b, 2) <= 1;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect(x1,y1,x2-x1,y2-y1);
	}
	/**
	 * This method returns a rectangle with all the points and its respective color for parsing purposes
	 * @param shapeInfo
	 * @return
	 */
	public static Rectangle parseShapeInfo(String[] shapeInfo) {
		return new Rectangle(
				Integer.parseInt(shapeInfo[0]),
				Integer.parseInt(shapeInfo[1]),
				Integer.parseInt(shapeInfo[2]),
				Integer.parseInt(shapeInfo[3]),
				new Color(Integer.parseInt(shapeInfo[4]))
		);
	}
	/**
	 * Returns string with the information required to parse through Rectangle. Note: The '|' was added by us as a signpost
	 * @return
	 */
	public String toString() {
		return "rectangle |"+x1+" "+y1+" "+x2+" "+y2+" "+color.getRGB()+"|";
	}
}


