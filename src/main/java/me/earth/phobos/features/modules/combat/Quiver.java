package me.earth.phobos.features.modules.combat;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.PotionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public
class Quiver extends Module {
    private final Setting < Integer > tickDelay = register ( new Setting <> ( "TickDelay" , 3 , 3 , 10 ) );

    public
    Quiver ( ) {
        super ( "Quiver" , "Rotates upwards and shoots yourself with good potion effects." , Module.Category.COMBAT , true , false , false );
    }

    @Override
    public
    void onUpdate ( ) {
        if ( mc.player != null ) {
            if ( mc.player.inventory.getCurrentItem ( ).getItem ( ) instanceof ItemBow && mc.player.isHandActive ( ) && mc.player.getItemInUseMaxCount ( ) >= tickDelay.getValue ( ) ) {
                mc.player.connection.sendPacket ( new CPacketPlayer.Rotation ( mc.player.cameraYaw , - 90f , mc.player.onGround ) );
                mc.playerController.onStoppedUsingItem ( mc.player );
            }
            List < Integer > arrowSlots = Collections.singletonList ( InventoryUtil.getItemHotbar ( Items.TIPPED_ARROW ) );
            if ( arrowSlots.get ( 0 ) == - 1 ) return;
            for (Integer slot : arrowSlots) {
                if ( ! ( Objects.requireNonNull ( PotionUtils.getPotionFromItem ( mc.player.inventory.getStackInSlot ( slot ) ).getRegistryName ( ) ) ).getPath ( ).contains ( "swiftness" ) ) {
                    Objects.requireNonNull ( PotionUtils.getPotionFromItem ( mc.player.inventory.getStackInSlot ( slot ) ).getRegistryName ( ) ).getPath ( );
                }
            }
        }
    }
}