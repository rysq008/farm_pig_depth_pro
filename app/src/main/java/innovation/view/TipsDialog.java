package innovation.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.xiangchuang.risks.utils.SystemUtil;
import com.xiangchuangtec.luolu.animalcounter.R;

import innovation.utils.ScreenUtil;

public class TipsDialog extends Dialog {

    private TextView tvTitle;
    private Button btnReCollect;
    private WebView wvContent;

    @SuppressLint("JavascriptInterface")
    public TipsDialog(Context context) {
        super(context, R.style.alert_dialog_style);
        setContentView(R.layout.tips_dialog_layout);
//        setContentView(view);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.alpha = 1.0f;
        params.width = (int) (ScreenUtil.getScreenWidth() - 35 * SystemUtil.getDensity());
        window.setAttributes(params);
        setCanceledOnTouchOutside(false);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        btnReCollect = (Button) findViewById(R.id.btnReCollect);
        wvContent = (WebView) findViewById(R.id.wv_content);

        WebSettings webSettings = wvContent.getSettings();
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptEnabled(true);

    }
    //设置点击回调
    public void setBtnReCollectListener (View.OnClickListener listener) {
        btnReCollect.setOnClickListener(listener);
    }
    //设置title
    public void setTitlemessage(String text) {
        tvTitle.setText(text);
    }
    //设置加载内容
    public void setContentmessage(String text) {
        setContentmessage(text, null);
    }


    public void setContentmessage(String text, handleDialogListener listener) {
        if(listener != null){
            listener.handleWv(wvContent, text);
        }else {
            wvContent.loadData(text, "text/html; charset=UTF-8",null);
        }
    }

    public interface handleDialogListener{
        public void handleWv(WebView wv, String string);
    }

}
