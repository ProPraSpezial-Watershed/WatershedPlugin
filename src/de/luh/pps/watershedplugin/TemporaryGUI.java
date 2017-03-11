package de.luh.pps.watershedplugin;

import java.awt.FlowLayout;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.luh.pps.watershedplugin.watershed.ImmersionThread;
import de.luh.pps.watershedplugin.watershed.WatershedThread;
import main.ImageStack;
import main.MasterControl;
import main.Segment;
import main.tools.ToolSegGen;
import misc.messages.Message;
import misc.messages.YObservable;
import misc.messages.YObserver;

public class TemporaryGUI implements YObserver{
	
	private JPanel panel=new JPanel();
	
	private JButton generate,nextSegment,prevSegment;
	
	private JLabel segmentLabel;
	
	private int currentSegment=0,numSegments=1;
	
	private int[] segments;
	
	private WatershedThread thread;
	
	private Segment seg;
	
	public TemporaryGUI(){
		seg=MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME);
		panel.setLayout(new FlowLayout());
		
		generate=new JButton("Generate");
		nextSegment=new JButton("Next Segment");
		prevSegment=new JButton("Previous Segment");
		
		segmentLabel=new JLabel("0/1");
		
		generate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				thread=new ImmersionThread(MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME),true);
				thread.start();
				segments=null;
			}
		});
		nextSegment.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(segments==null)
					segments=thread.getSortedList();
				currentSegment++;
				if(currentSegment>=thread.countSegments())
					currentSegment=0;
				seg.set_bc(thread.getSegment(segments[currentSegment]));
				seg.new_data(true);
				segmentLabel.setText(currentSegment+"/"+thread.countSegments());
			}
		});
		prevSegment.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(segments==null)
					segments=thread.getSortedList();
				currentSegment--;
				if(currentSegment<0)
					currentSegment=thread.countSegments()-1;
				seg.set_bc(thread.getSegment(segments[currentSegment]));
				seg.new_data(true);
				segmentLabel.setText(currentSegment+"/"+thread.countSegments());
			}
		});
		
		panel.add(prevSegment);
		panel.add(generate);
		panel.add(nextSegment);
		panel.add(segmentLabel);
	}
	
	public JPanel getPanel(){
		return panel;
	}

	@Override
	public void update(YObservable yo, Message m) {
		if(m._type==ImageStack.M_SEG_START)
			System.out.println("Start");
		else if(m._type==ImageStack.M_SEG_END)
			System.out.println("End");
	}
}
