package com.nd.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

public class FilesAndVirtualFsService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Path webappFolder;

	private String TEMP_DIR = "java.io.tmpdir";
	private String UPLOAD_DIR_NAME = "ng2DistUploadDir";
	private String ZIP_FILE_NAME = "ng2Dist.zip";

	public Resource init() throws Exception {

		log.info("creating the virtual webapp root folder.....");
		FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
		webappFolder = fs.getPath("/app");
		
		decompressInWebApp();

		return new PathResource(webappFolder);
	}

	public void setupUploadPage() throws Exception {

		log.info("copying 'upload.html' and 'uppie.min.js' to the the virtual folder.....");
		
		Path htmlTarget = webappFolder.resolve("upload.html");
		Path jsTarget = webappFolder.resolve("uppie.min.js");
		
		InputStream html = ClassLoader.getSystemResourceAsStream("uploadInterface/upload.html");
		InputStream js =  ClassLoader.getSystemResourceAsStream("uploadInterface/uppie.min.js");
		
				
		Files.copy(html, htmlTarget);
		Files.copy(js, jsTarget);
	}

	public File getTempUploadDir() {
		String tempDir = System.getProperty(TEMP_DIR);
		String uploadDirPath = tempDir + File.separator + UPLOAD_DIR_NAME;
		log.info("files will be saved in :" + uploadDirPath);
		File uploadDir = new File(uploadDirPath);

		// empty upload dir
		try {
			FileUtils.deleteDirectory(uploadDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		uploadDir.mkdirs();

		return uploadDir;
	}

	public void zipAndUpdate() {

		String tempDir = System.getProperty(TEMP_DIR);
		String zipFilePath = tempDir + File.separator + ZIP_FILE_NAME;
		String uploadDirPath = tempDir + File.separator + UPLOAD_DIR_NAME;

		File uploadDir = new File(uploadDirPath);
		File[] content = uploadDir.listFiles(File::isDirectory);

		// get dist folder
		File distDir = null;
		if (content != null && content.length > 0) {
			distDir = content[0];
		}

		if (distDir == null)
			return;

		log.info("distDir :" + distDir.getName());
		File[] filesAndDirToCompress = distDir.listFiles();

		log.info("zipping content of the directory :" + uploadDirPath);
		FileUtils.deleteQuietly(new File(zipFilePath));

		try {

			// zip
			FileOutputStream fos = new FileOutputStream(zipFilePath);
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			for (File f : filesAndDirToCompress) {
				zipFile(f, f.getName(), zipOut);
			}
			zipOut.close();
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		decompressInWebApp();

	}

	private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}

	private void decompressInWebApp() {

		log.info("decompressing zip in webapp...");

		String tempDir = System.getProperty(TEMP_DIR);
		String zipFilePath = tempDir + File.separator + ZIP_FILE_NAME;

		try {

			
			boolean exists = Files.exists(webappFolder);
			if(exists) {
				delete(webappFolder);
			}
			Files.createDirectory(webappFolder);
			
			try {
				setupUploadPage();
			} catch (Exception e) {e.printStackTrace();}

			
			Path zipFile = Paths.get(zipFilePath);			
			exists = Files.exists(zipFile);
			if(!exists) {
				log.info("no previous zipped ng2 dist found, please upload your files...");
				return;
			}else {
				log.info("found previous zipped ng2 dist, unzipping...");
			}

			try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
				ZipEntry entry;
				while ((entry = zipInputStream.getNextEntry()) != null) {

					 Path toPath = webappFolder.resolve(entry.getName());					 
					if (entry.isDirectory()) {
						Files.createDirectory(toPath);
					} else {
						Files.createDirectories(toPath.getParent());
						Files.copy(zipInputStream, toPath);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	private void delete(Path directory) throws IOException {

		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
 
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
 
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

}
