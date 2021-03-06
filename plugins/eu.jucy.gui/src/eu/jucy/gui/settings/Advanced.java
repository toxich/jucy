package eu.jucy.gui.settings;


import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;


import uc.PI;

import eu.jucy.gui.Lang;


public class Advanced extends UCPrefpage {
	
	public Advanced() {
		super(PI.PLUGIN_ID,"eu.jucy.gui.help.Preferences_Advanced");
	}

	@Override
	protected void createFieldEditors() {
		StringFieldEditor includes = new StringFieldEditor(PI.includeFiles,Lang.IncludedFiles ,50 ,
				getFieldEditorParent()) {

			@Override
			public boolean doCheckState() {
				try {
					String s = getStringValue();
					Pattern.compile(s);
				} catch(PatternSyntaxException e) {
					return false;
				}
				return true;
			}

		};

		addField(includes);	
		
		StringFieldEditor excludes = new StringFieldEditor(PI.excludedFiles,Lang.ExcludedFiles ,50, 
				getFieldEditorParent()) {

			@Override
			public boolean doCheckState() {
				try {
					String s = getStringValue();
					Pattern.compile(s);
				} catch(PatternSyntaxException e) {
					return false;
				}
				return true;
			}

	
			
		};
		addField(excludes);	
		
		IntegerFieldEditor maxHashSpeed= new IntegerFieldEditor(PI.maxHashSpeed,
				Lang.MaxHashSpeed,
				getFieldEditorParent());
		
		maxHashSpeed.setValidRange(0, 100000);
		addField(maxHashSpeed);
		
		IntegerFieldEditor filelistRefreshInterval= new IntegerFieldEditor(PI.filelistRefreshInterval,
				Lang.FilelistRefreshInterval,
				getFieldEditorParent());
		filelistRefreshInterval.setValidRange(5, 60*24*3); //max 3 days
		addField(filelistRefreshInterval);
		
		
		StringFieldEditor bindAddress = new StringFieldEditor(PI.bindAddress,"Bind address (empty for default)" , 
				getFieldEditorParent());
		addField(bindAddress);	
		
		
		
		IntegerFieldEditor minimumSegmentSize= new IntegerFieldEditor(PI.minimumSegmentSize,
				"Minimum Segment Size(MiB)",
				getFieldEditorParent());
		minimumSegmentSize.setValidRange(5, Integer.MAX_VALUE); 
		addField(minimumSegmentSize);
		
		IntegerFieldEditor autoSearchInterval= new IntegerFieldEditor(PI.autoSearchInterval,
				"Interval for Automatic search for alternatives (in Minutes)",
				getFieldEditorParent());
		autoSearchInterval.setValidRange(1, Integer.MAX_VALUE/600); 
		addField(autoSearchInterval);
		
		BooleanFieldEditor fullTextSearch = new BooleanFieldEditor(PI.fullTextSearch, 
				"Allow full text search", 
				getFieldEditorParent());
		addField(fullTextSearch);
		
	}

	
}
