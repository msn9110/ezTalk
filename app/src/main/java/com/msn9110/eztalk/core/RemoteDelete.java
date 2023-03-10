package com.msn9110.eztalk.core;

import static com.msn9110.eztalk.Settings.hashed_password;
import static com.msn9110.eztalk.Settings.user_id;
import static com.msn9110.eztalk.utils.Utils.getJSONString;

import com.msn9110.eztalk.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class RemoteDelete {
    private File mFile;
    public RemoteDelete(String path) {
        mFile = new File(path);
    }
    
    public void executeRemoteDelete() {
        final String filename = mFile.getName();
        if (mFile.getAbsolutePath().contains("upload")){
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    String label = mFile.getParentFile().getName();
                    JSONObject data = new JSONObject();
                    HttpURLConnection conn = null;
                    try {
                        data.put("filename", filename);
                        data.put("label", label);
                        String url = Settings.URL + "/remove";
                        String account = ", \"account\":{\"user_id\":"
                                + "\"" + user_id + "\", \"password\":"
                                + "\"" + hashed_password + "\"}";
                        String json = data.toString().replaceFirst("\\}\\s*$", "")
                                + account + "}";
                        System.out.println(json);

                        URL u = new URL(url);
                        conn = (HttpURLConnection) u.openConnection();
                        conn.setRequestMethod("PUT");
                        conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setUseCaches(false);

                        OutputStream os = conn.getOutputStream();
                        DataOutputStream writer = new DataOutputStream(os);
                        writer.write(json.getBytes("UTF-8"));
                        os.flush();
                        os.close();

                        String myResult = getJSONString(conn.getInputStream());

                        JSONObject response = new JSONObject(myResult);
                        boolean success = response.getBoolean("success");

                        System.out.println(success);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (conn != null)
                            conn.disconnect();
                    }
                }

            });
            worker.start();
        }
        
    }
}
