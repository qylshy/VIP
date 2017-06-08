package com.qyl.vip.download;

import java.io.File;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class DownloadRequest {

    private String mUri;

    private File mFolder;

    private String mName;

    private IProcessor mProcessor;

    private DownloadRequest() {
    }

    private DownloadRequest(String uri, File folder, String name, IProcessor processor) {
        this.mUri = uri;
        this.mFolder = folder;
        this.mName = name;
        this.mProcessor = processor;
    }

    public String getUri() {
        return mUri;
    }

    public File getFolder() {
        return mFolder;
    }

    public String getName() {
        return mName;
    }

    public IProcessor getProcessor() {
        return mProcessor;
    }

    public static class Builder {

        private String mUri;

        private File mFolder;

        private String mName;

        private IProcessor mProcessor;

        public Builder() {
        }

        public Builder setUri(String uri) {
            this.mUri = uri;
            return this;
        }

        public Builder setFolder(File folder) {
            this.mFolder = folder;
            return this;
        }

        public Builder setName(String name) {
            this.mName = name;
            return this;
        }

        public Builder setProcessor(IProcessor processor) {
            this.mProcessor = processor;
            return this;
        }

        public DownloadRequest build() {
            DownloadRequest request = new DownloadRequest(mUri, mFolder, mName, mProcessor);
            return request;
        }
    }

}
