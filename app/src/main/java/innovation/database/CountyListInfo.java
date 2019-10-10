package innovation.database;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.NameInDb;
import io.objectbox.annotation.Transient;

@Entity
public class CountyListInfo extends BaseTable {

   /* "code": "7034262",
    "createtime": "",
    "fullname": "平遥县支公司",
    "gsDepartmentId": "14072800",
    "hierarchy": 3,
    "id": 2009,
    "num": 1,
    "pid": 2008,
    "pids": "[0],[20],[2004],[2008],",
    "regionCode": "140728",
    "simplename": "平遥县支公司",
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
        return "CountyListInfo{" +
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
