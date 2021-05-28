package me.earth.phobos.features.modules.movement;

import me.earth.phobos.features.modules.Module;
import net.minecraft.block.material.Material;

public
class ReverseStep
        extends Module {
    public
    ReverseStep ( ) {
        super ( "ReverseStep" , "Screams chinese words and teleports you" , Module.Category.MOVEMENT , true , false , false );
    }

    @Override
    public
    void onUpdate ( ) {
        if ( mc.player == null || mc.world == null || mc.player.isInWater ( ) || mc.player.isInLava ( ) || mc.player.isInsideOfMaterial ( Material.GROUND ) ) {
            return;
        }
        if ( ReverseStep.mc.player.onGround ) {
            ReverseStep.mc.player.motionY -= 1.0;
        }
    }
}

