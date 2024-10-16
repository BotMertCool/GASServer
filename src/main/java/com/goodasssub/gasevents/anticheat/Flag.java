package com.goodasssub.gasevents.anticheat;

import lombok.Getter;

@Getter
public class Flag {
    private final String checkName;
    private final long timestamp;

    public Flag(String checkName, long timestamp) {
        this.checkName = checkName;
        this.timestamp = timestamp;
    }
}
