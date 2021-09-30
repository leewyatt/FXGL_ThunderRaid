package com.itcodebox.game.component;

import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.itcodebox.game.Config;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author LeeWyatt
 */
public class MovePointComponent extends Component {

    @Override
    public void onUpdate(double tpf) {
        entity.translateY(-Config.BG_SCROLL_SPEED * tpf);

    }

    @Override
    public void onAdded() {
        Viewport viewport = getGameScene().getViewport();
        viewport.setBounds(0, Integer.MIN_VALUE, getAppWidth(), getAppHeight());
        viewport.bindToEntity(entity, FXGL.getAppWidth()/2.0, FXGL.getAppHeight()/2.0);
    }
}
