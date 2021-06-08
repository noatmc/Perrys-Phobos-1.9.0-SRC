package me.earth.phobos.module.modules.exploit;

import me.earth.phobos.command.Command;
import me.earth.phobos.event.events.*;
import me.earth.phobos.module.Module;
import me.earth.phobos.module.Module.Category;
import me.earth.phobos.setting.Setting;
import me.earth.phobos.util.Timer;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
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
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DecimalFormat;


public class InstaMine extends Module {

	private BlockPos renderBlock;
	private BlockPos lastBlock;
	private boolean packetCancel = false;
	private Timer breaktimer = new Timer();
  private EnumFacing direction;

  public Setting <Boolean> picOnly = this.register(new Setting<Boolean>("PickaxeOnly"), true);
  public Setting <Integer> delay = this.register (new Setting<Integer>("Range", 5 ,1 , 6 ));
  public Setting <Boolean> autoBreak = this.register(new Setting<Boolean>("AutoBreak"), true);

	public static InstaMine INSTANCE;

  /*
    Original Author : Kami147 (https://github.com/Kami147)
  */
  
	@Override
	protected void onEnable() {
		INSTANCE = this;
	}

	public static InstaMine getInstance(){
		if(INSTANCE==null){
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
		if (canBreak(p_Event.getPos())) {

			if(lastBlock==null||p_Event.getPos().x!=lastBlock.x || p_Event.getPos().y!=lastBlock.y || p_Event.getPos().z!=lastBlock.z) {
				//Command.sendChatMessage("New Block");
				packetCancel = false;
				//Command.sendChatMessage(p_Event.getPos()+" : "+lastBlock);
				mc.player.swingArm(EnumHand.MAIN_HAND);
				mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
						p_Event.getPos(), p_Event.getDirection()));
				packetCancel = true;
			}else{
				packetCancel = true;
			}
			//Command.sendChatMessage("Breaking");
			mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
					p_Event.getPos(), p_Event.getDirection()));

			renderBlock = p_Event.getPos();
			lastBlock = p_Event.getPos();
			direction = p_Event.getDirection();

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
