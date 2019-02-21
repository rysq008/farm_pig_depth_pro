package innovation.database;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.NameInDb;
import io.objectbox.annotation.Uid;

@Entity
public class VideoUploadTable extends BaseTable {
    @NameInDb("FILE_PATH")
    public String fpath;
    @NameInDb("IS_COMPLETE")
    @Uid(8397522320393181039L)
    public boolean iscomplete;
    @Index
    @NameInDb("TIME_FLAG")
    @Uid(1022329793580276971L)
    public String timesflag;
}
