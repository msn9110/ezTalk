package com.msn9110.eztalk.listener;



public interface VoiceInputListener {
    void onFinishRecord(String path);
    void onFinishRecognition(String result, String filepath);

}
