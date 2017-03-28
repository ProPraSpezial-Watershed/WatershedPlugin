package de.luh.pps.watershedplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.luh.pps.watershedplugin.watershed.ImmersionThread;
import de.luh.pps.watershedplugin.watershed.WatershedThread;
import gui.HistoPanel;
import gui.JRangeSlider;
import jgridmaker.GMPanel;
import main.ImageStack;
import main.MasterControl;
import main.Segment;
import main.tools.ToolSegGen;
import misc.Voxel;
import misc.grid.BitCube;
import misc.messages.Message;
import misc.messages.YObservable;
import misc.messages.YObserverWantsAWTThread;
import yplugins.YModule;

public class WatershedModule extends GMPanel implements YModule, YObserverWantsAWTThread {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3354681511617261298L;
	// The "Generate" button
	private JButton start;
	// The next segmentation button
	private JButton next;
	
	private JButton fromSeed;
	// The previous segmentation button
	private JButton previous;
	// Textfield for min Value
	private JFormattedTextField min;
	// Textfield for max Value
	private JFormattedTextField max;
	// Slider for min max slection
	private JRangeSlider slider;
	// Histopanel
	private HistoPanel hp;
	// Selected High and Toplevel from User
	private int maxLevel;
	private int minLevel;
	// Slider for dynamic value within the algorithm
	private JSlider jslider;
	private JFormattedTextField value;
	private double dynamicValue;
	
	private JList<String> segmentationList;
	private JPanel listPanel;

	private WatershedThread my_thread;

	private int[] segments;

	private int currentSegment = 0;
	private DefaultListModel<String> model;

