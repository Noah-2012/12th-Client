package com.noadsch12.mixin;

import com.noadsch12.render.TrailRenderer;
import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class ProjectileTrailMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void spawnArrowTrail(CallbackInfo ci) {
        PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
        World world = projectile.getEntityWorld();

        // Check if we are on the client side and the projectile is moving
        // 'inGround' is a Yarn field that turns true when the arrow hits a block
        if (!projectile.isOnGround() && ClientSettingsScreen.ProjectileTrailEnabled) {

            // Spawn the particle. You can change ParticleTypes.END_ROD to
            // something else like ParticleTypes.FLAME or ParticleTypes.SOUL_FIRE_FLAME
            if (ClientSettingsScreen.trailIndex == 0) {
                double x = projectile.getX();
                double y = projectile.getY();
                double z = projectile.getZ();

                // The center (0,0,0) and the 6 directions (+/- 0.5)
                double[][] offsets = {
                        {0, 0, 0},
                        {0.5, 0, 0}, {-0.5, 0, 0},
                        {0, 0.5, 0}, {0, -0.5, 0},
                        {0, 0, 0.5}, {0, 0, -0.5}
                };

                for (double[] off : offsets) {
                    world.addParticleClient(
                            ParticleTypes.TOTEM_OF_UNDYING,
                            x + off[0], y + off[1], z + off[2],
                            0.0D, 0.0D, 0.0D
                    );
                }
            } else if (ClientSettingsScreen.trailIndex == 1) {
                world.addParticleClient(
                        ParticleTypes.EXPLOSION,
                        projectile.getX(),
                        projectile.getY(),
                        projectile.getZ(),
                        0.0D, 0.0D, 0.0D
                );
            } else if (ClientSettingsScreen.trailIndex == 2) {
                world.addParticleClient(
                        ParticleTypes.DAMAGE_INDICATOR,
                        projectile.getX(),
                        projectile.getY(),
                        projectile.getZ(),
                        0.0D, 0.0D, 0.0D
                );
            } else if (ClientSettingsScreen.trailIndex == 3) {
                TrailRenderer.addPoint(projectile.getUuid(), projectile.getEntityPos());
            }
        }
    }
}