package com.test.lib2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class MyClass {

    public static void main(String[] args){
        try {
            createStudent();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        try {
            createStudentByFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createStudentByFile() throws Exception{
        String studentString = "package com.test.lib2;public class Test{private String studentId;public String getStudentId(){return this.studentId;}public void setStudentId(String studentId){this.studentId = studentId;}}";
        String fileName = System.getProperty("user.dir") + "/lib2/src/main/java/com/test/lib2/Test.java";
        File file = new File(fileName);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(studentString);
        fileWriter.flush();
        fileWriter.close();


        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = compiler.getStandardFileManager(null,null,null);
        Iterable<? extends JavaFileObject> javaFileObjects = manager.getJavaFileObjects(fileName);
        String dest = System.getProperty("user.dir") + "/lib2/src/main/java/com/test/lib2/target/classes";

        //options就是指定编译输入目录，与我们命令行写javac -d C://是一样的

        List<String> options = new ArrayList<String>();
        options.add("-d");
        options.add(dest);
        JavaCompiler.CompilationTask task = compiler.getTask(null,manager,null,options,null,javaFileObjects);
        task.call();
        manager.close();
        URL[] urls = new URL[]{new URL("file:/" + System.getProperty("user.dir") + "/lib2/src/main/java/com/test/lib2/target/classes")};

        //加载class时要告诉你的classloader去哪个位置加载class文件

        ClassLoader classLoader = new URLClassLoader(urls);
        Object student = classLoader.loadClass("com.test.lib2.Test").newInstance();
//        logger.info("student===={}",student);

    }

    private static String classString = "/* hehha */"                +
            "public class Student{                                  "+
            "       private String  studentId;                      "+
            "       public String getStudentId(){                   "+
            "           return this.studentId;                      "+
            "       }                                               "+
            "}                                                    ";
    private static void createStudent() throws URISyntaxException, ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, NoSuchMethodException, InvocationTargetException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
        ClassJavaFileManager classJavaFileManager = new ClassJavaFileManager(standardFileManager);
        StringObject stringObject = new StringObject(new URI("Student.java"), JavaFileObject.Kind.SOURCE, classString);
        JavaCompiler.CompilationTask task = compiler.getTask(null,classJavaFileManager,null,null,null, Arrays.asList(stringObject));
        if(task.call()){
            ClassJavaFileObject javaFileObject =  classJavaFileManager.getClassJavaFileObject();
            ClassLoader classLoader = new MyClassLoader(javaFileObject);
            Object student = classLoader.loadClass("Student").newInstance();
            Method getStudetnId = student.getClass().getMethod("getStudentId");
            Object invoke = getStudetnId.invoke(student);
            Logger.global.info("class==={}"/*,student*/);
        }
    }
    /**
     *自定义fileManager
     */
    static class ClassJavaFileManager extends ForwardingJavaFileManager {

        private ClassJavaFileObject classJavaFileObject;
        public ClassJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        public ClassJavaFileObject getClassJavaFileObject() {
            return classJavaFileObject;
        }
        //这个方法一定要自定义
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            return (classJavaFileObject = new ClassJavaFileObject(className,kind));
        }
    }
    /**
     * 存储源文件
     */
    static class StringObject extends SimpleJavaFileObject {

        private String content;

        public StringObject(java.net.URI uri, Kind kind, String content) {
            super(uri, kind);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return this.content;
        }
    }

    /**
     * class文件（不需要存到文件中）
     */
    static class ClassJavaFileObject extends SimpleJavaFileObject{

        ByteArrayOutputStream outputStream;

        public ClassJavaFileObject(String className, Kind kind) {
            super(java.net.URI.create(className + kind.extension), kind);
            this.outputStream = new ByteArrayOutputStream();
        }
        //这个也要实现
        @Override
        public OutputStream openOutputStream() throws IOException {
            return this.outputStream;
        }

        public byte[] getBytes(){
            return this.outputStream.toByteArray();
        }
    }
    //自定义classloader
    static class MyClassLoader extends ClassLoader{
        private ClassJavaFileObject stringObject;
        public MyClassLoader(ClassJavaFileObject stringObject){
            this.stringObject = stringObject;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = this.stringObject.getBytes();
            return defineClass(name,bytes,0,bytes.length);
        }
    }
}
