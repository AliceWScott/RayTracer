package comp557.a4;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import javafx.geometry.Point3D;

/**
 * A simple sphere class.
 */
public class Sphere extends Intersectable {
    
	/** Radius of the sphere. */
	public double radius = 1;
    
	/** Location of the sphere center. */
	public Point3d center = new Point3d( 0, 0, 0 );
    
    /**
     * Default constructor
     */
    public Sphere() {
    	super();
    }
    
    /**
     * Creates a sphere with the request radius and center. 
     * 
     * @param radius
     * @param center
     * @param material
     */
    public Sphere( double radius, Point3d center, Material material ) {
    	super();
    	this.radius = radius;
    	this.center = center;
    	this.material = material;
    }
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: Objective 2: intersection of ray with sphere
    		Vector3d e = new Vector3d(ray.eyePoint);
    		e.sub(this.center);
    		double a = ray.viewDirection.dot(ray.viewDirection);
    		double b = 2 * ray.viewDirection.dot(e);
    		double c = e.dot(e) - this.radius * this.radius;
    		double discriminant = b*b - 4*a*c;
    		
   		if (discriminant == 0.0) {
   			
   			double t_pos = -0.5 * b / a;
   			// scale ray viewDirection to be t_pos length
			// point of intersection is at ray.eyePoint + scaled viewDirection vector
			Point3d point_of_intersection = new Point3d(ray.viewDirection);
			point_of_intersection.scale(t_pos);
			point_of_intersection.add(ray.eyePoint);
			
			// normal is point_of_intersection - sphere.center
			Vector3d normal = new Vector3d();
			normal.add(point_of_intersection);
			normal.sub(this.center);
			normal.normalize();
			
			result.p.set(point_of_intersection);
			result.material = this.material;
			result.t = t_pos;
			result.n.set(normal);
   			
   		}
   		else if (discriminant > 0.0) {
   			
   			// solve quadratic formula
   			double q = (b > 0) ? -0.5 * (b + Math.sqrt(discriminant)) : -0.5 * (b - Math.sqrt(discriminant));
    			double t_pos = q / a;
    			double t_neg = c / q;
    			
    			if (t_pos < t_neg) {
    				double temp = t_pos;
    				t_pos = t_neg;
    				t_neg = temp;
    			}
    				
    			if (t_neg > 0) {
    				// scale ray viewDirection to be t_neg length
    				// point of intersection is at ray.eyePoint + scaled viewDirection vector
        			Point3d point_of_intersection = new Point3d(ray.viewDirection);
        			point_of_intersection.scale(t_neg);
        			point_of_intersection.add(ray.eyePoint);
        			
        			// normal is point_of_intersection - sphere.center
        			Vector3d normal = new Vector3d();
        			normal.add(point_of_intersection);
        			normal.sub(this.center);
        			normal.normalize();
        			
        			result.p.set(point_of_intersection);
        			result.material = this.material;
        			result.t = t_neg;
        			result.n.set(normal);
    			}
	
    		}
    		
    		
    		
    		
    		

    }
    
}
