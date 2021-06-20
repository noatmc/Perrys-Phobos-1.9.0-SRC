package me.earth.phobos.features.modules.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.manager.FileManager;
import me.earth.phobos.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.stream.Collectors;

public
class Notifications
        extends Module {
    private static final String fileName = "phobos/util/ModuleMessage_List.txt";
    private static final List < String > modules = new ArrayList < String > ( );
    public static Set < EntityPlayer > strengthPlayers;
    public static Map < EntityPlayer, Integer > strMap;
    private static Notifications INSTANCE = new Notifications ( );

    static {
        strengthPlayers = new HashSet < EntityPlayer > ( );
        strMap = new HashMap < EntityPlayer, Integer > ( );
    }

    private final Timer timer = new Timer ( );
    private final List < EntityPlayer > burrowedPlayers = new ArrayList <> ( );
    public Setting < Boolean > totemPops = this.register ( new Setting < Boolean > ( "TotemPops" , true ) );
    public Setting < Boolean > totemNoti = this.register ( new Setting < Object > ( "TotemNoti" , Boolean.FALSE , v -> this.totemPops.getValue ( ) ) );
    public Setting < Integer > delay = this.register ( new Setting < Object > ( "Delay" , 0 , 0 , 5000 , v -> this.totemPops.getValue ( ) , "Delays messages." ) );
    public Setting < Boolean > clearOnLogout = this.register ( new Setting < Boolean > ( "LogoutClear" , false ) );
    public Setting < Boolean > moduleMessage = this.register ( new Setting < Boolean > ( "ModuleMessage" , true ) );
    private final Setting < Boolean > readfile = this.register ( new Setting < Object > ( "LoadFile" , Boolean.FALSE , v -> this.moduleMessage.getValue ( ) ) );
    public Setting < Boolean > list = this.register ( new Setting < Object > ( "List" , Boolean.FALSE , v -> this.moduleMessage.getValue ( ) ) );
    public Setting < Boolean > watermark = this.register ( new Setting < Object > ( "Watermark" , Boolean.TRUE , v -> this.moduleMessage.getValue ( ) ) );
    public Setting < Boolean > visualRange = this.register ( new Setting < Boolean > ( "VisualRange" , false ) );
    public Setting < Boolean > VisualRangeSound = this.register ( new Setting < Boolean > ( "VisualRangeSound" , false ) );
    public Setting < Boolean > coords = this.register ( new Setting < Object > ( "Coords" , Boolean.TRUE , v -> this.visualRange.getValue ( ) ) );
    public Setting < Boolean > leaving = this.register ( new Setting < Object > ( "Leaving" , Boolean.TRUE , v -> this.visualRange.getValue ( ) ) );
    public Setting < Boolean > pearls = this.register ( new Setting < Boolean > ( "PearlNotifs" , true ) );
    public Setting < Boolean > crash = this.register ( new Setting < Boolean > ( "Crash" , true ) );
    public Setting < Boolean > popUp = this.register ( new Setting < Boolean > ( "PopUpVisualRange" , false ) );
    public Setting < Boolean > burrow = this.register ( new Setting < Boolean > ( "Burrow" , false ) );
    public Setting < Boolean > strength = this.register ( new Setting < Boolean > ( "Strength" , false ) );
    public Timer totemAnnounce = new Timer ( );
    private List < EntityPlayer > knownPlayers = new ArrayList < EntityPlayer > ( );
    private boolean check;

    public
    Notifications ( ) {
        super ( "Notifications" , "Sends Messages." , Module.Category.CLIENT , true , false , false );
        this.setInstance ( );
    }

    public static
    Notifications getInstance ( ) {
        if ( INSTANCE == null ) {
            INSTANCE = new Notifications ( );
        }
        return INSTANCE;
    }

    public static
    void displayCrash ( Exception e ) {
        Command.sendMessage ( "\u00a7cException caught: " + e.getMessage ( ) );
    }

    private
    void setInstance ( ) {
        INSTANCE = this;
    }

    @Override
    public
    void onLoad ( ) {
        this.check = true;
        this.loadFile ( );
        this.check = false;
    }

    @Override
    public
    void onEnable ( ) {
        this.knownPlayers = new ArrayList < EntityPlayer > ( );
        if ( ! this.check ) {
            this.loadFile ( );
        }
    }

    @Override
    public
    void onUpdate ( ) {
        if ( this.readfile.getValue ( ) ) {
            if ( ! this.check ) {
                Command.sendMessage ( "Loading File..." );
                this.timer.reset ( );
                this.loadFile ( );
            }
            this.check = true;
        }
        if ( this.check && this.timer.passedMs ( 750L ) ) {
            this.readfile.setValue ( false );
            this.check = false;
        }
        if ( this.visualRange.getValue ( ) ) {
            ArrayList < EntityPlayer > tickPlayerList = new ArrayList <> ( Notifications.mc.world.playerEntities );
            if ( tickPlayerList.size ( ) > 0 ) {
                for (EntityPlayer player : tickPlayerList) {
                    if ( player.getName ( ).equals ( Notifications.mc.player.getName ( ) ) || this.knownPlayers.contains ( player ) )
                        continue;
                    this.knownPlayers.add ( player );
                    if ( Phobos.friendManager.isFriend ( player ) ) {
                        Command.sendMessage ( "Player \u00a7a" + player.getName ( ) + "\u00a7r" + " entered your visual range" + ( this.coords.getValue ( ) ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!" ) , this.popUp.getValue ( ) );
                    } else {
                        Command.sendMessage ( "Player \u00a7c" + player.getName ( ) + "\u00a7r" + " entered your visual range" + ( this.coords.getValue ( ) ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!" ) , this.popUp.getValue ( ) );
                    }
                    if ( this.VisualRangeSound.getValue ( ) ) {
                        me.earth.phobos.features.modules.client.Notifications.mc.player.playSound ( SoundEvents.BLOCK_ANVIL_LAND , 1.0f , 1.0f );
                    }
                    return;
                }
            }
            if ( this.knownPlayers.size ( ) > 0 ) {
                for (EntityPlayer player : this.knownPlayers) {
                    if ( tickPlayerList.contains ( player ) ) continue;
                    this.knownPlayers.remove ( player );
                    if ( this.leaving.getValue ( ) ) {
                        if ( Phobos.friendManager.isFriend ( player ) ) {
                            Command.sendMessage ( "Player \u00a7a" + player.getName ( ) + "\u00a7r" + " left your visual range" + ( this.coords.getValue ( ) ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!" ) , this.popUp.getValue ( ) );
                        } else {
                            Command.sendMessage ( "Player \u00a7c" + player.getName ( ) + "\u00a7r" + " left your visual range" + ( this.coords.getValue ( ) ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!" ) , this.popUp.getValue ( ) );
                        }
                    }
                    return;
                }
            }
        }
    }

    @Override
    public
    void onTick ( ) {
        if ( ! this.burrow.getValue ( ) ) {
            return;
        }
        for (EntityPlayer entityPlayer : mc.world.playerEntities.stream ( ).filter ( entityPlayer -> entityPlayer != mc.player ).collect ( Collectors.toList ( ) )) {
            if ( ! burrowedPlayers.contains ( entityPlayer ) && isInBurrow ( entityPlayer ) ) {
                Command.sendMessage ( ChatFormatting.RED + entityPlayer.getDisplayNameString ( ) + ChatFormatting.GREEN + " has burrowed." );
                burrowedPlayers.add ( entityPlayer );
            }
        }
    }

    private
    boolean isInBurrow ( EntityPlayer entityPlayer ) {
        BlockPos playerPos = new BlockPos ( getMiddlePosition ( entityPlayer.posX ) , entityPlayer.posY , getMiddlePosition ( entityPlayer.posZ ) );

        return mc.world.getBlockState ( playerPos ).getBlock ( ) == Blocks.OBSIDIAN
                || mc.world.getBlockState ( playerPos ).getBlock ( ) == Blocks.ENDER_CHEST
                || mc.world.getBlockState ( playerPos ).getBlock ( ) == Blocks.ANVIL;
    }

    private
    double getMiddlePosition ( double positionIn ) {
        double positionFinal = Math.round ( positionIn );

        if ( Math.round ( positionIn ) > positionIn ) {
            positionFinal -= 0.5;
        } else if ( Math.round ( positionIn ) <= positionIn ) {
            positionFinal += 0.5;
        }

        return positionFinal;
    }

    @SubscribeEvent
    public
    void onPotionColor ( final PotionColorCalculationEvent event ) {
        if ( ! this.strength.getValue ( ) ) {
            return;
        }
        if ( event.getEntityLiving ( ) instanceof EntityPlayer ) {
            boolean hasStrength = false;
            for (final PotionEffect potionEffect : event.getEffects ( )) {
                if ( potionEffect.getPotion ( ) == MobEffects.STRENGTH ) {
                    strMap.put ( (EntityPlayer) event.getEntityLiving ( ) , potionEffect.getAmplifier ( ) );
                    Command.sendMessage ( ChatFormatting.RED + event.getEntityLiving ( ).getName ( ) + ChatFormatting.GREEN + " has strength." );
                    hasStrength = true;
                    break;
                }
            }
            if ( strMap.containsKey ( event.getEntityLiving ( ) ) && ! hasStrength ) {
                strMap.remove ( event.getEntityLiving ( ) );
                Command.sendMessage ( ChatFormatting.RED + event.getEntityLiving ( ).getName ( ) + ChatFormatting.GREEN + " no longer has strength" );
            }
        }
    }

    public
    void loadFile ( ) {
        List < String > fileInput = FileManager.readTextFileAllLines ( fileName );
        Iterator < String > i = fileInput.iterator ( );
        modules.clear ( );
        while ( i.hasNext ( ) ) {
            String s = i.next ( );
            if ( s.replaceAll ( "\\s" , "" ).isEmpty ( ) ) continue;
            modules.add ( s );
        }
    }

    @SubscribeEvent
    public
    void onReceivePacket ( PacketEvent.Receive event ) {
        if ( event.getPacket ( ) instanceof SPacketSpawnObject && this.pearls.getValue ( ) ) {
            SPacketSpawnObject packet = event.getPacket ( );
            EntityPlayer player = Notifications.mc.world.getClosestPlayer ( packet.getX ( ) , packet.getY ( ) , packet.getZ ( ) , 1.0 , false );
            if ( player == null ) {
                return;
            }
            if ( packet.getEntityID ( ) == 85 ) {
                Command.sendMessage ( "\u00a7cPearl thrown by " + player.getName ( ) + " at X:" + (int) packet.getX ( ) + " Y:" + (int) packet.getY ( ) + " Z:" + (int) packet.getZ ( ) );
            }
        }
    }

    @SubscribeEvent
    public
    void onToggleModule ( ClientEvent event ) {
        int moduleNumber;
        Module module;
        if ( ! this.moduleMessage.getValue ( ) ) {
            return;
        }
        if ( ! ( event.getStage ( ) != 0 || ( module = (Module) event.getFeature ( ) ).equals ( this ) || ! modules.contains ( module.getDisplayName ( ) ) && this.list.getValue ( ) ) ) {
            moduleNumber = 0;
            for (char character : module.getDisplayName ( ).toCharArray ( )) {
                moduleNumber += character;
                moduleNumber *= 10;
            }
            if ( this.watermark.getValue ( ) ) {
                TextComponentString textComponentString = new TextComponentString ( Phobos.commandManager.getClientMessage ( ) + " " + "\u00a7r" + "\u00a7c" + module.getDisplayName ( ) + " disabled." );
                Notifications.mc.ingameGUI.getChatGUI ( ).printChatMessageWithOptionalDeletion ( textComponentString , moduleNumber );
            } else {
                TextComponentString textComponentString = new TextComponentString ( "\u00a7c" + module.getDisplayName ( ) + " disabled." );
                Notifications.mc.ingameGUI.getChatGUI ( ).printChatMessageWithOptionalDeletion ( textComponentString , moduleNumber );
            }
        }
        if ( event.getStage ( ) == 1 && ( modules.contains ( ( module = (Module) event.getFeature ( ) ).getDisplayName ( ) ) || ! this.list.getValue ( ) ) ) {
            moduleNumber = 0;
            for (char character : module.getDisplayName ( ).toCharArray ( )) {
                moduleNumber += character;
                moduleNumber *= 10;
            }
            if ( this.watermark.getValue ( ) ) {
                TextComponentString textComponentString = new TextComponentString ( Phobos.commandManager.getClientMessage ( ) + " " + "\u00a7r" + "\u00a7a" + module.getDisplayName ( ) + " enabled." );
                Notifications.mc.ingameGUI.getChatGUI ( ).printChatMessageWithOptionalDeletion ( textComponentString , moduleNumber );
            } else {
                TextComponentString textComponentString = new TextComponentString ( "\u00a7a" + module.getDisplayName ( ) + " enabled." );
                Notifications.mc.ingameGUI.getChatGUI ( ).printChatMessageWithOptionalDeletion ( textComponentString , moduleNumber );
            }
        }
    }
}