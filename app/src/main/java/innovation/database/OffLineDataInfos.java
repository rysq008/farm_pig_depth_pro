package innovation.database;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.NameInDb;

@Entity
public class OffLineDataInfos extends BaseTable {

    /**
        sheId	                猪舍id
        insureNo	            保单号
        reason	                死因
        preLon	                预理赔经度
        preLat	                预理赔纬度
        preTimesFlag	        预理赔唯一标识
        preZipFilePath		    预理赔图片zip文件路径
        preIsForce	            预理赔是否强制提交
        PreCreateTime	        预理赔时间
        preZipSmallVideoFilePath	预理赔小视频zip文件路径
        timesFlag		        理赔唯一标识
        lon	                    理赔经度
        lat	                    理赔纬度
        zipFilePath	            理赔图片zip文件
        isForce	                理赔是否强制提交
        zipWeightImgPath	    理赔称重照片zip文件
        createTime	            理赔时间
        deadPigLon              死猪照片经度
        deadPigLat              死猪照片纬度
        deadPigTime             死猪照片时间
     */
    @NameInDb("SHE_ID")
    public String sheId;
    @NameInDb("INSURE_NO")
    public String insureNo;
    @NameInDb("REASON")
    public String reason;
    @NameInDb("PRE_LON")
    public String preLon;
    @NameInDb("PRE_LAT")
    public String preLat;
    @Index
    @NameInDb("PRE_TIMES_FLAG")
    public String preTimesFlag;
    @NameInDb("PRE_ZIP_FILE_PATH")
    public String preZipFilePath;
    @NameInDb("PRE_IS_FORCE")
    public boolean preIsForce;
    @NameInDb("PRE_CREATE_TIME")
    public String preCreateTime;
    @NameInDb("PRE_ZIP_SMALL_VIDEO_FILE_PATH")
    public String preZipSmallVideoFilePath;
    @NameInDb("TIME_FLAG")
    public String timesFlag;
    @NameInDb("LON")
    public String lon;
    @NameInDb("LAT")
    public String lat;
    @NameInDb("ZIP_FILE_PATH")
    public String zipFilePath;
    @NameInDb("IS_FORCE")
    public boolean isForce;
    @NameInDb("ZIP_WEIGHT_IMG_PATH")
    public String zipWeightImgPath;
    @NameInDb("CREATE_TIME")
    public String createTime;
    @Index
    @NameInDb("EN_ID")
    public String enId;
    @NameInDb("PIG_AGE")
    public String pigAge;
    @NameInDb("PIG_DEATH_TIME")
    public String pigDeathTime;
    @NameInDb("ALREADY_ZIP")
    public boolean alreadyZip;
    @NameInDb("IMG_BASE_STR")
    public String imgBaseStr;
    @NameInDb("ZIP_ALL_PATH")
    public String zipAllPath;
    @NameInDb("SHE_NAME")
    public String sheName;
    @NameInDb("DEAD_PIG_LON")
    public String deadPigLon;
    @NameInDb("DEAD_PIG_LAT")
    public String deadPigLat;
    @NameInDb("DEAD_PIG_TIME")
    public String deadPigTime;
    @NameInDb("PRE_COLLECT_TIME")
    public String preCollectTime;
    @NameInDb("COLLECT_TIME")
    public String collectTime;

}
