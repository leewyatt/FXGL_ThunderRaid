package com.itcodebox.game;

import javafx.util.Duration;

/**
 * @author LeeWyatt
 */
public interface Config {
    double BG_SCROLL_SPEED = 160;

    double PLAYER_MOVE_SPEED = 6;

    Duration PLAYER_PROTECT_DURATION = Duration.seconds(7);
    Duration ENEMY_PROTECT_DURATION = Duration.seconds(0.9);

    int  WAVE = 200;
    int MAX_HP = 15;
    int MAX_ARMS_LEVEL = 4;
}
