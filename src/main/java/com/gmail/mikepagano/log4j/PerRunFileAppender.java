package com.gmail.mikepagano.log4j;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

/**
 * PerRunFileAppender extends FileAppender to create a new log every time the
 * process is ran. It also keeps a maximum number of logs.
 * 
 * @author Michael Pagano
 */
public class PerRunFileAppender extends FileAppender {

	
	protected int maxBackupIndex = -1;

	protected int maxBackupDays = -1;

	public PerRunFileAppender() {
		super();
	}

	public PerRunFileAppender(Layout layout, String filename)
			throws IOException {
		super(layout, filename);
	}

	public PerRunFileAppender(Layout layout, String filename, boolean append)
			throws IOException {
		super(layout, filename, append);
	}

	@Override
	public void activateOptions() {
		if (fileName != null) {
			try {
				fileName = getNewLogFileName();
				setFile(fileName, fileAppend, bufferedIO, bufferSize);
			} catch (Exception e) {
				errorHandler.error("Error while activating log options", e,
						ErrorCode.FILE_OPEN_FAILURE);
			}
		}
	}

	private int getFileMilliseconds(File file) {
		String filePath = file.getAbsolutePath();
		String fileName = filePath.substring(filePath.lastIndexOf('-') + 1,
				filePath.lastIndexOf(".log"));
		int milliFile = Integer.parseInt(fileName);
		return milliFile;
	}

	public int getMaxBackupDays() {
		return maxBackupDays;
	}

	public int getMaxBackupIndex() {
		return maxBackupIndex;
	}

	private String getNewLogFileName() {
		if (fileName != null) {
			final String DOT = ".";
			final String HIPHEN = "-";
			final File logFile = new File(fileName);
			final String fileName = logFile.getName();
			String newFileName = "";

			final int dotIndex = fileName.indexOf(DOT);
			if (dotIndex != -1) {
				newFileName = fileName.substring(0, dotIndex) + HIPHEN
						+ System.currentTimeMillis() + DOT
						+ fileName.substring(dotIndex + 1, fileName.length());
			} else {
				newFileName = fileName + HIPHEN + System.currentTimeMillis();
			}
			return logFile.getParent() + File.separator + newFileName;
		}
		return null;
	}

	private void rollOver() {
		
		final String fileRoot = fileName.substring(fileName.lastIndexOf(File.separator)+1,fileName.lastIndexOf('-'));
		FilenameFilter rootFilter = new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				String lower = name.toLowerCase();
				if(lower.startsWith(fileRoot.toLowerCase())){
					return true;
				}
				else{
					return false;	
				}
				
			}
		};
		
		File log = new File(this.getFile());
		String abPath = log.getAbsolutePath();
		String folderPath = abPath.substring(0,
				abPath.lastIndexOf(File.separator));

		File folder = new File(folderPath);
		File[] filesInFolder = folder.listFiles(rootFilter);

		Arrays.sort(filesInFolder);
		ArrayUtils.reverse(filesInFolder);
		
		if (maxBackupIndex != -1) {
			for (int i = 0; i < filesInFolder.length; i++) {
				if (i > maxBackupIndex - 1) {
					try {
						filesInFolder[i].delete();
					} catch (Exception e) {

					}
				}
			}
		}

		filesInFolder = folder.listFiles(rootFilter);

		Arrays.sort(filesInFolder);
		ArrayUtils.reverse(filesInFolder);

		if (maxBackupDays != -1) {
			Calendar daysAgo = Calendar.getInstance();
			daysAgo.add(Calendar.DAY_OF_YEAR, maxBackupDays * -1);
			int maxMili = (int) daysAgo.getTimeInMillis();
			for (int i = 0; i < filesInFolder.length; i++) {
				if (getFileMilliseconds(filesInFolder[i]) < maxMili) {
					try {
						filesInFolder[i].delete();
					} catch (Exception e) {

					}
				}
			}
		}

	}

	public void setMaxBackupDays(int maxBackupDays) {
		this.maxBackupDays = maxBackupDays;
	}

	public void setMaxBackupIndex(int maxBackupIndex) {
		this.maxBackupIndex = maxBackupIndex;
	}

	@Override
	protected void subAppend(LoggingEvent event) {
		super.subAppend(event);
		if ((fileName != null))
			this.rollOver();
	}
}
