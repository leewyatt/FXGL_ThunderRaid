package com.itcodebox.game.component;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.LocalTimer;
import com.itcodebox.game.Config;
import com.itcodebox.game.GameType;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.texture;

/**
 * @author LeeWyatt
 */
public class PlayerComponent extends Component {

    private final LocalTimer shootTimer = FXGL.newLocalTimer();
    private final Duration shootDelay = Duration.seconds(0.2);
    private final Texture mainBullet = texture("bullet/b1.png", 60, 60);
    private final Texture minorBullet = texture("bullet/b2.png",28,10);


    @Override
    public void onAdded() {
        shootTimer.capture();
    }

    @Override
    public void onUpdate(double tpf) {
        entity.translateY(-Config.BG_SCROLL_SPEED * tpf);

    }

    public void moveLeft() {
        entity.translateX(-Config.PLAYER_MOVE_SPEED);
    }

    public void moveRight() {
        entity.translateX(Config.PLAYER_MOVE_SPEED);
    }

    public void moveUp() {
        entity.translateY(-Config.PLAYER_MOVE_SPEED);
    }

    public void moveDown() {
        entity.translateY(Config.PLAYER_MOVE_SPEED);
    }

    public void shoot() {
        if (shootTimer.elapsed(shootDelay)) {
            FXGL.play("shoot.wav");
            int armsLevel = FXGL.geti("armsLevel");
            FXGL.spawn("bullet",
                    new SpawnData(entity.getCenter().subtract(30, entity.getHeight())).put("texture",mainBullet.copy()).put("dir", new Point2D(0,-1)).put("ownerType", GameType.PLAYER)
            );
            for (int i = 0; i < armsLevel-1&& i<Config.MAX_ARMS_LEVEL; i++) {
                FXGL.spawn("bullet",
                        new SpawnData(entity.getCenter().subtract(40, entity.getHeight())).put("texture",minorBullet.copy()).put("dir", new Point2D(-0.1-i*0.15,-0.7)).put("ownerType", GameType.PLAYER)
                );
                FXGL.spawn("bullet",
                        new SpawnData(entity.getCenter().subtract(-18, entity.getHeight())).put("texture", minorBullet.copy()).put("dir", new Point2D(0.1+i*0.15,-0.7)).put("ownerType", GameType.PLAYER)
                );
            }
            shootTimer.capture();
        }
    }
}
