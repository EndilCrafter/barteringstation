package fuzs.barteringstation.mixin;

import fuzs.barteringstation.capability.BarteringStationCapability;
import fuzs.barteringstation.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {

    @Inject(method = "stopHoldingOffHandItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;getBarterResponseItems(Lnet/minecraft/world/entity/monster/piglin/Piglin;)Ljava/util/List;"), cancellable = true)
    private static void stopHoldingOffHandItem$inject$invoke$getBarterResponseItems(Piglin piglin, boolean finishedHolding, CallbackInfo callback) {
        if (piglin.level.isClientSide) return;
        Optional<BarteringStationCapability> optional = ModRegistry.BARTERING_STATION_CAPABILITY.maybeGet(piglin);
        if (optional.isPresent() && optional.map(BarteringStationCapability::hasBarteringStationPos).orElseThrow()) {
            BarteringStationCapability capability = optional.orElseThrow(IllegalStateException::new);
            BlockPos pos = capability.getBarteringStationPos();
            capability.clearBarteringStationPos();
            piglin.level.getBlockEntity(pos, ModRegistry.BARTERING_STATION_BLOCK_ENTITY_TYPE.get()).ifPresent(blockEntity -> {
                List<ItemStack> items = getBarterResponseItems(piglin);
                items.removeIf(blockEntity::placeBarterResponseItem);
                if (!items.isEmpty()) {
                    throwItems(piglin, items);
                } else {
                    piglin.swing(InteractionHand.OFF_HAND);
                }
                callback.cancel();
            });
        }
    }

    @Shadow
    private static List<ItemStack> getBarterResponseItems(Piglin p_34997_) {
        throw new IllegalStateException();
    }

    @Shadow
    private static void throwItems(Piglin p_34861_, List<ItemStack> p_34862_) {
        throw new IllegalStateException();
    }
}
