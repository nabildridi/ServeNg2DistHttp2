# ServeNg2DistHttp2
Serve Angular 2+ dist files in HTTP/2 with an embedded Jetty server and Java 9

1. **Introduction** : 
	This application let you upload your Angular 2+ dist folder to a running embedded Jetty server  which will serve the files via the HTTP/2 protocol

 2. **Requirements** : 
	The project needs **JDK9** to run, it can't be run with any other version of Java

 3. **Getting started**
	* Execute `mvn clean install`, the generated jars will be under the directory `NgHttp2Server` in `target`, copy that directory where you want and execute `java -jar Server.jar` to start the server, it will start on 8080 port for http and on 8443 for https, you can change those ports by specifying them as argument, example  : `java -jar Server.jar -http 8888 -https 8444`
 4. Or launch 'com.nd.MainClass' from eclipse
 5. open https://localhost:8443/upload.html in the browser and upload the `dist` folder of your angular 2+ application
 6. The Ng2 application is accessible via the url https://localhost:8443

 7. **Deactivating http protocol**
 By default, the server is serving via http and https, if you want to deactivate http protocol start the server with `-nohttp` argument, example : `java -jar Server.jar -nohttp`     

 8. **SSl certificate used in this project**   
 This project was never meant to have a production quality level, it has a self-signed ssl certificate, if you want to put your own certificate then you will need : 
 9. To put your own cerificate by replacing the file `/src/main/resources/keystore`    
 10. And to change the passwords in the `com.nd.services.ServerService` class
 
 

 11. **Why the build output of the project is a folder and not as one jar with dependencies** 
 Building this project as one jar made the http2 requests failing when executed, the only way that made it work is to build it as a folder, it is a problem related to this : https://github.com/eclipse/jetty.project/issues/1548 