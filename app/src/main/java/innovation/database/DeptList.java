package innovation.database;

import java.util.List;

public class DeptList {

    private List<CityListInfo> cityList;

    private List<CountyListInfo> countyList;

    public List<CityListInfo> getCityList() {
        return cityList;
    }

    public void setCityList(List<CityListInfo> cityList) {
        this.cityList = cityList;
    }

    public List<CountyListInfo> getCountyList() {
        return countyList;
    }

    public void setCountyList(List<CountyListInfo> countyList) {
        this.countyList = countyList;
    }

    @Override
    public String toString() {
        return "DeptList{" +
                "cityList=" + cityList +
                ", countyList=" + countyList +
                '}';
    }
}
