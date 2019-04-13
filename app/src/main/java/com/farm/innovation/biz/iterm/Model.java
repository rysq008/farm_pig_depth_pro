package com.farm.innovation.biz.iterm;

/**
 * @author wbs on 11/2/17.
 */

public enum Model {
    //投保
    BUILD(1),
    //理赔
    VERIFY(2);

    private int model;

    Model(int model) {
        this.model = model;
    }

    public int value() {
        return model;
    }
}
