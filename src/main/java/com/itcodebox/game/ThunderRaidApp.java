package com.itcodebox.game;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.LocalTimer;
import com.itcodebox.game.component.EnemyComponent;
import com.itcodebox.game.component.PlayerComponent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author LeeWyatt
 */
public class ThunderRaidApp extends GameApplication {
    private LocalTimer launchTimer;
    private PlayerComponent player;
    private final String[] itemsAry = {"arms", "hp","missile"};
    /**
     * 保护无敌时间
     */
    private LocalTimer protectTimer;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(720);
        settings.setHeight(900);
        settings.setTitle("ThunderRaid");
        settings.setVersion("0.1");
        settings.setAppIcon("Icon.png");
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("armsLevel", 1);
        vars.put("protect", false);
        vars.put("missileNum", 2);
    }

    @Override
    protected void onPreInit() {
        getSettings().setGlobalMusicVolume(0.6);
        //getSettings().setGlobalSoundVolume(0.45);
        loopBGM("bgm.mp3");
    }

    @Override
    protected void onUpdate(double tpf) {
        if (protectTimer != null) {
            if (protectTimer.elapsed(Config.PLAYER_PROTECT_DURATION)) {
                FXGL.set("protect", false);
                protectTimer = null;
            }
        }
    }

    @Override
    protected void initInput() {
        int speed = 4;
        FXGL.getInput().addAction(new UserAction("UP") {
            @Override
            protected void onAction() {
                player.moveUp();
            }
        }, KeyCode.UP);
        FXGL.getInput().addAction(new UserAction("DOWN") {
            @Override
            protected void onAction() {
                player.moveDown();
            }
        }, KeyCode.DOWN);
        FXGL.getInput().addAction(new UserAction("LEFT") {
            @Override
            protected void onAction() {
                player.moveLeft();
            }
        }, KeyCode.LEFT);
        FXGL.getInput().addAction(new UserAction("RIGHT") {
            @Override
            protected void onAction() {
                player.moveRight();
            }
        }, KeyCode.RIGHT);

        FXGL.getInput().addAction(new UserAction("SHOOT") {
            @Override
            protected void onAction() {
                player.shoot();
            }
        }, KeyCode.SPACE);

        FXGL.getInput().addAction(new UserAction("LAUNCH MISSILE") {
            @Override
            protected void onAction() {
                if (launchTimer != null && FXGL.geti("missileNum") > 0 && launchTimer.elapsed(Duration.seconds(2))) {
                    FXGL.play("missileLaunch.wav");
                    for (int i = 0; i < 18; i++) {
                        spawn("missile", i * FXGL.getAppWidth() / 16.0 - 20, player.getEntity().getY());
                    }
                    FXGL.inc("missileNum", -1);
                    launchTimer.capture();
                }
            }
        }, KeyCode.F);
    }

    @Override
    protected void initPhysics() {
        CollisionHandler handler = new CollisionHandler(GameType.ENEMY, GameType.BULLET) {
            @Override
            protected void onCollisionBegin(Entity enemy, Entity b) {
                boolean isMissile = b.getBoolean("isMissile");
                if (b.isActive()) {
                    b.removeFromWorld();
                }
                if (enemy.getComponent(EnemyComponent.class).isProtected()) {
                    return;
                }

                FXGL.play(isMissile?"missileExplosion.wav":"explosion.wav");
                spawn("explosion", enemy.getCenter().subtract(60, 60));
                FXGL.inc("score", FXGLMath.random(10, 50));
                if (FXGLMath.randomBoolean(0.35)) {
                    spawn("items", new SpawnData(enemy.getCenter()).put("itemsName", FXGLMath.random(itemsAry).get()));
                }
                enemy.removeFromWorld();
            }
        };
        FXGL.getPhysicsWorld().addCollisionHandler(handler);

        CollisionHandler collisionBottom = new CollisionHandler(GameType.ENEMY, GameType.BOTTOM) {
            @Override
            protected void onCollisionEnd(Entity enemy, Entity b) {
                enemy.removeFromWorld();
            }
        };
        FXGL.getPhysicsWorld().addCollisionHandler(collisionBottom);
        FXGL.getPhysicsWorld().addCollisionHandler(collisionBottom.copyFor(GameType.ITEMS, GameType.BOTTOM));

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.PLAYER, GameType.BULLET) {

            @Override
            protected void onCollisionBegin(Entity p, Entity b) {
                b.removeFromWorld();
                if (FXGL.getb("protect")) {
                    return;
                }
                HealthIntComponent hp = p.getComponent(HealthIntComponent.class);
                hp.damage(1);
                //HP到了0就不会在变了
                if (hp.isZero()) {
                    //TODO Game Over
                }
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.PLAYER, GameType.ITEMS) {

            @Override
            protected void onCollisionBegin(Entity p, Entity items) {
                String name = items.getString("itemsName");
                FXGL.play("items.wav");
                items.removeFromWorld();

                if ("hp".equals(name)) {
                    HealthIntComponent hp = p.getComponent(HealthIntComponent.class);
                    if (hp.getValue() + 3 > hp.getMaxValue()) {
                        hp.setValue(hp.getMaxValue());
                    } else {
                        hp.damage(-3);
                    }
                }
                if ("arms".equals(name)) {
                    FXGL.inc("armsLevel", 1);
                }
                if ("missile".equals(name)) {
                    FXGL.inc("missileNum", 1);
                }

            }
        });

    }

    @Override
    protected void initUI() {
        Text descText = new Text("↑  ↓ ← → shoot:SPACE  rocket:F");
        descText.setFill(Color.LIGHTBLUE);
        descText.setFont(Font.font(28));
        FXGL.addUINode(descText, 200, FXGL.getAppHeight()-30);

        HBox missileBox = new HBox(10);
        missileBox.setAlignment(Pos.CENTER_LEFT);
        missileBox.getChildren().add(FXGL.texture("missileIcon.png"));
        Text text = new Text();
        text.setFill(Color.WHITE);
        text.setFont(Font.font(36));
        text.textProperty().bind(FXGL.getip("missileNum").asString());
        missileBox.getChildren().add(text);
        missileBox.setPrefWidth(FXGL.getAppWidth());
        missileBox.setLayoutY(FXGL.getAppHeight()-70);
        FXGL.addUINode(missileBox);

        HBox scoreBox = new HBox();
        scoreBox.setPrefWidth(FXGL.getAppWidth());
        scoreBox.setAlignment(Pos.CENTER_RIGHT);
        scoreBox.setPadding(new Insets(10));


        FXGL.addUINode(scoreBox);
        Image[] imgs = new Image[10];
        ImageView[] ivs = new ImageView[10];
        for (int i = 0; i < imgs.length; i++) {
            imgs[i] = FXGL.image("num/" + i + ".png", 32, 43);
            ivs[i] = new ImageView();
            scoreBox.getChildren().add(ivs[i]);
        }
        FXGL.getip("score").addListener((ob, ov, nv) -> {
            String s = nv + "";
            for (int i = 0; i < 10 - s.length(); i++) {
                ivs[i].setImage(null);
            }
            for (int i = 0; i < s.length(); i++) {
                ivs[10 - s.length() + i].setImage(imgs[Integer.parseInt(s.charAt(i) + "")]);
            }
        });
    }

    @Override
    protected void initGame() {
        launchTimer = FXGL.newLocalTimer();
        launchTimer.capture();
        getGameWorld().addEntityFactory(new GameEntityFactory());
        spawn("background");
        Entity point = spawn("movePoint");
        spawn("bottom");
        Entity playerEntity = spawn("player");
        player = playerEntity.getComponent(PlayerComponent.class);
        protectTimer = FXGL.newLocalTimer();
        Texture protectText = texture("protect.png");
        protectText.setTranslateY(-22.5);
        FXGL.getbp("protect").addListener((ob, ov, nv) -> {
            if (nv) {
                if (!playerEntity.getViewComponent().getChildren().contains(protectText)) {
                    playerEntity.getViewComponent().addChild(protectText);
                }
            } else {
                if (playerEntity.getViewComponent().getChildren().contains(protectText)) {
                    playerEntity.getViewComponent().removeChild(protectText);
                }
            }
        });
        FXGL.set("protect", true);

        FXGL.run(() -> {
            int random = FXGLMath.random(2, 6);
            for (int i = 0; i < random; i++) {
                spawn("enemy", FXGLMath.random(0, FXGL.getAppWidth() - 150), FXGLMath.random(point.getY() - FXGL.getAppHeight() / 2.0, point.getY() - FXGL.getAppHeight()));
            }
        }, Duration.seconds(2), Config.WAVE);
        FXGL.play("alert.wav");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
