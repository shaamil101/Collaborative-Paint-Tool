import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */

public class Polyline implements Shape {
    private ArrayList<Point> linePoints = new ArrayList<>();
    private Color color;

	//Constructor that gets the point with its x and y co-ordinates along with color
    public Polyline(Point p, Color color)
    {
        linePoints.add(p);
        this.color = color;
    }

	public void addPoint(Point p) {
		linePoints.add(p);
	}

	/**
	 * We run through the array list of points and update each point by adding them as a new point with their updates x and y values
	 * @param dx the x value to increment by
	 * @param dy the y value to increment by
	 */
	@Override
	public void moveBy(int dx, int dy) {
        for(int i =0;i<linePoints.size();i++)
        {
            linePoints.get(i).translate(dx, dy);
        }
	}

	@Override
	public Color getColor() {
	return color;
	}

	@Override
	public void setColor(Color color) {
	this.color= color;
	}

	/**
	 * First, we run over every point and stop at the second last point
	 * Second, we use the first and second point variables to check if the pointToSegmentDistance is less than a certain valur
	 * @param x x co-ordinate of clicked point
	 * @param y y co-ordinate of clicked point
	 * @return
	 */
	@Override
	public boolean contains(int x, int y) {
		for(int i=0;i<linePoints.size()-1;i++)
		{
			if (i % 10 == 0) System.out.println(i);
			Point p1 = linePoints.get(i);
			Point p2 = linePoints.get(i+1);

			if(Segment.pointToSegmentDistance(x,y,p1.x,p1.y,p2.x,p2.y)<=10)
			{
				return true;
			}
		}
		return false;


	}

	/**
	 * The draw method draws the polyline by first parsing through every pair of points and then drawing a line between each pair
	 * @param g
	 */
	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		for (int i = 0; i < linePoints.size() - 1; i++) {
			Point p1 = linePoints.get(i);
			Point p2 = linePoints.get(i + 1);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}

	/**
	 * This method returns a polyline with all the points and its respective color for parsing purposes
	 * @param shapeInfo
	 * @return
	 */
	public static Polyline parseShapeInfo(String[] shapeInfo) {
		 Polyline polyline = new Polyline(new Point(Integer.parseInt(shapeInfo[0]),
				 									Integer.parseInt(shapeInfo[1])),
				 							new Color(Integer.parseInt(shapeInfo[shapeInfo.length - 1])));
		 for(int i =2;i<shapeInfo.length-1;i+=2) {
			 polyline.addPoint(new Point(Integer.parseInt(shapeInfo[i]),Integer.parseInt(shapeInfo[i+1])));
		 }
		 return polyline;
	}

	/**
	 * Returns string with the information required to parse through Polyline. Note: The '|' was added by us as a signpost
	 * @return
	 */
	@Override
	public String toString() {
		String allPoints="";
		for(Point point:linePoints)
		{
			allPoints += point.x+" "+point.y+" ";
		}
		return "polyline |"+allPoints+color.getRGB() + "|";
	}
}
