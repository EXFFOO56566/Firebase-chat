package com.devlomi.fireapp.events;

public class AudioServiceCallbacksEvent {


    public static class setMax {
        private String id;
        private int pos;
        private int duration;

        public setMax(String id, int pos, int duration) {
            this.id = id;
            this.pos = pos;
            this.duration = duration;
        }

        public String getId() {
            return id;
        }

        public int getPos() {
            return pos;
        }

        public int getDuration() {
            return duration;
        }
    }

    public static class onPlay {
        private String id;
        private int pos;
        private int streamType;

        public onPlay(String id, int pos, int streamType) {
            this.id = id;
            this.pos = pos;
            this.streamType = streamType;
        }

        public String getId() {
            return id;
        }

        public int getPos() {
            return pos;
        }

        public int getStreamType() {
            return streamType;
        }
    }

    public static class onPause {
        private String id;
        private int pos;

        public onPause(String id, int pos) {
            this.id = id;
            this.pos = pos;
        }

        public String getId() {
            return id;
        }

        public int getPos() {
            return pos;
        }
    }

    public static class onComplete {
        private String id;
        private int pos;
        private int finalProgress;

        public onComplete(String id, int pos, int finalProgress) {
            this.id = id;
            this.pos = pos;
            this.finalProgress = finalProgress;
        }

        public String getId() {
            return id;
        }

        public int getPos() {
            return pos;
        }

        public int getFinalProgress() {
            return finalProgress;
        }
    }

    public static class onError {
        private String id;
        private int pos;

        public onError(String id, int pos) {
            this.id = id;
            this.pos = pos;
        }

        public String getId() {
            return id;
        }

        public int getPos() {
            return pos;
        }
    }

    public static class onProgressUpdate {
        private String id;
        private int pos;
        private int progress;
        private byte[] waves;

        public onProgressUpdate(String id, int pos, int progress, byte[] waves) {
            this.id = id;
            this.pos = pos;
            this.progress = progress;
            this.waves = waves;
        }

        public String getId() {
            return id;
        }

        public int getPos() {
            return pos;
        }

        public int getProgress() {
            return progress;
        }

        public byte[] getWaves() {
            return waves;
        }
    }
}
