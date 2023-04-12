package com.devlomi.fireapp.events;

import com.sinch.android.rtc.calling.Call;

public class PhoneCallEvents {
    public static class OnCallProgressing {
        Call call;

        public OnCallProgressing(Call call) {
            this.call = call;
        }

        public Call getCall() {
            return call;
        }
    }


    public static class OnCallEstablished {
        Call call;

        public OnCallEstablished(Call call) {
            this.call = call;
        }

        public Call getCall() {
            return call;
        }
    }


    public static class OnCallEnded {
        Call call;

        public OnCallEnded(Call call) {
            this.call = call;
        }

        public Call getCall() {
            return call;
        }
    }


    public static class OnShouldSendPushNotification {
        Call call;

        public OnShouldSendPushNotification(Call call) {
            this.call = call;
        }

        public Call getCall() {
            return call;
        }
    }


}
