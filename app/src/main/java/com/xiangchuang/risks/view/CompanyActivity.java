package com.xiangchuang.risks.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.OkHttp3Util;
import com.innovation.pig.insurance.netutils.PreferencesUtils;
import com.xiangchuang.risks.base.BaseActivity;
import com.xiangchuang.risks.model.adapter.CompanyAdapter;
import com.xiangchuang.risks.model.bean.InSureCompanyBean;
import com.xiangchuang.risks.update.AppUpgradeService;
import com.xiangchuang.risks.update.UpdateInformation;
import com.xiangchuang.risks.utils.AVOSCloudUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.innovation.pig.insurance.AppConfig.needUpDate;

public class CompanyActivity extends BaseActivity {

    TextView company_name;

    TextView addcompany;

    ListView company_listview;

    ImageView iv_cancel;

    TextView tvExit;

    ImageView ivSign;

    RelativeLayout rl_edit;

    EditText searchEdit;

    Button btnClear;

    private String en_id;
    private int userid;
    public static String TAG = "CompanyActivity";
    private List<InSureCompanyBean> inSureCompanyBeanlists = new ArrayList<>();
    private List<InSureCompanyBean> current = new ArrayList<>();
    private PopupWindow pop;
    private TextView tvPopExit;
    private TextView tvPopUpdate;
    private ImageView ivPopUpdateSign;

    @Override
    public void initView() {
        company_name = (TextView) findViewById(R.id.company_name);
        addcompany = (TextView) findViewById(R.id.addcompany);
        company_listview = (ListView) findViewById(R.id.company_listview);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        tvExit = (TextView) findViewById(R.id.tv_exit);
        ivSign = (ImageView) findViewById(R.id.iv_sign);
        rl_edit = (RelativeLayout) findViewById(R.id.rl_edit);
        searchEdit = (EditText) findViewById(R.id.search_tag_input_edit);
        btnClear = (Button) findViewById(R.id.bt_clear);
        findViewById(R.id.tv_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
        findViewById(R.id.addcompany).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView((View) v);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_company;
    }

    @Override
    protected void initData() {
        company_name.setText("企业列表");
        getDataFromNet();

        pop = new PopupWindow(CompanyActivity.this);
        View popview = getLayoutInflater().inflate(R.layout.item_setting, null);
        tvPopExit = popview.findViewById(R.id.tv_pop_exit);
        tvPopUpdate = popview.findViewById(R.id.tv_pop_update);
        ivPopUpdateSign = popview.findViewById(R.id.iv_pop_update_sign);

        if (needUpDate) {
            ivPopUpdateSign.setVisibility(View.VISIBLE);
            ivSign.setVisibility(View.VISIBLE);
        } else {
            ivPopUpdateSign.setVisibility(View.GONE);
            ivSign.setVisibility(View.GONE);
        }

        pop.setWidth(300);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(popview);
        
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    btnClear.setVisibility(View.GONE);
                } else {
                    btnClear.setVisibility(View.VISIBLE);
                }

                if (s.length() > 0) {
                    if (inSureCompanyBeanlists.size() > 0) {
                        current.clear();
                        for (InSureCompanyBean inSureCompanyBean : inSureCompanyBeanlists) {
                            if (inSureCompanyBean.getEnName().contains(s.toString())) {
                                current.add(inSureCompanyBean);
                            }
                        }
                        company_listview.setAdapter(new CompanyAdapter(CompanyActivity.this, current));
                    } else {
                        Toast.makeText(CompanyActivity.this, "当前没有可选猪场，请添加猪场。", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (inSureCompanyBeanlists.size() > 0) {
                        company_listview.setAdapter(new CompanyAdapter(CompanyActivity.this, inSureCompanyBeanlists));
                    }
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchEdit.setText("");
                btnClear.setVisibility(View.GONE);
            }
        });
    }

    public void setSign() {
        if (needUpDate) {
            ivPopUpdateSign.setVisibility(View.VISIBLE);
            ivSign.setVisibility(View.VISIBLE);
        } else {
            ivPopUpdateSign.setVisibility(View.GONE);
            ivSign.setVisibility(View.GONE);
        }
    }


