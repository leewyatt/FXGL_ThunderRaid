package com.itcodebox.game.component;

import com.almasb.fxgl.entity.component.Component;
import com.itcodebox.game.Config;

/**
 * @author LeeWyatt
 */
public class BottomComponent extends Component {

    @Override
    public void onUpdate(double tpf) {
        entity.translateY(-Config.BG_SCROLL_SPEED * tpf);
    }

}
