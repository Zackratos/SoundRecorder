package com.danielkim.soundrecorder;

import java.io.File;

/**
 * Created by IVY on 2017/2/20.
 */

public class Record {
//    private String name;
//    private long date;
    private File file;
    private int duration;

    public Record(File file, int duration) {
//        this.name = name;
//        this.date = date;
        this.file = file;
        this.duration = duration;
    }

    public File getFile() {
        return file;
    }

    public void setFail(File file) {
        this.file = file;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
