package com.farm.innovation.biz.iterm;

import android.util.Log;
import android.widget.Toast;


import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.biz.classifier.CowKeyPointsDetectTFlite;
import com.farm.innovation.biz.classifier.CowRotationPrediction;
import com.farm.innovation.biz.classifier.DonkeyKeyPointsDetectTFlite;
import com.farm.innovation.biz.classifier.DonkeyRotationPrediction;
import com.farm.innovation.biz.classifier.PigKeyPointsDetectTFlite;
import com.farm.innovation.biz.classifier.PigRotationPrediction;
import com.farm.innovation.biz.classifier.YakKeyPointsDetectTFlite;
import com.farm.innovation.biz.classifier.YakRotationPrediction;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.FileUtils;
import com.farm.innovation.utils.Rot2AngleType;

import org.tensorflow.demo.FarmCameraConnectionFragment;
import org.tensorflow.demo.FarmDetectorActivity;
import org.tensorflow.demo.FarmGlobal;
import org.tensorflow.demo.env.Logger;

import java.io.File;

import static com.farm.innovation.utils.ImageUtils.compressBitmap;
import static org.tensorflow.demo.FarmDetectorActivity.aNumber;
import static org.tensorflow.demo.FarmDetectorActivity.aTime;
import static org.tensorflow.demo.FarmDetectorActivity.allNumber;
import static org.tensorflow.demo.FarmDetectorActivity.allTime;
import static org.tensorflow.demo.FarmDetectorActivity.dNumber;
import static org.tensorflow.demo.FarmDetectorActivity.dTime;
import static org.tensorflow.demo.FarmDetectorActivity.kNumber;
import static org.tensorflow.demo.FarmDetectorActivity.kTime;

/**
 * Created by Luolu on 2018/10/30.
 * InnovationAI
 * luolu@innovationai.cn
 */
public class AnimalClassifierResultIterm {
    private final static float ADD_PIC_RATE = 1.6f;
    private static final Logger LOGGER = new Logger(AnimalClassifierResultIterm.class.getName());
    private static int count = 0;


