package de.luh.pps.watershedplugin;

import java.awt.FlowLayout;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.luh.pps.watershedplugin.watershed.ImmersionThread;
import main.ImageStack;
import main.MasterControl;
import main.tools.ToolSegGen;
import misc.messages.Message;
import misc.messages.YObservable;
import misc.messages.YObserver;

public class TemporaryGUI implements YObserver{
	
	private JPanel panel=new JPanel();
	
	private JButton generate,nextSegment,prevSegment;
	
	private JLabel segmentLabel;
	
	private int currentSegment=0,numSegments=1;
	
	public TemporaryGUI(){
		panel.setLayout(new FlowLayout());
		
		generate=new JButton("Generate");
		nextSegment=new JButton("Next Segment");
		prevSegment=new JButton("Previous Segment");
		
		segmentLabel=new JLabel("0/1");
		
		generate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				ImmersionThread thread=new ImmersionThread(MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME),true);
				thread.start();
			}
		});
		nextSegment.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				currentSegment++;
				if(currentSegment>=numSegments)
					currentSegment=0;
				segmentLabel.setText(currentSegment+"/"+numSegments);
			}
		});
		prevSegment.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				currentSegment--;
				if(currentSegment<0)
					currentSegment=numSegments-1;
				segmentLabel.setText(currentSegment+"/"+numSegments);
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

	public void setNumSegments(int numSegments){
		this.numSegments=numSegments;
		segmentLabel.setText(currentSegment+"/"+numSegments);
	}

	@Override
	public void update(YObservable yo, Message m) {
		if(m._type==ImageStack.M_SEG_START)
			System.out.println("Start");
		else if(m._type==ImageStack.M_SEG_END)
			System.out.println("End");
	}
}
