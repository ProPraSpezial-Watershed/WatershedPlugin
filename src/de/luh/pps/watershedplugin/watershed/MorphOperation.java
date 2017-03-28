package de.luh.pps.watershedplugin.watershed;

public class MorphOperation {
	
	private int dimX,dimY,dimZ;
	
	private int radiusX,radiusY,radiusZ;
	
	private short[] original,morph1,morph2;
	
	public MorphOperation(double relativeRadius,int dimX,int dimY,int dimZ){
		this.dimX=dimX;
		this.dimY=dimY;
		this.dimZ=dimZ;
		radiusX=(int) (relativeRadius*dimX);
		radiusY=(int) (relativeRadius*dimY);
		radiusZ=(int) (relativeRadius*dimZ);
	}
	
	private short findMaxValue(int x,int y,int z,short[] source){
		short max=0;
		for(int i=z-radiusZ/2;i<=z+radiusZ/2;i++){
			if(i<0 || i>=dimZ)
				continue;
			for(int j=y-radiusY/2;j<=y+radiusY/2;j++){
				if(j<0 || j>=dimY)
					continue;
				for(int k=x-radiusX/2;k<=x+radiusX/2;k++){
					if(k<0 || k>=dimX)
						continue;
					int pixel=k+j*dimX+i*dimY*dimX;
					if(source[pixel]>max)
						max=source[pixel];
				}
			}
		}
		return max;
	}
	
	private short findMinValue(int x,int y,int z,short[] source){
		short min=Short.MAX_VALUE;
		for(int i=z-radiusZ/2;i<=z+radiusZ/2;i++){
			if(i<0 || i>=dimZ)
				continue;
			for(int j=y-radiusY/2;j<=y+radiusY/2;j++){
				if(j<0 || j>=dimY)
					continue;
				for(int k=x-radiusX/2;k<=x+radiusX/2;k++){
					if(k<0 || k>=dimX)
						continue;
					int pixel=k+j*dimX+i*dimY*dimX;
					if(source[pixel]<min)
						min=source[pixel];
				}
			}
		}
		return min;
	}
	
	private void erode(short[] source,short[] dest){
		for(int z=0;z<dimZ;z++){
			System.out.println(z);
			for(int y=0;y<dimY;y++){
				for(int x=0;x<dimX;x++){
					int pixel=x+y*dimX+z*dimY*dimX;
					dest[pixel]=findMinValue(x,y,z,source);
				}
			}
		}
	}
	
	private void dilate(short[] source,short[] dest){
		for(int z=0;z<dimZ;z++){
			for(int y=0;y<dimY;y++){
				for(int x=0;x<dimX;x++){
					int pixel=x+y*dimX+z*dimY*dimX;
					dest[pixel]=findMaxValue(x,y,z,source);
				}
			}
		}
	}
	
	public short[] apply(short[] source){
		original=source;
		morph1=new short[source.length];
		morph2=new short[source.length];
		
		erode(original,morph1);
		dilate(morph1,morph2);
		return morph2;
	}
}
