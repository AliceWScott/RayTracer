package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials.  If both are defined, a 1x1 tile checker 
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {
    
	/** The second material, if non-null is used to produce a checker board pattern. */
	Material material2;
	
	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d( 0, 1, 0 );
    
    /**
     * Default constructor
     */
    public Plane() {
    	super();
    }

        
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    	
        // TODO: Objective 4: intersection of ray with plane
    	
    		double t = - ray.eyePoint.y / ray.viewDirection.y;
    		
    		if(t < Double.POSITIVE_INFINITY && 1e-9 < t) { // otherwise, parallel to plane
    			
    			Point3d point_of_intersection = new Point3d(ray.viewDirection);
			point_of_intersection.scale(t);
			point_of_intersection.add(ray.eyePoint);
			
			result.p = point_of_intersection;
    			result.n.set(n);
    			result.t = t;
    			
    			if(material2 != null) {
    				
    				int sum = (int)Math.floor(point_of_intersection.x) + (int)Math.floor(point_of_intersection.y) + (int)Math.floor(point_of_intersection.z);
       			
    				if((sum & 1) == 0) {
        				result.material = material;
        			} else {
        				result.material = material2;
        			}
    				
    			} else {
    				result.material = material;
    			}
		
    		}
    		
    	
    	
    }
    
}
