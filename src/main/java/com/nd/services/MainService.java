package com.nd.services;


import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainService {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static FilesAndVirtualFsService filesService;
	
	
	public void start() {
		filesService= new FilesAndVirtualFsService();
		Resource webappResource = null;
		
		try {
			webappResource = filesService.init();
		} catch (Exception e) {
			log.info("Unable to initialize the files service, exiting....");
			e.printStackTrace();
			return;
		}
		
		ServerService serverService = new ServerService();
		try {
			serverService.initAndStart(webappResource);
		} catch (Exception e) {
			log.info("Unable to initialize and start the server, exiting....");
			e.printStackTrace();
			return;
		}
		
	}
}
