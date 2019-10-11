package andbase.com.mytesttwo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.bean.CattleBean;
import com.innovationai.pigweight.event.EventManager;
import com.xiangchuang.risks.model.bean.GSCPigBean;
import com.xiangchuang.risks.view.LoginFamerActivity;

import java.util.List;
import java.util.Map;

import innovation.utils.FarmInnovationAiOpen;
import innovation.utils.PigInnovationAiOpen;

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
//                    Toast.makeText(MainActivity.this, "nb", Toast.LENGTH_LONG).show();
//                    Intent mIntent = new Intent(MainActivity.this, LoginPigAarActivity.class);
//                    mIntent.putExtra(PigAppConfig.TOKEY, "android_token");
//                    mIntent.putExtra(PigAppConfig.USER_ID, userid/*"android_userid3"*/);
//                    mIntent.putExtra(PigAppConfig.PHONE_NUMBER, phone);
//                    mIntent.putExtra(PigAppConfig.NAME, "android_name");
//                    mIntent.putExtra(PigAppConfig.DEPARTMENT_ID, pid/*"14079900"*//*"android_department"*/);
//                    mIntent.putExtra(PigAppConfig.IDENTITY_CARD, "android_identitry");
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
                    FarmInnovationAiOpen.getInstance().requestInnovationApi(MainActivity.this, "98765432101", "89979dc663caa2580164f88b57796251",
                            "14112100", "文水县支公司", "总公司/山西分公司/吕梁市中心支公司/文水县支公司",
                            "00000000,14000000,14119900,", FarmInnovationAiOpen.INSURE, "111111111111111111",
                            "test", new Handler.Callback() {
                                @Override
                                public boolean handleMessage(Message msg) {
                                    CattleBean bean = (CattleBean) msg.obj;
                                    if (bean.type == FarmInnovationAiOpen.INSURE)
                                        Toast.makeText(MainActivity.this, "投保返回", Toast.LENGTH_LONG).show();
                                    return true;
                                }
                            });
                }
            }).show();

        } else if (view.getId() == R.id.farm_tv) {
//            InnovationAiOpen.getInstance().requestInnovationApi(MainActivity.this, "98765432102", "89979dc663caa2580164f88b57796251",
//                    "14112100", "文水县支公司", "总公司/山西分公司/吕梁市中心支公司/文水县支公司",
//                    "00000000,14000000,14119900,", InnovationAiOpen.PAY, "111111111111111111",
//                    "test", new Handler.Callback() {
//                        @Override
//                        public boolean handleMessage(Message msg) {
//                            CattleBean bean = (CattleBean) msg.obj;
//                            if(bean.type == InnovationAiOpen.PAY)
//                                Toast.makeText(MainActivity.this, "理赔返回", Toast.LENGTH_LONG).show();
//                            return true;
//                        }
//                    });


//                        Bundle bundle = new Bundle();
//            bundle.putString(Constants.ACTION_APPID,"oL-mw59d4mEgDxG49-nQVM2hIha4");
//            bundle.putString(Constants.ACTION_TOKEN,"android_token");
////            SplashActivity.start(this,bundle);
//            EventManager.getInstance().requestWeightApi(this, bundle, new Handler.Callback() {
//                @Override
//                public boolean handleMessage(Message msg) {
//                    return false;
//                }
//            });

//            EventManager.getInstance().requestWeightApi(MainActivity.this, "2c28450ebe993dd61cb4d76eff7a916d", "token", new Handler.Callback() {
//                @Override
//                public boolean handleMessage(Message msg) {
//                    if (MainActivity.this == null || MainActivity.this.isFinishing())
//                        return false;
//                    Map<String, Object> map = (Map<String, Object>) msg.obj;
//                    if (map == null) {
//                        Toast.makeText(MainActivity.this, "没有返回结果", Toast.LENGTH_SHORT).show();
//                        return false;
//                    }
//                    try {
//                        Toast.makeText(MainActivity.this, "模型结果反馈： " + map.get("data").toString(), Toast.LENGTH_LONG).show();
//                        ((TextView) view).setText(map.get("data").toString());
//                        byte[] bis = (byte[]) map.get("bitmap");
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
//                        view.setBackground(new BitmapDrawable(bitmap));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Toast.makeText(MainActivity.this, "aaaaa", Toast.LENGTH_SHORT).show();
//                    }
//                    return false;
//                }
//            });

            if(true){
                PigInnovationAiOpen.getInstance().requestInnovationApi(this, "98765432101", "89979dc663caa2580164f88b57796251",
                        "14112100", "文水县支公司", "3","14119900","总公司/山西分公司/吕梁市中心支公司/文水县支公司",
                        "00000000,14000000,14119900,", "文水县养殖场", PigInnovationAiOpen.INSURE, "test", new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message msg) {
                                List<GSCPigBean> beanS = (List<GSCPigBean>) msg.obj;
                                for (GSCPigBean bean:beanS) {
                                    Log.d("aaaaaa", "handleMessage: ---->"+bean.string());
                                }
                                if(msg.what == PigInnovationAiOpen.INSURE) {
                                    innovation.utils.Toast.makeText(MainActivity.this, "投保返回", innovation.utils.Toast.LENGTH_LONG).show();
                                }

                                if(msg.what == PigInnovationAiOpen.PAY) {

                                    List<GSCPigBean> beans = (List<GSCPigBean>)msg.obj;
                                    innovation.utils.Toast.makeText(MainActivity.this, "理赔返回", innovation.utils.Toast.LENGTH_LONG).show();
                                }

                                return true;
                            }
                        });
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FarmInnovationAiOpen.getInstance().removeEvent(this);
    }
}
