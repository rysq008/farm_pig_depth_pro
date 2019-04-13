package com.farm.innovation.login.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.farm.innovation.base.BaseFragment;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.BaoDanNetBean;
import com.farm.innovation.bean.QueryBaodanBean;
import com.farm.innovation.biz.insurance.YanBiaoDanActivity;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.location.AlertDialogManager;
import com.farm.innovation.login.DatabaseHelper;
import com.farm.innovation.login.Utils;
import com.farm.innovation.login.model.LocalModelNongxian;
import com.farm.innovation.login.model.MyUIUTILS;
import com.farm.innovation.login.model.ToubaoLocalAdapter;
import com.farm.innovation.login.model.ToubaoNetAdapter;
import com.farm.innovation.login.presenter.TouBaoPresenter;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.OkHttp3Util;
import com.farm.innovation.utils.PreferencesUtils;
import com.farm.innovation.utils.UIUtils;
import com.google.gson.Gson;
import com.innovation.pig.insurance.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.demo.FarmGlobal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.dmoral.prefs.Prefs;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ToubaoFragment extends BaseFragment implements ITouBao {

    private static String TAG = "ToubaoFragment";
    private TextView search_input_edit;
    private ImageView btn_toubao_add;
    private static Context mContext;
    private Unbinder unbinder;
    ArrayList<QueryBaodanBean> localInsureList = new ArrayList<>();
    private RecyclerView localInsuredRecyclerView;
    private RecyclerView mRecyclerView;
    static Handler mHandler;
    private ToubaoLocalAdapter localInsureAdapter;
    ArrayList<QueryBaodanBean> userList;
    ArrayList<QueryBaodanBean> showuserList;
    private TouBaoPresenter presenter;
    //    private DatabaseHelper databaseHelper;
    private CharSequence mcharSequence = "";
    private static int type;
    private Button keywordSearchButton;
    private int userid;
    private List<QueryBaodanBean> beanList = new ArrayList<>();
    private List<QueryBaodanBean> beanList_item;
    //    private List<BaoDanNetBean> baoDanNetBeans;
    private Vector<BaoDanNetBean> baoDanNetBeans;
    private LinearLayout fragment_toubao;

    private Button btClear;

    private DatabaseHelper databaseHelper;

    @SuppressLint("HandlerLeak")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = new TouBaoPresenter(this);
        mContext = context;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0://接收消息
                        String insuredNo = (String) msg.obj;
                        QueryBaodanBean insured = null;
                        for (QueryBaodanBean baodanBean : userList) {
                            if (baodanBean.baodanNo.equals(insuredNo)) {
                                insured = baodanBean;
                            }
                        }
                        if (insured != null) {
                            localInsureList.add(insured);
                        }
                        break;
                    default:

                }
            }
        };
        prefs = Prefs.with(FarmAppConfig.getActivity(), "insured_nos");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public View createSuccessView() {
        View view = MyUIUTILS.inflate(R.layout.farm_fragment_toubao);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // TODO: 2018/8/8 加载数据
        String jsonString = prefs.read("insured_nos");
        QueryBaodanBean[] queryBaodanBeans = gson.fromJson(jsonString, QueryBaodanBean[].class);
        //localInsureList.addAll(list);
        // TODO: 2018/8/8   获取数据
        if (queryBaodanBeans != null) {
            for (QueryBaodanBean queryBaodanBean : queryBaodanBeans) {
                localInsureList.add(queryBaodanBean);
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        FarmAppConfig.isOfflineMode = false;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseHelper = DatabaseHelper.getInstance(getActivity());
        FarmGlobal.model = Model.BUILD.value();
        SharedPreferences pref = FarmAppConfig.getActivity().getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        userid = pref.getInt("uid", 0);

        btn_toubao_add = (ImageView) view.findViewById(R.id.btn_toubao_add);
        btn_toubao_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent add_intent = new Intent(getActivity(), YanBiaoDanActivity.class);
                startActivity(add_intent);
            }
        });
        localInsuredRecyclerView = view.findViewById(R.id.local_insured_recyclerView);
        mRecyclerView = view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setHasFixedSize(true);
        //创建默认的线性LayoutManager
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        search_input_edit = (EditText) view.findViewById(R.id.search_tag_input_edit);
        keywordSearchButton = view.findViewById(R.id.keywordSearchButton);
        keywordSearchButton.setOnClickListener(keywordSearchButtonClickListener);
        btClear = view.findViewById(R.id.bt_clear);


        search_input_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    btClear.setVisibility(View.GONE);
                } else {
                    btClear.setVisibility(View.VISIBLE);
                }
            }
        });
        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_input_edit.setText("");
                btClear.setVisibility(View.GONE);
            }
        });

        localInsuredRecyclerView.setLayoutManager(new LinearLayoutManager(FarmAppConfig.getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    private synchronized void doPostQuery() {
        Map<String, String> map = new HashMap<>();
        map.put(HttpUtils.AppKeyAuthorization, "hopen");
        map.put("Content-Type", "application/x-www-form-urlencoded");
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put("uid", String.valueOf(userid));
        OkHttp3Util.doPost(HttpUtils.SEARCH_YANBIAO, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("TouBaoFra", e.toString());
                AVOSCloudUtils.saveErrorMessage(e, ToubaoFragment.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String string = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(string);
                        int status = jsonObject.getInt("status");
                        String msg = jsonObject.optString("msg");
                        if (status == 1) {
                            ArrayList<BaoDanNetBean> baoDanNetBeans = new ArrayList<>();
                            try {
                                JSONArray data = jsonObject.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject jsonObject1 = data.getJSONObject(i);
                                    String baodanNo = jsonObject1.optString("baodanNo");
                                    String yanBiaoName = jsonObject1.optString("yanBiaoName");
                                    String name = jsonObject1.optString("name");
                                    String cardNo = jsonObject1.optString("cardNo");
                                    String animalType = jsonObject1.optString("animalType");
                                    String createtime = jsonObject1.optString("createtime");
                                    String collectAmount = jsonObject1.optString("collectAmount");
                                    int id = jsonObject1.getInt("id");
                                    PreferencesUtils.saveKeyValue("animalType", animalType, getContext());
                                    String baodanName = jsonObject1.getString("baodanName");
                                    BaoDanNetBean baoDanNetBean = new BaoDanNetBean(baodanNo, yanBiaoName, name, cardNo, createtime, baodanName, collectAmount);
                                    baoDanNetBean.setBaodan_id(id + "");
                                    baoDanNetBeans.add(baoDanNetBean);
                                }
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i("====", baoDanNetBeans.toString() + "");
                                        ToubaoNetAdapter toubaoNetAdapter = new ToubaoNetAdapter(baoDanNetBeans, FarmAppConfig.getActivity(), databaseHelper);
                                        mRecyclerView.setAdapter(toubaoNetAdapter);
                                        toubaoNetAdapter.notifyDataSetChanged();
                                        toubaoNetAdapter.setListner(new ISExist() {
                                            @Override
                                            public void isexist(boolean exist) {
                                                if (exist) {
                                                    Toast.makeText(FarmAppConfig.getActivity(), "保单信息已添加到离线", Toast.LENGTH_SHORT).show();
                                                    List<LocalModelNongxian> localModels = databaseHelper.queryLocalDatas(PreferencesUtils.getStringValue(HttpUtils.user_id, getContext()));
                                                    if (null != localModels) {
                                                        showLocalDataBase(localModels);
                                                    }

                                                } else {
                                                    Toast.makeText(FarmAppConfig.getActivity(), "查询到" + ToubaoFragment.this.userList.size() + "条数据", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                                AVOSCloudUtils.saveErrorMessage(e, ToubaoFragment.class.getSimpleName());
                            }


                        } else {
                            AlertDialogManager.showMessageDialog(getContext(), "tishi", msg, new AlertDialogManager.DialogInterface() {
                                @Override
                                public void onPositive() {

                                }

                                @Override
                                public void onNegative() {

                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private View.OnClickListener keywordSearchButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            keywordSearchButton.setEnabled(false);
            if (("").equals(search_input_edit.getText().toString().trim())) {
                TreeMap<String, String> query = new TreeMap<String, String>();
                query.put("uid", String.valueOf(userid));
                presenter.setparameter(HttpUtils.SEARCH_YANBIAO, query);
            } else {
                TreeMap<String, String> treeMapKeywordSearch = new TreeMap<String, String>();
                treeMapKeywordSearch.put("keyword", search_input_edit.getText().toString().trim());
                treeMapKeywordSearch.put("uid", String.valueOf(userid));
                Log.i("keywordSearch:", search_input_edit.getText().toString().trim() + "");
                presenter.setparameter(HttpUtils.INSUR_DETAIL_QUERY_URL, treeMapKeywordSearch);
            }
        }
    };

    @Override
    public void onPause() {
        Log.i("onPause:", "ToubaoFragment");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.i("onResume:", "ToubaoFragment");
        initView();
        super.onResume();

    }

    private void initView() {
        //显示离线保单
        List<LocalModelNongxian> localModels = databaseHelper.queryLocalDatas(PreferencesUtils.getStringValue(HttpUtils.user_id, getContext()));
        showLocalDataBase(localModels);
        doPostQuery();
    }

    //展示本地缓存的数据
    private void showLocalDataBase(List<LocalModelNongxian> localModels) {
        if (localModels != null && localModels.size() > 0) {
            localInsuredRecyclerView.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp = localInsuredRecyclerView.getLayoutParams();
            lp.height = UIUtils.dp2px(FarmAppConfig.getActivity(), 150 * 1);
            localInsuredRecyclerView.setLayoutParams(lp);
            localInsureAdapter = new ToubaoLocalAdapter(localModels, FarmAppConfig.getActivity(), mHandler, databaseHelper);
            localInsuredRecyclerView.setAdapter(localInsureAdapter);
            //删除后刷新本地数据和网络查询的数据
            localInsureAdapter.setListner(new ISExist() {
                @Override
                public void isexist(boolean exist) {
                    if (exist) {
                        List<LocalModelNongxian> localModels1 = databaseHelper.queryLocalDatas(PreferencesUtils.getStringValue(HttpUtils.user_id, getContext()));
                        Log.i("====deleteanimalAfter", localModels1.size() + "");
                        showLocalDataBase(localModels1);
                        //showNetDataBase(beanList);
                        doPostQuery();
                    }

                }
            });
        } else {
            localInsuredRecyclerView.setVisibility(View.GONE);
        }
    }

    Prefs prefs;
    Gson gson = new Gson();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

    }

    @Override
    public void onDestroy() {
        String jsonString = gson.toJson(localInsureList);
        prefs.write("insured_nos", jsonString);
        super.onDestroy();
    }


    @Override
    public void success(String string) {
        keywordSearchButton.setEnabled(true);
        beanList.clear();
       /* beanList_item = GsonUtils.getBeanList(string, QueryBaodanBean.class);
        beanList.addAll(beanList_item);
        Log.i("===beanList===", beanList.size() + "");
        showNetDataBase(beanList);
        List<LocalModelNongxian> localModels = databaseHelper.queryLocalDatas(PreferencesUtils.getStringValue(HttpUtils.user_id, getActivity()));
        showLocalDataBase(localModels);*/
        Log.i("===beanstring===", string);


        try {
            List<BaoDanNetBean> baoDanNetBeans = new ArrayList<>();
            JSONArray jsonArra = new JSONArray(string);
            for (int i = 0; i < jsonArra.length(); i++) {
                JSONObject jsonObject1 = jsonArra.getJSONObject(i);
                String baodanNo = jsonObject1.optString("baodanNo");
                String yanBiaoName = jsonObject1.optString("yanBiaoName");
                String name = jsonObject1.optString("name");
                String cardNo = jsonObject1.optString("cardNo");
                String animalType = jsonObject1.optString("animalType");
                String createtime = jsonObject1.optString("createtime");
                int id = jsonObject1.getInt("id");
                PreferencesUtils.saveKeyValue("animalType", animalType, getContext());
                String baodanName = jsonObject1.getString("baodanName");
                String collectAmount = jsonObject1.optString("collectAmount");
                BaoDanNetBean baoDanNetBean = new BaoDanNetBean(baodanNo, yanBiaoName, name, cardNo, createtime, baodanName, collectAmount);
                baoDanNetBean.setBaodan_id(id + "");
                baoDanNetBeans.add(baoDanNetBean);
                ToubaoNetAdapter toubaoNetAdapter = new ToubaoNetAdapter(baoDanNetBeans, FarmAppConfig.getActivity(), databaseHelper);
                mRecyclerView.setAdapter(toubaoNetAdapter);
                toubaoNetAdapter.notifyDataSetChanged();
                toubaoNetAdapter.setListner(new ISExist() {
                    @Override
                    public void isexist(boolean exist) {
                        if (exist) {
                            Toast.makeText(FarmAppConfig.getActivity(), "保单信息已添加到离线", Toast.LENGTH_SHORT).show();
                            List<LocalModelNongxian> localModels = databaseHelper.queryLocalDatas(PreferencesUtils.getStringValue(HttpUtils.user_id, getContext()));
                            if (null != localModels) {
                                showLocalDataBase(localModels);
                            }

                        } else {
                            Toast.makeText(FarmAppConfig.getActivity(), "查询到" + ToubaoFragment.this.userList.size() + "条数据", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void error(String string) {
        keywordSearchButton.setEnabled(true);
        Toast.makeText(FarmAppConfig.getActivity(), "保单查询失败 ：" + string, Toast.LENGTH_SHORT).show();
    }
}