    public void onClickView(View view) {
        int i = view.getId();
        if (i == R.id.addcompany) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("type", false);
            goToActivity(AddCompanyActivity.class, bundle);

        } else if (i == R.id.iv_cancel) {
            finish();

        } else if (i == R.id.tv_exit) {
            ivSign.setVisibility(View.GONE);
            pop.showAsDropDown(rl_edit);
            tvPopExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pop.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(CompanyActivity.this)
                            .setIcon(R.drawable.cowface).setTitle("提示")
                            .setMessage("退出登录")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //如果退出，清空保存的相关状态， 跳转到登录页
                                    PreferencesUtils.removeAllKey(CompanyActivity.this);
                                    Intent addIntent = new Intent(CompanyActivity.this, LoginFamerActivity.class);
                                    startActivity(addIntent);
                                    finish();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.setCancelable(false);
                    builder.show();
                }
            });

            tvPopUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pop.dismiss();
                    if (needUpDate) {
                        if (ivSign.getVisibility() == View.VISIBLE) {
                            ivSign.setVisibility(View.GONE);
                        }

                        AlertDialog.Builder mDialog = new AlertDialog.Builder(CompanyActivity.this);
                        mDialog.setIcon(R.drawable.cowface);
                        mDialog.setTitle("版本更新");
                        mDialog.setMessage(UpdateInformation.upgradeinfo);
                        mDialog.setCancelable(false);
                        mDialog.setPositiveButton("马上升级", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ivPopUpdateSign.setVisibility(View.GONE);
                                Intent mIntent = new Intent(CompanyActivity.this, AppUpgradeService.class);
                                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //传递数据
                                //mIntent.putExtra("appname", UpdateInformation.appname);
                                mIntent.putExtra("mDownloadUrl", UpdateInformation.updateurl);
                                mIntent.putExtra("appname", UpdateInformation.appname);
                                CompanyActivity.this.startService(mIntent);
                            }
                        }).setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        }).create().show();
                    } else {
                        AlertDialog.Builder mDialog = new AlertDialog.Builder(CompanyActivity.this);
                        mDialog.setIcon(R.drawable.cowface);
                        mDialog.setTitle("提示");
                        mDialog.setMessage("当前已是最新版本");
                        mDialog.setCancelable(false);
                        mDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                }
            });

        } else {
        }

    }

    private void getDataFromNet() {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.AppKeyAuthorization, "hopen");
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put(Constants.deptId, PreferencesUtils.getStringValue(Constants.deptId, AppConfig.getAppContext()));
        OkHttp3Util.doPost(Constants.ENLIST, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e, CompanyActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                String string = response.body().string();
                Log.i(TAG, string);
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    int status = jsonObject.getInt("status");
                    String msg = jsonObject.getString("msg");
                    if (status != 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showDialogError(msg);
                                    }
                                });
                            }
                        });
                    } else {
                        inSureCompanyBeanlists.clear();
                        JSONArray datas = jsonObject.getJSONArray("data");
                        for (int i = 0; i < datas.length(); i++) {
                            JSONObject jsonObject1 = datas.getJSONObject(i);
                            String enId = jsonObject1.getString("enId");
                            String enName = jsonObject1.getString("enName");
                            String enUserId = jsonObject1.getString("enUserId");
                            String enUserName = jsonObject1.getString("enUserName");
                            String canUse = jsonObject1.getString("canUse");
                            InSureCompanyBean inSureCompanyBean = new InSureCompanyBean();
                            inSureCompanyBean.setEnId(enId);
                            inSureCompanyBean.setEnName(enName);
                            inSureCompanyBean.setEnUserId(enUserId);
                            inSureCompanyBean.setEnUserName(enUserName);
                            inSureCompanyBean.setCanUse(canUse);
                            inSureCompanyBeanlists.add(inSureCompanyBean);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (!TextUtils.isEmpty(searchEdit.getText())) {
                                    if (inSureCompanyBeanlists.size() > 0) {
                                        current.clear();
                                        for (InSureCompanyBean inSureCompanyBean : inSureCompanyBeanlists) {
                                            if (inSureCompanyBean.getEnName().contains(searchEdit.getText().toString())) {
                                                current.add(inSureCompanyBean);
                                            }
                                        }
                                        company_listview.setAdapter(new CompanyAdapter(CompanyActivity.this, current));
                                    } else {
                                        Toast.makeText(CompanyActivity.this, "当前没有可选猪场，请添加猪场。", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    company_listview.setAdapter(new CompanyAdapter(CompanyActivity.this, inSureCompanyBeanlists));
                                }
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AVOSCloudUtils.saveErrorMessage(e, CompanyActivity.class.getSimpleName());
                }


            }
        });
    }

    private long firstTime = 0;

    @Override
    public void onBackPressed() {
        if ("com.xiangchuangtec.luolu.animalcounter".equals(AppConfig.getAppContext().getPackageName())) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
//            finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        }else{
            super.onBackPressed();
        }
    }

}
