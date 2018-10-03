/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.sun.net.httpserver.HttpExchange;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTOHelper;
import org.eclipse.keyple.plugin.remote_se.transport.json.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Utility class
 */
public class HttpHelper {


    private static final Logger logger = LoggerFactory.getLogger(HttpHelper.class);


    public static String PLUGIN_ENDPOINT = "/plugin";
    public static String READER_ENDPOINT = "/reader";
    public static String TRANSMIT_ENDPOINT = "/transmit";

    /**
     * Parse HTTP Body TO JSON Object
     * 
     * @param is
     * @return
     */
    static public JsonObject parseBody(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        Gson gson = JsonParser.getGson();
        return gson.fromJson(result, JsonObject.class);
    }

    /**
     * Parse HTTP Body TO JSON String
     * 
     * @param is
     * @return
     */
    static public String parseBodyToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    }

    public static JsonObject httpPUTJSON(HttpURLConnection conn, String json) throws IOException {
        logger.trace("Url {} HTTP PUT  : {} ", conn.getURL(), json);
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.connect();

        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
        osw.write(json);
        osw.flush();
        osw.close();

        int responseCode = conn.getResponseCode();
        logger.trace("Response code {}", responseCode);
        JsonObject jsonObject = HttpHelper.parseBody((InputStream) conn.getContent());
        logger.trace("Response {}", jsonObject);
        return jsonObject;
    }

    public static JsonObject httpPOSTJson(HttpURLConnection conn, String json) throws IOException {
        logger.trace("Url {} HTTP POST  : {} ", conn.getURL(), json);
        // Encode data
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.connect();

        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
        osw.write(json);
        osw.flush();
        osw.close();

        conn.setConnectTimeout(70000);
        conn.setReadTimeout(70000);


        int responseCode = conn.getResponseCode();
        logger.trace("Response code {}", responseCode);
        JsonObject jsonObject = HttpHelper.parseBody((InputStream) conn.getContent());
        logger.trace("Response {}", jsonObject);
        return jsonObject;
    }

    public static HttpURLConnection getConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        return (HttpURLConnection) url.openConnection();
    }


    public static JsonObject httpPoll(HttpURLConnection conn, String json) throws IOException {
        logger.trace("Url {} HTTP POST  : {} ", conn.getURL(), json);
        // Encode data


        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.connect();

        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
        osw.write(json);
        osw.flush();
        osw.close();


        conn.setConnectTimeout(70000);
        conn.setReadTimeout(70000);

        int responseCode = conn.getResponseCode();
        logger.trace("Response code {}", responseCode);
        JsonObject jsonObject = HttpHelper.parseBody((InputStream) conn.getContent());
        logger.trace("Response {}", jsonObject);
        return jsonObject;
    }

    static public void setHttpResponse(HttpExchange t, KeypleDTO resp) throws IOException {
        if (!resp.getAction().isEmpty()) {
            String responseBody = KeypleDTOHelper.toJson(resp);
            Integer responseCode = 200;
            t.getResponseHeaders().add("Content-Type", "application/json");
            t.sendResponseHeaders(responseCode, responseBody.length());
            OutputStream os = t.getResponseBody();
            os.write(responseBody.getBytes());
            os.close();
            logger.debug("Outcoming Response Code {} ", responseCode);
            logger.debug("Outcoming Response Body {} ", responseBody);


        } else {
            String responseBody = "{}";
            Integer responseCode = 200;
            t.getResponseHeaders().add("Content-Type", "application/json");
            t.sendResponseHeaders(responseCode, responseBody.length());
            OutputStream os = t.getResponseBody();
            os.write(responseBody.getBytes());
            os.close();
        }
    }



}
