package com.MohafizDZ.framework_repository.service;

public abstract class SearchOnline{
        private long lastTimeInMillis;
        private long timeIntervalInMillis;
        private boolean searched = true;

        public SearchOnline(long timeIntervalInMillis) {
            this(timeIntervalInMillis, false);
        }

        public SearchOnline(long timeIntervalInMillis, boolean startDirectly){
            this.timeIntervalInMillis = timeIntervalInMillis;
            if(startDirectly){
                searched = false;
                lastTimeInMillis = System.currentTimeMillis() - timeIntervalInMillis;
            }
        }

        public void search(){
            if(searched) {
                lastTimeInMillis = System.currentTimeMillis();
                searched = false;
                return;
            }
            long currentTimeInMillis = System.currentTimeMillis();
            if(currentTimeInMillis - lastTimeInMillis >= timeIntervalInMillis){
                searched = onSearchOnline();
            }
        }

        protected abstract boolean onSearchOnline();
    }