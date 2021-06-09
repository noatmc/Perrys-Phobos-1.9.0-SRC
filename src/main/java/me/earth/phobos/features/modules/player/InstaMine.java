package me.earth.phobos.features.modules.player;

// phobos sutff
import me.earth.phobos.features.command.Command;
import me.earth.phobos.event.events.*;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.Timer;
// alpine stuff
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
// minecraft
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
// lwjgl
import org.lwjgl.opengl.GL11;
// java stuff
import java.awt.*;
import java.text.DecimalFormat;


public class InstaMine extends Module {

	private BlockPos renderBlock;
	private BlockPos lastBlock;
	private boolean packetCancel = false;
	private Timer breaktimer = new Timer();
  private EnumFacing direction;

  public Setting <Boolean> picOnly = this.register(new Setting<Boolean>("PickaxeOnly", true));
  public Setting <Integer> delay = this.register(new Setting<Integer>("Range", 5 ,1 , 6 ));
  public Setting <Boolean> autoBreak = this.register(new Setting<Boolean>("AutoBreak", true));

	public static InstaMine INSTANCE;

  /*
  Original Author : Kami147 (https://github.com/Kami147)
  */

  public InstaMine() {
    super("InstaMine", "Instantly breaks the block using exploit :)", Module.Category.PLAYER, true, false, false);
  }

	@Override
	public void onEnable() {
		INSTANCE = this;
	}

	public static InstaMine getInstance(){
		if( INSTANCE == null ){
			INSTANCE = new InstaMine();
		}
		return INSTANCE;
	}

	@Override
	public void onUpdate() {
		if(renderBlock != null) {
			if(autoBreak.getValue() && breaktimer.passedMs(delay.getValue())) {
				if(picOnly.getValue()&&!(mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.DIAMOND_PICKAXE)) return;
				mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
						renderBlock, direction));
				breaktimer.reset();
			}

		}

		try {
			mc.playerController.blockHitDelay = 0;

		} catch (Exception e) {
		}
	}

	@EventHandler
	private Listener<PacketEvent.Send> packetSendListener = new Listener<>(event -> {
		Packet packet = event.getPacket();
		if (packet instanceof CPacketPlayerDigging) {
			CPacketPlayerDigging digPacket = (CPacketPlayerDigging) packet;
			if(((CPacketPlayerDigging) packet).getAction()== CPacketPlayerDigging.Action.START_DESTROY_BLOCK && packetCancel) event.cancel();
		}
	});

	@EventHandler
	private Listener<DamageBlockEvent> OnDamageBlock = new Listener<>(p_Event -> {
		if (canBreak(p_Event.getBlockPos())) {

			if( lastBlock == null || p_Event.getBlockPos().x != lastBlock.x || p_Event.getBlockPos().y != lastBlock.y || p_Event.getBlockPos().z != lastBlock.z) {
				//Command.sendChatMessage("New Block");
				packetCancel = false;
				//Command.sendChatMessage(p_Event.getBlockPos()+" : "+lastBlock);
				mc.player.swingArm(EnumHand.MAIN_HAND);
				mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
						p_Event.getBlockPos(), p_Event.getEnumFacing()));
				packetCancel = true;
			} else {
				packetCancel = true;
			}
			//Command.sendChatMessage("Breaking");
			mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
					p_Event.getBlockPos(), p_Event.getEnumFacing()));

			renderBlock = p_Event.getBlockPos();
			lastBlock = p_Event.getBlockPos();
			direction = p_Event.getEnumFacing();

			p_Event.cancel();

		}
	});

	private boolean canBreak(BlockPos pos) {
		final IBlockState blockState = mc.world.getBlockState(pos);
		final Block block = blockState.getBlock();

		return block.getBlockHardness(blockState, mc.world, pos) != -1;
	}

	public BlockPos getTarget(){
		return renderBlock;
	}

	public void setTarget(BlockPos pos){
		renderBlock = pos;
		packetCancel = false;
		mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
				pos, EnumFacing.DOWN));
		packetCancel = true;
		mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
				pos, EnumFacing.DOWN));
		direction = EnumFacing.DOWN;
		lastBlock = pos;
	}

}
