package de.luh.pps.watershedplugin.watershed;

import main.MasterControl;

/**This class encapsulates information on how the source data should be quantized to 
 * prevent oversegmentation. This includes a min/max range and an interval range. Voxels less than min
 * are treated as zero, voxels higher than max are treated as max+1 and all voxels that lie in the same
 * interval are treated as having the same value.
 * 
 * @author Jonas Rinke
 */
public class WatershedQuantizer {
	
	private int min=0,max=Integer.MAX_VALUE;
	
	private int interval;
	
	public WatershedQuantizer(int min,int max,int interval){
		setMin(min);
		setMax(max);
		setInterval(interval);
	}
	
	public WatershedQuantizer(){
		this(0,MasterControl.get_is().get_vch().get_max_value(),64);
	}
	
	public WatershedQuantizer(int min,int max){
		this(min,max,64);
	}
	
	public WatershedQuantizer(int interval){
		this(0,MasterControl.get_is().get_vch().get_max_value(),64);
	}
	
	public void setMin(int min){
		if(min<0 || min>max)
			throw new IllegalArgumentException("Minimum value must be within [0, max]");
		this.min=min;
	}
	
	public int getMin(){
		return min;
	}
	
	public void setMax(int max){
		if(max<min)
			throw new IllegalArgumentException("Maximum value must be greater than min");
		this.max=max;
	}
	
	public int getMax(){
		return max;
	}
	
	public void setInterval(int interval){
		if(interval<1 || interval>max)
			throw new IllegalArgumentException("Interval must be within [1 ; max]");
		this.interval=interval;
	}
	
	public double getInterval(){
		return interval;
	}
	
	/**
	 * Returns the range of the quantizes values.
	 * @return the range.
	 */
	public int getRange(){
		int range=max-min;
		if(range % interval==0)
			return range/interval+1;
		else
			return range/interval+2;
	}
	
	/**
	 * Quantizes an input value according to the quantization configuration of this object.
	 * The output value will lie between 0 and getRange().
	 * @param input The input value.
	 * @return The quantizes value.
	 */
	public int transformValue(int input){
		if(input>max)
			return getRange()-1;
		else if(input<min)
			return 0;
		else
			return (input-min)/interval;
	}
}
