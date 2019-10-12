package innovation.biz.iterm;


import android.util.Log;

import innovation.biz.classifier.NewKeyPointsDetectTFlite;
import innovation.utils.Toast;

import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.BuildConfig;
import com.xiangchuang.risks.utils.PigPreferencesUtils;

import innovation.media.Model;
import innovation.utils.FileUtils;
import innovation.utils.Rot2AngleType;

import org.tensorflow.demo.CameraConnectionFragment_pig;
import org.tensorflow.demo.DetectorActivity_pig;
import org.tensorflow.demo.Global;
import org.tensorflow.demo.env.Logger;

import java.io.File;

import static innovation.utils.ImageUtils.compressBitmap;
import static org.tensorflow.demo.DetectorActivity_pig.aNumber;
import static org.tensorflow.demo.DetectorActivity_pig.allNumber;
import static org.tensorflow.demo.DetectorActivity_pig.allTime;
import static org.tensorflow.demo.DetectorActivity_pig.dNumber;
import static org.tensorflow.demo.DetectorActivity_pig.dTime;
import static org.tensorflow.demo.DetectorActivity_pig.kTime;

/**
 * Created by Luolu on 2018/10/30.
 * InnovationAI
 * luolu@innovationai.cn
 */
public class AnimalClassifierResultIterm {
    private final static float ADD_PIC_RATE = 1.6f;
    private static final Logger LOGGER = new Logger(AnimalClassifierResultIterm.class.getName());
    private static int count = 0;


