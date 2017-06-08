package com.qyl.vip.download.bean;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class ThreadInfo {
    private int id;
    private String uri;
    private long start;
    private long end;
    private long finished;

    public ThreadInfo() {
    }

    public ThreadInfo(int id, String uri, long finished) {
        this.id = id;
        this.uri = uri;
        this.finished = finished;
    }

    public ThreadInfo(int id, String uri, long start, long end, long finished) {
        this.id = id;
        this.uri = uri;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }



}
