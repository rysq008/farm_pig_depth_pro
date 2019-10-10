package innovation.database;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.NameInDb;
import io.objectbox.annotation.Transient;

@Entity
public class SheInfo extends BaseTable {

    /**
     * enId : 163
     * sheId : 猪舍id
     * sheName : 猪舍名
     * pigType : 猪舍类型
     * pigTypeName : 猪舍类型名称
     * dianshuTime : 点数时间
     * count : 点数头数
     * insureNo : 保单号
     * endTime : 保单结束时间
     */
    @Index
    @NameInDb("END_ID")
    public String enId;
    @Index
    @NameInDb("SHE_ID")
    public String sheId;
    @NameInDb("SHE_NAME")
    public String sheName;
    @NameInDb("PIG_TYPE")
    public String pigType;
    @NameInDb("PIG_TYPE_NAME")
    public String pigTypeName;
    @NameInDb("DIAN_SHU_TIME")
    public String dianshuTime;
    @NameInDb("COUNT")
    public String count;
    @NameInDb("INSURE_NO")
    public String insureNo;
    @NameInDb("END_TIME")
    public String endTime;
    @NameInDb("JUAN_CNT")
    public String juanCnt;
    @NameInDb("AUTO_COUNT")
    public String autoCount;
    @Transient
    public boolean select;


    @Override
    public String toString() {
        return "SheInfo{" +
                "enId='" + enId + '\'' +
                ", sheId='" + sheId + '\'' +
                ", sheName='" + sheName + '\'' +
                ", pigType='" + pigType + '\'' +
                ", pigTypeName='" + pigTypeName + '\'' +
                ", dianshuTime='" + dianshuTime + '\'' +
                ", count='" + count + '\'' +
                ", insureNo='" + insureNo + '\'' +
                ", endTime='" + endTime + '\'' +
                ", juanCnt='" + juanCnt + '\'' +
                ", autoCount='" + autoCount + '\'' +
                '}';
    }
}
