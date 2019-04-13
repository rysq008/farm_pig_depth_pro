package com.farm.innovation.login;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.bean.BaodanBean;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.HttpRespObject;
import com.farm.innovation.utils.HttpUtils;
import com.innovation.pig.insurance.R;

import org.tensorflow.demo.FarmGlobal;

import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.farm.innovation.base.FarmAppConfig.getStringTouboaExtra;

public class LipeiDetailActivity extends BaseActivity {

    private static String TAG = "LipeiDetailActivity";
    private TextView tv_title;
    private ImageView iv_cancel;
    private String baodanNumber;
    private TextView lipei_detail_number;
    private ToubaoTask mToubaoTask;

    private String errStr = "";
    private BaodanBean insurresp;
    private TextView lipei_detail_num;
    private TextView lipei_detail_pay;
    private TextView lipei_detail_date;
    private TextView lipei_detail_name;
    private TextView lipei_detail_idcard;
    private TextView lipei_detail_address;
    private Button update_toubao;
    private Button continue_toubao;
    private TextView lipei_detail_tel;
    private Button general_lipei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.farm_activity_lipei_detail);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_lipei_detail;
    }

    @Override
    protected void initData() {
        FarmGlobal.model = Model.BUILD.value();
        tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText("理赔详情");

        iv_cancel = (ImageView)findViewById(R.id.iv_cancel);
        iv_cancel.setVisibility(View.VISIBLE);
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        baodanNumber = getIntent().getStringExtra("baodanNumber");

        lipei_detail_number = (TextView)findViewById(R.id.lipei_detail_number);
        lipei_detail_num = (TextView)findViewById(R.id.lipei_detail_num);
        lipei_detail_pay = (TextView)findViewById(R.id.lipei_detail_pay);
        lipei_detail_date = (TextView)findViewById(R.id.lipei_detail_date);
        lipei_detail_name = (TextView)findViewById(R.id.lipei_detail_name);
        lipei_detail_idcard = (TextView)findViewById(R.id.lipei_detail_idcard);
        lipei_detail_tel = (TextView)findViewById(R.id.lipei_detail_tel);
        lipei_detail_address = (TextView)findViewById(R.id.lipei_detail_address);

        lipei_detail_number.setText(baodanNumber);

//        update_toubao = (Button)findViewById(R.id.update_toubao);
//        update_toubao.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClickView(View v) {
//                Intent intent = new Intent();
//                intent.putExtra("baodanNumber",baodanNumber);
//                intent.setClass(getApplicationContext(), UpdateToubaoActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//
        general_lipei = (Button)findViewById(R.id.general_lipei);
        general_lipei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LipeiDetailActivity.this, "成功生成一条理赔单", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        TreeMap query = new TreeMap<String, String>();
        query.put("baodanNo", baodanNumber);
        getStringTouboaExtra = baodanNumber;
        if (baodanNumber != null) {
            mToubaoTask = new ToubaoTask(HttpUtils.INSUR_QUERY_URL, query);
            mToubaoTask.execute((Void) null);
        }
    }


    public class ToubaoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final TreeMap<String, String> mQueryMap;

        ToubaoTask(String url, TreeMap map) {
            mUrl = url;
            mQueryMap = map;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //  attempt authentication against a network service.
            try {
                FormBody.Builder builder = new FormBody.Builder();
                // Add Params to Builder
                for ( TreeMap.Entry<String, String> entry : mQueryMap.entrySet() ) {
                    builder.add( entry.getKey(), entry.getValue() );
                }
                // Create RequestBody
                RequestBody formBody = builder.build();

                String response = HttpUtils.post(mUrl, formBody);
                Log.d(TAG, "response:" + response);

                if (HttpUtils.INSUR_QUERY_URL.equalsIgnoreCase(mUrl)) {
                    insurresp = (BaodanBean) HttpUtils.processResp_insurInfo(response, mUrl);
                    if (insurresp == null) {
//                        errStr = getString(R.string.error_newwork);
                        errStr = "请求错误！";
                        return false;
                    }
                    if(insurresp.status != HttpRespObject.STATUS_OK){
                        errStr = insurresp.msg;
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                errStr = "服务器错误！";
                AVOSCloudUtils.saveErrorMessage(e, LipeiDetailActivity.class.getSimpleName());
                return false;
            }
            //  register the new account here.

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mToubaoTask = null;
            if (success & HttpUtils.INSUR_QUERY_URL.equalsIgnoreCase(mUrl)) {
//                InfoUtils.saveInsurInfo(InsuranceNewActivity.this, insurresp);
//                startDetectActity();
//                Intent add_intent = new Intent(getActivity(),ToubaoDetailActivity.class);
//                startActivity(add_intent);
//                getActivity().finish();
                lipei_detail_num.setText(String.valueOf(insurresp.iamount));
                lipei_detail_pay.setText(String.valueOf(insurresp.imoney));
                lipei_detail_date.setText(String.valueOf(insurresp.ibaodanTime.substring(0,insurresp.ibaodanTime.trim().length()-2)));

                lipei_detail_tel.setText(String.valueOf(insurresp.ibaodanPhone));
                lipei_detail_address.setText(String.valueOf(insurresp.iaddress));

                lipei_detail_name.setText(String.valueOf(insurresp.iname));
                lipei_detail_idcard.setText(insurresp.icardNo);


            } else if (!success) {
                //  显示失败
                Log.d(TAG, errStr);
//                tv_info.setText(errStr);
            }
        }

        @Override
        protected void onCancelled() {
            mToubaoTask = null;
        }
    }
}
