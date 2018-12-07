package innovation.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangchuangtec.luolu.animalcounter.R;

import org.tensorflow.demo.DetectorActivity;
import org.tensorflow.demo.Global;
import org.tensorflow.demo.env.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import innovation.entry.PayObject;
import innovation.media.MediaProcessor;
import innovation.media.Model;
import innovation.media.PayTransferData;
import innovation.utils.ConstUtils;
import innovation.utils.HttpRespObject;
import innovation.utils.HttpUtils;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import static innovation.entry.InnApplication.ANIMAL_TYPE;
import static innovation.entry.InnApplication.getCowType;
import static innovation.entry.InnApplication.getStringTouboaExtra;
import static innovation.entry.InnApplication.getlipeiTempNumber;


public class AddLipeiActivity extends AppCompatActivity {
    private static String TAG = "AddLipeiActivity";
    private static final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    private static final Logger logger = new Logger();
    @BindView(R.id.areaSpinner)
    Spinner areaSpinner;
    @BindView(R.id.area2Spinner)
    Spinner area2Spinner;
    @BindView(R.id.area3Spinner)
    Spinner area3Spinner;
    private TextView tv_title;
    private ImageView iv_cancel;
    private Button btn_imageAcquisition;
    private EditText et_lipeiTempNumber;
    private String mTempLipeiNumber;
    private TextView lp_toubao_number;

    private PayObject insurresp;
    private String errStr = "";
    private AddLipeiTask mAddLipeiTask;

