package com.xiangchuang.risks.model.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * 未完成处理bean
 */
public class UncompletedBean implements Parcelable {

        private String step;
        private String innocuousId;//生成处理id
        private currentStep currentStep;
        private List<lipeiInfo> lipeiInfos;

        public String getStep() {
            return step;
        }

        public void setStep(String step) {
            this.step = step;
        }

        public String getInnocuousId() {
            return innocuousId;
        }

        public void setInnocuousId(String innocuousId) {
            this.innocuousId = innocuousId;
        }

        public currentStep getCurrentStep() {
            return currentStep;
        }

        public void setCurrentStep(currentStep currentStep) {
            this.currentStep = currentStep;
        }

        public List<lipeiInfo> getLipeiInfos() {
            return lipeiInfos;
        }

        public void setLipeiInfos(List<lipeiInfo> lipeiInfos) {
            this.lipeiInfos = lipeiInfos;
        }

    @Override
    public String toString() {
        return "UncompletedBean{" +
                "dispose_step='" + step + '\'' +
                ", innocuousId='" + innocuousId + '\'' +
                ", currentStep=" + currentStep +
                ", lipeiInfos=" + lipeiInfos +
                '}';
    }

    /**
     * 处理步骤
     */
    public static class currentStep implements Parcelable {
        private String name;
        private List<currentStepInfo> currentStepInfoList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<currentStepInfo> getCurrentStepInfoList() {
            return currentStepInfoList;
        }

        public void setCurrentStepInfoList(List<currentStepInfo> currentStepInfoList) {
            this.currentStepInfoList = currentStepInfoList;
        }

        @Override
        public String toString() {
            return "currentStep{" +
                    "name='" + name + '\'' +
                    ", currentStepInfoList=" + currentStepInfoList +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeTypedList(this.currentStepInfoList);
        }

        public currentStep() {
        }

        protected currentStep(Parcel in) {
            this.name = in.readString();
            this.currentStepInfoList = in.createTypedArrayList(currentStepInfo.CREATOR);
        }

        public static final Parcelable.Creator<currentStep> CREATOR = new Parcelable.Creator<currentStep>() {
            @Override
            public currentStep createFromParcel(Parcel source) {
                return new currentStep(source);
            }

            @Override
            public currentStep[] newArray(int size) {
                return new currentStep[size];
            }
        };
    }

    /**
     * 具体处理步骤
     */
    public static class currentStepInfo implements Parcelable {

        private int step;
        private String describe;
        private int dealType;   //处理方式  0图片 1视频
        private String recTime; //录制时长
        private String stepId; //处理id

        public String getStepId() {
            return stepId;
        }

        public void setStepId(String stepId) {
            this.stepId = stepId;
        }

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }

        public String getDescribe() {
            return describe;
        }

        public void setDescribe(String describe) {
            this.describe = describe;
        }

        public int getDealType() {
            return dealType;
        }

        public void setDealType(int dealType) {
            this.dealType = dealType;
        }

        public String getRecTime() {
            return recTime;
        }

        public void setRecTime(String recTime) {
            this.recTime = recTime;
        }