    public static void pigAngleCalculateTFlite(PostureItem postureItem) {
        int type;
        int maxLeft = PigPreferencesUtils.getMaxPics(PigPreferencesUtils.FACE_ANGLE_MAX_LEFT, AppConfig.getAppContext());
        int maxMiddle = PigPreferencesUtils.getMaxPics(PigPreferencesUtils.FACE_ANGLE_MAX_MIDDLE, AppConfig.getAppContext());
        int maxRight = PigPreferencesUtils.getMaxPics(PigPreferencesUtils.FACE_ANGLE_MAX_RIGHT, AppConfig.getAppContext());
        DetectorActivity_pig.AngleTrackType = 10;
        PigFaceKeyPointsItem pigFaceKeyPointsItem = PigFaceKeyPointsItem.getInstance();
        type = Rot2AngleType.getPigAngleType((float) postureItem.rot_x, (float) postureItem.rot_y);
        String imagefilename = "";
        String imageSrcFileName = "";
        String txtfilename = "";
        String oriImageName = "";
        String oriInfoPath = "";
        String oriInfoImageName = "";

        oriInfoPath = Global.mediaPayItem.getOriInfoTXTFileName();
        oriInfoImageName = Global.mediaPayItem.getOriInfoBitmapFileName("");
        imagefilename = Global.mediaPayItem.getBitmapFileName(type);

        // 角度分类模型无效标志 true为无效 反之有效
        final boolean ANGLE_JUDGE_SKIP_FLG = false;
        // 关键点模型无效标志  true为无效  反之有效
        final boolean KEYPOINT_JUDGE_SKIP_FLG = false;

        // 理赔标志
        boolean isLiPei = (Global.model == Model.VERIFY.value());


        boolean addImgFlag = false;

        // 判断图片角度
        if((NewKeyPointsDetectTFlite.pigPredictAngleTypeR == 1 || ANGLE_JUDGE_SKIP_FLG)
                && (NewKeyPointsDetectTFlite.pigKeypointsK1 == true || KEYPOINT_JUDGE_SKIP_FLG)) {
            DetectorActivity_pig.AngleTrackType = 1;
            type = 1;
            // 未达到上限时增加图片
            if(DetectorActivity_pig.type1Count < maxLeft * ADD_PIC_RATE) {
                DetectorActivity_pig.type1Count++;
                if (DetectorActivity_pig.type1Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            }else{
                //左脸达到上限发消息 关闭左脸提示
                CameraConnectionFragment_pig.collectNumberHandler.sendEmptyMessage(3);
            }
        } else if((NewKeyPointsDetectTFlite.pigPredictAngleTypeR == 2|| ANGLE_JUDGE_SKIP_FLG)
                && (NewKeyPointsDetectTFlite.pigKeypointsK2 == true || KEYPOINT_JUDGE_SKIP_FLG)){
            DetectorActivity_pig.AngleTrackType = 2;
            type = 2;
            // 未达到上限时增加图片
            if(DetectorActivity_pig.type2Count < maxMiddle * ADD_PIC_RATE) {
                DetectorActivity_pig.type2Count++;
                if (DetectorActivity_pig.type2Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            }
        } else if((NewKeyPointsDetectTFlite.pigPredictAngleTypeR == 3|| ANGLE_JUDGE_SKIP_FLG)
                && (NewKeyPointsDetectTFlite.pigKeypointsK3 == true || KEYPOINT_JUDGE_SKIP_FLG)) {
            DetectorActivity_pig.AngleTrackType = 3;
            type = 3;
            // 未达到上限时增加图片
            if(DetectorActivity_pig.type3Count < maxRight * ADD_PIC_RATE) {
                DetectorActivity_pig.type3Count++;
                if (DetectorActivity_pig.type3Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            }else{
                //右脸达到上限发消息 关闭右脸提示
                CameraConnectionFragment_pig.collectNumberHandler.sendEmptyMessage(4);
            }
        } else {
            // 啥也不干
            type = 10;
            // 未识别角度
            Log.e("maxMiddle", count + "---maxMiddle: " + maxMiddle);
//            if (count <= maxMiddle) {
//                //保存原图
//                File file = new File(oriImageName);
//                FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file);
//                count++;
//            }
            allTime = System.currentTimeMillis() - allTime;
            FileUtils.saveInfoToTxtFile(oriInfoPath,
                    imagefilename.substring(imagefilename.lastIndexOf("/") + 1) +
                            "；totalNum：" + allNumber +
                            "；DetectTime：" + dTime +
                            "；AngleTime&KeypointTime：" + kTime +
                            "；totalTime：" + allTime);
            DetectorActivity_pig.resetParameter();
            return;
        }


        imagefilename = Global.mediaPayItem.getBitmapFileName(type);
        txtfilename = Global.mediaPayItem.getTxtFileNme(type);
        oriImageName = Global.mediaPayItem.getOriBitmapFileName();

        //保存角度信息
        String contenType = imagefilename.substring(imagefilename.lastIndexOf("/") + 1);
        contenType += ":";
        contenType += "rot_x = " + postureItem.rot_x + "; ";
        contenType += "rot_y = " + postureItem.rot_y + "; ";
        contenType += "rot_z = " + postureItem.rot_z + "; ";
        contenType += "box_x0 = " + postureItem.modelX0 + "; ";
        contenType += "box_y0 = " + postureItem.modelY0 + "; ";
        contenType += "box_x1 = " + postureItem.modelX1 + "; ";
        contenType += "box_y1 = " + postureItem.modelY1 + "; ";
        contenType += "score = " + postureItem.modelDetectedScore + "; ";
        contenType += "point0 = " + pigFaceKeyPointsItem.getPointFloat0().toString() + "; ";
        contenType += "point1 = " + pigFaceKeyPointsItem.getPointFloat1().toString() + "; ";
        contenType += "point2 = " + pigFaceKeyPointsItem.getPointFloat2().toString() + "; ";
        contenType += "point3 = " + pigFaceKeyPointsItem.getPointFloat3().toString() + "; ";
        contenType += "point4 = " + pigFaceKeyPointsItem.getPointFloat4().toString() + "; ";
        contenType += "point5 = " + pigFaceKeyPointsItem.getPointFloat5().toString() + "; ";
        contenType += "point6 = " + pigFaceKeyPointsItem.getPointFloat6().toString() + "; ";
        contenType += "point7 = " + pigFaceKeyPointsItem.getPointFloat7().toString() + "; ";
        contenType += "point8 = " + pigFaceKeyPointsItem.getPointFloat8().toString() + "; ";
        contenType += "point9 = " + pigFaceKeyPointsItem.getPointFloat9().toString() + "; ";
        contenType += "point10 = " + pigFaceKeyPointsItem.getPointFloat10().toString() + "; ";
        LOGGER.i("pigFaceKeyPointsItem:" + pigFaceKeyPointsItem.toString());

        // 符合角度且未达到上限时增加图片
        if (addImgFlag) {
            Log.e("img_path", "imagefilename: "+imagefilename);

            FileUtils.saveInfoToTxtFile(txtfilename, contenType + "angle:" + type);

            allTime = System.currentTimeMillis() - allTime;
            Log.e("allTime", "allTime==="+allTime);
            FileUtils.saveInfoToTxtFile(oriInfoPath,
                    imagefilename.substring(imagefilename.lastIndexOf("/") + 1) +
                            "；totalNum：" + allNumber +
                            "；DetectTime：" + dTime +
                            "；AngleTime&KeypointTime：" + kTime +
                            "；totalTime：" + allTime);
            DetectorActivity_pig.resetParameter();

            //保存图片
            File tmpimagefile = new File(imagefilename);
            // File tmpImageSrcFileName = new File(imageSrcFileName);
            // save clip scale bitmap
            FileUtils.saveBitmapToFile(postureItem.clipBitmap, tmpimagefile);

            //保存原图
//            File file = new File(oriImageName);
//            FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file);

            // 保存src图片
            // FileUtils.saveBitmapToFile(postureItem.srcBitmap, tmpImageSrcFileName);
            DetectorActivity_pig.tracker.getCountOfCurrentImage(DetectorActivity_pig.type1Count,
                    DetectorActivity_pig.type2Count, DetectorActivity_pig.type3Count);
        }

        if (DetectorActivity_pig.type1Count >= maxLeft && DetectorActivity_pig.type2Count >= maxMiddle && DetectorActivity_pig.type3Count >= maxRight) {
            AppConfig.debugNub = -1;
            FileUtils.saveInfoToTxtFile(oriInfoPath,
                    "totalNum：" + allNumber +
                            "；Detect_Success_Num：" + dNumber +
                            "；Angle&Keypoint_Success_Num：" + aNumber +
                            "；total_Success_Num：" + (DetectorActivity_pig.type1Count+ DetectorActivity_pig.type2Count+ DetectorActivity_pig.type3Count));
            FileUtils.saveInfoToTxtFile(oriInfoPath,"phoneModel："+ android.os.Build.BRAND + " "+android.os.Build.MODEL + "\r\nversion：" + AppConfig.version);
            CameraConnectionFragment_pig.collectNumberHandler.sendEmptyMessage(1);
            if (BuildConfig.DEBUG){
                Toast.makeText(AppConfig.getAppContext(), "猪脸数据采集完成!!!", Toast.LENGTH_LONG).show();
            }
        }
    }

}

