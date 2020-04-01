package insane96mcp.iguanatweaksreborn.modules;

import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.setup.ModConfig;
import insane96mcp.iguanatweaksreborn.utils.RandomHelper;
import insane96mcp.iguanatweaksreborn.utils.Utils;
import net.minecraft.block.*;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.Collections;

public class FarmingModule {

	public static class Livestock {

	}

	public static class Agriculture {

		/**
		 * Handles crops require water too
		 */
		public static void nerfBonemeal(BonemealEvent event) {
			if (event.getWorld().isRemote)
				return;
			if (!ModConfig.Modules.farming)
				return;
			//If farmland is dry and cropsRequireWater is set to anycase then cancel the event
			if (!isCropOnWetFarmland(event.getWorld(), event.getPos()) && ModConfig.Farming.Agriculture.cropsRequireWater.equals(CropsRequireWater.ANY_CASE)) {
				event.setCanceled(true);
				return;
			}
			if (ModConfig.Farming.Agriculture.nerfedBonemeal.equals(NerfedBonemeal.DISABLED))
				return;
			BlockState state = event.getWorld().getBlockState(event.getPos());
			if (!(state.getBlock() instanceof CropsBlock))
				return;
			boolean isBeetroot = state.getBlock() instanceof BeetrootBlock;
			int age = 0;
			int maxAge = Collections.max(CropsBlock.AGE.getAllowedValues());
			if (isBeetroot) {
				age = state.get(BeetrootBlock.BEETROOT_AGE);
				maxAge = Collections.max(BeetrootBlock.BEETROOT_AGE.getAllowedValues());
			}
			else
				age = state.get(CropsBlock.AGE);
			if (age == maxAge)
				return;
			if (ModConfig.Farming.Agriculture.nerfedBonemeal.equals(NerfedBonemeal.SLIGHT))
				age += RandomHelper.getInt(event.getWorld().getRandom(), 1, 2);
			else if (ModConfig.Farming.Agriculture.nerfedBonemeal.equals(NerfedBonemeal.NERFED))
				age++;
			if (age > maxAge)
				age = maxAge;
			if (isBeetroot) {
				state = state.with(BeetrootBlock.BEETROOT_AGE, age);
			}
			else
				state = state.with(CropsBlock.AGE, age);
			event.getWorld().setBlockState(event.getPos(), state, 3);
			event.setResult(Event.Result.ALLOW);
		}

		public static void cropsRequireWater(BlockEvent.CropGrowEvent.Pre event) {
			if (!ModConfig.Modules.farming)
				return;
			if (ModConfig.Farming.Agriculture.cropsRequireWater.equals(CropsRequireWater.NO))
				return;
			if (!(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof CropsBlock))
				return;
			if (!isCropOnWetFarmland(event.getWorld(), event.getPos()))
				event.setResult(Event.Result.DENY);
		}

		private static boolean isCropOnWetFarmland(IWorld world, BlockPos cropPos) {
			BlockState sustainState = world.getBlockState(cropPos.down());
			if (!(sustainState.getBlock() instanceof FarmlandBlock))
				return false;
			int moisture = sustainState.get(FarmlandBlock.MOISTURE);
			if (moisture < 7)
				return false;
			return true;
		}


		/**
		 * Handles Crop Growth Speed Multiplier and NoSunlight Growth multiplier
		 */
		public static void cropsGrowthSpeedMultiplier(BlockEvent.CropGrowEvent.Post event) {
			if (!ModConfig.Modules.farming)
				return;
			if (ModConfig.Farming.Agriculture.cropsGrowthMultiplier == 1.0d && ModConfig.Farming.Agriculture.noSunlightGrowthMultiplier == 1.0d)
				return;
			IWorld world = event.getWorld();
			BlockState state = event.getOriginalState();
			if (!(state.getBlock() instanceof CropsBlock))
				return;
			double chance;
			if (ModConfig.Farming.Agriculture.cropsGrowthMultiplier == 0.0d)
				chance = -1d;
			else
				chance = 1d / ModConfig.Farming.Agriculture.cropsGrowthMultiplier;
			int skyLight = world.getLightFor(LightType.SKY, event.getPos());
			if (skyLight < ModConfig.Farming.Agriculture.minSunlight)
				if (ModConfig.Farming.Agriculture.noSunlightGrowthMultiplier == 0.0d)
					chance = -1d;
				else
					chance *= 1d / ModConfig.Farming.Agriculture.noSunlightGrowthMultiplier;
			if (event.getWorld().getRandom().nextDouble() > chance)
				world.setBlockState(event.getPos(), state, 2);
		}

