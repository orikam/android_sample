package com.blackstars.export;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

//#define SKETCHFAB_CLIENT_KEY_AUTHORIZATION  @"IgZH20xNHDmRgPkaGYwUG43WeidMhBAmDddW0HS6" //@"4g6stNOKNHKilmK1yZVqvwgwCYhXBYYAWFsWy9vE"
//        #define SKETCHFAB_CLIENT_SECRET_AUTHORIZATION @"EENxzBc4FtTbsufSZsXwCLiH3NFwdWKZvrfd4npEjyhEkAOJB4wUXYX9cpTTPqwjnAExO7mfdkfEtkygqzwVMKqs3yDIyRDsblqIZLz5jex0sLWZinXRbdRs8ILWJ2eg" //@"caPVSR0EfptCaXiG1P3osQzkPl4x6DlstFvWl8MdOuwwQouYj8Hvi5A6MmWDwT96SOWdAun9dbZoxzVRPuXzelbRq9MzyHUFf366ahDDKuEiHKugI8CKvKYmydC1fseI"
//        #define SKETCHFAB_REDIRECT_AUTHORIZATION @"http://eyecue-tech.com/sketch/Qlone" //@"http://eyecue-tech.com/sketch/testauthorization"
////                                                  eyecue-tech.com/sketch/Qlone

public class Sketchfab extends Activity {
    Activity ctx;
    private WebView mWebView;
    private  String SKETCHFAB_CLIENT_KEY_AUTHORIZATION = "IgZH20xNHDmRgPkaGYwUG43WeidMhBAmDddW0HS6";
    private String SKETCHFAB_CLIENT_SECRET_AUTHORIZATION = "EENxzBc4FtTbsufSZsXwCLiH3NFwdWKZvrfd4npEjyhEkAOJB4wUXYX9cpTTPqwjnAExO7mfdkfEtkygqzwVMKqs3yDIyRDsblqIZLz5jex0sLWZinXRbdRs8ILWJ2eg";
    private String SKETCHFAB_REDIRECT_AUTHORIZATION = "http://eyecue-tech.com/sketch/Qlone";
    private String accessToken;
    private byte[] model;
    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_sketchfab);

        ctx = this;

        try {
            InputStream inStream =  getAssets().open("model.zip");
            model = new byte[inStream.available()];
            inStream.read(model);
//            model = inStream.read
        } catch (IOException e) {
            e.printStackTrace();
        }
        queue = Volley.newRequestQueue(ctx);
        mWebView = (WebView) findViewById(R.id.myWebView);
        mWebView.setWebChromeClient(new WebChromeClient() {

        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // open in Webview
                if (url.contains("android_asset") ){
                    // Can be clever about it like so where myshost is defined in your strings file
                    // if (Uri.parse(url).getHost().equals(getString(R.string.myhost)))
                    return false;
                }
                // open rest of URLS in default browser
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                startActivity(intent);
                view.loadUrl(url);
                Log.d("ooori","url =" + url);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                String code = extractCode(request.getUrl().toString());
                if(code != null) {
                    Log.d("ooori", "code =" + code);

                    getToken(code);
                }
                else {
                    view.loadUrl(request.getUrl().toString());
                    //                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                    //                startActivity(intent);
                    Log.d("ooori", "url request =" + request.getUrl().toString());
                    return true;
                }
                return false;
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("https://sketchfab.com/oauth2/authorize/?response_type=code&client_id="+SKETCHFAB_CLIENT_KEY_AUTHORIZATION+"&redirect_uri="+SKETCHFAB_REDIRECT_AUTHORIZATION);

    }
    private String extractCode(String str) {
        if (str.contains(SKETCHFAB_REDIRECT_AUTHORIZATION)) {
            String parts[] = str.split("=");
            return parts[1];
        }
        return null;
    }

    private String getToken(final String  code) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://sketchfab.com/oauth2/token/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("oooOri", response);
                        try {
                            JSONObject res = new JSONObject(response);
                            accessToken = res.getString("access_token");
                            uploadModel();
                            Log.d("oooOri", "access token = " + accessToken);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("volley", "Error: " + error.getMessage());
                        error.printStackTrace();

                    }
                }) {

                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=UTF-8";
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("grant_type", "authorization_code");
                        params.put("code", code);
                        params.put("client_id", SKETCHFAB_CLIENT_KEY_AUTHORIZATION);
                        params.put("client_secret", SKETCHFAB_CLIENT_SECRET_AUTHORIZATION);
                        params.put("redirect_uri",SKETCHFAB_REDIRECT_AUTHORIZATION );
                        return params;
                    }

                };
        queue.add(stringRequest);
        return null;
    }

    private  void uploadModel() {
        String url = "https://api.sketchfab.com/v3/models";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                Log.i("oooOri", "upload success " + resultResponse);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    String uri = result.getString("uri");
                    String uid = result.getString("uid");


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    Log.e("Error result upload model", result);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("license", "by-nd");
                params.put("options", "{\"shading\":\"shadeless\"}");
                params.put("name", "Android");
                params.put("tags", "Android test");
                params.put("description", "this is the first model from android");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("modelFile", new DataPart("test.zip", model, ""));
//                params.put("cover", new DataPart("file_cover.jpg", AppHelper.getFileDataFromDrawable(getBaseContext(), mCoverImage.getDrawable()), "image/jpeg"));

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "multipart/form-data; charset=UTF-8");
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };
        queue.add(multipartRequest);
    }

}