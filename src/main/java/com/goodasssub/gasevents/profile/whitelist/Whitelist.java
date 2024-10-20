package com.goodasssub.gasevents.profile.whitelist;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class Whitelist {
    private boolean isEnabled = false;
    private Map<String, String> players = new HashMap<>() {{
        put("41b4126f-ce44-417f-81d3-f6cd392bb3d9", "BotMert");
    }};

}
