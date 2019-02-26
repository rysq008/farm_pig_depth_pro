package innovation.database;

import java.lang.annotation.Annotation;

import io.objectbox.annotation.BaseEntity;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.NameInDb;

/**
 * @Entity：这个对象需要持久化。
 * @Id：这个对象的主键,默认情况下，id是会被objectbox管理的，也就是自增id，如果你想手动管理id需要在注解的时候加上@Id(assignable = true)即可。当你在自己管理id的时候如果超过long的最大值，objectbox 会报错.id的值不能为负数。当id等于0时objectbox会认为这是一个新的实体对象,因此会新增到数据库表中
 * @Index：这个对象中的索引。对经常大量进行查询的字段创建索引，会提高你的查询性能。
 * @Transient:如果你有某个字段不想被持久化，可以使用此注解,那么该字段将不会保存到数据库
 * @NameInDb：有的时候数据库中的字段跟你的对象字段不匹配的时候，可以使用此注解。
 * @ToOne:做一对一的关联注解，例如示例中表示一张学生表（Student）关联一张班级表（Class）,此外还有一对多，多对多的关联，例如Class的示例：
 * @Entity //表示这是一个需要持久化的实体
 * public class Student {
 * @Id public long id;
 * @Index public String name;
 * @NameInDb("USERNAME") public String name;
 * public ToOne<Class> classToOne;
 * }
 * @ToMany:做一对多的关联注解，如示例中表示一张班级表(Class)关联多张学生表(Student) \n
 * * @Entity public class Class{
 * * @Id long id;
 * * @Backlink(to = "classToOne")
 * * public ToMany<Student> studentEntitys;
 * * }
 * @Backlink:表示反向关联
 */

@Entity
public class BaseTable implements BaseEntity {
    @Id
    @NameInDb("_ID")
    long _id;

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
