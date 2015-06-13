package com.funwander.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RemoteServer {
	
	private static Logger logger = LoggerFactory.getLogger(RemoteServer.class);

	public static void downloadRoadMap() throws IOException {
		Configuration configuration = Configuration.getInstance();
		String localStoragePath = configuration.get("storage.path");
		// TODO: 
		new File(localStoragePath + "UA-gh").mkdirs();
		for (String fileName : configuration.get("gh.files").split(";")) {
			File file = new File(localStoragePath + "UA-gh/" +  fileName);
			if (!file.exists()) {
				downloadRoadMapFile(fileName, localStoragePath + "UA-gh/" + fileName); // !!
			}
		}
	}

	private static void downloadRoadMapFile(String fileName, String to)
			throws IOException {
		Configuration configuration = Configuration.getInstance();
		// TODO: hard code
		//String url = configuration.get("server.files_url") + fileName;
		String url = "http://gang.org.ua/fw/static/" + fileName;

		HttpURLConnection conn = (HttpURLConnection) new URL(url)
				.openConnection();

		conn.setDoInput(true);

		conn.setConnectTimeout(configuration.getInt("server.timeout", 5000));
		logger.info("Trying connect to " + url);
		conn.connect();

		int length = conn.getContentLength();
		InputStream input = conn.getInputStream();
		FileOutputStream out = new FileOutputStream(to);
		try {
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			long sum = 0;
			while ((bytesRead = input.read(buffer)) != -1) {
				sum += bytesRead;
				out.write(buffer, 0, bytesRead);
				// if (progressListener != null)
				// progressListener.update((int) (100 * sum / length));
			}
		} finally {
			input.close();
			out.flush();
			out.close();
		}
	}

}
