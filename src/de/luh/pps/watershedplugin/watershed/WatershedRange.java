package de.luh.pps.watershedplugin.watershed;

/**This class encapsulates the range that the ImmersionThread should operate on.
 * 
 * This class contains a min and max value as well as a scalefactor. Values outside of the min/max range
 * are threated as 255. Values inside the range are multiplied with the scalefactor and converted to an integer.
 * @author Jonas
 */
public class WatershedRange {
	
	private int min,max;
	
	private double scaleFactor;
	
	public WatershedRange(int min,int max,double scaleFactor){
		setMin(min);
		setMax(max);
		setScaleFactor(scaleFactor);
	}
	
	public WatershedRange(){
		this(0,255,1.0);
	}
	
	public WatershedRange(int min,int max){
		this(min,max,1.0);
	}
	
	public WatershedRange(double scaleFactor){
		this(0,255,scaleFactor);
	}
	
	public void setMin(int min){
		if(min<0 || min>255)
			throw new IllegalArgumentException("Minimum value must be within [0, 255]");
		this.min=min;
	}
	
	public int getMin(){
		return min;
	}
	
	public void setMax(int max){
		if(max<0 || max>255)
			throw new IllegalArgumentException("Maximum value must be within [0, 255]");
		this.max=max;
	}
	
	public int getMax(){
		return max;
	}
	
	public void setScaleFactor(double scaleFactor){
		if(scaleFactor>1.0 || scaleFactor<=0)
			throw new IllegalArgumentException("Scalefactor must be within (0.0, 1.0]");
		this.scaleFactor=scaleFactor;
	}
	
	public double getScaleFactor(){
		return scaleFactor;
	}
	
	/**Transforms a value to according to this ImmersionRange.
	 * 
	 * The returned value will be 255 if input is outside of the min/max range and (int) (input*scalefactor) otherwise.
	 * @param input The input value.
	 * @return The transformed value.
	 */
	public int transformValue(int input){
		if(input<min || input>max)
			return 255;
		else
			return (int) (input*scaleFactor);
	}
}
