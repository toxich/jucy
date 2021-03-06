package eu.jucy.gui.texteditor;

import helpers.GH;
import helpers.SizeEnum;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logger.LoggerFactory;


import org.apache.log4j.Logger;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;



import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;


import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.jucy.gui.Application;
import eu.jucy.gui.ApplicationWorkbenchWindowAdvisor;
import eu.jucy.gui.GUIPI;
import eu.jucy.gui.GuiAppender;
import eu.jucy.gui.GuiHelpers;
import eu.jucy.gui.IImageKeys;
import eu.jucy.gui.Lang;
import eu.jucy.gui.favhub.FavHubEditor;
import eu.jucy.gui.search.OpenSearchEditorHandler;
import eu.jucy.gui.texteditor.StyledTextViewer.ControlReplacement;
import eu.jucy.gui.texteditor.StyledTextViewer.Message;
import eu.jucy.gui.texteditor.StyledTextViewer.TextReplacement;



import uc.DCClient;
import uc.FavHub;
import uc.IHub;
import uc.PI;
import uc.files.MagnetLink;
import uc.files.downloadqueue.AbstractDownloadQueueEntry;
import uc.files.downloadqueue.AbstractDownloadFinished;
import uihelpers.SUIJob;


public class URLTextModificator implements ITextModificator {

	private static final Logger logger =  LoggerFactory.make();

//	private static final char URL_CHAR = '\uFFFC';
	
	private static Image IMAGE_URL_ICON,IMAGE_SEARCH_ICON;
	
	
	private static char[] 	OPENING_DELIMTER = new char[]{'\"','<','(','{','[','\''},
							CLOSING_DELIMITER = new char[]{'\"','>',')','}',']','\''};
	
	private static Image getImageURLIcon() {
		if (IMAGE_URL_ICON == null) {
			IMAGE_URL_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(
					Application.PLUGIN_ID, IImageKeys.VIEWIMAGEICON).createImage();
		}
		return IMAGE_URL_ICON;
	}
	
	private static Image getImageSearchIcon() {
		if (IMAGE_SEARCH_ICON == null) {
			IMAGE_SEARCH_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(
					Application.PLUGIN_ID, IImageKeys.SEARCH_16).createImage();
		}
		return IMAGE_SEARCH_ICON;
	}
	
	
	public static final String ID = "eu.jucy.gui.URLTextModificator";
	
	private static final String URLENDING = "\\w[\\S]*[\\S&&[^<>\"\\)]]"; 
	//	"[\\w\\p{L}\\-_]+(\\.[\\w\\p{L}\\-_]+)+([\\w\\p{L}\\-\\.,@?^=%&amp;:/~\\+#\\(\\)]*[\\w\\p{L}\\-\\@?^=%&amp;/~\\+#])?";
	private static final String URL = "(http|ftp|https):\\/\\/"+URLENDING;


	
	private final AbstractLinkType[] LINK_TYPES = new AbstractLinkType[]{
				new HTTPLink(),new MagLink(),new HubLink()}; 
	
	private static final Pattern ANY_URL = Pattern.compile(
			"((?:"+URL+")|(?:"+MagnetLink.MagnetURI+")|(?:"+HubLink.HL_PAT+"))"); 
	
	private static final String[] IMAGE_ENDINGS =  new String[] {".png",".jpg",".bmp", ".gif"} ;
	

	
	private StyledText text;
	private StyledTextViewer viewer;
	
	

	public void init(StyledText st,StyledTextViewer viewer, IHub hub) {
		if (st.isDisposed()) {
			throw new IllegalStateException("can't init on disposed Text: "+hub.getName()+"  "+hub.getFavHub().getHubaddy());
		}
		this.viewer = viewer;
		this.text = st;
		text.setBackgroundMode(SWT.INHERIT_FORCE);
	
	}
	
	
	
