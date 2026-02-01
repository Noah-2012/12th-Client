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

import com.noadsch12.ClientCrashHandler;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public class CrashReportMixin {


    @Unique
    private static int crash_counter = 0;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private static void onCrash(
            String message, Throwable cause, CallbackInfo ci
    ) {
        crash_counter = crash_counter + 1;
        if (crash_counter != 1) {
            ClientCrashHandler.handleCrash(cause);
        }
    }
}
