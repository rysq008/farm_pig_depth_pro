package innovation.entry;


import innovation.utils.NewHttpRespObject;

public class NewBuildObject extends NewHttpRespObject {

    //data
    public String build_data = "";


    @Override
    public void setdata(String data) {
        if(data == null)
            return;
        build_data = data;
    }

}