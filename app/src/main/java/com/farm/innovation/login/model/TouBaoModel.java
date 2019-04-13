package com.farm.innovation.login.model;

import android.os.AsyncTask;
import android.util.Log;

import com.farm.innovation.bean.MultiBaodanBean;
import com.farm.innovation.login.presenter.ITouBaoPresenter;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.HttpRespObject;
import com.farm.innovation.utils.HttpUtils;

import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class TouBaoModel {
    private static String TAG = "TouBaoModel";
    private ITouBaoPresenter iTouBaoPresenter;
    private ToubaoTask mToubaoTask;
    private MultiBaodanBean insurresp;
    private String errStr;

    public TouBaoModel(ITouBaoPresenter iTouBaoPresenter) {
        this.iTouBaoPresenter = iTouBaoPresenter;
    }

    public void setparameter(String url, TreeMap query) {
        if (query != null) {
            mToubaoTask = new ToubaoTask(url, query);
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
            try {
                String response = null;
                FormBody.Builder builder = new FormBody.Builder();
                for (TreeMap.Entry<String, String> entry : mQueryMap.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }
                RequestBody formBody = builder.build();
                response = HttpUtils.post(mUrl, formBody);
                if (response != null) {
                    Log.d(TAG, mUrl + "\nresponse:\n" + response);
                }
                if (HttpUtils.INSUR_DETAIL_QUERY_URL.equalsIgnoreCase(mUrl) || HttpUtils.SEARCH_YANBIAO.equalsIgnoreCase(mUrl)) {
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
                errStr = "请求异常！";
                AVOSCloudUtils.saveErrorMessage(e, TouBaoModel.class.getSimpleName());

                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mToubaoTask = null;
            if (success) {
                if (null != insurresp.data) {
                    iTouBaoPresenter.success(String.valueOf(insurresp.data));
                }
            } else if (!success) {
                //  显示失败
                Log.d(TAG, errStr);
                iTouBaoPresenter.error(errStr);
            }
        }


        @Override
        protected void onCancelled() {
            mToubaoTask = null;
        }
    }
}
