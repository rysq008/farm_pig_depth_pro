package innovation.biz.iterm;

import java.util.List;

import innovation.utils.PointFloat;

public class NewPigKeyPointAndRotationItem {

    private PredictRotationIterm predictRotationIterm;
    private List<PointFloat> points;

    public PredictRotationIterm getPredictRotationIterm() {
        return predictRotationIterm;
    }

    public void setPredictRotationIterm(PredictRotationIterm predictRotationIterm) {
        this.predictRotationIterm = predictRotationIterm;
    }

    public List<PointFloat> getPoints() {
        return points;
    }

    public void setPoints(List<PointFloat> points) {
        this.points = points;
    }
}