    public static void donkeyAngleCalculateTFlite(PostureItem postureItem) {
        int type;
        int maxLeft = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_LEFT, FarmAppConfig.getApplication());
        int maxMiddle = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_MIDDLE, FarmAppConfig.getApplication());
        int maxRight = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_RIGHT, FarmAppConfig.getApplication());
        FarmDetectorActivity.AngleTrackType = 10;
        DonkeyFaceKeyPointsItem donkeyFaceKeyPointsItem = DonkeyFaceKeyPointsItem.getInstance();
        type = Rot2AngleType.getDonkeyAngleType((float) postureItem.rot_x, (float) postureItem.rot_y);

        String imagefilename = "";
        String imageSrcFileName = "";
        String txtfilename = "";
        String oriImageName = "";
        String oriInfoPath = "";
        String oriInfoImageName = "";

        boolean isLiPei = (FarmGlobal.model == Model.VERIFY.value());

        if (FarmGlobal.model == Model.BUILD.value()) {
            imagefilename = FarmGlobal.mediaInsureItem.getBitmapFileName(type);
            imageSrcFileName = FarmGlobal.mediaInsureItem.getSrcBitmapFileName(type);
            txtfilename = FarmGlobal.mediaInsureItem.getTxtFileNme(type);
            oriImageName = FarmGlobal.mediaInsureItem.getOriBitmapFileName();
            oriInfoPath = FarmGlobal.mediaInsureItem.getOriInfoTXTFileName();
            oriInfoImageName = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("");
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            oriImageName = FarmGlobal.mediaPayItem.getOriBitmapFileName();
            oriInfoPath = FarmGlobal.mediaPayItem.getOriInfoTXTFileName();
            imagefilename = FarmGlobal.mediaPayItem.getBitmapFileName(type);
            txtfilename = FarmGlobal.mediaPayItem.getTxtFileNme(type);
            oriInfoImageName = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("");

        }

        boolean addImgFlag = false;
        // 判断图片角度
        if (DonkeyRotationPrediction.donkeyPredictAngleType == 1 || isLiPei
                && DonkeyKeyPointsDetectTFlite.donkeyKeypointsK1 == true) {
            FarmDetectorActivity.AngleTrackType = 1;
            type = 1;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type1Count < maxLeft * ADD_PIC_RATE) {
                FarmDetectorActivity.type1Count++;
                if (FarmDetectorActivity.type1Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            } else {
                //左脸达到上限发消息 关闭左脸提示
                FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(3);
            }
        } else if (DonkeyRotationPrediction.donkeyPredictAngleType == 2 || isLiPei
                && DonkeyKeyPointsDetectTFlite.donkeyKeypointsK2 == true) {
            FarmDetectorActivity.AngleTrackType = 2;
            type = 2;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type2Count < maxMiddle * ADD_PIC_RATE) {
                FarmDetectorActivity.type2Count++;
                if (FarmDetectorActivity.type2Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            }
        } else if (DonkeyRotationPrediction.donkeyPredictAngleType == 3 || isLiPei
                && DonkeyKeyPointsDetectTFlite.donkeyKeypointsK3 == true) {
            FarmDetectorActivity.AngleTrackType = 3;
            type = 3;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type3Count < maxRight * ADD_PIC_RATE) {
                FarmDetectorActivity.type3Count++;
                if (FarmDetectorActivity.type3Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            } else {
                //右脸达到上限发消息 关闭右脸提示
                FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(4);
            }
        } else {
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
                            "；AngleTime：" + aTime +
                            "；KeypointTime：" + kTime +
                            "；totalTime：" + allTime);
            FarmDetectorActivity.resetParameter();
            return;
        }

        //save model outputs
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
        contenType += "point0 = " + donkeyFaceKeyPointsItem.getPointFloat0().toString() + "; ";
        contenType += "point1 = " + donkeyFaceKeyPointsItem.getPointFloat1().toString() + "; ";
        contenType += "point2 = " + donkeyFaceKeyPointsItem.getPointFloat2().toString() + "; ";
        contenType += "point3 = " + donkeyFaceKeyPointsItem.getPointFloat3().toString() + "; ";
        contenType += "point4 = " + donkeyFaceKeyPointsItem.getPointFloat4().toString() + "; ";
        contenType += "point5 = " + donkeyFaceKeyPointsItem.getPointFloat5().toString() + "; ";
        contenType += "point6 = " + donkeyFaceKeyPointsItem.getPointFloat6().toString() + "; ";
        contenType += "point7 = " + donkeyFaceKeyPointsItem.getPointFloat7().toString() + "; ";
        contenType += "point8 = " + donkeyFaceKeyPointsItem.getPointFloat8().toString() + "; ";
        contenType += "point9 = " + donkeyFaceKeyPointsItem.getPointFloat9().toString() + "; ";
        contenType += "point10 = " + donkeyFaceKeyPointsItem.getPointFloat10().toString() + "; ";

        LOGGER.i("donkeyFaceKeyPointsItem:" + donkeyFaceKeyPointsItem.toString());

        // 符合角度且未达到上限时增加图片
        if (addImgFlag) {
            FileUtils.saveInfoToTxtFile(txtfilename, contenType + "angle:" + type);

            allTime = System.currentTimeMillis() - allTime;
            Log.e("allTime", "allTime==="+allTime);
            FileUtils.saveInfoToTxtFile(oriInfoPath,
                    imagefilename.substring(imagefilename.lastIndexOf("/") + 1) +
                            "；totalNum：" + allNumber +
                            "；DetectTime：" + dTime +
                            "；AngleTime：" + aTime +
                            "；KeypointTime：" + kTime +
                            "；totalTime：" + allTime);
            FarmDetectorActivity.resetParameter();

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
            FarmDetectorActivity.tracker.getCountOfCurrentImage(FarmDetectorActivity.type1Count,
                    FarmDetectorActivity.type2Count, FarmDetectorActivity.type3Count);
        }
        if (FarmDetectorActivity.type1Count >= maxLeft && FarmDetectorActivity.type2Count >= maxMiddle && FarmDetectorActivity.type3Count >= maxRight) {
            FarmAppConfig.debugNub = -1;
            FileUtils.saveInfoToTxtFile(oriInfoPath,
                    "totalNum：" + allNumber +
                            "；Detect_Success_Num：" + dNumber +
                            "；Angle_Success_Num：" + aNumber +
                            "；Keypoint_Success_Num：" + kNumber +
                            "；total_Success_Num：" + (FarmDetectorActivity.type1Count+ FarmDetectorActivity.type2Count+ FarmDetectorActivity.type3Count));
            FileUtils.saveInfoToTxtFile(oriInfoPath,"phoneModel："+ android.os.Build.BRAND + " "+android.os.Build.MODEL + "\r\nversion：" + FarmAppConfig.version);

            FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(1);
        }
    }

    public static void pigAngleCalculateTFlite(PostureItem postureItem) {
        int type;
        int maxLeft = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_LEFT, FarmAppConfig.getApplication());
        int maxMiddle = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_MIDDLE, FarmAppConfig.getApplication());
        int maxRight = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_RIGHT, FarmAppConfig.getApplication());
        FarmDetectorActivity.AngleTrackType = 10;
        PigFaceKeyPointsItem pigFaceKeyPointsItem = PigFaceKeyPointsItem.getInstance();
        type = Rot2AngleType.getPigAngleType((float) postureItem.rot_x, (float) postureItem.rot_y);

        String imagefilename = "";
        String imageSrcFileName = "";
        String txtfilename = "";
        String oriImageName = "";
        String oriInfoPath = "";
        String oriInfoImageName = "";

        if (FarmGlobal.model == Model.BUILD.value()) {
            imagefilename = FarmGlobal.mediaInsureItem.getBitmapFileName(type);
            imageSrcFileName = FarmGlobal.mediaInsureItem.getSrcBitmapFileName(type);
            txtfilename = FarmGlobal.mediaInsureItem.getTxtFileNme(type);
            oriImageName = FarmGlobal.mediaInsureItem.getOriBitmapFileName();
            oriInfoPath = FarmGlobal.mediaInsureItem.getOriInfoTXTFileName();
            oriInfoImageName = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("");
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            oriImageName = FarmGlobal.mediaPayItem.getOriBitmapFileName();
            oriInfoPath = FarmGlobal.mediaPayItem.getOriInfoTXTFileName();
            imagefilename = FarmGlobal.mediaPayItem.getBitmapFileName(type);
            txtfilename = FarmGlobal.mediaPayItem.getTxtFileNme(type);
            oriInfoImageName = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("");
        }

        boolean isLiPei = (FarmGlobal.model == Model.VERIFY.value());


        boolean addImgFlag = false;

        // 判断图片角度
        if (PigRotationPrediction.pigPredictAngleType == 1 || isLiPei
                && PigKeyPointsDetectTFlite.pigKeypointsK1 == true) {
            FarmDetectorActivity.AngleTrackType = 1;
            type = 1;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type1Count < maxLeft * ADD_PIC_RATE) {
                FarmDetectorActivity.type1Count++;
                if (FarmDetectorActivity.type1Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            } else {
                //左脸达到上限发消息 关闭左脸提示
                FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(3);
            }
        } else if (PigRotationPrediction.pigPredictAngleType == 2 || isLiPei
                && PigKeyPointsDetectTFlite.pigKeypointsK2 == true) {
            FarmDetectorActivity.AngleTrackType = 2;
            type = 2;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type2Count < maxMiddle * ADD_PIC_RATE) {
                FarmDetectorActivity.type2Count++;
                if (FarmDetectorActivity.type2Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            }
        } else if (PigRotationPrediction.pigPredictAngleType == 3 || isLiPei
                && PigKeyPointsDetectTFlite.pigKeypointsK3 == true) {
            FarmDetectorActivity.AngleTrackType = 3;
            type = 3;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type3Count < maxRight * ADD_PIC_RATE) {
                FarmDetectorActivity.type3Count++;
                if (FarmDetectorActivity.type3Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            } else {
                //右脸达到上限发消息 关闭右脸提示
                FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(4);
            }
        } else {
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
                            "；AngleTime：" + aTime +
                            "；KeypointTime：" + kTime +
                            "；totalTime：" + allTime);
            FarmDetectorActivity.resetParameter();
            return;
        }

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

            Log.e("imagefilename", "imagefilename: " + imagefilename);

            FileUtils.saveInfoToTxtFile(txtfilename, contenType + "angle:" + type);

            allTime = System.currentTimeMillis() - allTime;
            Log.e("allTime", "allTime==="+allTime);
            FileUtils.saveInfoToTxtFile(oriInfoPath,
                    imagefilename.substring(imagefilename.lastIndexOf("/") + 1) +
                            "；totalNum：" + allNumber +
                            "；DetectTime：" + dTime +
                            "；AngleTime：" + aTime +
                            "；KeypointTime：" + kTime +
                            "；totalTime：" + allTime);
            FarmDetectorActivity.resetParameter();

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
            FarmDetectorActivity.tracker.getCountOfCurrentImage(FarmDetectorActivity.type1Count,
                    FarmDetectorActivity.type2Count, FarmDetectorActivity.type3Count);
        }

        if (FarmDetectorActivity.type1Count >= maxLeft && FarmDetectorActivity.type2Count >= maxMiddle && FarmDetectorActivity.type3Count >= maxRight) {
            FarmAppConfig.debugNub = -1;
            FileUtils.saveInfoToTxtFile(oriInfoPath,
                    "totalNum：" + allNumber +
                            "；Detect_Success_Num：" + dNumber +
                            "；Angle_Success_Num：" + aNumber +
                            "；Keypoint_Success_Num：" + kNumber +
                            "；total_Success_Num：" + (FarmDetectorActivity.type1Count+ FarmDetectorActivity.type2Count+ FarmDetectorActivity.type3Count));
            FileUtils.saveInfoToTxtFile(oriInfoPath,"phoneModel："+ android.os.Build.BRAND + " "+android.os.Build.MODEL + "\r\nversion：" + FarmAppConfig.version);
            FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(1);
            if (FarmAppConfig.isApkDebugable())
                Toast.makeText(FarmAppConfig.getApplication(), "猪脸数据采集完成!!!", Toast.LENGTH_LONG).show();
        }
    }

    public static void cowAngleCalculateTFlite(PostureItem postureItem) {

        int type;
        int maxLeft = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_LEFT, FarmAppConfig.getApplication());
        int maxMiddle = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_MIDDLE, FarmAppConfig.getApplication());
        int maxRight = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_RIGHT, FarmAppConfig.getApplication());
        FarmDetectorActivity.AngleTrackType = 10;
        CowFaceKeyPointsItem cowFaceKeyPointsItem = CowFaceKeyPointsItem.getInstance();
        type = Rot2AngleType.getCowAngleType((float) postureItem.rot_x, (float) postureItem.rot_y);

        String imagefilename = "";
        String imageSrcFileName = "";
        String txtfilename = "";
        String oriImageName = "";
        String oriInfoPath = "";
        String oriInfoImageName = "";

        boolean isLiPei = (FarmGlobal.model == Model.VERIFY.value());

        if (FarmGlobal.model == Model.BUILD.value()) {
            imagefilename = FarmGlobal.mediaInsureItem.getBitmapFileName(type);
            imageSrcFileName = FarmGlobal.mediaInsureItem.getSrcBitmapFileName(type);
            txtfilename = FarmGlobal.mediaInsureItem.getTxtFileNme(type);
            oriImageName = FarmGlobal.mediaInsureItem.getOriBitmapFileName();
            oriInfoPath = FarmGlobal.mediaInsureItem.getOriInfoTXTFileName();
            oriInfoImageName = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("");
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            oriImageName = FarmGlobal.mediaPayItem.getOriBitmapFileName();
            oriInfoPath = FarmGlobal.mediaPayItem.getOriInfoTXTFileName();
            imagefilename = FarmGlobal.mediaPayItem.getBitmapFileName(type);
            txtfilename = FarmGlobal.mediaPayItem.getTxtFileNme(type);
            oriInfoImageName = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("");

        }

        boolean addImgFlag = false;
        // 判断图片角度
        if ((CowRotationPrediction.cowRotationPredictSuccessR1 || isLiPei)
                && CowKeyPointsDetectTFlite.cowKeypointsDetectedK1) {
            FarmDetectorActivity.AngleTrackType = 1;
            type = 1;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type1Count < maxLeft * ADD_PIC_RATE) {
                FarmDetectorActivity.type1Count++;
                if (FarmDetectorActivity.type1Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            } else {
                //左脸达到上限发消息 关闭左脸提示
                FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(3);
            }
        } else if ((CowRotationPrediction.cowRotationPredictSuccessR2 || isLiPei)
                && CowKeyPointsDetectTFlite.cowKeypointsDetectedK2) {
            FarmDetectorActivity.AngleTrackType = 2;
            type = 2;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type2Count < maxMiddle * ADD_PIC_RATE) {
                FarmDetectorActivity.type2Count++;
                if (FarmDetectorActivity.type2Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            }
        } else if ((CowRotationPrediction.cowRotationPredictSuccessR3 || isLiPei)
                && CowKeyPointsDetectTFlite.cowKeypointsDetectedK3) {
            FarmDetectorActivity.AngleTrackType = 3;
            type = 3;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type3Count < maxRight * ADD_PIC_RATE) {
                FarmDetectorActivity.type3Count++;
                if (FarmDetectorActivity.type3Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            } else {
                //右脸达到上限发消息 关闭右脸提示
                FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(4);
            }
        } else {
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
                            "；AngleTime：" + aTime +
                            "；KeypointTime：" + kTime +
                            "；totalTime：" + allTime);
            FarmDetectorActivity.resetParameter();
            return;
        }


        //save model outputs
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
        contenType += "point0 = " + cowFaceKeyPointsItem.getPointFloat0().toString() + "; ";
        contenType += "point1 = " + cowFaceKeyPointsItem.getPointFloat1().toString() + "; ";
        contenType += "point2 = " + cowFaceKeyPointsItem.getPointFloat2().toString() + "; ";
        contenType += "point3 = " + cowFaceKeyPointsItem.getPointFloat3().toString() + "; ";
        contenType += "point4 = " + cowFaceKeyPointsItem.getPointFloat4().toString() + "; ";
        contenType += "point5 = " + cowFaceKeyPointsItem.getPointFloat5().toString() + "; ";
        contenType += "point6 = " + cowFaceKeyPointsItem.getPointFloat6().toString() + "; ";
        contenType += "point7 = " + cowFaceKeyPointsItem.getPointFloat7().toString() + "; ";
        contenType += "point8 = " + cowFaceKeyPointsItem.getPointFloat8().toString() + "; ";
        contenType += "point9 = " + cowFaceKeyPointsItem.getPointFloat9().toString() + "; ";
        contenType += "point10 = " + cowFaceKeyPointsItem.getPointFloat10().toString() + "; ";
        contenType += "point11 = " + cowFaceKeyPointsItem.getPointFloat11().toString() + "; ";
        contenType += "point12 = " + cowFaceKeyPointsItem.getPointFloat12().toString() + "; ";

        LOGGER.i("yakFaceKeyPointsItem:" + cowFaceKeyPointsItem.toString());


        // 符合角度且未达到上限时增加图片
        if (addImgFlag) {
            //保存图片信息写入txt文件
            FileUtils.saveInfoToTxtFile(txtfilename, contenType + "angle:" + type);
            Log.e("imagefilename", "imagefilename: " + imagefilename);

            allTime = System.currentTimeMillis() - allTime;
            Log.e("allTime", "allTime==="+allTime);
            FileUtils.saveInfoToTxtFile(oriInfoPath,
                    imagefilename.substring(imagefilename.lastIndexOf("/") + 1) +
                            "；totalNum：" + allNumber +
                            "；DetectTime：" + dTime +
                            "；AngleTime：" + aTime +
                            "；KeypointTime：" + kTime +
                            "；totalTime：" + allTime);
            FarmDetectorActivity.resetParameter();
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
            FarmDetectorActivity.tracker.getCountOfCurrentImage(FarmDetectorActivity.type1Count,
                    FarmDetectorActivity.type2Count, FarmDetectorActivity.type3Count);
        }

        if (FarmDetectorActivity.type1Count >= maxLeft && FarmDetectorActivity.type2Count >= maxMiddle && FarmDetectorActivity.type3Count >= maxRight) {
            FarmAppConfig.debugNub = -1;
            FileUtils.saveInfoToTxtFile(oriInfoPath,
                    "totalNum：" + allNumber +
                    "；Detect_Success_Num：" + dNumber +
                    "；Angle_Success_Num：" + aNumber +
                    "；Keypoint_Success_Num：" + kNumber +
                    "；total_Success_Num：" + (FarmDetectorActivity.type1Count+ FarmDetectorActivity.type2Count+ FarmDetectorActivity.type3Count));
            FileUtils.saveInfoToTxtFile(oriInfoPath,"phoneModel："+ android.os.Build.BRAND + " "+android.os.Build.MODEL + "\r\nversion：" + FarmAppConfig.version);
            FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(1);
        }
    }

    public static void yakAngleCalculateTFlite(PostureItem postureItem) {

        int type;
        int maxLeft = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_LEFT, FarmAppConfig.getApplication());
        int maxMiddle = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_MIDDLE, FarmAppConfig.getApplication());
        int maxRight = FarmerPreferencesUtils.getMaxPics(FarmerPreferencesUtils.FACE_ANGLE_MAX_RIGHT, FarmAppConfig.getApplication());
        FarmDetectorActivity.AngleTrackType = 10;
        YakFaceKeyPointsItem yakFaceKeyPointsItem = YakFaceKeyPointsItem.getInstance();
        type = Rot2AngleType.getYakAngleType((float) postureItem.rot_y);

        String imagefilename = "";
        String imageSrcFileName = "";
        String txtfilename = "";
        String oriImageName = "";
        String oriInfoPath = "";
        String oriInfoImageName = "";

        if (FarmGlobal.model == Model.BUILD.value()) {
            imagefilename = FarmGlobal.mediaInsureItem.getBitmapFileName(type);
            imageSrcFileName = FarmGlobal.mediaInsureItem.getSrcBitmapFileName(type);
            txtfilename = FarmGlobal.mediaInsureItem.getTxtFileNme(type);
            oriImageName = FarmGlobal.mediaInsureItem.getOriBitmapFileName();
            oriInfoPath = FarmGlobal.mediaInsureItem.getOriInfoTXTFileName();
            oriInfoImageName = FarmGlobal.mediaInsureItem.getOriInfoBitmapFileName("");
        } else if (FarmGlobal.model == Model.VERIFY.value()) {
            oriImageName = FarmGlobal.mediaPayItem.getOriBitmapFileName();
            oriInfoPath = FarmGlobal.mediaPayItem.getOriInfoTXTFileName();
            imagefilename = FarmGlobal.mediaPayItem.getBitmapFileName(type);
            txtfilename = FarmGlobal.mediaPayItem.getTxtFileNme(type);
            oriInfoImageName = FarmGlobal.mediaPayItem.getOriInfoBitmapFileName("");
        }

        // 关键点模型无效标志  true为无效  反之有效
        boolean KEYPOINT_JUDGE_SKIP_FLG = true;

        int animalType = FarmerPreferencesUtils.getAnimalType(FarmAppConfig.getApplication());

        boolean isLiPei = false;
        //牦牛只判断角度
        if (animalType != 4) {
            isLiPei = (FarmGlobal.model == Model.VERIFY.value());
            KEYPOINT_JUDGE_SKIP_FLG = false;
        }

        boolean addImgFlag = false;
        // 判断图片角度
        if ((YakRotationPrediction.yakRotationPredictSuccessR1 || isLiPei)
                && (YakKeyPointsDetectTFlite.yakKeypointsDetectedK1 || KEYPOINT_JUDGE_SKIP_FLG)) {
            FarmDetectorActivity.AngleTrackType = 1;
            type = 1;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type1Count < maxLeft * ADD_PIC_RATE) {
                FarmDetectorActivity.type1Count++;
                if (FarmDetectorActivity.type1Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            } else {
                //左脸达到上限发消息 关闭左脸提示
                FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(3);
            }
        } else if ((YakRotationPrediction.yakRotationPredictSuccessR2 || isLiPei)
                && (YakKeyPointsDetectTFlite.yakKeypointsDetectedK2 || KEYPOINT_JUDGE_SKIP_FLG)) {
            FarmDetectorActivity.AngleTrackType = 2;
            type = 2;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type2Count < maxMiddle * ADD_PIC_RATE) {
                FarmDetectorActivity.type2Count++;
                if (FarmDetectorActivity.type2Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            }
        } else if ((YakRotationPrediction.yakRotationPredictSuccessR3 || isLiPei)
                && (YakKeyPointsDetectTFlite.yakKeypointsDetectedK3 || KEYPOINT_JUDGE_SKIP_FLG)) {
            FarmDetectorActivity.AngleTrackType = 3;
            type = 3;
            // 未达到上限时增加图片
            if (FarmDetectorActivity.type3Count < maxRight * ADD_PIC_RATE) {
                FarmDetectorActivity.type3Count++;
                if (FarmDetectorActivity.type3Count < 3) {
                    //保存原图
                    File file1 = new File(oriInfoImageName);
                    FileUtils.saveBitmapToFile(compressBitmap(postureItem.oriBitmap), file1);
                }
                addImgFlag = true;
            } else {
                //右脸达到上限发消息 关闭右脸提示
                FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(4);
            }
        } else {
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
                            "；AngleTime：" + aTime +
                            "；KeypointTime：" + kTime +
                            "；totalTime：" + allTime);
            FarmDetectorActivity.resetParameter();
            return;
        }

        //save model outputs
        String contenType = imagefilename.substring(imagefilename.lastIndexOf("/") + 1);
        contenType += ":";
        contenType += "rot_y = " + postureItem.rot_y + "; ";
        contenType += "box_x0 = " + postureItem.modelX0 + "; ";
        contenType += "box_y0 = " + postureItem.modelY0 + "; ";
        contenType += "box_x1 = " + postureItem.modelX1 + "; ";
        contenType += "box_y1 = " + postureItem.modelY1 + "; ";
        contenType += "score = " + postureItem.modelDetectedScore + "; ";
        contenType += "point0 = " + yakFaceKeyPointsItem.getPointFloat0().toString() + "; ";
        contenType += "point1 = " + yakFaceKeyPointsItem.getPointFloat1().toString() + "; ";
        contenType += "point2 = " + yakFaceKeyPointsItem.getPointFloat2().toString() + "; ";
        contenType += "point3 = " + yakFaceKeyPointsItem.getPointFloat3().toString() + "; ";
        contenType += "point4 = " + yakFaceKeyPointsItem.getPointFloat4().toString() + "; ";
        contenType += "point5 = " + yakFaceKeyPointsItem.getPointFloat5().toString() + "; ";
        contenType += "point6 = " + yakFaceKeyPointsItem.getPointFloat6().toString() + "; ";
        contenType += "point7 = " + yakFaceKeyPointsItem.getPointFloat7().toString() + "; ";
        contenType += "point8 = " + yakFaceKeyPointsItem.getPointFloat8().toString() + "; ";
        contenType += "point9 = " + yakFaceKeyPointsItem.getPointFloat9().toString() + "; ";
        contenType += "point10 = " + yakFaceKeyPointsItem.getPointFloat10().toString() + "; ";
        contenType += "point11 = " + yakFaceKeyPointsItem.getPointFloat11().toString() + "; ";
        contenType += "point12 = " + yakFaceKeyPointsItem.getPointFloat12().toString() + "; ";

        LOGGER.i("yakFaceKeyPointsItem:" + yakFaceKeyPointsItem.toString());


        // 符合角度且未达到上限时增加图片
        if (addImgFlag) {
            //保存图片信息写入txt文件
            FileUtils.saveInfoToTxtFile(txtfilename, contenType + "angle:" + type);
            Log.e("imagefilename", "imagefilename: " + imagefilename);

            allTime = System.currentTimeMillis() - allTime;
            Log.e("allTime", "allTime==="+allTime);
            FileUtils.saveInfoToTxtFile(oriInfoPath,
                    imagefilename.substring(imagefilename.lastIndexOf("/") + 1) +
                            "；totalNum：" + allNumber +
                            "；DetectTime：" + dTime +
                            "；AngleTime：" + aTime +
                            "；KeypointTime：" + kTime +
                            "；totalTime：" + allTime);
            FarmDetectorActivity.resetParameter();

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
            FarmDetectorActivity.tracker.getCountOfCurrentImage(FarmDetectorActivity.type1Count,
                    FarmDetectorActivity.type2Count, FarmDetectorActivity.type3Count);
        }

        if (FarmDetectorActivity.type1Count >= maxLeft && FarmDetectorActivity.type2Count >= maxMiddle && FarmDetectorActivity.type3Count >= maxRight) {
            FarmAppConfig.debugNub = -1;
            FileUtils.saveInfoToTxtFile(oriInfoPath,
                    "totalNum：" + allNumber +
                            "；Detect_Success_Num：" + dNumber +
                            "；Angle_Success_Num：" + aNumber +
                            "；Keypoint_Success_Num：" + kNumber +
                            "；total_Success_Num：" + (FarmDetectorActivity.type1Count+ FarmDetectorActivity.type2Count+ FarmDetectorActivity.type3Count));
            FileUtils.saveInfoToTxtFile(oriInfoPath,"phoneModel："+ android.os.Build.BRAND + " "+android.os.Build.MODEL + "\r\nversion：" + FarmAppConfig.version);

            FarmCameraConnectionFragment.collectNumberHandler.sendEmptyMessage(1);
        }
    }


}
