package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class Viewing_news extends AppCompatActivity {
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing_news);
        Intent in=getIntent();
        String p=in.getStringExtra("url");
        webView=findViewById(R.id.web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        try {
            webView.loadUrl(p);
        }catch (Exception e) {
            Toast.makeText(getApplicationContext(),"NOTHING FOUND FOR THIS ARTICLE",Toast.LENGTH_SHORT).show();
        }
    }
}