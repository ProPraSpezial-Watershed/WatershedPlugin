package de.luh.pps.watershedplugin;

import main.ImageStack;
import main.MasterControl;
import misc.messages.Message;
import misc.messages.YObservable;
import misc.messages.YObserver;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import yplugins.YModuleType;
import yplugins.YPlugin;

@PluginImplementation
public class ProPraPlugin implements YPlugin,YObserver {
	@Init
	public void init() {
		MasterControl.register_module(new TestSegmentationModule(), YModuleType.SEGMENTING);		
		MasterControl.get_is().addObserver(this, "ProPraPlugin wants to know when GUI is ready");
		
		System.out.println(get_plugin_name()+" initialized");
	}
	
	@Override
	public String get_plugin_name() {
		return "ProPra 2016 Plugin";
	}

	@Override
	public String get_plugin_short_descr() {
		return "Plugin to learn how to build a Plugin";
	}

	@Override
	public void update(YObservable sender, Message m) {
		System.out.println("ProPraPlugin::update received message from "+sender.getClass()+": "+Message.get_message_string(m._type));

		if (m._type==ImageStack.M_INITIALIZED) {
			System.out.println("GUI initialized");
		}
	}
}
