package com.farm.innovation.utils;

import android.util.Log;

/**
 * Created by Luolu on 2018/10/30.
 * InnovationAI
 * luolu@innovationai.cn
 */
public class Rot2AngleType {
    public static int getCowAngleType(float rotX, float rotY) {
        int angleType = 10;
        float angleX, angleY;
        //弧度转角度
        angleX = (float) (rotX * 180 / 3.14);
        angleY = (float) (rotY * 180 / 3.14);

        if (angleX >= 0 && angleX <= 90
                && angleY >= -90 && angleY <= -25) {
            angleType = 1;
        } else if (angleX >= 0 && angleX <= 60
                && angleY >= -9 && angleY <= 9) {
            angleType = 2;
        } else if (angleX >= 0 && angleX <= 90
                && angleY >= 20 && angleY <= 90) {
            angleType = 3;
        } else {
            angleType = 10;
        }

        return angleType;
    }

    public static int getCowAngleTypeLiPei(float rotX, float rotY) {
        int angleType = 10;
        float angleX, angleY;
        //弧度转角度
        angleX = (float) (rotX * 180 / 3.14);
        angleY = (float) (rotY * 180 / 3.14);

        if (angleX >= -30 && angleX <= 90
                && angleY >= -90 && angleY <= -20) {
            angleType = 1;
        } else if (angleX >= -10 && angleX <= 60
                && angleY >= -9 && angleY <= 9) {
            angleType = 2;
        } else if (angleX >= -30 && angleX <= 90
                && angleY >= 18 && angleY <= 90) {
            angleType = 3;
        } else {
            angleType = 10;
        }

        return angleType;
    }

    public static int getDonkeyAngleType(float rotX, float rotY) {
        int angleType = 10;
        float angleX, angleY;
        //弧度转角度
        angleX = (float) (rotX * 180 / 3.14);
        angleY = (float) (rotY * 180 / 3.14);

        if (angleX >= 0 && angleX <= 90
                && angleY >= -90 && angleY <= -40) {
            angleType = 1;
        } else if (angleX >= 0 && angleX <= 90
                && angleY >= -10 && angleY <= 10) {
            angleType = 2;
        } else if (angleX >= 0 && angleX <= 90
                && angleY >= 40 && angleY <= 90) {
            angleType = 3;
        } else {
            angleType = 10;
        }

        return angleType;
    }

    public static int getPigAngleType(float rotX, float rotY) {
        int angleType = 10;
        float angleX, angleY;
        //弧度转角度
        angleX = (float) (rotX * 180 / 3.14);
        angleY = (float) (rotY * 180 / 3.14);

        if (angleX >= 0 && angleX <= 90
                && angleY >= -90 && angleY <= -20) {
            angleType = 1;
        } else if (angleX >= 0 && angleX <= 90
                && angleY >= -15 && angleY <= 15) {
            angleType = 2;
        } else if (angleX >= 0 && angleX <= 90
                && angleY >= 20 && angleY <= 90) {
            angleType = 3;
        } else {
            angleType = 10;
        }

        return angleType;
    }

    public static int getYakAngleType(float rotY) {
        int angleType = 10;
        float angleY;
        //弧度转角度
        angleY = (float) (rotY * 180 / 3.14);

        if (angleY >= -90 && angleY <= -25) {
            angleType = 1;
        } else if (angleY >= -18 && angleY <= 18) {
            angleType = 2;
        } else if (angleY >= 25 && angleY <= 90) {
            angleType = 3;
        } else {
            angleType = 10;
        }

        return angleType;
    }

    public static boolean getYakAngleType(int checkAngle, int[] pointsExists) {
        int angleType = 10;
        if (checkAngle == 1) {
            if (pointsExists[8] + pointsExists[9] > 0) {
                return false;
            }
            if (pointsExists[13] == 0) {
                return false;
            }
        } else if (checkAngle == 2) {
            if (pointsExists[8] + pointsExists[13] > 0) {
                return false;
            }
            return true;
        } else if (checkAngle == 3) {
            boolean isFlag = true;
            if (pointsExists[12] + pointsExists[13] > 0) {
                isFlag = false;
            }

            if (pointsExists[8] == 0) {
                isFlag = false;
            }
            Log.e("Rot2AngleType", "isFlag: " + isFlag + " >>> " + pointsExists[12] + " --- " + pointsExists[13] + "---" + pointsExists[8]);
            return isFlag;

        }
        Log.e("Rot2AngleType", "getYakAngleType: " + pointsExists[12] + "____" + pointsExists[13] + "____" + pointsExists[8]);
        return true;

    }
}
