package andbase.com.mytesttwo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.farm.innovation.bean.CattleBean;
import com.innovationai.pigweight.Constants;
import com.innovationai.pigweight.event.EventManager;

import innovation.utils.InnovationAiOpen;

import static android.widget.LinearLayout.VERTICAL;

public class MainActivity extends Activity {

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
//                    Intent mIntent = new Intent(MainActivity.this, LoginPigAarActivity.class);
//                    mIntent.putExtra(AppConfig.TOKEY, "android_token");
//                    mIntent.putExtra(AppConfig.USER_ID, userid/*"android_userid3"*/);
//                    mIntent.putExtra(AppConfig.PHONE_NUMBER, phone);
//                    mIntent.putExtra(AppConfig.NAME, "android_name");
//                    mIntent.putExtra(AppConfig.DEPARTMENT_ID, pid/*"14079900"*//*"android_department"*/);
//                    mIntent.putExtra(AppConfig.IDENTITY_CARD, "android_identitry");
//                    startActivity(mIntent);
                    dialog.dismiss();
//                    innovation.f.j.a().a();
                    /**

                     actionId 任务号或者保单号（必填） 1075274131248574464
                     userid 用户id（必填）      89979dc663caa2580164f88b57796251
                     officeCode 机构编码（必填）         14112100
                     officeName 机构名称（必填）     文水县支公司
                     officeLevel 机构层级                  3
                     parentCode  父机构编码               14119900
                     parentOfficeNames 机构层级（必填）   总公司/山西分公司/吕梁市中心支公司/文水县支公司
                     parentOfficeCodes 机构层级编码（必填）   00000000,14000000,14119900,
                     type 操作类型（必填）
                     phone 手机号
                     idcard 身份证号（必填）
                     username 用户名（必填）
                     */
                    InnovationAiOpen.getInstance().requestInnovationApi(MainActivity.this, "1075274131248574464", "89979dc663caa2580164f88b57796251",
                            "14112100","文水县支公司","总公司/山西分公司/吕梁市中心支公司/文水县支公司",
                            "00000000,14000000,14119900,",InnovationAiOpen.INSURE,"111111111111111111",
                            "test",new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            CattleBean bean = (CattleBean) msg.obj;
                            return false;
                        }
                    });
                }
            }).show();
        } else if (view.getId() == R.id.farm_tv) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ACTION_APPID,"oL-mw59d4mEgDxG49-nQVM2hIha4");
            bundle.putString(Constants.ACTION_TOKEN,"android_token");
//            SplashActivity.start(this,bundle);
            EventManager.getInstance().requestWeightApi(this, bundle, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    return false;
                }
            });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        InnovationAiOpen.getInstance().removeEvent(this);
    }
}