    private ArrayAdapter<String> adapter;
    private Spinner dropdown;
    private String[] cardType;
    private TextView tv_lipei_idcard;
    private TextView lipei_name;
    private Spinner spinnerReason;
    private String[] strReason;
    private TextView tv_cow_earsNumber;
    private String[] cowType;
    private Spinner dropdownCowType;
    private int userId;
    private PayTransferData payTransferData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lipei);
        Global.model = Model.VERIFY.value();
        ButterKnife.bind(this);

        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("申请理赔");

        SharedPreferences pref = getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        userId = pref.getInt("uid", 0);

        btn_imageAcquisition = findViewById(R.id.btn_imageAcquisition);
        btn_imageAcquisition.setEnabled(true);
        btn_imageAcquisition.setOnClickListener(btn_imageAcquisitionClickListener);

        et_lipeiTempNumber = findViewById(R.id.et_lipei_number);


        lp_toubao_number = (TextView) findViewById(R.id.lp_toubao_number);

        tv_lipei_idcard = (TextView) findViewById(R.id.tv_lipei_idcard);
        lipei_name = (TextView) findViewById(R.id.lipei_name);

        spinnerReason = (Spinner) findViewById(R.id.spinnerReason);
        strReason = new String[]{"传染病（疫病）", "非传染病", "疫病/疾病免疫副反应", "中毒", "扑杀", "意外事故", "难产"};
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, strReason);
        spinnerReason.setAdapter(adapter);

        dropdown = (Spinner) findViewById(R.id.spinnerCardType);
        cardType = new String[]{"身份证号", "营业执照号"};
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, cardType);
        dropdown.setAdapter(adapter);

        dropdownCowType = findViewById(R.id.spinnerCowType);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ConstUtils.getAnimalSubTypeCaptions(ANIMAL_TYPE));
        dropdownCowType.setAdapter(adapter);

        tv_cow_earsNumber = (TextView) findViewById(R.id.tv_cow_earsNumber);


        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        iv_cancel.setVisibility(View.VISIBLE);
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.area));
        ArrayAdapter arrayAdapter2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.area2));
        ArrayAdapter arrayAdapter3 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.area3));
        areaSpinner.setAdapter(arrayAdapter);
        area2Spinner.setAdapter(arrayAdapter2);
        area3Spinner.setAdapter(arrayAdapter3);
        areaSpinner.setSelection(0, true);
        area2Spinner.setSelection(0, true);
        area3Spinner.setSelection(0, true);
    }


    private String strcowType = "";
    private String getCowEarID = "";
    public static String lipeiNumber;
    private View.OnClickListener btn_imageAcquisitionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            btn_imageAcquisition.setEnabled(false);

            String lipeiname = lipei_name.getText().toString().trim();

            String toubaoNumber = lp_toubao_number.getText().toString().trim();
            getStringTouboaExtra = toubaoNumber;
            getlipeiTempNumber = toubaoNumber;
            String str_lipei_idcard = tv_lipei_idcard.getText().toString();

            mTempLipeiNumber = mSimpleDateFormat.format(new Date(System.currentTimeMillis()));
            lipeiNumber = mTempLipeiNumber;

            String strIDcard = IDCardValidate.validateIDcardNumber(str_lipei_idcard, true);

            String cardType = dropdown.getSelectedItem().toString();

            String int_cardType = "";

            if (cardType.indexOf("身份证号") > -1) {
                int_cardType = "1";
            } else if (cardType.indexOf("营业执照号") > -1) {
                int_cardType = "2";
            }

            String str_reason = spinnerReason.getSelectedItem().toString();
            strcowType = ConstUtils.getAnimalSubTypeCodeByCaption(dropdownCowType.getSelectedItem().toString());


            if (tv_cow_earsNumber.getText().toString().equals("")) {
                getCowEarID = "";
            } else {
                getCowEarID = tv_cow_earsNumber.getText().toString();
            }

            getCowEarID = getCowEarID;
            if (lipeiname.matches("")) {
                Toast.makeText(AddLipeiActivity.this, "被保险人姓名不能为空！", Toast.LENGTH_SHORT).show();
                btn_imageAcquisition.setEnabled(true);
                return;
            }

            if (cardType.indexOf("身份证号") > -1) {
                if (!(strIDcard.length() == 15 || strIDcard.length() == 18)) {
                    Toast.makeText(getApplicationContext(), strIDcard, Toast.LENGTH_SHORT).show();
                    btn_imageAcquisition.setEnabled(true);
                    return;
                }
            } else if (cardType.indexOf("营业执照号") > -1) {
                if (!isValid(str_lipei_idcard)) {

                    Toast.makeText(getApplicationContext(), "请输入正确的统一社会信用代码", Toast.LENGTH_SHORT).show();
                    btn_imageAcquisition.setEnabled(true);
                    return;
                }
            }

            if (str_reason.matches("")) {
                Toast.makeText(AddLipeiActivity.this, "请选择出险原因！", Toast.LENGTH_SHORT).show();
                btn_imageAcquisition.setEnabled(true);
                return;
            }
            if (lipeiNumber.matches("")) {
                Toast.makeText(AddLipeiActivity.this, "理赔单号不能为空！", Toast.LENGTH_SHORT).show();
                btn_imageAcquisition.setEnabled(true);
                return;
            }
            if (toubaoNumber.matches("")) {
                Toast.makeText(AddLipeiActivity.this, "投保单号不能为空！", Toast.LENGTH_SHORT).show();
                btn_imageAcquisition.setEnabled(true);
                return;
            }

            if (strcowType.matches("")) {
                Toast.makeText(AddLipeiActivity.this, "请重新选择种类！", Toast.LENGTH_SHORT).show();
                btn_imageAcquisition.setEnabled(true);
                return;
            }
            getCowType = strcowType;

            TreeMap query = new TreeMap<String, String>();
            query.put("baodanNo", toubaoNumber);
            query.put("amount", "1");
            query.put("lipeiNo", lipeiNumber);
            query.put("name", lipeiname);
            query.put("cardType", int_cardType);
            query.put("cardNo", str_lipei_idcard);
            query.put("bankNo", "123");
            query.put("type", "1");
            query.put("reason", str_reason.trim());
            query.put("uid", String.valueOf(userId));
            query.put("yiji", areaSpinner.getSelectedItem().toString());
            query.put("erji", area2Spinner.getSelectedItem().toString());
            query.put("sanji", area3Spinner.getSelectedItem().toString());

            Log.d("yiji", areaSpinner.getSelectedItem().toString());
            Log.d("erji", area2Spinner.getSelectedItem().toString());
            Log.d("sanji", area3Spinner.getSelectedItem().toString());


            new MediaProcessor(getApplicationContext()).transerPayData(areaSpinner.getSelectedItem().toString(), area2Spinner.getSelectedItem().toString(), area3Spinner.getSelectedItem().toString());
            mAddLipeiTask = new AddLipeiTask(HttpUtils.LIPEI_NEW_URL, query);
            mAddLipeiTask.execute((Void) null);


        }
    };


    public class AddLipeiTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final TreeMap<String, String> mQueryMap;

        AddLipeiTask(String url, TreeMap map) {
            mUrl = url;
            mQueryMap = map;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //  attempt authentication against a network service.
            try {
                FormBody.Builder builder = new FormBody.Builder();
                // Add Params to Builder
                for (TreeMap.Entry<String, String> entry : mQueryMap.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }
                // Create RequestBody
                RequestBody formBody = builder.build();

                String response = HttpUtils.post(mUrl, formBody);
                Log.d(TAG, mUrl + "\nresponse:" + response);

                if (HttpUtils.LIPEI_NEW_URL.equalsIgnoreCase(mUrl)) {
                    insurresp = (PayObject) HttpUtils.processResp_insurInfo(response, mUrl);
                    if (insurresp == null) {
//                        errStr = getString(R.string.error_newwork);
                        errStr = "请求错误！";
                        return false;
                    }
                    if (insurresp.status != HttpRespObject.STATUS_OK) {
                        errStr = insurresp.msg;
                        return false;
                    }
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                errStr = "服务器错误！";
                return false;
            }
            //  register the new account here.

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddLipeiTask = null;
            if (success & HttpUtils.LIPEI_NEW_URL.equalsIgnoreCase(mUrl)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AddLipeiActivity.this)
                        .setIcon(R.drawable.cowface)
                        .setTitle("提示")
                        .setMessage("提交成功")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Global.model = Model.VERIFY.value();
                                logger.i("AddLipeiActivity Model.VERIFY.value: " + Model.VERIFY.value());
                                Intent intent = new Intent(AddLipeiActivity.this, DetectorActivity.class);
                                intent.putExtra("ToubaoTempNumber", lp_toubao_number.getText().toString());
                                intent.putExtra("LipeiTempNumber", mTempLipeiNumber);
                                intent.putExtra("cowEarNumber", getCowEarID);
                                intent.putExtra("cowType", strcowType);
                                startActivity(intent);
                                finish();

                            }
                        });
                builder.setCancelable(false);
                builder.show();


            } else if (!success) {
                //  显示失败
                Log.d(TAG, errStr);
                Toast.makeText(AddLipeiActivity.this, errStr, Toast.LENGTH_SHORT).show();
                btn_imageAcquisition.setEnabled(true);
//                tv_info.setText(errStr);

            }
        }

        @Override
        protected void onCancelled() {
            mAddLipeiTask = null;
        }
    }

    private boolean isValid(String businessCode) {
        if ((businessCode.equals("")) || businessCode.length() != 18) {
            return false;
        }
        String baseCode = "0123456789ABCDEFGHJKLMNPQRTUWXY";
        char[] baseCodeArray = baseCode.toCharArray();
        Map<Character, Integer> codes = new HashMap<Character, Integer>();
        for (int i = 0; i < baseCode.length(); i++) {
            codes.put(baseCodeArray[i], i);
        }
        char[] businessCodeArray = businessCode.toCharArray();
        Character check = businessCodeArray[17];
        if (baseCode.indexOf(check) == -1) {
            return false;
        }
        int[] wi = {1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            Character key = businessCodeArray[i];
            if (baseCode.indexOf(key) == -1) {
                return false;
            }
            sum += (codes.get(key) * wi[i]);
        }
        int value = 31 - sum % 31;
        return value == codes.get(check);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
