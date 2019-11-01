package com.farm.innovation.biz.iterm;

import com.farm.innovation.utils.PointFloat;

import java.util.List;

public class YakKeyPointAndRotationItem {

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
