package eu.jucy.gui.settings;

import java.io.File;


import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;

import eu.jucy.gui.Lang;


import uc.PI;


public class DownloadsPreferencePage extends UCPrefpage {
	

	public DownloadsPreferencePage(){
		super(PI.PLUGIN_ID);
	}
	@Override
	protected void createFieldEditors() {
		
		
		//Directories
		checkExistance(PI.downloadDirectory);
		DirectoryFieldEditor defdownloaddir = new DirectoryFieldEditor(PI.downloadDirectory,
				Lang.DefaultDownloadDirectory,
				getFieldEditorParent());
		addField(defdownloaddir);
		
		checkExistance(PI.tempDownloadDirectory);
		DirectoryFieldEditor tempdownloaddir = new DirectoryFieldEditor(PI.tempDownloadDirectory,
				Lang.UnfinishedDownloadsDirectory,
				getFieldEditorParent());
		addField(tempdownloaddir);
		
		//Limits
		
		IntegerFieldEditor maxsimDownloads = new IntegerFieldEditor(PI.maxSimDownloads,
				Lang.MaximumSimultaneousDownloads,
				getFieldEditorParent());
		maxsimDownloads.setValidRange(0, Integer.MAX_VALUE);
		addField( maxsimDownloads );

		IntegerFieldEditor downlimit = new IntegerFieldEditor(PI.downloadLimit,
				Lang.DownloadLimit,
				getFieldEditorParent());
		downlimit.setValidRange(0, Integer.MAX_VALUE/1024);
		addField( downlimit );	
	}
	
	private static File checkExistance(String pref) {
		File dir = new File(PI.get(pref));
		if (!dir.isDirectory() && !dir.mkdirs()) {
			logger.error("Unable to use directory: "+dir);
			dir = new File(DefaultScope.INSTANCE .getNode(PI.PLUGIN_ID).get(pref, dir.toString()));
			if (!dir.isDirectory() && !dir.mkdirs()) {
				logger.fatal("Unable to set fallback directory.  "+pref);
			} 
		}
		return dir;
	}


}
