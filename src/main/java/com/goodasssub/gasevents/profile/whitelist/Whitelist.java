package com.goodasssub.gasevents.profile.whitelist;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Whitelist {
    private boolean isEnabled = false;
    private List<String> players = List.of(
        "41b4126f-ce44-417f-81d3-f6cd392bb3d9" // BotMert UUID
    );

}
