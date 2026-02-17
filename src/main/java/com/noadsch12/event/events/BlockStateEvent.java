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

/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package com.noadsch12.event.events;

import java.util.ArrayList;

import com.noadsch12.event.listeners.AbstractListener;
import com.noadsch12.event.listeners.BlockStateListener;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockStateEvent extends AbstractEvent {
	private final BlockPos blockPos;
	private final BlockState blockState;
	private final BlockState previousBlockState;

	public BlockStateEvent(BlockPos blockPos, BlockState state, BlockState previousState) {
		this.blockPos = blockPos;
		blockState = state;
		previousBlockState = previousState;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public BlockState getBlockState() {
		return blockState;
	}

	public BlockState getPreviousBlockState() {
		return previousBlockState;
	}

	@Override
	public void Fire(ArrayList<? extends AbstractListener> listeners) {
		for (AbstractListener listener : listeners) {
			BlockStateListener blockStateListener = (BlockStateListener) listener;
			blockStateListener.onBlockStateChanged(this);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<BlockStateListener> GetListenerClassType() {
		return BlockStateListener.class;
	}
}