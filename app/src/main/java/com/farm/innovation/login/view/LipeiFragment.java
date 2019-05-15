package com.farm.innovation.login.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.farm.innovation.bean.LiPeiLocalBean;
import com.farm.innovation.bean.MultiBaodanBean;
import com.farm.innovation.bean.QueryBaodanBean;
import com.farm.innovation.biz.Insured.PayActivity;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.login.DatabaseHelper;
import com.farm.innovation.login.LipeiAdapter;
import com.farm.innovation.login.model.LipeiLocalAdapter;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.HttpRespObject;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.PreferencesUtils;
import com.innovation.pig.insurance.R;

import org.tensorflow.demo.FarmGlobal;
import org.tensorflow.demo.env.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.RequestBody;


/**
 * luolu
 */
public class LipeiFragment extends Fragment {

    private static String TAG = "LipeiFragment";
    private static final Logger logger = new Logger();
    private TextView btn_lipei_add;
    private RecyclerView mylipei_recycler_view;
    public final ArrayList<QueryBaodanBean> newsBeanArrayList;
    private LipeiAdapter mAdapter;
    private EditText search_lipei_input_edit;
    private LinearLayoutManager mLayoutManager;

    private String errStr = "";
    private MultiBaodanBean insurresp;
    private LipeiTask mLipeiTask;
    private static int type;
    private ArrayList<QueryBaodanBean> showuserList;
    private ArrayList<QueryBaodanBean> userList;
    private boolean isFleg = false;
    private List<LiPeiLocalBean> liPeiLocalBeans = new ArrayList<>();
    private LipeiLocalAdapter lipeiLocalAdapter;

    public LipeiFragment() {
        newsBeanArrayList = new ArrayList<QueryBaodanBean>();
    }

    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.farm_fragment_lipei, container, false);

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseHelper = DatabaseHelper.getInstance(getActivity());
        FarmGlobal.model = Model.VERIFY.value();

        btn_lipei_add = (TextView) view.findViewById(R.id.btn_lipei_add);

        btn_lipei_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent add_intent = new Intent(getActivity(), PayActivity.class);
                startActivity(add_intent);
            }
        });

        mylipei_recycler_view = (RecyclerView) view.findViewById(R.id.mylipei_recycler_view);
//创建默认的线性LayoutManager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mylipei_recycler_view.setLayoutManager(mLayoutManager);
//如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mylipei_recycler_view.setHasFixedSize(true);

        /*liPeiLocalBeans = databaseHelper.queryLocalDataFromLiPei();
        if (null != liPeiLocalBeans) {
            lipeiLocalAdapter = new LipeiLocalAdapter(liPeiLocalBeans, getActivity());
            mylipei_recycler_view.setAdapter(lipeiLocalAdapter);
            lipeiLocalAdapter.notifyDataSetChanged();//更新RecycleView
        }*/

        search_lipei_input_edit = (EditText) view.findViewById(R.id.search_lipei_input_edit);
        search_lipei_input_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            //当EditText内容发生变化时会调用此方法
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                TreeMap<String, String> query = new TreeMap<>();
                query.put("baodanNo", charSequence.toString());
                if (charSequence.toString() != null) {
                    mLipeiTask = new LipeiTask(HttpUtils.INSUR_QUERY_URL, query);
                    mLipeiTask.execute((Void) null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        isFleg = true;
        Log.i("==resume===", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        isFleg = true;
        Log.i("==resume===", "onStop");
    }

    @Override
    public void onResume() {
        initView();
        super.onResume();
    }

    private void initView() {

        Log.i("==resume===", "resume");
        Log.i("==resume===", "isFleg" + isFleg);
        liPeiLocalBeans.clear();
        if (null != databaseHelper.queryLocalDataFromLiPei(PreferencesUtils.getStringValue(HttpUtils.user_id, getContext()))) {
            liPeiLocalBeans.addAll(databaseHelper.queryLocalDataFromLiPei(PreferencesUtils.getStringValue(HttpUtils.user_id, getContext())));
        }
        Log.e(TAG, "liPeiLocalBeans: "+ liPeiLocalBeans.toString() );
        if (null != liPeiLocalBeans) {
//            if (lipeiLocalAdapter == null) {
                lipeiLocalAdapter = new LipeiLocalAdapter(liPeiLocalBeans, getContext());
                lipeiLocalAdapter.setOnUpdateClickListener(new LipeiLocalAdapter.OnUpdateClickListener() {
                    @Override
                    public void onUpdateClick(File uploadFile, int model, int userId, String pbaodanNo) {

                    }
                });
                mylipei_recycler_view.setAdapter(lipeiLocalAdapter);
//            } else {
//                lipeiLocalAdapter.notifyDataSetChanged();//更新RecycleView
//            }
        }
    }

    public class LipeiTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final TreeMap<String, String> mQueryMap;

        LipeiTask(String url, TreeMap map) {
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
                Log.d(TAG, "response:" + response);

                if (HttpUtils.INSUR_QUERY_URL.equalsIgnoreCase(mUrl)) {
                    insurresp = (MultiBaodanBean) HttpUtils.processResp_new_detail_query(response);
                    if (insurresp == null) {
                        errStr = "请求错误！";
                        return false;
                    }
                    if (insurresp.status != HttpRespObject.STATUS_OK) {
                        errStr = insurresp.msg;
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                errStr = "服务器错误！";
                AVOSCloudUtils.saveErrorMessage(e, LipeiFragment.class.getSimpleName());
                return false;
            }
            //  register the new account here.

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLipeiTask = null;
            newsBeanArrayList.clear();
            if (success & HttpUtils.INSUR_QUERY_URL.equalsIgnoreCase(mUrl)) {
                showuserList = new ArrayList<>();

                mAdapter = new LipeiAdapter(newsBeanArrayList, getContext());
                mylipei_recycler_view.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();//更新RecycleView

            } else if (!success) {
                //  显示失败
                com.orhanobut.logger.Logger.d( errStr);
            }
        }

        @Override
        protected void onCancelled() {
            mLipeiTask = null;
        }
    }

}
