package pro.komaru.tridot.api.render.bossbars;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.sounds.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.common.networking.packets.*;
import pro.komaru.tridot.util.Col;

import java.util.*;

public abstract class TridotBossBar{
    public final UUID id;
    public Component name;
    public float health;
    public float maxHealth;
    public float percentage;
    public boolean aboutToDie = false;

    public Col color = Col.white;
    public SoundEvent bossMusic = SoundEvents.EMPTY;
    public ResourceLocation texture;

    public boolean rainbow = false;
    public boolean createWorldFog = false;
    public boolean playBossMusic = false;
    public boolean darkenScreen = false;
    public ResourceLocation clientBossbarType = Tridot.ofTridot("generic");

    public TridotBossBar(UUID pId, Component pName){
        this.id = pId;
        this.name = pName;
        this.health = 0.0F;
        this.maxHealth = 0.0F;
        this.percentage = 0.0F;
    }

    public TridotBossBar setType(ResourceLocation typeId) {
        this.clientBossbarType = typeId;
        return this;
    }

    public ResourceLocation getType() {
        return this.clientBossbarType;
    }

    public UUID getId() {
        return this.id;
    }

    public Component getName() {
        return this.name;
    }

    public TridotBossBar setName(Component pName) {
        this.name = pName;
        return this;
    }

    public TridotBossBar setAboutToDie(boolean aboutToDie){
        this.aboutToDie = aboutToDie;
        return this;
    }

    public boolean isAboutToDie(){
        return aboutToDie;
    }

    public TridotBossBar setPercentage(float percentage) {
        this.percentage = percentage;
        return this;
    }

    public float getPercentage() {
        return percentage;
    }

    public float getMaxHealth(){
        return maxHealth;
    }

    public float getHealth(){
        return health;
    }

    public TridotBossBar setHealth(float health, float maxHealth){
        this.health = health;
        this.maxHealth = maxHealth;
        return this;
    }

    public TridotBossBar setBossMusic(SoundEvent music) {
        this.bossMusic = music;
        return this;
    }

    public TridotBossBar setPlayBossMusic(boolean playBossMusic){
        this.playBossMusic = playBossMusic;
        return this;
    }

    public SoundEvent getBossMusic() {
        return this.bossMusic;
    }

    public boolean shouldPlayBossMusic() {
        return this.playBossMusic;
    }

    public boolean shouldCreateWorldFog(){
        return createWorldFog;
    }

    public TridotBossBar setCreateWorldFog(boolean pCreateFog) {
        this.createWorldFog = pCreateFog;
        return this;
    }

    public boolean shouldDarkenScreen() {
        return this.darkenScreen;
    }

    public TridotBossBar setDarkenScreen(boolean pDarkenSky) {
        this.darkenScreen = pDarkenSky;
        return this;
    }

    public Col getColor(){
        return color;
    }

    public TridotBossBar setColor(Col color){
        this.color = color;
        return this;
    }

    public ResourceLocation getTexture(){
        return texture;
    }

    public TridotBossBar setTexture(ResourceLocation texture){
        this.texture = texture;
        return this;
    }

    public TridotBossBar setRainbow(boolean rainbow){
        this.rainbow = rainbow;
        return this;
    }

    public boolean isRainbow(){
        return rainbow;
    }

    public void readCustomData(FriendlyByteBuf buf) {

    }
}