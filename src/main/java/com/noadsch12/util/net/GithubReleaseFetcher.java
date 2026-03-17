/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 *
 * This file is part of the 12th Client project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details.
 */

package com.noadsch12.util.net;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class GithubReleaseFetcher {
    public static String getLatestTag(String user, String repo) throws Exception {
        String apiUrl = "https://api.github.com/repos/" + user + "/" + repo + "/releases/latest";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github+json");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
        return json.get("tag_name").getAsString();
    }

    public static boolean downloadLatestRelease(String user, String repo, String downloadPath) throws Exception {
        String apiUrl = "https://api.github.com/repos/" + user + "/" + repo + "/releases/latest";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github+json");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray assets = json.getAsJsonArray("assets");

        if (assets.size() == 0) {
            throw new Exception("No assets were found in newest Release!");
        }

        // Wir nehmen einfach das erste Asset
        JsonObject asset = assets.get(0).getAsJsonObject();
        String downloadUrl = asset.get("browser_download_url").getAsString();
        String fileName = asset.get("name").getAsString();

        // Herunterladen
        System.out.println("Downloading " + fileName + " from " + downloadUrl);

        URL downloadLink = new URL(downloadUrl);
        HttpURLConnection downloadConn = (HttpURLConnection) downloadLink.openConnection();
        downloadConn.setRequestProperty("Accept", "application/octet-stream");

        InputStream inStream = new BufferedInputStream(downloadConn.getInputStream());
        FileOutputStream outStream = new FileOutputStream(downloadPath + "/" + fileName);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        outStream.close();
        inStream.close();

        System.out.println("Finished Downloading: " + downloadPath + "/" + fileName);
        return true;
    }
}
