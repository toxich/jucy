package eu.jucy.gui.statusline;

import helpers.IObservable;
import helpers.Observable.IObserver;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import uc.DCClient;
import uihelpers.SUIJob;


import eu.jucy.gui.Application;
import eu.jucy.gui.ApplicationWorkbenchWindowAdvisor;
import eu.jucy.gui.IImageKeys;
import eu.jucy.gui.UserColumns.Nick;

public class AwayStatus extends Label implements IObserver<String> {

	private final Image offline;
	public AwayStatus(Composite parent) {
		super(parent, SWT.BORDER);
		offline = AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID, IImageKeys.OFFLINE_16).createImage();
		updateAwayStatus();
		ApplicationWorkbenchWindowAdvisor.get().getAwayObservable().addObserver(this);
		addListener(SWT.MouseDown,new Listener() {
			public void handleEvent(Event event) {
				DCClient dcc = ApplicationWorkbenchWindowAdvisor.get();
				dcc.setAway(!dcc.isAway());
			}
		});
	}
	
	
	public void dispose() {
		offline.dispose();
		ApplicationWorkbenchWindowAdvisor.get().getAwayObservable().deleteObserver(this);
		super.dispose();
	}
	
	protected void checkSubclass () {}
	

	public void update(IObservable<String> o, String arg) {
		new SUIJob(this) {
			@Override
			public void run() {
				updateAwayStatus();
			}
		}.schedule();
	}


	public void updateAwayStatus() {
		DCClient dcc = ApplicationWorkbenchWindowAdvisor.get();
		setImage(dcc.isAway()?offline: Nick.getDefaultUserImage());	
		setToolTipText(dcc.isAway()?dcc.getAwayMessage():"Online"); 
	}

}
