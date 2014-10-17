package org.crowdcomputer.bpmn.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AppZip {
	List<String> fileList;
	private String OUTPUT_ZIP_FILE = "ZipToUpload.zip";

	public AppZip(ArrayList<String> filelist, String output) {
		this.fileList = filelist;
		this.OUTPUT_ZIP_FILE = output + ".zip";
//		this.SOURCE_FOLDER = filelist;
	}

	public void zip() {
//		generateFileList(new File(SOURCE_FOLDER));
		zipIt(OUTPUT_ZIP_FILE);
	}
	
	public void delete(){
		fileList = null;
		for (String file  : fileList) 	{
			File f = new File(file);
			if (!f.delete()){
				f.deleteOnExit();
			}
		}
	}

	/**
	 * Zip it
	 * 
	 * @param zipFile
	 *            output ZIP file location
	 */
	public void zipIt(String zipFile) {

		byte[] buffer = new byte[1024];

		try {

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);


			for (String file : this.fileList) {

				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream in = new FileInputStream(file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			    File f = new File(file);
                f.delete();
            }

			zos.closeEntry();
			// remember close it
			zos.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Traverse a directory and get all files, and add the file into fileList
	 * 
	 * @param node
	 *            file or directory
	 */
	public void generateFileList(File node) {

		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(node.getName().toString()));
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}

	}

	/**
	 * Format the file path for zip
	 * 
	 * @param file
	 *            file path
	 * @return Formatted file path
	 */
	private String generateZipEntry(String file) {
		return file;
//		return file.substring(SOURCE_FOLDER.length() + 1, file.length());
	}
}
