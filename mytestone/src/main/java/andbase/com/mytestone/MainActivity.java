package andbase.com.mytestone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.innovation.pig.insurance.netutils.Constants;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.view.LoginFamerActivity;
import com.xiangchuang.risks.view.LoginFamerServer;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int a() {
        return R.layout.activity_main;
    }

    @Override
    protected void c() {

    }

//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_main;
//    }
//
//    @Override
//    protected void initData() {
//
//    }

    public void onClickView(View view) {
        Toast.makeText(this, "nb", Toast.LENGTH_LONG).show();
        Intent mIntent = new Intent(this, LoginFamerActivity.class);
        mIntent.putExtra(Constants.TOKEY,"android_token");
        mIntent.putExtra(Constants.USER_ID,"android_userid2");
        mIntent.putExtra(Constants.PHONE_NUMBER,"19000000002");
        mIntent.putExtra(Constants.NAME,"android_name");
        mIntent.putExtra(Constants.DEPARTMENT_ID,"2"/*"android_department"*/);
        mIntent.putExtra(Constants.IDENTITY_CARD,"android_identitry");
        startActivity(mIntent);
//        Intent it = new Intent(this, LoginFamerServer.class);
//        startService(it);
    }
}