	public void dispose() {}
	

//	public String modifyMessage(String message, Message original, boolean pm) {
//		if (message.indexOf(URL_CHAR) != -1 ) {
//			message = message.replace(URL_CHAR, ' '); //replace invalid chars..
//		}
//		
//		Matcher m = ANY_URL.matcher(message);
//		int minimumSearchpos = 0;
//		while (minimumSearchpos < message.length() && m.find(minimumSearchpos)) {
//			String uri = m.group();
//			AbstractLinkType alt = getMatching(uri);
//			
//			int start = m.start();
//			logger.debug("found image URI: "+uri+" "+uri.length() );
//			message = message.substring(0, start)+URL_CHAR
//					+(alt.hasImageAfterURI(uri)?" ":"")+message.substring(m.end());
//			m = ANY_URL.matcher(message);
//		
//			minimumSearchpos = start+2;
//		}
//	
//		return message;
//	}
	
	
	public void getMessageModifications(Message original, boolean pm,List<TextReplacement> replacement) {
		String message = original.getMessage();
		Matcher m = ANY_URL.matcher(message);
		int minimumSearchpos = 0;
		while (minimumSearchpos < message.length() && m.find(minimumSearchpos)) {
			int start = m.start();
			String u = m.group();
			for (int i=0; i < OPENING_DELIMTER.length;i++) {
				if (message.charAt(start-1) == OPENING_DELIMTER[i] && u.charAt(u.length()-1) == CLOSING_DELIMITER[i]) {
					u = u.substring(0,u.length()-1);
					break;
				}
			}
			final String uri = u;
			
			int end = start+uri.length();
			AbstractLinkType alt = getMatching(uri);
			
			replacement.add(new ControlReplacement(start,uri) {
				
				@Override
				public Control createControl(StyledText createOn,float[] ascent) {
					AbstractLinkType alt = getMatching(this.replacedText);
					String linkText = alt.getTextReplacement(this.replacedText);
					
					Link link = new Link(createOn, SWT.NONE);
					link.setBackground( GUIPI.getColor(GUIPI.urlModCol));
					link.setFont(GUIPI.getFont(GUIPI.urlModFont));
					link.setData(this.replacedText);
					link.setToolTipText(GuiHelpers.escapeMnemonics(this.replacedText));
					link.setText("<a>"+linkText+"</a>");
					ascent[0]= 0.8f;
					
					Menu menu = new Menu(link);
					MenuItem mi = new MenuItem(menu,SWT.PUSH);
					mi.setData(this.replacedText);
					mi.setText(Lang.CopyAddressToClipboard);
					mi.addSelectionListener(adapter);
					link.setMenu(menu);
					
					link.addListener (SWT.Selection, new Listener () {
						public void handleEvent(Event event) {
							logger.debug("Selection: " + event.text+ " "+event.widget.getData());
							String uri = (String)event.widget.getData();
							getMatching(uri).execute(uri);
						}
					});
					
					return link;
				}
			});
			if (alt.hasImageAfterURI(uri)) {
				replacement.add(new ControlReplacement(end,"") {
					
					public void apply(StyledText st,List<StyleRange> toAdd,List<ObjectPoint<Image>> imagePoints,List<ObjectPoint<Control>> controlPoints ,int positionInText,Message message) {
						float[] ascent = new float[] {2f/3f};
						Control c = createControl(st,ascent);
						final ObjectPoint<Control> op = ObjectPoint.create(positionInText,replacedText, ascent[0], c, toAdd);
						c.addMouseListener(new MouseAdapter() {
							public void mouseDown(MouseEvent e) {
								String uri = (String)e.widget.getData();
								getMatching(uri).executeImageClick(uri,op,URLTextModificator.this);
							}
						});
						controlPoints.add(op);
						
					}
					@Override
					public Control createControl(StyledText createOn,float[] ascent) {
						AbstractLinkType alt = getMatching(uri);
						Label lab = new Label(createOn,SWT.NONE);
						Image img = alt.getImageAfterURI(uri);
						lab.setImage(img);
						lab.setData(uri);
						ascent[0]=2f/3f;
						
						
						return lab;
					}
					
				});
			}
			
			
			
			logger.debug("found image URI: "+uri+" "+uri.length() );
		
			minimumSearchpos = end;
		}
		
	}


	private static final SelectionAdapter adapter = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			String uri = (String)e.widget.getData();
			GuiHelpers.copyTextToClipboard(uri);
		}
	};
	
