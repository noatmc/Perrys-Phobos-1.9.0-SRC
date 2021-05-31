package me.earth.phobos.features.modules.render;

import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.features.modules.Module;

public class ViewModel extends Module
{
    public Setting<Float> sizeX;
    public Setting<Float> sizeY;
    public Setting<Float> sizeZ;
    public Setting<Float> rotationX;
    public Setting<Float> rotationY;
    public Setting<Float> rotationZ;
    public Setting<Float> positionX;
    public Setting<Float> positionY;
    public Setting<Float> positionZ;
    public Setting<Float> itemFOV;
    private static ViewModel INSTANCE;

    public ViewModel() {
        super("ViewModel", "Changes view of items held in ur main/offhand", Module.Category.RENDER, false, false, false);
        this.sizeX = (Setting<Float>)this.register(new Setting <> ("Size-X", 1.0f, 0.0f, 2.0f));
        this.sizeY = (Setting<Float>)this.register(new Setting <> ("Size-Y", 1.0f, 0.0f, 2.0f));
        this.sizeZ = (Setting<Float>)this.register(new Setting <> ("Size-X", 1.0f, 0.0f, 2.0f));
        this.rotationX = (Setting<Float>)this.register(new Setting <> ("Rotation-X", 0.0f, 0.0f, 1.0f));
        this.rotationY = (Setting<Float>)this.register(new Setting <> ("Rotation-Y", 0.0f, 0.0f, 1.0f));
        this.rotationZ = (Setting<Float>)this.register(new Setting <> ("Rotation-Z", 0.0f, 0.0f, 1.0f));
        this.positionX = (Setting<Float>)this.register(new Setting <> ("Position-X", 0.0f, (-2.0f), 2.0f));
        this.positionY = (Setting<Float>)this.register(new Setting <> ("Position-Y", 0.0f, (-2.0f), 2.0f));
        this.positionZ = (Setting<Float>)this.register(new Setting <> ("Position-Z", 0.0f, (-2.0f), 2.0f));
        this.setInstance();
    }

    private void setInstance() {
        ViewModel.INSTANCE = this;
    }

    public static ViewModel getINSTANCE() {
        if (ViewModel.INSTANCE == null) {
            ViewModel.INSTANCE = new ViewModel();
        }
        return ViewModel.INSTANCE;
    }

    static {
        ViewModel.INSTANCE = new ViewModel();
    }
}