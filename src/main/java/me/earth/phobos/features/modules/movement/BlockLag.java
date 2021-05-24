package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.RotationUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public
class BlockLag
        extends Module {
    private static BlockLag INSTANCE;
    private final Timer timer = new Timer ( );
    private final Setting < Boolean > packet = this.register ( new Setting < Boolean > ( "Packet" , true ) );
    private final Setting < Boolean > invalidPacket = this.register ( new Setting < Boolean > ( "InvalidPacket" , false ) );
    private final Setting < Integer > rotations = this.register ( new Setting < Integer > ( "Rotations" , 5 , 1 , 10 ) );
    private final Setting < Integer > timeOut = this.register ( new Setting < Integer > ( "TimeOut" , 194 , 0 , 1000 ) );
    private BlockPos startPos;
    private int lastHotbarSlot = - 1;
    private int blockSlot = - 1;

    public
    BlockLag ( ) {
        super ( "BlockLag" , "Lags You back" , Module.Category.MOVEMENT , true , false , false );
        INSTANCE = this;
    }

    public static
    BlockLag getInstance ( ) {
        if ( INSTANCE == null ) {
            INSTANCE = new BlockLag ( );
        }
        return INSTANCE;
    }

    @Override
    public
    void onEnable ( ) {
        this.lastHotbarSlot = - 1;
        this.blockSlot = - 1;
        if ( BlockLag.fullNullCheck ( ) ) {
            this.disable ( );
            return;
        }
        this.blockSlot = this.findBlockSlot ( );
        this.startPos = new BlockPos ( BlockLag.mc.player.getPositionVector ( ) );
        if ( ! BlockUtil.isElseHole ( this.startPos ) || this.blockSlot == - 1 ) {
            this.disable ( );
            return;
        }
        BlockLag.mc.player.jump ( );
        this.timer.reset ( );
    }

    @SubscribeEvent
    public
    void onUpdateWalkingPlayer ( UpdateWalkingPlayerEvent event ) {
        if ( event.getStage ( ) != 0 || ! this.timer.passedMs ( this.timeOut.getValue ( ).intValue ( ) ) ) {
            return;
        }
        this.lastHotbarSlot = BlockLag.mc.player.inventory.currentItem;
        InventoryUtil.switchToHotbarSlot ( this.blockSlot , false );
        for (int i = 0; i < this.rotations.getValue ( ); ++ i) {
            RotationUtil.faceVector ( new Vec3d ( this.startPos ) , true );
        }
        BlockUtil.placeBlock ( this.startPos , this.blockSlot == - 2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND , false , this.packet.getValue ( ) , BlockLag.mc.player.isSneaking ( ) );
        InventoryUtil.switchToHotbarSlot ( this.lastHotbarSlot , false );
        if ( this.invalidPacket.getValue ( ).booleanValue ( ) ) {
            BlockLag.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( BlockLag.mc.player.posX , 1337.0 , BlockLag.mc.player.posZ , true ) );
        }
        this.disable ( );
    }

    private
    int findBlockSlot ( ) {
        int obbySlot = InventoryUtil.findHotbarBlock ( BlockObsidian.class );
        if ( obbySlot == - 1 ) {
            if ( InventoryUtil.isBlock ( BlockLag.mc.player.getHeldItemOffhand ( ).getItem ( ) , BlockObsidian.class ) ) {
                return - 2;
            }
            int echestSlot = InventoryUtil.findHotbarBlock ( BlockEnderChest.class );
            if ( echestSlot == - 1 && InventoryUtil.isBlock ( BlockLag.mc.player.getHeldItemOffhand ( ).getItem ( ) , BlockEnderChest.class ) ) {
                return - 2;
            }
            return - 1;
        }
        return obbySlot;
    }
}

