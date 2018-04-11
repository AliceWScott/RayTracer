package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import comp557.a4.PolygonSoup.Vertex;

public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}			
		
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		List<Vertex> vertexList = soup.vertexList;
		
		// TODO: Objective 7: ray triangle intersection for meshes
		for(int[] f: soup.faceList) {
			List<Point3d> points = new ArrayList<>();
			for (int i : f) {
				Point3d p = vertexList.get(i).p;
				points.add(p);
			}
			
			Point3d a = points.get(1);
			Point3d b = points.get(0);
			Point3d c = points.get(2);
			
			Vector3d b_minus_a = new Vector3d(b);
			b_minus_a.sub(a);
			
			Vector3d c_minus_a = new Vector3d(c);
			c_minus_a.sub(a);
			
			Vector3d c_minus_b = new Vector3d(c);
			c_minus_b.sub(b);
			
			Vector3d a_minus_c = new Vector3d(a);
			c_minus_b.sub(c);
			
			Vector3d normal = new Vector3d();
			normal.cross( c_minus_a, b_minus_a);
			normal.normalize();
			
			// t = ((a-ray.eyepoint) (dot) normal)/ (viewdirection (dot) n)	
			Vector3d e = new Vector3d(a);
			e.sub(ray.eyePoint);
			double t = normal.dot(e);
			t = t / normal.dot(ray.viewDirection);
			
			Point3d Q = new Point3d(ray.viewDirection);
			Q.scale(t);
			Q.add(ray.eyePoint);
			
			Vector3d cond1_vec = new Vector3d(Q);
			cond1_vec.sub(a);
			cond1_vec.cross(b_minus_a, cond1_vec);
			
			Vector3d cond2_vec = new Vector3d(Q);
			cond2_vec.sub(b);
			cond2_vec.cross(c_minus_b, cond2_vec);
			
			Vector3d cond3_vec = new Vector3d(Q);
			cond3_vec.sub(c);
			cond3_vec.cross(a_minus_c, cond3_vec);
			
			if((cond1_vec.dot(normal) >= 0) && (cond2_vec.dot(normal)>=0) && (cond3_vec.dot(normal)>=0)){
				// intersection
				result.n.set(normal);
				result.p.set(Q);
				result.t = t;
				result.material = this.material;
				break;
			}
			
			
		}
		
		
		
	}

}
