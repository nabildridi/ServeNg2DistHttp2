package com.nd.services;

import static org.eclipse.jetty.util.resource.Resource.newClassPathResource;

import org.eclipse.jetty.alpn.ALPN;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nd.servlets.UploadServlet;

public class ServerService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static int HTTP_PORT = 8080;
	public static int HTTPS_PORT = 8443;
	public static boolean NO_HTTP = false;
	
	private Server server;

    public Server initAndStart(Resource webappResource) throws Exception {
    	
    	log.info("starting the server....");
    	log.info("Http port:" + HTTP_PORT);
    	log.info("Https port:" + HTTPS_PORT);
    	log.info("No Http:" + NO_HTTP);
    	
        server = new Server();
        
        server.setStopAtShutdown(true);
        
        if(!NO_HTTP) {
			ServerConnector connector = new ServerConnector(server);
			connector.setPort(HTTP_PORT);
			connector.setReuseAddress(true);
			server.setConnectors(new Connector[] { connector });
        }

		

        
        WebAppContext webAppCtx = new WebAppContext(webappResource, "/");
		webAppCtx.setResourceBase(webappResource.getURI().toString());
		webAppCtx.setClassLoader(Thread.currentThread().getContextClassLoader());
		server.setHandler(webAppCtx);
		
 
        webAppCtx.addServlet(UploadServlet.class, "/api/files");

        // HTTP Configuration
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(HTTPS_PORT);

        // SSL Context Factory for HTTPS and HTTP/2
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStoreResource(newClassPathResource("keystore"));
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);

        // HTTPS Configuration
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        // HTTP/2 Connection Factory
        HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(https_config);
        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        alpn.setDefaultProtocol("h2");

        // SSL Connection Factory
        SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory,alpn.getProtocol());

        // HTTP/2 Connector
        ServerConnector http2Connector = new ServerConnector(server,ssl,alpn,h2,new HttpConnectionFactory(https_config));
        http2Connector.setPort(HTTPS_PORT);
        server.addConnector(http2Connector);

        ALPN.debug=false;


        server.start();
        server.join();
        
        return server;
    }

}
