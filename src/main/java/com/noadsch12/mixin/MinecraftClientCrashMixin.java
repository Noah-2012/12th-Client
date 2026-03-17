/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 *
 * This file is part of the 12th Client project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details.
 */

package com.noadsch12.mixin;

import com.noadsch12.macro.MACRO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(MinecraftClient.class)
public class MinecraftClientCrashMixin {

    @Shadow
    private java.util.function.Supplier<net.minecraft.util.crash.CrashReport> crashReportSupplier;

    // NOTE: This is just a Backup Injection
    // The Main Injection for the Crash Catch
    // is at the CrashReportMixin

    @Deprecated
    @Inject(method = "printCrashReport", at = @At("HEAD"))
    private void onPrintCrashReport(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        MinecraftClientAccessor accessor = (MinecraftClientAccessor) client;

        Supplier<CrashReport> original = accessor.getCrashReportSupplier();
        if (original == null) return;

        // Replace supplier with wrapped version
        accessor.setCrashReportSupplier(() -> {
            CrashReport report = original.get();
            if (report != null) {
                MACRO.handleCrash(report.getCause());
            }
            return report;
        });
    }
}
