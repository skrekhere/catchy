package dev.shrek.catchy.item;

import dev.shrek.catchy.config.ModConf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

import java.util.concurrent.atomic.AtomicReference;

import static dev.shrek.catchy.ExampleMod.LOGGER;

public class ItemBigJar extends Item{
    public ItemBigJar(Properties p) {
        super(p);
    }


    @Override
    public int getItemStackLimit(ItemStack stack) {
        return (containsEntity(stack)) ? 1 : 64;
    }

    @Override
    @Nonnull
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null)return ActionResultType.FAIL;
        Hand hand = Hand.MAIN_HAND;
        ItemStack stack = player.getMainHandItem();
        if (context.getLevel().isClientSide() || !containsEntity(stack)) return ActionResultType.FAIL;
        Entity entity = getEntityFromStack(stack, context.getLevel(), true);
        BlockPos blockPos = context.getClickedPos();
        entity.setPos(blockPos.getX() + 0.5d, blockPos.getY() + 1d, blockPos.getZ() + 0.5d);
        stack.setTag(null);
        player.setItemInHand(hand, stack);
        context.getLevel().addFreshEntity(entity);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        if (target.level.isClientSide() || target instanceof PlayerEntity || !target.isAlive() || containsEntity(stack))
            return ActionResultType.FAIL;
        String entityID = target.getType().getRegistryName().toString();
        if (isBlacklisted(entityID)) return ActionResultType.FAIL;
        ItemStack newStack = stack.copy();
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("entity", entityID);
        nbt.put("oldnbt", target.serializeNBT());
        nbt.putInt("CustomModelData", 1);
        target.deserializeNBT(nbt);
        ItemStack newerStack = newStack.split(1);
        newerStack.setTag(nbt);
        player.setItemSlot(EquipmentSlotType.MAINHAND, newStack);
        //player.setItemInHand(hand, newerStack);
        if(!player.addItem(newerStack)){
            ItemEntity itemEntity = new ItemEntity(player.level,player.getX(),player.getY(),player.getZ(),newerStack);
            player.level.addFreshEntity(itemEntity);
        }
        target.remove();
        player.getCooldowns().addCooldown(this, 5);
        return ActionResultType.PASS;
    }
    @OnlyIn(Dist.CLIENT)
    public ITextComponent getName(ItemStack item){
        if(containsEntity(item)){
            String entityName = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(item.getTag().getString("entity"))).getDescription().getString();
            //TranslationTextComponent t = new TranslationTextComponent();
            return new StringTextComponent(entityName + " in a Big Jar");
        }else{
            return new StringTextComponent("Big Jar");
        }
    }



    public static boolean containsEntity(@Nonnull ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("entity");
    }

    public static String getID(ItemStack stack) {
        return stack.getOrCreateTag().getString("entity");
    }

    public static boolean isBlacklisted(String entity) {
        return ModConf.BLACKLISTED.contains(entity);
    }

    public static Entity getEntityFromStack(ItemStack stack, World world, boolean withInfo) {
        Entity entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(stack.getTag().getString("entity"))).create(world);
        if (withInfo) entity.deserializeNBT(stack.getTag().getCompound("oldnbt"));
        return entity;
    }
}
