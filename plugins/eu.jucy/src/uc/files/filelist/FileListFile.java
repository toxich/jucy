package uc.files.filelist;



import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import uc.IUser;
import uc.crypto.HashValue;
import uc.files.AbstractDownloadable.AbstractDownloadableFile;
import uc.files.IDownloadable.IDownloadableFile;



public class FileListFile extends AbstractDownloadableFile implements IDownloadableFile, IFileListItem {
	


	private final FileListFolder parent;
	private final String filename;
	private final long size;
	private final HashValue tth;

	public FileListFile(FileListFolder parent,String filename, long size, HashValue tth){
		this.parent		= parent;
		this.filename	= filename;
		this.size		= size;
		this.tth		= tth;
		parent.addChild(this);
	}

	/**
	 * be aware even if this looks like XML
	 * it is not intended for.. (no xml escaping ...)
	 * for xml string see writeToXML method..
	 */
	public String toString() {
		return "File Name=\""+filename+"\" Size=\""+size+"\" TTH=\""+tth+"\"";
	}

	public String getPath() {
		return parent.getPath()+filename;
	}

	public void writeToXML(TransformerHandler hd,AttributesImpl atts) throws SAXException {
		atts.clear();
		atts.addAttribute("", "", "Name", "CDATA", filename);
		atts.addAttribute("", "", "Size", "CDATA", Long.toString(size));
		atts.addAttribute("", "", "TTH", "CDATA", tth.toString());
		hd.startElement("", "", "File", atts);
		hd.endElement("", "", "File");
	}
	
	public boolean mayDownload(IUser usr) {
		//TODO here identity check ... on trying to download here coukld be denied if file is not available to other user
		return true;
	}
	
	public boolean automaticExtraSlot() {
		return false;
	}



	/**
	 * @return the filename
	 */
	public String getName() {
		return filename;
	}


	/**
	 * @return the parent
	 */
	public FileListFolder getParent() {
		return parent;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}



	/**
	 * @return the tTH
	 */
	public HashValue getTTHRoot() {
		return tth;
	}
	/**
	 * @return the fiellist owner
	 */
	public IUser getUser(){
		return getParent().getFilelist().getUsr();
	}
	
	
	/**
	 *  variable to signal what files where originally
	 *  in the filelist and which were added by something like ADL search
	 *  or some other processor..
	 * @return always true for normal filelistFile
	 */
	public boolean isOriginal() {
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + (int) (size ^ (size >>> 32));
		result = prime * result + ((tth == null) ? 0 : tth.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FileListFile other = (FileListFile) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (size != other.size)
			return false;
		if (tth == null) {
			if (other.tth != null)
				return false;
		} else if (!tth.equals(other.tth))
			return false;
		return true;
	}




	
	



}
