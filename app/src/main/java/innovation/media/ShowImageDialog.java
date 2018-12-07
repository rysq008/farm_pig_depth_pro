package innovation.media;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xiangchuangtec.luolu.animalcounter.R;

import java.io.File;

import innovation.utils.ScreenUtil;

/**
 * Created by haojie on 2018/6/6.
 */

public class ShowImageDialog extends Dialog {
    private String TAG = "ImageShowDialog";
    private Activity mActivity;
    private ImageView imageView = null;

    public ShowImageDialog(Activity activity) {
        super(activity, R.style.Alert_Dialog_Style);
        setContentView(R.layout.show_image_layout);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.alpha = 1.0f;
        params.height = (int) (ScreenUtil.getScreenHeight() - 35 * ScreenUtil.getDensity());
        params.width = (int) (ScreenUtil.getScreenWidth() - 35 * ScreenUtil.getDensity());
        window.setAttributes(params);
        setCanceledOnTouchOutside(false);

        mActivity = activity;
        imageView = (ImageView)findViewById(R.id.image_show);
    }

    public void updateView(String path)
    {
        File file = new File(path);
        Glide.with(mActivity).load(file).into(imageView);

    }
}
