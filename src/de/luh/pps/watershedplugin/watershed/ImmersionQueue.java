package de.luh.pps.watershedplugin.watershed;

class ImmersionQueue {
	
	private int[] buffer;
	
	private int head=0,tail=0;
	
	public ImmersionQueue(){
		buffer=new int[256];
	}
	
	public void enqueue(int value){
		buffer[head]=value;
		head++;
		if(head>=buffer.length)
			head=0;
		if(head==tail){
			head=buffer.length;
			tail=0;
			expand();
		}
	}
	
	public int dequeue(){
		int value=buffer[tail];
		tail++;
		if(tail>=buffer.length)
			tail=0;
		return value;
	}
	
	private void expand(){
		int[] largerBuffer=new int[buffer.length*2];
		System.arraycopy(buffer,head,largerBuffer,0,buffer.length-head);
		System.arraycopy(buffer, 0, largerBuffer, buffer.length-head, head);
		buffer=largerBuffer;
	}
	
	public boolean isEmpty(){
		return head==tail;
	}
}