		public static void plantGrowthMultiplier(BlockEvent.CropGrowEvent.Post event, Class<? extends Block> blockClass, double multiplier) {
			if (!ModConfig.Modules.farming)
				return;
			if (multiplier == 1.0d)
				return;
			IWorld world = event.getWorld();
			BlockState state = event.getOriginalState();
			if (!(state.getBlock().getClass().isInstance(blockClass)))
				return;
			double chance;
			if (multiplier == 0.0d)
				chance = -1d;
			else
				chance = 1d / multiplier;
			if (event.getWorld().getRandom().nextDouble() > chance)
				world.setBlockState(event.getPos(), state, 2);
		}

		public enum NerfedBonemeal {
			DISABLED,
			SLIGHT,
			NERFED
		}

		public enum CropsRequireWater {
			NO,
			BONEMEAL_ONLY,
			ANY_CASE
		}

		public static void disabledHoes(UseHoeEvent event) {
			if (!ModConfig.Modules.farming)
				return;
			if (event.getPlayer().world.isRemote)
				return;
			if (!ModConfig.Farming.Agriculture.disableLowTierHoes)
				return;
			if (!isHoeDisabled(event.getContext().getItem().getItem()))
				return;
			ItemStack hoe = event.getContext().getItem();
			hoe.damageItem(15, event.getPlayer(), (player) -> player.sendBreakAnimation(event.getContext().getHand()));
			event.getPlayer().sendStatusMessage(new StringTextComponent("This hoe is too weak to be used"), true);
			event.setCanceled(true);
		}

		public static void harderTilling(UseHoeEvent event) {
			if (!ModConfig.Modules.farming)
				return;
			if (event.getPlayer().world.isRemote)
				return;
			if (isHoeDisabled(event.getContext().getItem().getItem()))
				return;
			if (event.getContext().getFace() == Direction.DOWN || !event.getContext().getWorld().isAirBlock(event.getContext().getPos().up()))
				return;
			BlockState blockstate = HoeItem.HOE_LOOKUP.get(event.getContext().getWorld().getBlockState(event.getContext().getPos()).getBlock());
			if (blockstate == null || blockstate.getBlock() != Blocks.FARMLAND)
				return;
			ItemStack stack = event.getContext().getItem();
			int cooldown = 0;
			for (ModConfig.Farming.Agriculture.HoeCooldown hoeCooldown : ModConfig.Farming.Agriculture.hoesCooldowns) {
				//System.out.println(hoeTillChance.cooldown);
				if (Utils.isInTagOrItem(hoeCooldown, stack.getItem(), null)) {
					cooldown = hoeCooldown.cooldown;
				}
			}
			if (ModConfig.Farming.Agriculture.hoesDamageOnUseMultiplier > 1)
				stack.damageItem(ModConfig.Farming.Agriculture.hoesDamageOnUseMultiplier - 1, event.getPlayer(), (player) -> player.sendBreakAnimation(event.getContext().getHand()));
			//if (RandomHelper.getFloat(event.getPlayer().getRNG(), 0f, 1f) >= chance)
			//event.setCanceled(true);
			event.getPlayer().getCooldownTracker().setCooldown(stack.getItem(), cooldown);
		}

		private static boolean isHoeDisabled(Item item) {
			return item.getTags().contains(new ResourceLocation(IguanaTweaksReborn.MOD_ID, "disabled_hoes"));
		}
	}
}