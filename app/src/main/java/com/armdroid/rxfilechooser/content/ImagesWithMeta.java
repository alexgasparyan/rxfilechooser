package com.armdroid.rxfilechooser.content;

import java.util.List;

public class ImagesWithMeta {

    private List<ImageContent> mImages;
    private Meta mMeta;

    public ImagesWithMeta(List<ImageContent> images) {
        this.mImages = images;
    }

    public List<ImageContent> getImages() {
        return mImages;
    }

    public void setImages(List<ImageContent> images) {
        this.mImages = images;
    }

    public Meta getMeta() {
        return mMeta;
    }

    public void setMeta(Meta meta) {
        this.mMeta = meta;
    }

    public class Meta {
        private int mTotalCount;
        private int mLoadedCount;
        private int mCurrentPage;
        private int mTotalPages;

        public Meta(int totalCount, int currentPage, int limit) {
            mTotalCount = totalCount;
            mLoadedCount = mImages.size();
            mCurrentPage = currentPage;
            int extra = (totalCount == 0 || totalCount % limit != 0) ? 1 : 0;
            mTotalPages = totalCount / limit + extra;
        }

        public int getTotalCount() {
            return mTotalCount;
        }

        public void setTotalCount(int totalCount) {
            this.mTotalCount = totalCount;
        }

        public int getLoadedCount() {
            return mLoadedCount;
        }

        public void setLoadedCount(int loadedCount) {
            this.mLoadedCount = loadedCount;
        }

        public int getCurrentPage() {
            return mCurrentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.mCurrentPage = currentPage;
        }

        public int getmotalPages() {
            return mTotalPages;
        }

        public void setTotalPages(int totalPages) {
            this.mTotalPages = totalPages;
        }
    }
}
