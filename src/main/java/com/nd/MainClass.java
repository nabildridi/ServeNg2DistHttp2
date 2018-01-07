package com.nd;

import com.nd.services.MainService;
import com.nd.services.ServerService;

public class MainClass {

	public static void main(String[] args) {

		CliArgs cliArgs = new CliArgs(args);

		try {
			Long httpPortArg = cliArgs.switchLongValue("-http");
			if (httpPortArg != null) {
				ServerService.HTTP_PORT = httpPortArg.intValue();
			}
		} catch (Exception e) {}

		try {
			Long httpsPortArg = cliArgs.switchLongValue("-https");
			if (httpsPortArg != null) {
				ServerService.HTTPS_PORT = httpsPortArg.intValue();
			}
		} catch (Exception e) {}
		
		try {
			boolean nohttp = cliArgs.switchPresent("-nohttp");
			if (nohttp) {
				ServerService.NO_HTTP = true;
			}
		} catch (Exception e) {}

		MainService mainService = new MainService();
		mainService.start();

	}

}
