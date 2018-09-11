/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.async.webservice.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.Scanner;
import org.eclise.keyple.example.remote.server.serializer.json.SeProxyJsonParser;
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

    /**
     * Parse HTTP Body TO JSON Object
     * 
     * @param is
     * @return
     */
    static public JsonObject parseBody(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        Gson gson = SeProxyJsonParser.getGson();
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
        logger.debug("Url {} HTTP PUT  : {} ", conn.getURL(), json);
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
        logger.debug("Response code {}", responseCode);
        JsonObject jsonObject = HttpHelper.parseBody((InputStream) conn.getContent());
        logger.debug("Response {}", jsonObject);
        return jsonObject;
    }

    public static JsonObject httpPOSTJson(HttpURLConnection conn, String json) throws IOException {
        logger.debug("Url {} HTTP POST  : {} ", conn.getURL(), json);
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

        int responseCode = conn.getResponseCode();
        logger.debug("Response code {}", responseCode);
        JsonObject jsonObject = HttpHelper.parseBody((InputStream) conn.getContent());
        logger.debug("Response {}", jsonObject);
        return jsonObject;
    }
}