        @Override
        public String toString() {
            return "currentStepInfo{" +
                    "dispose_step='" + step + '\'' +
                    ", describe='" + describe + '\'' +
                    ", dealType='" + dealType + '\'' +
                    ", recTime='" + recTime + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.step);
            dest.writeString(this.describe);
            dest.writeInt(this.dealType);
            dest.writeString(this.recTime);
            dest.writeString(this.stepId);
        }

        public currentStepInfo() {
        }

        protected currentStepInfo(Parcel in) {
            this.step = in.readInt();
            this.describe = in.readString();
            this.dealType = in.readInt();
            this.recTime = in.readString();
            this.stepId = in.readString();
        }

        public static final Creator<currentStepInfo> CREATOR = new Creator<currentStepInfo>() {
            @Override
            public currentStepInfo createFromParcel(Parcel source) {
                return new currentStepInfo(source);
            }

            @Override
            public currentStepInfo[] newArray(int size) {
                return new currentStepInfo[size];
            }
        };
    }

    /**
     * 未完成处理的理赔列表
     */
    public static class lipeiInfo implements Parcelable {
        private String date;
        private List<payInfo> payInfoList;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public List<payInfo> getPayInfoList() {
            return payInfoList;
        }

        public void setPayInfoList(List<payInfo> payInfoList) {
            this.payInfoList = payInfoList;
        }

        @Override
        public String toString() {
            return "lipeiInfo{" +
                    "date='" + date + '\'' +
                    ", payInfoList=" + payInfoList +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.date);
            dest.writeTypedList(this.payInfoList);
        }

        public lipeiInfo() {
        }

        protected lipeiInfo(Parcel in) {
            this.date = in.readString();
            this.payInfoList = in.createTypedArrayList(payInfo.CREATOR);
        }

        public static final Parcelable.Creator<lipeiInfo> CREATOR = new Parcelable.Creator<lipeiInfo>() {
            @Override
            public lipeiInfo createFromParcel(Parcel source) {
                return new lipeiInfo(source);
            }

            @Override
            public lipeiInfo[] newArray(int size) {
                return new lipeiInfo[size];
            }
        };
    }

    /**
     * 理赔信息
     */
    public static class payInfo implements Parcelable {
        //保单号
        private String baodanNo;
        //死亡日期
        private String deathTime;
        //重量
        private String weight;
        //类型
        private String pigTypeName;
        //标的识别码
        private String seqNo;
        //重复理赔
        private String repeat;
        //理赔单号
        private String lipeiNo;
        private String lipeiDate;
        private String imgLeft;
        private String imgRight;
        private String imgMiddle;
        private boolean isSelected = false;

        public String getLipeiDate() {
            return lipeiDate;
        }

        public void setLipeiDate(String lipeiDate) {
            this.lipeiDate = lipeiDate;
        }

        public String getBaodanNo() {
            return baodanNo;
        }

        public void setBaodanNo(String baodanNo) {
            this.baodanNo = baodanNo;
        }

        public String getDeathTime() {
            return deathTime;
        }

        public void setDeathTime(String deathTime) {
            this.deathTime = deathTime;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getPigTypeName() {
            return pigTypeName;
        }

        public void setPigTypeName(String pigTypeName) {
            this.pigTypeName = pigTypeName;
        }

        public String getSeqNo() {
            return seqNo;
        }

        public void setSeqNo(String seqNo) {
            this.seqNo = seqNo;
        }

        public String getRepeat() {
            return repeat;
        }

        public void setRepeat(String repeat) {
            this.repeat = repeat;
        }

        public String getLipeiNo() {
            return lipeiNo;
        }

        public void setLipeiNo(String lipeiNo) {
            this.lipeiNo = lipeiNo;
        }

        public String getImgLeft() {
            return imgLeft;
        }

        public void setImgLeft(String imgLeft) {
            this.imgLeft = imgLeft;
        }

        public String getImgRight() {
            return imgRight;
        }

        public void setImgRight(String imgRight) {
            this.imgRight = imgRight;
        }

        public String getImgMiddle() {
            return imgMiddle;
        }

        public void setImgMiddle(String imgMiddle) {
            this.imgMiddle = imgMiddle;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.baodanNo);
            dest.writeString(this.deathTime);
            dest.writeString(this.weight);
            dest.writeString(this.pigTypeName);
            dest.writeString(this.seqNo);
            dest.writeString(this.repeat);
            dest.writeString(this.lipeiNo);
            dest.writeString(this.lipeiDate);
            dest.writeString(this.imgLeft);
            dest.writeString(this.imgRight);
            dest.writeString(this.imgMiddle);
            dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        }

        public payInfo() {
        }

        protected payInfo(Parcel in) {
            this.baodanNo = in.readString();
            this.deathTime = in.readString();
            this.weight = in.readString();
            this.pigTypeName = in.readString();
            this.seqNo = in.readString();
            this.repeat = in.readString();
            this.lipeiNo = in.readString();
            this.lipeiDate = in.readString();
            this.imgLeft = in.readString();
            this.imgRight = in.readString();
            this.imgMiddle = in.readString();
            this.isSelected = in.readByte() != 0;
        }

        public static final Creator<payInfo> CREATOR = new Creator<payInfo>() {
            @Override
            public payInfo createFromParcel(Parcel source) {
                return new payInfo(source);
            }

            @Override
            public payInfo[] newArray(int size) {
                return new payInfo[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.step);
        dest.writeString(this.innocuousId);
        dest.writeParcelable(this.currentStep, flags);
        dest.writeTypedList(this.lipeiInfos);
    }

    public UncompletedBean() {
    }

    protected UncompletedBean(Parcel in) {
        this.step = in.readString();
        this.innocuousId = in.readString();
        this.currentStep = in.readParcelable(currentStep.class.getClassLoader());
        this.lipeiInfos = in.createTypedArrayList(lipeiInfo.CREATOR);
    }

    public static final Parcelable.Creator<UncompletedBean> CREATOR = new Parcelable.Creator<UncompletedBean>() {
        @Override
        public UncompletedBean createFromParcel(Parcel source) {
            return new UncompletedBean(source);
        }

        @Override
        public UncompletedBean[] newArray(int size) {
            return new UncompletedBean[size];
        }
    };
}
