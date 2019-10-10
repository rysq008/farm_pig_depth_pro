package innovation.database;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.NameInDb;
import io.objectbox.annotation.Transient;

/**
 * 中支（市）公司
 */
@Entity
public class CityListInfo extends BaseTable {

    /*
        "fullname": "晋中市支公司",
        "hierarchy": 2,
        "id": 2008,
        "num": 1,
        "pid": 2004,
        "pids": "[0],[20],[2004],", //注："[最高级]，[国寿财]，[省]"
        "regionCode": "140700",
        "simplename": "晋中市支公司",
        "tips": "APP",
        "version": 1
    */

    @NameInDb("FULL_NAME")
    public String fullname;
    @NameInDb("HIERARCHY")
    public int hierarchy;
    @NameInDb("ID")
    public int id;
    @NameInDb("PID")
    public int pid;
    @NameInDb("PIDS")
    public String pids;
    @NameInDb("SIMPLE_NAME")
    public String simplename;
    @Transient
    public boolean isSelect;

    @Override
    public String toString() {
        return "CityListInfo{" +
                "fullname='" + fullname + '\'' +
                ", hierarchy=" + hierarchy +
                ", id=" + id +
                ", pid=" + pid +
                ", pids='" + pids + '\'' +
                ", simplename='" + simplename + '\'' +
                ", isSelect=" + isSelect +
                '}';
    }
}
