package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see max) corner. 
 */
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;
	
    /**
     * Default constructor. Creates a 2x2x2 box centered at (0,0,0)
     */
    public Box() {
    	super();
    	this.max = new Point3d( 1, 1, 1 );
    	this.min = new Point3d( -1, -1, -1 );
    }	

	@Override
	public void intersect(Ray ray, IntersectResult result) {
		// TODO: Objective 6: intersection of Ray with axis aligned box
		
		result.t = Double.POSITIVE_INFINITY;
		
		// align box with axis of coordinate system
		//  equation for a line: y = mx + b
		// equation for a ray: r(t) = origin + t*d
		// origin.x + t* viewdir.x = min.x
		double t_min_x = (min.x - ray.eyePoint.x) / ray.viewDirection.x ;
		double t_max_x = (max.x - ray.eyePoint.x) / ray.viewDirection.x;
		double t_min_y = (min.y - ray.eyePoint.y) / ray.viewDirection.y;
		double t_max_y = (max.y - ray.eyePoint.y) / ray.viewDirection.y;
		double t_min_z = (min.z - ray.eyePoint.z) / ray.viewDirection.z;
		double t_max_z = (max.z - ray.eyePoint.z) / ray.viewDirection.z;
		double tmin;
		double tmax;

		//swap x
		if(t_min_x < t_max_x) {
			tmin = t_min_x;
			tmax = t_max_x;
		} else { //swap
			tmin = t_max_x;
			tmax = t_min_x;
		}
		
		// swap y
		if(t_min_y > t_max_y) {
			t_min_y = (max.y - ray.eyePoint.y) / ray.viewDirection.y;
			t_max_y =  (min.y - ray.eyePoint.y) / ray.viewDirection.y;
		}
		
		if((tmin < t_max_y) && (t_min_y < tmax)) { // otherwise, no intersection
			
			tmin = Math.max(t_min_y, tmin);
			tmax = Math.min(t_max_y, tmax);
			
			if(t_min_z > t_max_z) {
				t_min_z = (max.z - ray.eyePoint.z) / ray.viewDirection.z;
				t_max_z = (min.z - ray.eyePoint.z) / ray.viewDirection.z;
			}
			
			if((tmin < t_max_z) && (t_min_z < tmax)) {
				tmin = Math.max(tmin, t_min_z);
				tmax = Math.min(tmax,t_max_z);
				
				if(tmin < tmax && tmin < result.t && 0 < tmax) {
					
					if (tmin < 0) {
						result.t = tmax;
					} else {
						result.t = tmin;	
					}
					result.p.set(ray.viewDirection);
					result.p.scale(tmin);
					result.p.add(ray.eyePoint);
					result.material = this.material;
					
					if(Math.abs(result.p.x - min.x) < 1e-9) {
						result.n.set(-1,0,0);
					} else if (Math.abs(result.p.x - max.x) < 1e-9) {
						result.n.set(1,0,0);
					} else if (Math.abs(result.p.y - min.y) < 1e-9) {
						result.n.set(0,-1,0);
					} else if (Math.abs(result.p.y - max.y) < 1e-9) {
						result.n.set(0,1,0);
					} else if (Math.abs(result.p.z - min.z) < 1e-9) {
						result.n.set(0,0,-1);
					} else if (Math.abs(result.p.z - max.z) < 1e-9) {
						result.n.set(0,0,1);
					} 
				}
				
				

			}
			
		} 
		
		
	}	

}
