package com.nd.servlets;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nd.services.MainService;

public class UploadServlet extends HttpServlet {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	private static final long serialVersionUID = 3010217183662657550L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		log.info("Got files upload...");
		
		File uploadDir = MainService.filesService.getTempUploadDir();
	
		//process only if its multipart content
        if(ServletFileUpload.isMultipartContent(request)){
            try {
                
            	List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
              
                for(FileItem item : multiparts){
                    if(!item.isFormField()){
                    	
                    	//log.info("item:" + item.getName());                    	
                    	File targetFile = new File( uploadDir.getAbsoluteFile() + File.separator + item.getName());
                    	targetFile.getParentFile().mkdirs();
                        item.write( targetFile );
                        
                    }
                }
                
                //zip
                MainService.filesService.zipAndUpdate();
                
               //File uploaded successfully
                log.info("File Uploaded Successfully");
                response.setStatus(HttpServletResponse.SC_OK);
                
            } catch (Exception ex) {
            	log.info("File Upload Failed due to " + ex);
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }          
         
        } else {
        	log.info("Sorry this Servlet only handles file upload request");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        
        response.setContentType("application/json");
	    response.setStatus(HttpServletResponse.SC_OK);
	    
	}

	
    
    
    
}
