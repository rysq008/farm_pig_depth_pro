package com.xiangchuang.risks.model;

import java.util.ArrayList;
import java.util.List;


public class PigpenManager {

    public class PigstyInfo {
        private int pigpenId;
        private int id;
        private String name;
        private int pigCount;

        protected PigstyInfo(int pigpenId, int id, String name, int pigCount) {
            this.pigpenId = pigpenId;
            this.id = id;
            this.name = name;
            this.pigCount = pigCount;
        }

        public int getPigpenId() {
            return pigpenId;
        }

        public int getId() {
            return id;
        }

        protected synchronized void setName(String name) {
            this.name = name;
        }

        public synchronized String getName() {
            return name;
        }

        public synchronized void setPigCount(int pigCount) {
            this.pigCount = pigCount;
        }

        protected synchronized int getPigCount() {
            return pigCount;
        }

    }

    public class PigpenInfo {
        private int id;
        private String name;
        private ArrayList<PigstyInfo> pigstyList;

        protected PigpenInfo(int id, String name, ArrayList<PigstyInfo> pigstyList) {
            this.id = id;
            this.name = name;
            this.pigstyList = pigstyList;
        }

        public int getId() {
            return id;
        }

        protected synchronized void setName(String name) {
            this.name = name;
        }

        public synchronized String getName() {
            return name;
        }

        public ArrayList<PigstyInfo> getPigstyList() {
            return pigstyList;
        }
    }

    private ArrayList<PigpenInfo> pigpenList = new ArrayList<>();

    static private PigpenManager pigpenManager;

    /*static public PigpenManager createInstance(List<LoginBean.MergeLoginBodyBean.Sty1ListBean> list) {
        if (pigpenManager == null)
            pigpenManager = new PigpenManager(list);
        return pigpenManager;
    }*/

    static public PigpenManager getInstance() {
        return pigpenManager;
    }

   /* private PigpenManager(List<LoginBean.MergeLoginBodyBean.Sty1ListBean> list) {
        for (LoginBean.MergeLoginBodyBean.Sty1ListBean bean : list) {
            ArrayList<PigstyInfo> pigstyList = new ArrayList<>();
            int pigpenId = bean.getSty1Id();
            for (LoginBean.MergeLoginBodyBean.Sty1ListBean.Sty2ListBean sty2bean : bean.getSty2List()) {
                PigstyInfo pigsty = new PigstyInfo(pigpenId, sty2bean.getSty2Id(), sty2bean.getName(), sty2bean.getCount());
                pigstyList.add(pigsty);
            }
            PigpenInfo pigpen = new PigpenInfo(pigpenId, bean.getName(), pigstyList);
        }
    }*/

    public List<PigpenInfo> getPigpenList() {
        return pigpenList;
    }

    public void setPigpenName(int pigpenId, String name) {
        for (PigpenInfo info : pigpenList) {
            if (info.getId() == pigpenId) {
                info.setName(name);
                break;
            }
        }
    }

    public void setPigstyName(int pigpenId, int pigstyId, String name) {
        for (PigpenInfo info : pigpenList) {
            if (info.getId() == pigpenId) {
                for (PigstyInfo pigstyInfo : info.getPigstyList()) {
                    if (pigstyInfo.getId() == pigstyId) {
                        pigstyInfo.setName(name);
                        break;
                    }
                }
            }
        }
    }

    public void setPigCount(int pigpenId, int pigstyId, int pigCount) {
        for (PigpenInfo info : pigpenList) {
            if (info.getId() == pigpenId) {
                for (PigstyInfo pigstyInfo : info.getPigstyList()) {
                    if (pigstyInfo.getId() == pigstyId) {
                        pigstyInfo.setPigCount(pigCount);
                        break;
                    }
                }
            }
        }
    }

    public int getPigpenPigCount(int pigpenId) {
        return 0;
    }

    public int getPigstyPigCount(int pigpenId, int pigstyId) {
        return 0;
    }

    public void addPigpen(int pigpenId, String name) {
        PigpenInfo info = new PigpenInfo(pigpenId, name, new ArrayList<>());
        pigpenList.add(info);
    }

    public void save() {

    }

    public void load() {

    }

    private PigpenInfo getPigpenInfo(int pigpenId) {
        for (PigpenInfo info : pigpenList) {
            if (info.id == pigpenId)
                return info;
        }
        return null;
    }
}