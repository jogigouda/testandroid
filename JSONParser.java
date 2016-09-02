package com.ga.lm.Webservice;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ga.lm.utility.InternetUtil;
import com.ga.lm.utility.Utils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class JSONParser {

    String charset = "UTF-8";
    HttpURLConnection conn;
    DataOutputStream wr;
    StringBuilder result;
    URL urlObj;
    JSONObject jObj = null;
    StringBuilder sbParams;
    String paramsString;
    Context context;
    OnServiceCallListener listner;

    String url, methodType;
    ServerResponse serverResponse;

    public static String methodPost = "POST";
    public static String methodGet = "GET";

    public JSONParser(Context context) {
        this.context = context;

        listner = (OnServiceCallListener) context;
        serverResponse = new ServerResponse();
    }

    public JSONParser(Context context, Object object) {
        this.context = context;
        listner = (OnServiceCallListener) object;
        serverResponse = new ServerResponse();
    }

    public void callPOST(String s, String type, JSONObject jsonParam) {

        if (InternetUtil.isOnline(context)) {
            if (jsonParam != null) {
                new PostApiCalling().execute(s, type, jsonParam.toString());
            } else {
                new PostApiCallingProductGrid().execute(s, type, paramsString);
            }
        }
    }

    public void callPOSTWithoutProgress(String s, String type, JSONObject jsonParam) {

        if (InternetUtil.isOnline(context)) {

            new PostApiCallingProductGrid().execute(s, type, jsonParam.toString());
        }
    }

    public void callGET(String s, String type, HashMap<String, String> param) {

        url = s;
        methodType = type;
        if (InternetUtil.isOnline(context))
            new GetApiCalling().execute(param);
    }


    public String HttpGetRequest(String url, String method, HashMap<String, String> params) {

        String response = null;
        sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0) {
                    sbParams.append("&");
                }
                sbParams.append(key).append("=").append(URLEncoder.encode(params.get(key), charset));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }

        if (method.equalsIgnoreCase("GET")) {
            // request method is GET

            if (sbParams.length() != 0) {
                url += "?" + sbParams.toString();
            }

            try {
                urlObj = new URL(url);
                Log.e("url", url);

                conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(false);

                conn.setRequestMethod("GET");

                conn.setRequestProperty("Accept-Charset", charset);

               /* conn.setRequestProperty("device-token", sessionManager.getIMEINO());

                //conn.setRequestProperty("auth-token", "f5e42047-4f67-48bc-b32d-37c2a6c3b248");
                //conn.setConnectTimeout(15000);
                if (sessionManager.getAccessToken() != null) {
                    conn.setRequestProperty("auth-token", sessionManager.getAccessToken());
                }*/

                conn.connect();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {

            int status = ((HttpURLConnection) conn).getResponseCode();
            InputStream is = null;
            if (status >= 200 && status < 400) {
                // Create an InputStream in order to extract the response object
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }

            InputStream in = new BufferedInputStream(is);
            response = readStream(in);

            String apiname = conn.getHeaderField("RequestUri");
            if (apiname != null) {

                Log.e("api", apiname);

                serverResponse.setApiName(apiname);
            } else {

                serverResponse.setApiName("googleplaceapi");
            }

            serverResponse.setResponseCode("" + conn.getResponseCode());
            serverResponse.setResponseMsg(conn.getResponseMessage());
            serverResponse.setResponse(response);

            Log.e("JSON Parser", "result: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }

        conn.disconnect();

        // return JSON Object
        return response;
    }


    public ServerResponse HttpPostRequest(String url, String method, JSONObject obj) {

        String response = null;
        if (method.equalsIgnoreCase("POST")) {
            // request method is POST
            try {
                urlObj = new URL(url);

                conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(true);

                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept-Charset", charset);

                conn.setRequestProperty("Content-Type", "application/json");

                conn.connect();

                wr = new DataOutputStream(conn.getOutputStream());
                if (obj != null)
                    wr.writeBytes(obj.toString());
                wr.flush();
                wr.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (method.equalsIgnoreCase("GET")) {
            // request method is GET

            if (sbParams.length() != 0) {
                url += "?" + sbParams.toString();
            }

            try {
                urlObj = new URL(url);

                conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(false);

                conn.setRequestMethod("GET");

                conn.setRequestProperty("Accept-Charset", charset);

                /*conn.setRequestProperty("device-token", sessionManager.getIMEINO());

                conn.setRequestProperty("auth-token", "82c8228b-18eb-46ac-a38e-ee4150685a29");*/

                conn.connect();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {


            int status = ((HttpURLConnection) conn).getResponseCode();
            InputStream is = null;
            if (status >= 200 && status < 400) {
                // Create an InputStream in order to extract the response object
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }

            //String msg = conn.getResponseMessage();
            //Log.i("Status code", "Status : " + status);
            //Log.i("Status msg", msg);

            // Map<String, List<String>> map = conn.getHeaderFields();

            /*System.out.println(conn.getHeaderField("authentication-token"));
            if (conn.getHeaderField("authentication-token") != null) {
                sessionManager.SetAccessToken(conn.getHeaderField("authentication-token"));
            }*/

            /*System.out.println("Printing Response Header...\n");

            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                System.out.println("Key : " + entry.getKey() + " ,Value : " + entry.getValue());
            }*/

            //Receive the response from the server
            InputStream in = new BufferedInputStream(is);
            response = readStream(in);
            String apiname = conn.getHeaderField("RequestUri");
            if (apiname != null) {

                Log.e("api", apiname);

                serverResponse.setApiName(apiname);
            } else {

                serverResponse.setApiName("googleplaceapi");
            }

            serverResponse.setResponseCode("" + conn.getResponseCode());
            serverResponse.setResponseMsg(conn.getResponseMessage());
            serverResponse.setResponse(response);


            Log.e("JSON Parser", "result: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }

        conn.disconnect();

        return serverResponse;
    }

    /**
     * Convert InputStream to String
     *
     * @param in inputStream for reading response
     */
    private String readStream(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    class PostApiCalling extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {

            if (Utils.IsProgressShown) {

            } else {

                Utils.ShowDialog(context);
                Utils.IsProgressShown = true;
            }
            /*pDialog = new ProgressDialog(context);
            pDialog.setIndeterminate(false);
            pDialog.setMessage(context.getString(R.string.pleaswait));
            pDialog.setCancelable(true);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();*/
        }

        @Override
        protected String doInBackground(String... args) {

            try {

                Log.d("request", "starting");
                JSONObject jsonParam = null;
                if (args[2] != null) {
                    jsonParam = new JSONObject(args[2]);
                }
                serverResponse = HttpPostRequest(args[0], args[1], jsonParam);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String response) {

            if (Utils.IsProgressShown) {
                Utils.DismissDialog(context);
                Utils.IsProgressShown = false;
            }

            /*if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }*/

            try {
                listner.onResponse(serverResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    class GetApiCalling extends AsyncTask<HashMap<String, String>, String, String> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
           /* pDialog = new ProgressDialog(context);
            pDialog.setIndeterminate(false);
            pDialog.setMessage(context.getString(R.string.pleaswait));
            pDialog.setCancelable(false);
            pDialog.show();*/
        }

        @Override
        protected String doInBackground(HashMap<String, String>... args) {

            try {

                Log.d("request", "starting");

                String jsonResult = HttpGetRequest(url, methodType, args[0]);

                if (jsonResult != null) {

                    return jsonResult;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String json) {

           /* if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }*/

            try {
                listner.onResponse(serverResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    class PostApiCallingProductGrid extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... args) {

            try {

                Log.d("request", "starting");
                JSONObject jsonParam = null;
                if (args[2] != null) {
                    jsonParam = new JSONObject(args[2]);
                }
                serverResponse = HttpPostRequest(args[0], args[1], jsonParam);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String response) {

            try {
                listner.onResponse(serverResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}