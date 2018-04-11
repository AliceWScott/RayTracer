package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import javafx.geometry.Point3D;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {
    
    /** List of surfaces in the scene */
    public static List<Intersectable> surfaceList = new ArrayList<Intersectable>();
	
	/** All scene lights */
	public Map<String,Light> lights = new HashMap<String,Light>();

    /** Contains information about how to render the scene */
    public Render render;
    
    /** The ambient light colour */
    public Color3f ambient = new Color3f();
    
    /** 
     * Default constructor.
     */
    public Scene() {
    	this.render = new Render();
    }
    
    /**
     * renders the scene
     */
    public void render(boolean showPanel) {
 
        Camera cam = render.camera; 
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        final double[] offset = {-0.5, 0.5};
        
       
        render.init(w, h, showPanel);
        
        
        for ( int i = 0; i < h && !render.isDone(); i++ ) {
            for ( int j = 0; j < w && !render.isDone(); j++ ) {
          
            	
            		//for each pixel:
            		// compute viewing ray
            		// intersect ray with scene
            		// compute illumination at visible point
            		// put result into image
            	
                // TODO: Objective 1: generate a ray (use the generateRay method)
            		Color4f sum = new Color4f(0,0,0,0);
            		
            		// sampling done according to "algorithm #2" described in the following source from Stanford:
            		// https://graphics.stanford.edu/courses/cs348b-96/antialiasing/antialiasing1.html
            		for (int x = 0; x < (int)Math.sqrt(render.samples); x++){
            			for (int y = 0; y < (int)Math.sqrt(render.samples); y++) {
            				
            			 	Ray ray = new Ray();
            			 	offset[0] = x;
            			 	offset[1] = y;
            			 	
         		         generateRay(j, i, offset, cam, ray);
        			            		
        			                // TODO: Objective 2: test for intersection with scene surfaces
        			        IntersectResult closest = new IntersectResult();
        			        
        			        for(Intersectable s: surfaceList) {
        			        		IntersectResult res = new IntersectResult();
        			            	s.intersect(ray, res); 	
        			            	
        			            	if (s instanceof SceneNode) {
        			            		for (Intersectable child : ((SceneNode) s).children) {
        			            			IntersectResult childres = new IntersectResult();
        			            			child.intersect(ray, childres);
        			            			if (childres.t < closest.t) {
        			            				closest = childres;
        			            			}
        			            		}
        			            	}
        			            	
        			            	if(res.t != Double.POSITIVE_INFINITY && res.t < closest.t) {
        			            		closest = new IntersectResult(res);
        			            	} 
        			        }
        			        
        			        if(closest.t == Double.POSITIVE_INFINITY) {
        			        		Color4f colour = new Color4f(render.bgcolor.x , render.bgcolor.y, render.bgcolor.z, 1);
        		        			sum.add(colour);
        			        } else {
        			        		Color4f colour = setLighting(closest, cam, ray);
        			        		sum.add(colour);
        			        		
        			        }  
            			}
            		}		
            		sum.scale((float)1/render.samples);
	        		sum.clamp(0, 1);
        			int r = (int)(255*sum.x);
        			int g = (int)(255*sum.y);
        			int b = (int)(255*sum.z);
        			int a = 255;
        			int argb = (a<<24 | r<<16 | g<<8 | b);
        			//update render image
        			render.setPixel(j, i, argb);
            	
		                
            }
        }
        
        // save the final render image
        render.save();
        
        // wait for render viewer to close
        render.waitDone();
        
    }
    
    
    public Color4f setLighting(IntersectResult closest, Camera cam, Ray ray) {
    	Color4f colour = new Color4f();

       		colour.set(closest.material.diffuse.x*ambient.x, closest.material.diffuse.y*ambient.y, closest.material.diffuse.z*ambient.z, 1);
       	
			for(Light light: lights.values()) {
				
				Ray shadowRay = new Ray();
				IntersectResult shadowResult = new IntersectResult();
				boolean isInShadow = inShadow(closest, light, null , shadowResult, shadowRay);

				if(!isInShadow) {
					
			    		closest.n.normalize();
			    	
					// light vector
	        			Vector3d l = new Vector3d(light.from);
	        			l.sub(closest.p);
	        			l.normalize();
	        			
	        			// view vector
	        			Vector3d v = new Vector3d(ray.viewDirection);
	        			v.normalize();
	        			v.negate();
	
	        			Vector3d h_vec = new Vector3d(l);
	        			h_vec.add(v);
	        			h_vec.normalize();
	        			
	        			// Diffuse component: L_d = k_d * I * max(0, n (dot) l)
	        			Color4f diffuse = new Color4f(closest.material.diffuse);
	        			diffuse.set(diffuse.x * light.color.x, diffuse.y * light.color.y, diffuse.z * light.color.z, 1);
	        			diffuse.scale((float) light.power);
	        			diffuse.scale((float) Math.max(0, closest.n.dot(l)));
	        			colour.add(diffuse);
	        			
	        			// Specular component: L_s = k_s * I * max(0, n (dot) h)^p
	        			Color4f specular = new Color4f(closest.material.specular);
	        			specular.set(specular.x * light.color.x, specular.y * light.color.y, specular.z * light.color.z, 1);
	        			specular.scale((float) light.power);
	        			specular.scale((float) Math.pow(Math.max(0, closest.n.dot(h_vec)), closest.material.shinyness));
	        			colour.add(specular);
				}	
    		}
		return colour;
    }
    
 
    // get random jittering
    // (stochastic sampling)
    public double getJitterVal() {
    		return (render.jitter) ? (new Random()).nextDouble() : 0.5;
    }
    
    
    /**
     * Generate a ray through pixel (i,j).
     * 
     * @param i The pixel row.
     * @param j The pixel column.
     * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
     * @param cam The camera.
     * @param ray Contains the generated ray.
     */
	public void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {
		// TODO: Objective 1: generate rays given the provided parameters
		
		// normal vector of the viewing plane
		Vector3d w_vec = new Vector3d(cam.from);
		w_vec.sub(cam.to);
		w_vec.normalize();
		
		Vector3d u_vec = new Vector3d();
		u_vec.cross(cam.up, w_vec);
		u_vec.normalize();
		
		Vector3d v_vec = new Vector3d();
		v_vec.cross(w_vec, u_vec);
		v_vec.normalize();
	
		// view rectangle calculations
		float aspect_ratio = (float)cam.imageSize.width / cam.imageSize.height;
		double b = Math.tan(Math.toRadians(cam.fovy /2));
		double t_minus_b = -2 *  Math.tan(Math.toRadians(cam.fovy /2));
		double l = - aspect_ratio * Math.tan(Math.toRadians(cam.fovy/2));
		double r_minus_l =  2 * aspect_ratio * Math.tan(Math.toRadians(cam.fovy /2));

		double offset_i = (offset[0] + getJitterVal())/Math.sqrt(render.samples);
		double offset_j = (offset[1] + getJitterVal())/Math.sqrt(render.samples);
		double u = l + (r_minus_l * (i + offset_i)/cam.imageSize.width);
		double v = b + (t_minus_b * (j + offset_j)/cam.imageSize.height);
		
		// do u * u_vec, v * v_vec, -d * w_vec
		u_vec.scale(u);
		v_vec.scale(v);
		w_vec.scale(-1);
		
		// s = e + u * u_vec + v * v_vec + (-d * vp_normal)
		Vector3d s = new Vector3d(cam.from);
		s.add(u_vec);
		s.add(v_vec);
		s.add(w_vec);
		
		// find d = s - e
		Vector3d d = new Vector3d(s);
		d.sub(cam.from);
		
		ray.set(cam.from, d);
	}
	

	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result Intersection result from raytracing. 
	 * @param light The light to check for visibility.
	 * @param root The scene node.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise. 
	 */
	public boolean inShadow(final IntersectResult result, final Light light, final SceneNode root, IntersectResult shadowResult, Ray shadowRay) {
		
		// TODO: Objective 5: check for shadows and use it in your lighting computation
		
		// light direction = light position - point of intersection
		Vector3d light_dir = new Vector3d(light.from);
		light_dir.sub(result.p);
		
		Vector3d bias = new Vector3d(light_dir);
		bias.scale(0.1);
		
		Point3d eyePoint = new Point3d(result.p.x, result.p.y, result.p.z);
		eyePoint.add(bias);
		// eye point = point of intersection, viewDirection = light origin - intersection
		shadowRay.set(eyePoint, light_dir);
		
		for(Intersectable s: surfaceList) {
			s.intersect(shadowRay, shadowResult);
			if(shadowResult.material != null) {
				return true;
			}
		}
		
		return false;
	}    
}
