package com.innovation.pig.insurance;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.demo.env.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import innovation.login.RespObject;
import innovation.media.DormNumInfoDialog;
import innovation.upload.UploadHelper;
import innovation.upload.UploadResp;
import innovation.utils.FileUtils;
import innovation.utils.StorageUtils;
import innovation.utils.ZipUtil;


public class ResultDetailFragment extends Fragment {

    // TODO: Rename and change types of parameters
    private String date = new SimpleDateFormat("yyyy年MM月dd日HH时mm分").format(new Date());
    private static final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss",
            Locale.getDefault());
    private Logger mlogger = new Logger();
    private Button exitButton;
    private Button buttonResultUpload;
    private TextView textViewFragmentResult;
    private DormNumInfoDialog dormNumInfoDialog;
    private String strtext;
    private String strtext1;
    private final Handler mCheckUoloadHandler;
    private final Handler mUIHandler;
    private String mGenName;
    private ProgressDialog mProgressDialog;

    private static final int MSG_MYUIHANDLER_Proc_ZIP_IMG = 1;
    private static final int MSG_MYUIHANDLER_UPLOAD_FAILED_IMG = 2;
    private static final int MSG_MYUIHANDLER_UPLOAD_SUCCESS_IMG = 3;


    public ResultDetailFragment() {
        // Required empty public constructor
        HandlerThread handlerThread = new HandlerThread("ResultDetailFragmentHandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        mCheckUoloadHandler = new Handler(looper);
        mUIHandler = new MyUiHandler(Looper.getMainLooper());
        mGenName = mSimpleDateFormat.format(new Date(System.currentTimeMillis()));
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        strtext = getArguments().getString("edttext");
        strtext1 = getArguments().getString("edttext1");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);

        textViewFragmentResult = view.findViewById(R.id.textViewFragmentResult);
        exitButton = view.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(exitButtonClickListener);
        buttonResultUpload = view.findViewById(R.id.buttonResultUpload);
        buttonResultUpload.setOnClickListener(buttonResultUploadClickListener);
        dormNumInfoDialog = new DormNumInfoDialog(getActivity());

//        textViewFragmentResult.append(strtext);
        textViewFragmentResult.append(strtext1);
        textViewFragmentResult.setMovementMethod(new ScrollingMovementMethod());
        showProgressDialog(getActivity());

    }

    private View.OnClickListener exitButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getActivity().finish();
            System.exit(0);
        }
    };

    private View.OnClickListener buttonResultUploadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Toast.makeText(getActivity(),"无可上传！",Toast.LENGTH_SHORT).show();
            mCheckUoloadHandler.post(new Runnable() {

                @Override
                public void run() {
                    File detectDir = new File("/sdcard/innovation/animal/image");
                    File[] files = detectDir.listFiles();
                    if (!detectDir.exists() || files.length == 0) {
                        Toast.makeText(getActivity(), "文件夹为空！", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mUIHandler.sendEmptyMessage(MSG_MYUIHANDLER_Proc_ZIP_IMG);
                        }
                    });

                    File zipFile = new File("/sdcard/innovation/animal/image/",
                            mGenName + ".zip");
                    ZipUtil.zipFiles(files, zipFile);
                    UploadResp imgResp = UploadHelper.uploadImages(getActivity(), 0, zipFile);
                    if (imgResp == null || imgResp.status != RespObject.STATUS_0) {
                        int status = imgResp == null ? -1 : imgResp.status;
                        mlogger.e("upload images failed, status: %d", status);

                        mUIHandler.sendEmptyMessage(MSG_MYUIHANDLER_UPLOAD_FAILED_IMG);

                        return;
                    }
                    mUIHandler.sendEmptyMessage(MSG_MYUIHANDLER_UPLOAD_SUCCESS_IMG);
                    Toast.makeText(getActivity(), "图片上传成功！！", Toast.LENGTH_SHORT).show();
            FileUtils.deleteFile(zipFile);
            FileUtils.deleteFile(StorageUtils.getSrcImageDir(getActivity()));


                }
            });
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public class MyUiHandler extends Handler {


        public MyUiHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            mlogger.i("MyUiHandler message: %d", msg.what);
            String showMessage;
            switch (msg.what) {
                case MSG_MYUIHANDLER_Proc_ZIP_IMG:
                    showMessage = "压缩图片......";
                    mProgressDialog.setMessage(showMessage);
                    mProgressDialog.show();
                    break;
                case MSG_MYUIHANDLER_UPLOAD_FAILED_IMG:
                    showMessage = "上传失败......";
                    mProgressDialog.setMessage(showMessage);
                    // TODO: 12/2/17 try to upload image?
                    break;
                case MSG_MYUIHANDLER_UPLOAD_SUCCESS_IMG:
                    showMessage = "上传成功......";
                    mProgressDialog.setMessage(showMessage);
                    // TODO: 12/2/17 try to upload image?
                    break;
            }
        }

        public void publishProgress(int what) {
            mUIHandler.sendEmptyMessage(what);
        }
    }

    private void showProgressDialog(Activity activity) {
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);//false


        mProgressDialog.setCanceledOnTouchOutside(false);//false
        mProgressDialog.setIcon(R.drawable.ic_launcher);
//        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "确定", mProgClickListener);
        mProgressDialog.setMessage("正在处理......");
//        mProgressDialog.show();
//        Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
//        if (positive != null) {
//            positive.setVisibility(View.GONE);
//        }
    }
}
