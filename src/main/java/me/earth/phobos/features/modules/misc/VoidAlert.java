package me.earth.phobos.features.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;

public
class VoidAlert extends Module {
    public
    VoidAlert ( ) {
        super ( "VoidAlert" , "Alerts you if someone is being gay." , Category.MISC , false , false , false );
    }

    @Override
    public
    void onUpdate ( ) {
        double yLevel = mc.player.posY;
        if ( yLevel <= .5 ) {
            Command.sendMessage ( ChatFormatting.GREEN + mc.player.getName ( ) + ChatFormatting.RED + " Is in the void!" );
        }
    }
}