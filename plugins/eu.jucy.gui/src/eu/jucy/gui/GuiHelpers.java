package eu.jucy.gui;


import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ParameterizedCommand;


import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;





import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logger.LoggerFactory;



public final class GuiHelpers {
	
	private static final Logger logger = LoggerFactory.make();


	/**
	 * never instantiated
	 *
	 */
	private GuiHelpers(){}

	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM HH:mm:ss");
	
	
	
	
	public static String dateToString(Date date){
		return dateFormat.format(date);
	} 
	
	
	/**
	 * 
	 * @param howMuch  how much we want..
	 * @param max equals 100%
	 * @return
	 */
	public static String toPercentString(long howMuch , long max ){
		if (howMuch == 0) {
			return "0%";
		}
		float percentage= (((float)howMuch*100f) / max) ;
		String perc = Float.toString(percentage);
		
		if (percentage < 100) {
			return perc.substring(0, perc.indexOf('.')+2 )+"%";
		} else if (percentage > 100) {
			return perc.substring(0, perc.indexOf('.') )+"%";
		} else {
			return "100%";
		}
			
	}


	public static void copyTextToClipboard(String text) {
		Display d = Display.getCurrent();
		if (d != null && text != null) {
			Clipboard clipboard = new Clipboard(d);
			TextTransfer textTransfer = TextTransfer.getInstance();
			clipboard.setContents(new Object[]{text}, new Transfer[]{textTransfer});
			clipboard.dispose();
		}
	}
	
	private static int getNumberOfLines(String text) {
		int count =0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\n') {
				count++;
			}
		}
		return count;
	}

	public static String getLastXLines(String text, int lines) {
		while (getNumberOfLines(text) > lines) {
			int i = text.indexOf('\n');
			text = text.substring(i+1);
		}
		return text;
	}
	
	/**
	 * little helper executes given command, logs failures
	 * 
	 * @param commandID
	 */
	public static void executeCommand(String commandId,Map<String,String> parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		executeCommand(window,commandId, parameters);
	}
	
	public static void executeCommand(IWorkbenchWindow window,
			String commandId,Map<String,String> parameters) {
		
		ICommandService cmdService = (ICommandService) window.getService(
			    ICommandService.class);
		IHandlerService handlerService = (IHandlerService)window.getService(IHandlerService.class);
		
		Command com = cmdService.getCommand(commandId);
		
		ParameterizedCommand paCom = ParameterizedCommand.generateCommand(com, parameters);	
		
		ExecutionEvent e = handlerService.createExecutionEvent(paCom, null);
		
		try {
			com.executeWithChecks(e);
		} catch(Exception exc) {
			logger.warn(exc, exc);
		}
	}
	

	/**
	 * 
	 * @param main - image to be added to
	 * @param corner - the icon that should be placed in the lower left corner
	 * @return image copy of main+cornericon
	 */
	public static Image addCornerIcon(Image main,Image corner) {
		Rectangle m = main.getBounds();
		Image i = new Image(null,m.height,m.width); ;
	
		GC gc = new GC(i);
		gc.setBackground(main.getDevice().getSystemColor(SWT.COLOR_WHITE));
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(main, 0, 0);
		
		Rectangle c = corner.getBounds();
		int size = c.width*2 /3;
		gc.drawImage(corner, 0, 0, c.width, c.height, 0, m.width-size, size, size);
		
		gc.dispose();
		
		ImageData id = i.getImageData();
		id.transparentPixel = id.palette.getPixel(new RGB(255,255,255));
		i.dispose();
		
		return new Image(null, id);
	}
	
	/**
	 * takes toString() output and creates rect from it
	 * "Rectangle {" + x + ", " + y + ", " + width + ", " + height + "}"
	 * 
	 * @param s
	 * @return
	 */
	public static Rectangle fromString(String s) {
		
		String regexp = "Rectangle \\{(\\d+), (\\d+), (\\d+), (\\d+)\\}";
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(s);
		if (m.matches()) {
			return new Rectangle(Integer.parseInt(m.group(1))
					, Integer.parseInt(m.group(2))
					, Integer.parseInt(m.group(3))
					, Integer.parseInt(m.group(4)));
		} else {
			return null;
		}
		
	}
	
	public static String escapeMnemonics(String toEscape) {
		return toEscape.replace("&", "&&");
	}
	
	
}