//	public void getStyleRange(String message, int start,
//			Message originalMessage, List<StyleRange> ranges, List<ObjectPoint<Image>> images) {
//		
//		Matcher m = ANY_URL.matcher(originalMessage.getMessage());
//		int messagePos = 0;
//		int charPos = 0;
//		while (m.find(messagePos)) {
//			String foundURI = m.group();
//			AbstractLinkType alt = getMatching(foundURI);
//			String linkText = alt.getTextReplacement(foundURI);
//			
//			int posOfURLChar = message.indexOf(URL_CHAR,charPos);
//			int posURI = posOfURLChar+start; // to full text..
//			Link link = new Link(text, SWT.NONE);
//			link.setBackground( GUIPI.getColor(GUIPI.urlModCol));
//			link.setFont(GUIPI.getFont(GUIPI.urlModFont));
//			link.setData(foundURI);
//			link.setToolTipText(foundURI);
//			link.setText("<a>"+linkText+"</a>");
//		
//			viewer.addControl(link,posURI, 0.8f); 
//			Menu menu = new Menu(link);
//			MenuItem mi = new MenuItem(menu,SWT.PUSH);
//			mi.setData(foundURI);
//			mi.setText("Copy URI to Clipboard");
//			mi.addSelectionListener(adapter);
//			link.setMenu(menu);
//			
//			link.addListener (SWT.Selection, new Listener () {
//				public void handleEvent(Event event) {
//					logger.debug("Selection: " + event.text+ " "+event.widget.getData());
//					String uri = (String)event.widget.getData();
//					getMatching(uri).execute(uri);
//				}
//			});
//			
//			if (alt.hasImageAfterURI(foundURI)) {
//				logger.debug("added image: "+foundURI);
//				addLabelImage(posURI+1,foundURI);
//			}
//			
//			messagePos = m.end();
//			charPos = posOfURLChar+1;
//		}
//	}
	
	
	void addLabelImage(int pos,String uri) {
		AbstractLinkType alt = getMatching(uri);
		Label lab = new Label(text,SWT.NONE);
		Image img = alt.getImageAfterURI(uri);
		lab.setImage(img);
		lab.setData(uri);
		final ObjectPoint<Control> op =  viewer.addControl(lab, pos,"", 2f/3f);
		lab.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				String uri = (String)e.widget.getData();
				getMatching(uri).executeImageClick(uri,op,URLTextModificator.this);
			}
		});
	}
	
	void addLabelReplacementImage(int pos,String uri,final Image img) {
		Label lab = new Label(text,SWT.NONE);
		lab.setImage(img);
		lab.setData(uri);
		final ObjectPoint<Control> op = viewer.addControl(lab, pos,"", 2f/3f);
		lab.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				String uri = (String)e.widget.getData();
				addLabelImage(op.x,uri);
				text.redraw(); 
			}
		});
		lab.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				img.dispose();
			}
		});
	}
	

	
	public AbstractLinkType getMatching(String uri) {
		for (AbstractLinkType alt: LINK_TYPES) {
			Matcher m	= alt.getLinkPat().matcher(uri);
			if (m.matches()) {
				return alt;
			}
		}
		throw new IllegalStateException();
	}
	
	public static abstract class AbstractLinkType {
		
		
		private final Pattern linkPat;

		private AbstractLinkType(String linkPat) {
			super();
			this.linkPat = Pattern.compile(linkPat);
		}
		
		public abstract void execute(String matched);
	
		public String getTextReplacement(String matched) {
			return matched;
		}
		
		/**
		 * 
		 * @param uri - provided URI
		 * @return an image if there should be an image put after the URI ...
		 */
		public Image getImageAfterURI(String uri) {
			return null;
		}
		
		public boolean hasImageAfterURI(String uri) {
			return getImageAfterURI(uri) != null;
		}
		
		/**
		 * executed if the image after the URI was clicked instead of the URI itself
		 * @param uri - the URI before the image..
		 */
		public void executeImageClick(String uri,ObjectPoint<Control> point,URLTextModificator mod) {}
		
		public Pattern getLinkPat() {
			return linkPat;
		}	
	}
	
	private static class HTTPLink extends AbstractLinkType {
		private HTTPLink() {
			super(URL);
		}

		@Override
		public void execute(String matched) {
			try {
				URL url = new URL(matched);
				
				IWorkbenchBrowserSupport browserSupport =
				PlatformUI.getWorkbench().getBrowserSupport();
				
				IWebBrowser  browser = browserSupport.createBrowser("myid");
				browser.openURL(url);
				
			} catch (IOException ioe) {
				logger.warn(ioe,ioe);
			} catch (PartInitException io2) {
				logger.warn(io2,io2);
			}
		}
		
//		@Override
//		public void getStyleRanges(List<StyleRange> ranges, int start, int length,String matched, List<ObjectPoint<Image>> images) {
//			ranges.add(getURLRange(start, length, GUIPI.getColor(GUIPI.urlModCol)) );
//		}

		@Override
		public void executeImageClick(String uri,ObjectPoint<Control> point,URLTextModificator mod) {
			logger.debug("Image url icon pressed: "+uri);
			try {
				new GraphicalFileDownloader(new URL(uri), point,mod).start();
			} catch (MalformedURLException e) {
				logger.warn(e,e);
			}
		}

		public boolean hasImageAfterURI(String uri) {
			for (String ending: IMAGE_ENDINGS) {
				if (uri.toLowerCase().endsWith(ending)) {
					return true;
				}
			}
			return false;
		}
		@Override
		public Image getImageAfterURI(String uri) {
			for (String ending: IMAGE_ENDINGS) {
				if (uri.toLowerCase().endsWith(ending)) {
					return getImageURLIcon();
				}
			}
			return null;
		}
	}
	
	private static class MagLink extends AbstractLinkType {
		private MagLink() {
			super(MagnetLink.MagnetURI);
		}

		@Override
		public void execute(String matched) {
			MagnetLink magnetLink = MagnetLink.parse(matched);
			if (magnetLink != null) {
				if (magnetLink.isComplete()) {	
					magnetLink.download();
					logger.log(GuiAppender.GUI,String.format(Lang.AddedFileViaMagnet,magnetLink.getName()));
				} else {
					String search = null;
					if (magnetLink.getTTHRoot() != null) {
						search = magnetLink.getTTHRoot().toString();
					} else if (magnetLink.get(MagnetLink.KEYWORD_TOPIC) != null) {
						search = magnetLink.get(MagnetLink.KEYWORD_TOPIC);
					}
					if (!GH.isNullOrEmpty(search)) {
						OpenSearchEditorHandler.openSearchEditor(
								PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								, search);
					}
				}
			}
		}

		

		@Override
		public void executeImageClick(final String uri,
				final ObjectPoint<Control> point,final URLTextModificator mod) {
			MagnetLink magnetLink = MagnetLink.parse(uri); 
			
			if (magnetLink != null) {
				if (magnetLink.isComplete()) {
					point.obj.setEnabled(false);
					DCClient dcc = ApplicationWorkbenchWindowAdvisor.get();
					File target = dcc.getFilelist().getFile(magnetLink.getTTHRoot());
					if (target != null && target.isFile()) {
						openFile(target,uri,point,mod);
					} else {
						target = new File(PI.getTempPath(),magnetLink.getName());
						if (target.isFile()) {
							openFile(target,uri,point,mod);
						} else {
							AbstractDownloadQueueEntry adqe = magnetLink.download(target);
							if (adqe != null) {
								adqe.addDoAfterDownload(new AbstractDownloadFinished() {
									public void finishedDownload(final File f) {
										new SUIJob() {
											@Override
											public void run() {
												openFile(f,uri,point,mod);
												f.deleteOnExit();
											}
										}.schedule();
									}
								});
								logger.log(GuiAppender.GUI,String.format(Lang.AddedFileViaMagnet,magnetLink.getName()));
							}
						}
					}
				} else if (magnetLink.get(MagnetLink.KEYWORD_TOPIC) != null) {
					execute(uri);
				}
			}
		}
		
		private void openFile(File f,String uri,ObjectPoint<Control> point,URLTextModificator mod) {
			try {
				ImageData imgda = ImageDescriptor.createFromURL(f.toURI().toURL()).getImageData();
				Image img  = GraphicalFileDownloader.scaleIfNeeded(imgda);
				mod.addLabelReplacementImage(point.x, uri, img);
			} catch (Exception e) {
				logger.log(GuiAppender.GUI,"Download failed: "+e,e);
			}
		}

		@Override
		public Image getImageAfterURI(String uri) {
			MagnetLink magnetLink = MagnetLink.parse(uri); 
			if (magnetLink != null) {
				if (magnetLink.isComplete()) {	
					String end = "."+magnetLink.getEnding();
					logger.debug("found ending: "+end);
					for (String ending: IMAGE_ENDINGS) {
						if (end.equalsIgnoreCase(ending)) {
							return getImageURLIcon();
						}
					}
				} else if (magnetLink.get(MagnetLink.KEYWORD_TOPIC) != null) {
					return getImageSearchIcon();
				}
			}
			return null;
		}

		

		@Override
		public String getTextReplacement(String matched) {
			MagnetLink ml = MagnetLink.parse(matched);
			if (ml.isComplete()) {
				return String.format("%s (%s)",ml.getName(),SizeEnum.getReadableSize(ml.getSize()));
			} else if (ml.get(MagnetLink.KEYWORD_TOPIC) != null) {
				return String.format("%s ",ml.getName() == null?ml.get(MagnetLink.KEYWORD_TOPIC):ml.getName());
			} else {
				return matched;
			}
		}
		
		
	}
	
	private static class HubLink extends AbstractLinkType {
		private static final String HL_PAT = "((?:dchub)|(?:nmdc)|(?:adc))s?:\\/\\/"+URLENDING;
		
		private static final Image FAVHUB_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(
				Application.PLUGIN_ID, IImageKeys.FAVHUBS).createImage();
		
		private HubLink() {
			super(HL_PAT);
		}
		
		@Override
		public void execute(String matched) {
			new FavHub(matched).connect(ApplicationWorkbenchWindowAdvisor.get());
		}

		@Override
		public void executeImageClick(String uri, ObjectPoint<Control> point,
				URLTextModificator mod) {
			FavHub fh = new FavHub(uri);
			fh.addToFavHubs(ApplicationWorkbenchWindowAdvisor.get().getFavHubs());
			GuiHelpers.executeCommand(FavHubEditor.OPEN_FAVHUBS_COMMAND_ID,
					Collections.<String,String>emptyMap());
		}

		@Override
		public Image getImageAfterURI(String uri) {
			return FAVHUB_ICON;
		}

		@Override
		public boolean hasImageAfterURI(String uri) {
			return !ApplicationWorkbenchWindowAdvisor.get().getFavHubs().contains(uri);
		}

	}
	
	
 

}
