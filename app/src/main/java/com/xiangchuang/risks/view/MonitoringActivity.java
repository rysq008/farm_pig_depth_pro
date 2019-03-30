package com.xiangchuang.risks.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.PreferencesUtils;

import innovation.utils.HttpUtils;

import static android.webkit.WebSettings.LOAD_NO_CACHE;

/**
 * 监控信息
 */
public class MonitoringActivity extends AppCompatActivity {

    private WebView webView;
    private String en_id;
    private ImageView ivCancel;

    private ProgressBar progressBar;

    private String url;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        webView = (WebView) findViewById(R.id.webview);
        progressBar = (ProgressBar)findViewById(R.id.progressbar);

        en_id = PreferencesUtils.getStringValue(Constants.en_id, AppConfig.getAppContext(), "0");
        Log.e("en_id", "en_id: "+en_id);

        url = "http://47.92.167.61:8007/list.html?enId="+en_id+"&url="+ HttpUtils.baseUrl;
        Log.e("en_id", "url====="+url);
//        webView.loadUrl("http://www.baidu.com");
        webView.loadUrl(url);

        webView.addJavascriptInterface(this,"android");//添加js监听 这样html就能调用客户端
        webView.setWebChromeClient(webChromeClient);
        webView.setWebViewClient(webViewClient);

        WebSettings webSettings=webView.getSettings();
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(LOAD_NO_CACHE);

        ivCancel = (ImageView) findViewById(R.id.iv_cancel);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()){
                    if(webView.getUrl().equals(url)){
                        finish();
                    }else{
                        webView.goBack();
                    }
                }else{
                    finish();
                }
            }
        });
    }

    //WebViewClient主要帮助WebView处理各种通知、请求事件
    private WebViewClient webViewClient=new WebViewClient(){
        @Override
        public void onPageFinished(WebView view, String url) {//页面加载完成
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//页面开始加载
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("ansen","拦截url:"+url);
            if(url.equals("http://www.google.com/")){
                Toast.makeText(MonitoringActivity.this,"国内不能访问google,拦截该url", Toast.LENGTH_LONG).show();
                return true;//表示我已经处理过了
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

    };

    //WebChromeClient主要辅助WebView处理Javascript的对话框、网站图标、网站title、加载进度等
    private WebChromeClient webChromeClient=new WebChromeClient(){
        //不支持js的alert弹窗，需要自己监听然后通过dialog弹窗
        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
            localBuilder.setMessage(message).setPositiveButton("确定",null);
            localBuilder.setCancelable(false);
            localBuilder.create().show();

            //注意:
            //必须要这一句代码:result.confirm()表示:
            //处理结果为确定状态同时唤醒WebCore线程
            //否则不能继续点击按钮
            result.confirm();
            return true;
        }

        //获取网页标题
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            Log.i("ansen","网页标题:"+title);
        }

        //加载进度回调
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
        }
    };

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()){
            if(webView.getUrl().equals(url)){
                super.onBackPressed();
            }else{
                webView.goBack();
            }
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放资源
        webView.destroy();
        webView=null;
    }
}
