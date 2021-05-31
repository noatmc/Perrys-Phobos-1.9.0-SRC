package me.earth.phobos.features.modules.movement;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.*;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public
class BlockLag
        extends Module {
    private static BlockLag INSTANCE;

    private final Setting < Mode > mode = this.register ( new Setting < Mode > ( "Mode" , Mode.OBSIDIAN ) );
    private final Setting < Boolean > smartTp = this.register ( new Setting < Boolean > ( "SmartTP" , true ) );
    private final Setting < Integer > tpMin = this.register ( new Setting < Integer > ( "TPMin" , 4 , 3 , 10 , v -> this.smartTp.getValue ( ) ) );
    private final Setting < Integer > tpMax = this.register ( new Setting < Integer > ( "TPMax" , 25 , 10 , 40 , v -> this.smartTp.getValue ( ) ) );
    private final Setting < Boolean > noVoid = this.register ( new Setting < Boolean > ( "NoVoid" , true, v -> this.smartTp.getValue ( ) ) );
    private final Setting < Integer > tpHeight = this.register ( new Setting < Integer > ( "TPHeight" , 25 , -40 , 40 , v -> ! this.smartTp.getValue ( ) ) );
    private final Setting < Boolean > keepInside = this.register ( new Setting < Boolean > ( "KeepInside" , true ) );
    private final Setting < Boolean > rotate = this.register ( new Setting < Boolean > ( "Rotate" , false ) );
    private final Setting < Boolean > sneaking = this.register ( new Setting < Boolean > ( "Sneak" , false ) );
    private final Setting < Boolean > offground = this.register ( new Setting < Boolean > ( "Offground" , false ) );
    private final Setting < Boolean > chat = this.register ( new Setting < Boolean > ( "Chat Msgs" , true ) );
    private final Setting < Boolean > tpdebug = this.register ( new Setting < Boolean > ( "Debug" , false, v -> this.chat.getValue ( ) && this.smartTp.getValue ( ) ) );

    private BlockPos burrowPos;
    private int lastBlock;
    private int blockSlot;
    private Class block;
    private String name;
    private Vec3d back;

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

        this.burrowPos = new BlockPos ( BlockLag.mc.player.posX , Math.ceil ( BlockLag.mc.player.posY ) , BlockLag.mc.player.posZ );
        this.blockSlot = this.findBlockSlot ( );
        this.lastBlock = BlockLag.mc.player.inventory.currentItem;

        if ( ! doChecks ( ) || this.blockSlot == - 1 ) {
            this.disable ( );
            return;
        }

        if ( this.keepInside.getValue ( ) ) {
            double x = BlockLag.mc.player.posX - Math.floor ( BlockLag.mc.player.posX );
            double z = BlockLag.mc.player.posZ - Math.floor ( BlockLag.mc.player.posZ );

            if ( x <= 0.3 || x >= 0.7 ) {
                x = ( x > 0.5 ? 0.69 : 0.31 );
            }

            if ( z < 0.3 || z > 0.7 ) {
                z = ( z > 0.5 ? 0.69 : 0.31 );
            }

            BlockLag.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( Math.floor ( BlockLag.mc.player.posX ) + x , BlockLag.mc.player.posY , Math.floor ( BlockLag.mc.player.posZ ) + z , BlockLag.mc.player.onGround ) );
            BlockLag.mc.player.setPosition ( Math.floor ( BlockLag.mc.player.posX ) + x , BlockLag.mc.player.posY , Math.floor ( BlockLag.mc.player.posZ ) + z );
            // no fucking clue how this worked i made it drunk
        }

        BlockLag.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( mc.player.posX , mc.player.posY + 0.41999998688698D , mc.player.posZ , ! this.offground.getValue ( ) ) );
        BlockLag.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( mc.player.posX , mc.player.posY + 0.7531999805211997D , mc.player.posZ , ! this.offground.getValue ( ) ) );
        BlockLag.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( mc.player.posX , mc.player.posY + 1.00133597911214D , mc.player.posZ , ! this.offground.getValue ( ) ) );
        BlockLag.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( mc.player.posX , mc.player.posY + 1.16610926093821D , mc.player.posZ , ! this.offground.getValue ( ) ) );
    }

    @SubscribeEvent
    public
    void onUpdateWalkingPlayer ( UpdateWalkingPlayerEvent event ) {
        if ( event.getStage ( ) != 0 ) {
            return;
        }

        if ( this.rotate.getValue ( ) ) {
            float[] angle = MathUtil.calcAngle(BlockLag.mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((float) burrowPos.getX() + 0.5f, (float) burrowPos.getY() + 0.5f, (float) burrowPos.getZ() + 0.5f));
            Phobos.rotationManager.setPlayerRotations(angle[0], angle[1]);
        }

        InventoryUtil.switchToHotbarSlot ( this.blockSlot , false );
        BlockUtil.placeBlock ( this.burrowPos , this.blockSlot == - 2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND , false , true , this.sneaking.getValue ( ) );
        InventoryUtil.switchToHotbarSlot( this.lastBlock , false );

        BlockLag.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( BlockLag.mc.player.posX , ( this.smartTp.getValue ( ) ? this.adaptiveTpHeight ( ) : this.tpHeight.getValue ( ) + BlockLag.mc.player.posY ) , BlockLag.mc.player.posZ , ! this.offground.getValue ( ) ) );

        this.disable ( );
    }

    private
    int findBlockSlot ( ) {

        // theres prob an easier way to do this but im retarded
        // bobos uses "this." everywhere so im just gonna keep doing it even though its kinda pointless
        switch ( this.mode.getValue ( ) ) {
            case ECHEST:
                this.block = BlockEnderChest.class;
                this.name = "Ender Chests";
                break;
            case OBSIDIAN:
                this.block = BlockObsidian.class;
                this.name = "Obsidian";
                break;
            case SOULSAND:
                this.block = BlockSoulSand.class;
                this.name = "Soul Sand";
                break;
        }

        int slot = InventoryUtil.findHotbarBlock ( this.block );
        if ( slot == - 1 ) {
            if ( InventoryUtil.isBlock ( BlockLag.mc.player.getHeldItemOffhand ( ).getItem ( ) , this.block ) ) {
                return - 2;
            } else {
                if ( this.chat.getValue ( ) ) Command.sendMessage ( "\u00A77" + this.displayName.getValue ( ) + ":\u00A7c No " + this.name + " to use" );
            }
        }

        return slot;
    }

    private
    int adaptiveTpHeight ( ) {

        int airblock = ( this.noVoid.getValue ( ) && this.tpMax.getValue ( ) * - 1 + this.burrowPos.getY ( ) < 0 ? this.burrowPos.getY ( ) * - 1 : this.tpMax.getValue ( ) * - 1 );

        while ( airblock < this.tpMax.getValue ( ) ) {
            if ( Math.abs ( airblock ) < this.tpMin.getValue ( ) || ! BlockLag.mc.world.isAirBlock ( this.burrowPos.offset ( EnumFacing.UP, airblock ) ) || ! BlockLag.mc.world.isAirBlock ( this.burrowPos.offset ( EnumFacing.UP, airblock + 1 ) ) ) {
                airblock++;
            } else {
                if ( this.tpdebug.getValue ( ) ) Command.sendMessage ( Integer.toString ( airblock ) );
                return this.burrowPos.getY ( ) + airblock;
            }
        }

        return 69420; // if there isn't any room
    }

    private boolean
    doChecks ( ) {

        if ( ! BlockLag.fullNullCheck ( ) ) {

            if ( this.smartTp.getValue ( ) ) {
                if ( adaptiveTpHeight ( ) == 69420 ) { // check if there is room to tp
                    if ( this.chat.getValue ( ) ) Command.sendMessage ( "\u00A77" + this.displayName.getValue() + ":\u00A7c Not enough room" );
                    return false;
                }
            }

            if ( BlockLag.mc.world.getBlockState ( this.burrowPos ).getBlock ( ).equals ( Blocks.OBSIDIAN ) ) { // is the player already burrowed
                return false;
            }

            if ( ! BlockLag.mc.world.isAirBlock ( this.burrowPos.offset ( EnumFacing.UP, 2 ) ) ) { // is the player trapped
                if ( this.chat.getValue ( ) ) Command.sendMessage ( "\u00A77" + this.displayName.getValue ( ) + ":\u00A7c Not enough room");
                return false;
            }

            for ( final Entity entity : BlockLag.mc.world.loadedEntityList ) {
                if ( ! ( entity instanceof EntityItem ) && ! entity.equals ( BlockLag.mc.player ) ) {
                    if ( new AxisAlignedBB ( burrowPos ).intersects ( entity.getEntityBoundingBox ( ) ) ) { // is there another player in the hole
                        if ( this.chat.getValue ( ) ) Command.sendMessage ( "\u00A77" + this.displayName.getValue ( ) + ":\u00A7c Not enough room");
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    public
    enum Mode {
        OBSIDIAN,
        ECHEST,
        SOULSAND

    }
}
