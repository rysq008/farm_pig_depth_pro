package innovation.database;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.NameInDb;

@Entity
public class OffLineCount extends BaseTable {

    /**
        sheId	                 猪舍id
        sheName                  猪舍名
        count                    点数个数
        autoCount                自动识别个数
        location                 点数详情json
        timeLength               点数时长
        juanCnt                  圈号
        createuser               点数人id、
        isUpload                 是否上传
     */
    @Index
    @NameInDb("END_ID")
    public String enId;
    @NameInDb("SHE_ID")
    public String zhuSheId;
    @NameInDb("SHE_NAME")
    public String zhuSheName;
    @NameInDb("COUNT")
    public int zhuSheCount;
    @NameInDb("AUTO_COUNT")
    public int zhuSheAutoCount;
    @NameInDb("LOCATION")
    public String zhuSheLocation;
    @NameInDb("TIME_LENGTH")
    public int zhuSheTimeLength;
    @NameInDb("JUAN_CNT")
    public int juanCnt;
    @NameInDb("CREATE_USER")
    public String zhuSheCreateUser;
    @NameInDb("IS_UPLOAD")
    public boolean zhuSheIsUpload;
    @NameInDb("ZHU_SHE_ZIPFILE")
    public String zhuSheZipFile;
    @NameInDb("ZHU_SHE_TIMES_FLAG")
    public String zhuSheTimesFlag;



}