	public WatershedModule() {
		// Initiate Generate Button
		this.start = new JButton("Generate");
		// Set Action Listener when the button was clicked
		this.start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Segment tmp_seg = MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME);
				my_thread = new ImmersionThread(tmp_seg, true, minLevel, maxLevel, 1, dynamicValue);
				System.gc();
				segments = null;
				currentSegment = 0;
				my_thread.start();
				 
					
				 
			}
		});
		// next Generate Button
		this.next = new JButton("Next Segment");
		this.next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (segments != null) {
					currentSegment++;

					if (currentSegment >= segments.length)
						currentSegment = 0;

					BitCube segmentData = my_thread.getSegment(segments[currentSegment]);
					MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME).set_bc(segmentData);
					segmentationList.setSelectedIndex(currentSegment);
				}
			}
		});
		//Generate Seed button
		this.fromSeed = new JButton("From Seed");
		this.fromSeed.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ImageStack is=MasterControl.get_is();
				Voxel seed=is.get_seeds().get(0);
				BitCube segmentData = my_thread.getSegmentAt(seed._x,seed._y,seed._z);
				is.get_segment(ToolSegGen.TMP_SEG_NAME).set_bc(segmentData);
			}
		});
		// prev Generate Button
		this.previous = new JButton("Prev Segment");
		this.previous.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (segments != null) {
					currentSegment--;
					if (currentSegment < 0)
						currentSegment = segments.length - 1;

					BitCube segmentData = my_thread.getSegment(segments[currentSegment]);
					MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME).set_bc(segmentData);
					segmentationList.setSelectedIndex(currentSegment);
				}
			}
		});

		ImageStack is = MasterControl.get_is();
		is.addObserver(this, "Test Module Listener");
		// Create min Textfield and add a change listener for input
		this.min = new JFormattedTextField(NumberFormat.getIntegerInstance());
		this.min.setColumns(8);
		this.min.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// get new Value, set Slider and histopanel highlight new
				int minTemp = Integer.parseInt(min.getText());
				minLevel = minTemp;
				slider.setRange(minLevel, maxLevel);
				hp.highlight_interval(minLevel, maxLevel, new Color(255, 0, 0, 80));
			}
		});

		// Create max Textfield and add a change listener for input
		this.max = new JFormattedTextField(NumberFormat.getIntegerInstance());
		this.max.setColumns(8);
		this.max.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// get new Value, set Slider and histopanel highlight new
				int maxTemp = Integer.parseInt(max.getText());
				maxLevel = maxTemp;
				slider.setRange(minLevel, maxLevel);
				hp.highlight_interval(minLevel, maxLevel, new Color(255, 0, 0, 80));
			}
		});

		// Create the histopanel
		this.hp = new HistoPanel(is.get_vch(), "HPTest", false, false);
		JPanel hpPanel = new JPanel();
		hpPanel.setBorder(new EmptyBorder(0, 16, 0, 16));

		hpPanel.setLayout(new BorderLayout());
		hpPanel.add(this.hp, "Center");

		// Create slider and add change listener
		this.slider = new JRangeSlider(0, 1000, 100, 300, 1);
		this.slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// get new Value, histopanel highlight and textfields new
				minLevel = slider.getLowValue();
				maxLevel = slider.getHighValue();
				min.setText(minLevel + "");
				max.setText(maxLevel + "");
				hp.highlight_interval(minLevel, maxLevel, new Color(255, 0, 0, 80));
			}
		});
		// Create Slider with value between 0 and 10,000 (or 0-1) with 0.15 as
		// ini value
		this.jslider = new JSlider(JSlider.HORIZONTAL, 0, 10000, 1500);
		// Create change listener to update textfield
		this.jslider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e){
				//Get Real number we wont 
				dynamicValue = jslider.getValue()/1000.0;	//Changed range to 0-10
				value.setText(dynamicValue+"");
			}
		});
		// Create textfield to see the slider value
		this.value = new JFormattedTextField(NumberFormat.INTEGER_FIELD);
		this.value.setColumns(8);
		dynamicValue = this.jslider.getValue()/1000.0;
		this.value.setText(dynamicValue+"");
		
		

		// add listview component		
		this.model = new DefaultListModel<String>();
		this.segmentationList = new JList<String>(this.model);
		this.segmentationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.segmentationList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		
	   
	    //this.segmentationList.setPreferredSize(null);
	   
		this.segmentationList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				currentSegment = segmentationList.getSelectedIndex();
				BitCube segmentData =  my_thread.getSegment(segments[currentSegment]);
				MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME).set_bc(segmentData);
				
			}
		});
		  this.listPanel = new JPanel();
		this.listPanel.setLayout(new BorderLayout());
		this.listPanel.add(this.segmentationList, "Center");
		// Add components
		add("generate", this.start);
		add("fromseed",this.fromSeed);
		add("min", this.min);
		add("max", this.max);
		add("hp", hpPanel);
		add("slider", this.slider);
		add("next", this.next);
		add("prev", this.previous);
		add("jslider", this.jslider);
		add("value", this.value);
		add("segList",this.listPanel);

		// Set layout
		set_layout(
				"   <table width='100%' height='100%' cellpadding='0' border='0'>											"
						+ " 	<tr height='97%'> 																			"
						+ "   		<td fill='both'>::hp::</td>																"
						+ "     </tr>																						"
						+ "  	<tr height='6%'> 																			"
						+ "   		<td fill='horizontal'>::slider::</td>													"
						+ "     </tr>																						"
						+ "   	<tr height='1%'>  																			"
						+ "   		<td fill='horizontal'> 																	"
						+ "  	    	<table width='100%' cellpadding='0' cellspacing='0' margin='0' border='0'>    		"
						+ "   				<tr>																			"
						+ "       		 		<td width='1%' anchor='west'>::min::</td>   								"
						+ "        	     		<td width='1%' anchor='west'>Min</td>   									"	
						+ "        	     		<td width='100%'>::generate::</td>   							         	"	
						+ "       		 		<td width='1%' anchor='east'>Max</td>                                 		"
						+ "       		 		<td width='1%' anchor='east'>::max::</td>  							   		"
						+ "   				</tr> 																			"
						+ "   			</table>																	    	"
						+ "  		</td> 																					"
						+ "  	</tr>																						"						
						+ "		<tr height='1%'> 																	    	"
						+ "       	 <td fill='horizontal'> 																"
						+ "     		<table width='100%' cellpadding='0' cellspacing='0' margin='0' border='0'>     	    "
						+ "     			<tr>         														            "
						+ "						<td colspan='3' fill='both'>::jslider::</td>								"
						+ "						<td anchor='west' >::value::</td>	 							    		"
						+ "       		 		<td anchor='east'>::prev::</td>												"
						+ "						<td anchor='west'>::next::</td>												"
						+ "						<td anchor='west'>::fromseed::</td>											"						
						+ "					</tr> 																	  	    "
						+ "				</table> 																			"
						+ "			</td>																					"
						+ "		</tr>																						"
						+ "     <tr>"
						+"      <td fill='both'>::segList::</td>"
						+"</tr>"
						+ "	</table>																						");

	}
	
	private void updateUIFields(){
		for (int j = 0; j < 3; j++) {
			// load stack to set data
			// need to do 3 times ? don't know at the moment
			ImageStack is = MasterControl.get_is();
			if (is.get_state() == 2) {
				minLevel = is.get_vch().get_nonzero_min();
				maxLevel = is.get_vch().get_nonzero_max();
				slider.setMinimum(minLevel);
				slider.setMaximum(maxLevel);
				slider.setRange(minLevel, maxLevel);
				min.setText(is.get_raw_value(minLevel) + "");
				max.setText(is.get_raw_value(maxLevel) + "");
			}

		}
	}

	@Override
	public String get_module_name() {
		return "Watershed";
	}

	@Override
	public String get_module_short_descr() {
		return "Applies a gradient watershed transform";
	}

	@Override
	public JPanel get_module_interface() {
		return this;
	}

	@Override
	public void update(YObservable sender, Message m) {
		System.out.println("SampleSegModule::update received message from " + sender.getClass() + ": "
				+ Message.get_message_string(m._type));
	 
		if (sender.getClass() == ImageStack.class) {
			if (m._type == ImageStack.M_LOADING_END) {
				System.out.println("TestModule received M_LOADING_END message");
				updateUIFields();			
			}

			if (m._type == ImageStack.M_SEG_END) {
				segments = my_thread.getSortedList();
				for(int i = 0; i<segments.length;i++){
					model.addElement("Segment: "+segments[i]);
					
				}
				// not nice here need to clean up but so it do what it should do so 
				// creates a list with the segmentation of the watershed algo
				this.segmentationList.setMinimumSize(new Dimension(100,60));
				this.segmentationList.setMaximumSize(new Dimension(100,60));
				this.segmentationList.setSize(new Dimension(100,60));
				JScrollPane listScroller = new JScrollPane(this.segmentationList);
				listScroller.setMinimumSize(new Dimension(100,60));
				listScroller.setMaximumSize(new Dimension(100,60));
				listScroller.setSize(new Dimension(100,60));
				listScroller.setPreferredSize(new Dimension(100,60));
			    this.listPanel.removeAll();
				this.listPanel.setLayout(new BorderLayout());
				this.listPanel.add(listScroller );
				listScroller.repaint();
				this.listPanel.repaint();
			}
		}
	}
}
