package com.itcodebox.game;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.*;
import com.almasb.fxgl.dsl.views.ScrollingBackgroundView;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.ui.ProgressBar;
import com.itcodebox.game.component.BottomComponent;
import com.itcodebox.game.component.EnemyComponent;
import com.itcodebox.game.component.MovePointComponent;
import com.itcodebox.game.component.PlayerComponent;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.VerticalDirection;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.getAssetLoader;

/**
 * @author LeeWyatt
 */
public class GameEntityFactory implements EntityFactory {

    private Duration explosionShowTime = Duration.seconds(0.35);

    @Spawns("background")
    public Entity newBackground(SpawnData data) {
        Texture t = getAssetLoader().loadTexture("bg2.jpg");
        ScrollingBackgroundView backgroundView = new ScrollingBackgroundView(t.superTexture(t, VerticalDirection.DOWN).getImage(), FXGL.getAppWidth(), FXGL.getAppHeight(), Orientation.VERTICAL);

        return FXGL.entityBuilder(data)
                .view(backgroundView)
                .build();
    }

    @Spawns("movePoint")
    public Entity newMovePoint(SpawnData data) {
        return FXGL.entityBuilder(data)
                .with(new MovePointComponent())
                .build();
    }

    @Spawns("bottom")
    public Entity newBottom(SpawnData data) {
        CollidableComponent cc = new CollidableComponent(true);
        cc.addIgnoredType(GameType.PLAYER);
        cc.addIgnoredType(GameType.BULLET);
        cc.addIgnoredType(GameType.MOVE_POINT);

        return FXGL.entityBuilder(data)
                .at(-2000, FXGL.getAppHeight()/2.0)
                .type(GameType.BOTTOM)
                .bbox(BoundingShape.box(FXGL.getAppWidth()+4000,3))
                .with(new BottomComponent())
                .with(cc)
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        HealthIntComponent hp = new HealthIntComponent(Config.MAX_HP);
        hp.setValue(Config.MAX_HP);
        ProgressBar hpBar = new ProgressBar(true);
        hpBar.setWidth(60);
        hpBar.setFill(Color.LIGHTGREEN);
        hpBar.setMaxValue(Config.MAX_HP);
        hpBar.currentValueProperty().bind(hp.valueProperty());
        hpBar.setTranslateY(80);
        hpBar.setTranslateX(30);

        return FXGL.entityBuilder(data)
                .type(GameType.PLAYER)
                .at(FXGL.getAppWidth() / 2.0 - 60, -FXGL.getAppHeight() -80)
                .viewWithBBox("player.png")
                .with(new KeepOnScreenComponent())
                .with(new PlayerComponent())
                .with(hp)
                .view(hpBar)
                .collidable()
                .build();
    }

    @Spawns("bullet")
    public Entity newBullet(SpawnData data) {
        CollidableComponent collidableComponent = new CollidableComponent(true);
        Point2D dir = data.get("dir");
        Texture t = data.get("texture");
        GameType ownerType = data.get("ownerType");
        collidableComponent.addIgnoredType(ownerType);
        return FXGL.entityBuilder(data)
                .type(GameType.BULLET)
                .viewWithBBox(t)
                .with(new OffscreenCleanComponent())
                .with(new ProjectileComponent(dir, 800))
                .with(collidableComponent)
                .build();
    }

    @Spawns("missile")
    public Entity newMissile(SpawnData data) {
        CollidableComponent collidableComponent = new CollidableComponent(true);
        collidableComponent.addIgnoredType(GameType.PLAYER);
        return FXGL.entityBuilder(data)
                .type(GameType.BULLET)
                .viewWithBBox("bullet/missile.png")
                .with(new OffscreenCleanComponent())
                .with(new ProjectileComponent(new Point2D(0, -1), 980))
                .with(collidableComponent)
                .build();
    }


    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.ENEMY)
                .viewWithBBox("enemy/e"+ FXGLMath.random(1, 9) +".png")
                .with(new EnemyComponent())
                .collidable()
                .build();
    }

   private final AnimationChannel ac = new AnimationChannel(FXGL.image("explosion/redExplosion.png"), explosionShowTime, 10);

    @Spawns("explosion")
    public Entity newExplosion(SpawnData data) {
        explosionShowTime = Duration.seconds(0.35);
        return FXGL.entityBuilder(data)
                .view(new AnimatedTexture(ac).loop())
                .with(new ExpireCleanComponent(explosionShowTime))
                .build();
    }

    /**
     * 游戏道具
     */
    @Spawns("items")
    public Entity newItems(SpawnData data) {
        CollidableComponent cc = new CollidableComponent(true);
        cc.addIgnoredType(GameType.MOVE_POINT);
        cc.addIgnoredType(GameType.BULLET);
        cc.addIgnoredType(GameType.ENEMY);
        return FXGL.entityBuilder(data)
                .type(GameType.ITEMS)
                .viewWithBBox("items/"+data.<String>get("itemsName")+".png")
                .with(cc)
                .build();
    }
}
