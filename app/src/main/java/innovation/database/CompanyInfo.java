package innovation.database;

import java.util.List;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.NameInDb;
import io.objectbox.annotation.Transient;
import io.objectbox.annotation.Uid;

@Entity
public class CompanyInfo extends BaseTable {

    /**
     * canUse : 1
     * enId : 163企业id
     * enName : 好多企业一个保单2
     * enUserId : 165
     * enUserName : 小西养殖场采集员
     * enAddress : 企业地址
     * pids: "[0],[20],[2004],",
     * sheInfo : [{"enId":"163","sheId":"猪舍id","sheName":"猪舍名","pigType":"猪舍类型","pigTypeName":"猪舍类型名称","dianshuTime":"点数时间","count":"点数头数","insureNo":"保单号","EndTime":"保单结束时间"},{"enId":"163","sheId":"猪舍id","sheName":"猪舍名","pigType":"猪舍类型","pigTypeName":"猪舍类型名称","dianshuTime":"点数时间","count":"点数头数","insureNo":"保单号","EndTime":"保单结束时间"}]
     */
    @Index
    @NameInDb("EN_ID")
    @Uid(8912966556455185991L)
    public String enId;
    @NameInDb("CAN_USE")
    public int canUse;
    @NameInDb("EN_NAME")
    public String enName;
    @NameInDb("EN_USER_ID")
    public String enUserId;
    @NameInDb("EN_USER_NAME")
    public String enUserName;
    @NameInDb("EN_ADDRESS")
    public String enAddress;
    @NameInDb("PIDS")
    public String pids;
    @Transient
    public String sortIndex;
    public List<SheInfo> sheInfo;

    @Override
    public String toString() {
        return "CompanyInfo{" +
                "enId='" + enId + '\'' +
                ", canUse=" + canUse +
                ", enName='" + enName + '\'' +
                ", enUserId='" + enUserId + '\'' +
                ", enUserName='" + enUserName + '\'' +
                ", enAddress='" + enAddress + '\'' +
                ", pids='" + pids + '\'' +
                ", sortIndex='" + sortIndex + '\'' +
                ", sheInfo=" + sheInfo +
                '}';
    }
}
