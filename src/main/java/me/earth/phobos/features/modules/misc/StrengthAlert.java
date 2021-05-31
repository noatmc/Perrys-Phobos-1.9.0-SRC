package me.earth.phobos.features.modules.misc;

import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public
class StrengthAlert extends Module {
    public static final Set < EntityPlayer > strengthPlayers;
    public static final Map < EntityPlayer, Integer > strMap;

    static {
        strengthPlayers = new HashSet < EntityPlayer > ( );
        strMap = new HashMap < EntityPlayer, Integer > ( );
    }

    public
    StrengthAlert ( ) {
        super ( "StrengthAlert" , "Alerts u if players strength since apparently ur blind." , Module.Category.MISC , true , false , false );
    }

    @SubscribeEvent
    public
    void onPotionColor ( final PotionColorCalculationEvent event ) {
        if ( event.getEntityLiving ( ) instanceof EntityPlayer ) {
            boolean hasStrength = false;
            for (final PotionEffect potionEffect : event.getEffects ( )) {
                if ( potionEffect.getPotion ( ) == MobEffects.STRENGTH ) {
                    StrengthAlert.strMap.put ( (EntityPlayer) event.getEntityLiving ( ) , potionEffect.getAmplifier ( ) );
                    Command.sendMessage ( event.getEntityLiving ( ).getName ( ) + " has strength" );
                    hasStrength = true;
                    break;
                }
            }
            if ( StrengthAlert.strMap.containsKey ( event.getEntityLiving ( ) ) && ! hasStrength ) {
                StrengthAlert.strMap.remove ( event.getEntityLiving ( ) );
                Command.sendMessage ( event.getEntityLiving ( ).getName ( ) + " no longer has strength" );
            }
        }
    }
}