package com.msn9110.eztalk.core;

import static com.msn9110.eztalk.AppValue.RECOGNITION_FINISHED_ACTION;
import static com.msn9110.eztalk.Settings.hashed_password;
import static com.msn9110.eztalk.Settings.user_id;
import static com.msn9110.eztalk.utils.Utils.getJSONString;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.msn9110.eztalk.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class RecognitionTask extends AsyncTask<File, Void, String> {

    private Context mContext;
    private ProgressDialog recognitionDialog;
    private HttpURLConnection conn = null;
    private long start = 0;

    public RecognitionTask(Context context) {
        this.mContext = context;
        recognitionDialog = new ProgressDialog(mContext);
        recognitionDialog.setTitle("辨識中");
        recognitionDialog.setMessage("請稍候...");
        recognitionDialog.setCanceledOnTouchOutside(false);
        recognitionDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        recognitionDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
        recognitionDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        recognitionDialog.dismiss();
                    }
                });
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        start = System.currentTimeMillis();
        recognitionDialog.show();
    }

    @Override
    protected String doInBackground(File... files) {
        String result = "辨識完成";
        String apiName = "/recognize";
        String url = Settings.URL + apiName;
        if (files.length == 1) {
            for (File mFile: files) {
                String label = mFile.getParentFile().getName();
                try {
                    byte[] raws = new byte[(int)mFile.length()];
                    FileInputStream in = new FileInputStream(mFile);
                    in.read(raws);
                    int[] raw = new int[raws.length];
                    for (int i = 0; i < raws.length; i++)
                        raw[i] = 0xff & raws[i];
                    JSONArray rawData = new JSONArray(Arrays.asList(raw));
                    JSONObject account = new JSONObject();
                    account = account.put("user_id", user_id)
                            .put("password", hashed_password);
                    JSONObject data = new JSONObject()
                            .put("account", account)
                            .put("label", label)
                            .put("num_of_stn", 8)
                            .put("filename", mFile.getName())
                            .put("raw", rawData.getJSONArray(0));
                    String accounts = ", \"account\":{\"user_id\":"
                            + "\"" + user_id + "\", \"password\":"
                            + "\"" + hashed_password + "\"}";
                    String json = data.toString();
                    System.out.println(json);

                    URL u = new URL(url);
                    conn = (HttpURLConnection) u.openConnection();
                    conn.setRequestMethod("POST");
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

                    conn.disconnect();

                    Intent intent = new Intent(RECOGNITION_FINISHED_ACTION);
                    intent.putExtra("response", myResult);
                    intent.putExtra("filepath", mFile.getAbsolutePath());
                    mContext.sendBroadcast(intent);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    result = "unsupportedEncoding";
                } catch (IOException e) {
                    e.printStackTrace();
                    result = "POST Error";
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    conn = null;
                }
            }
            return result;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        recognitionDialog.dismiss();
        if (s != null) {
            String time = ":" + String.valueOf((System.currentTimeMillis() - start) / 1000)
                    + "sec";
            Toast.makeText(mContext, s + time, Toast.LENGTH_SHORT).show();
        }
    }
}
