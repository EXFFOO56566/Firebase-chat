package com.bytesbee.firebase.chat.activities.models;

import java.io.Serializable;

public class Others implements Serializable {

    private boolean typing;
    private String typingwith;

    public Others() {
    }

    public Others(boolean typing) {
        this.typing = typing;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public String getTypingwith() {
        return typingwith;
    }

    public void setTypingwith(String typingwith) {
        this.typingwith = typingwith;
    }
}
