package uc.files.filelist;

import java.io.File;
import java.util.Set;


import uc.IUser;
import uc.IStoppable.IStartable;
import uc.crypto.HashValue;
import uc.files.filelist.OwnFileList.SearchParameter;

public interface IOwnFileList extends IStartable {

	/** <sizerestricted>?<ismaxsize>?<size>?<datatype>?<searchpattern>
	 * 
	 * @param key - the searchwords
	 * @param fileendings - which fileEndings the file must match. empty if all allowed
	 * @param type
	 * @param active
	 * @return
	 */
	Set<IFileListItem> search(SearchParameter sp);

	/**
	 * 
	 * @param tth - tth of the searched file..
	 * @return null if nothing found, or the found File.
	 */
	FileListFile search(HashValue tth);

	/**
	 * Get a java.io.File from a TTH
	 * @param tth - the hashvalue of the file
	 * @return the path to the local filesystem holding the file , null if not found
	 * 
	 */
	File getFile(HashValue tth);

	/**
	 * Get a java.io.file from a filelist File
	 * @param file a Filelist file
	 * @return a real java.io.File
	 */
	File getFile(FileListFile file);

	long getSharesize();

	int getNumberOfFiles();
	
	void refresh(boolean wait);
	

	
	FileList getFileList();
	
	/**
	 * immediately add given file to The own filelist
	 * @param file - the file to be added.. possibly something downloaded
	 * @param if true the file will be added even if the location is not shared..
	 * @param restrictForUser   if not null -> file may only be downloaded by given user (only if added outsode ofshare..)
	 * @param callback - called when adding is finished 
	 * 
	 * @return true if the file will be added outs
	 * 
	 */
	void immediatelyAddFile(File file,boolean force,IUser restrictForUser,AddedFile callback);
	
	void immediatelyAddFile(File file);
	
	public static class AddedFile {
		public void addedFile(FileListFile file,boolean addedOutsideOfShare){}
	}

}