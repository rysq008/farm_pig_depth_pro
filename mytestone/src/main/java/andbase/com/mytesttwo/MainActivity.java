package andbase.com.mytesttwo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.innovation.pig.insurance.AppConfig;
import com.innovationai.pigweight.Constants;
import com.innovationai.pigweight.activitys.SplashActivity;
import com.xiangchuang.risks.view.LoginPigAarActivity;

import static android.widget.LinearLayout.VERTICAL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        if (view.getId() == R.id.pig_tv) {
            EditText et = new EditText(this);
            et.setInputType(InputType.TYPE_CLASS_PHONE);
            et.setHint("请输入手机号码");
            EditText uet = new EditText(this);
            uet.setHint("请输入用户名");
            EditText pet = new EditText(this);
            pet.setInputType(InputType.TYPE_CLASS_PHONE);
            pet.setHint("请输入部门编号");
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(VERTICAL);
            linearLayout.addView(uet);
            linearLayout.addView(et);
            linearLayout.addView(pet);
            new AlertDialog.Builder(this).setTitle("请输入用户名和手机号码").setView(linearLayout).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String phone = et.getText().toString().trim();
                    String userid = uet.getText().toString().trim();
                    String pid = pet.getText().toString().trim();
                    if (TextUtils.isEmpty(phone)) {
                        phone = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("phone", "19000000001");
                    } else {
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("phone", phone).apply();
                    }
                    if (TextUtils.isEmpty(userid)) {
                        userid = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("userid", "android_userid6");
                    } else {
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("userid", userid).apply();
                    }
                    if (TextUtils.isEmpty(pid)) {
                        pid = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("pid", "28");
                    } else {
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("pid", pid).apply();
                    }
                    Toast.makeText(MainActivity.this, "nb", Toast.LENGTH_LONG).show();
                    Intent mIntent = new Intent(MainActivity.this, LoginPigAarActivity.class);
                    mIntent.putExtra(AppConfig.TOKEY, "android_token");
                    mIntent.putExtra(AppConfig.USER_ID, userid/*"android_userid3"*/);
                    mIntent.putExtra(AppConfig.PHONE_NUMBER, phone);
                    mIntent.putExtra(AppConfig.NAME, "android_name");
                    mIntent.putExtra(AppConfig.DEPARTMENT_ID, pid/*"14079900"*//*"android_department"*/);
                    mIntent.putExtra(AppConfig.IDENTITY_CARD, "android_identitry");
                    startActivity(mIntent);
                    dialog.dismiss();
                }
            }).show();
        } else if (view.getId() == R.id.farm_tv) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ACTION_APPID,"oL-mw59d4mEgDxG49-nQVM2hIha4");
            bundle.putString(Constants.ACTION_TOKEN,"android_token");
            SplashActivity.start(this,bundle);

//            EditText et = new EditText(this);
//            et.setInputType(InputType.TYPE_CLASS_PHONE);
//            et.setHint("请输入手机号码");
//            EditText uet = new EditText(this);
//            uet.setHint("请输入用户名");
//            LinearLayout linearLayout = new LinearLayout(this);
//            linearLayout.setOrientation(VERTICAL);
//            linearLayout.addView(uet);
//            linearLayout.addView(et);
//            new AlertDialog.Builder(this).setTitle("请输入用户名和手机号码").setView(linearLayout).setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    String phone = et.getText().toString().trim();
//                    String userid = uet.getText().toString().trim();
//                    if (TextUtils.isEmpty(phone)) {
//                        phone = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("phone", "19000000003");
//                    } else {
//                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("phone", phone).apply();
//                    }
//                    if (TextUtils.isEmpty(userid)) {
//                        userid = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("userid", "android_userid6");
//                    } else {
//                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("userid", userid).apply();
//                    }
//                    Toast.makeText(MainActivity.this, "nb", Toast.LENGTH_LONG).show();
////                    Intent mIntent = new Intent(MainActivity.this, LoginFamerAarActivity.class);
////                    mIntent.putExtra(FarmAppConfig.TOKEY, "android_token");
////                    mIntent.putExtra(FarmAppConfig.USER_ID, userid);
////                    mIntent.putExtra(FarmAppConfig.PHONE_NUMBER, phone);
////                    mIntent.putExtra(FarmAppConfig.NAME, "android_name");
////                    mIntent.putExtra(FarmAppConfig.DEPARTMENT_ID, "14079900"/*"android_department"*/);
////                    mIntent.putExtra(FarmAppConfig.IDENTITY_CARD, "android_identitry");
////                    startActivity(mIntent);
//                    dialog.dismiss();
//
//                }
//            }).show();

        }


//        Intent it = new Intent(this, LoginFamerServer.class);
//        startService(it);
    }
}
