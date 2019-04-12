package innovation.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.utils.SystemUtil;

import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;
import innovation.utils.ScreenUtil;

public class ImageShowDialog extends Dialog {

    private Button btnReCollect;
    private BGABanner imgBanner;

    public ImageShowDialog(Context context) {
        super(context, R.style.alert_dialog_style);
        setContentView(R.layout.img_show_dialog_layout);
//        setContentView(view);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.alpha = 1.0f;
        params.width = (int) (ScreenUtil.getScreenWidth() - 35 * SystemUtil.getDensity());
        window.setAttributes(params);
        setCanceledOnTouchOutside(true);

        btnReCollect = (Button) findViewById(R.id.btnReCollect);
        imgBanner = findViewById(R.id.img_banner);
        imgBanner.setAdapter(new BGABanner.Adapter() {
            @Override
            public void fillBannerItem(BGABanner banner, View view, Object model, int position) {
                Glide.with(context).load((String) model).into((ImageView) view);
            }
        });
    }
    //设置点击回调
    public void setBtnReCollectListener (View.OnClickListener listener) {
        btnReCollect.setOnClickListener(listener);
    }
    //设置加载内容
    public void setContentmessage(List<String> imgList) {
        setContentmessage(imgList, null);
    }

    public void setContentmessage(List<String> imgList, handleDialogListener listener) {
        imgBanner.setData(imgList, null);
    }

    public interface handleDialogListener{
        public void handleWv(WebView wv, String string);
    }

}
