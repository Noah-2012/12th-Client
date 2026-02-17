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

package com.noadsch12.event.events;

import java.util.ArrayList;

import com.noadsch12.event.listeners.AbstractListener;
import com.noadsch12.event.listeners.ItemUsedListener;
import net.minecraft.item.ItemStack;

public class ItemUsedEvent {
	public static class Pre extends AbstractEvent {
		private final ItemStack itemStack;

		public Pre(ItemStack itemStack) {
			this.itemStack = itemStack;
		}

		@Override
		public void Fire(ArrayList<? extends AbstractListener> listeners) {
			for (AbstractListener listener : listeners) {
				ItemUsedListener itemUsedEvent = (ItemUsedListener) listener;
				itemUsedEvent.onItemUsed(this);
			}
		}

		@Override
		public Class<ItemUsedListener> GetListenerClassType() {
			return ItemUsedListener.class;
		}

		public ItemStack getItemStack() {
			return itemStack;
		}
	}

	public static class Post extends AbstractEvent {
		private final ItemStack itemStack;

		public Post(ItemStack itemStack) {
			this.itemStack = itemStack;
		}

		@Override
		public void Fire(ArrayList<? extends AbstractListener> listeners) {
			for (AbstractListener listener : listeners) {
				ItemUsedListener itemUsedEvent = (ItemUsedListener) listener;
				itemUsedEvent.onItemUsed(this);
			}
		}

		@Override
		public Class<ItemUsedListener> GetListenerClassType() {
			return ItemUsedListener.class;
		}

		public ItemStack getItemStack() {
			return itemStack;
		}
	}
}
