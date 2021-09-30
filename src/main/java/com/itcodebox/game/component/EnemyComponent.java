package com.itcodebox.game.component;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.LocalTimer;
import com.itcodebox.game.Config;
import com.itcodebox.game.GameType;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Point2D;
import javafx.util.Duration;

/**
 * @author LeeWyatt
 */
public class EnemyComponent extends Component {
    private LocalTimer shootTimer = FXGL.newLocalTimer();
    private Duration delay = Duration.seconds(FXGLMath.random(0.35, 0.8 ));
    private double speed = FXGLMath.random(20, 100);
    private int bType = FXGLMath.random(3, 6);
    private boolean isProtected = true;
    private Texture protectTexture;
    private HorizontalDirection dir = FXGLMath.random(HorizontalDirection.values()).get();
    private boolean isHorMove = FXGLMath.randomBoolean(0.5);

    @Override
    public void onUpdate(double tpf) {
        entity.translateY(speed * tpf);
        if (shootTimer.elapsed(delay)) {
            shoot();
            shootTimer.capture();
        }
        if (isHorMove) {
            if (entity.getX() <= -entity.getWidth()/2) {
                dir = HorizontalDirection.RIGHT;
            } else if (entity.getX() >= (FXGL.getAppWidth()-entity.getWidth()/2)) {
                dir = HorizontalDirection.LEFT;
            }
            entity.translateX(dir==HorizontalDirection.LEFT?-2:2);
        }

    }

    public boolean isProtected() {
        return isProtected;
    }

    @Override
    public void onAdded() {
        shootTimer.capture();
        protectTexture = FXGL.texture("protectRed.png");
        double pw = protectTexture.getWidth();
        double ph = protectTexture.getHeight();
        double w = entity.getWidth();
        double h = entity.getHeight();
        protectTexture.setTranslateX((w - pw) / 2);
        protectTexture.setTranslateY((h - ph) / 2);
        entity.getViewComponent().addChild(protectTexture);
        FXGL.run(() -> {
            isProtected = false;
            if (entity!=null&&entity.isActive() && entity.getViewComponent().getChildren().contains(protectTexture)) {
                entity.getViewComponent().removeChild(protectTexture);
            }
        }, Config.ENEMY_PROTECT_DURATION);

    }

    private void shoot() {
        Texture texture = FXGL.texture("bullet/b3.png");
        FXGL.spawn("bullet", new SpawnData(entity.getCenter().subtract(texture.getWidth() / 2, -entity.getHeight() / 2)).put("dir", new Point2D(0, 1)).put("ownerType", GameType.ENEMY).put("texture", texture));
    }
}
