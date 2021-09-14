package com.example.myapplication;

public interface DowmloadListener {
    void onprogress(int progress);
    void onsuccess();
    void onfailed();
    void onpaused();
    void oncanceled();
}
