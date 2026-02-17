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
import com.noadsch12.event.listeners.ParticleListener;
import net.minecraft.particle.ParticleEffect;

public class ParticleEvent extends AbstractEvent {
	private final ParticleEffect particleEffect;

	public ParticleEvent(ParticleEffect particleEffect) {
		this.particleEffect = particleEffect;
	}

	public ParticleEffect getParticleEffect() {
		return particleEffect;
	}

	@Override
	public void Fire(ArrayList<? extends AbstractListener> listeners) {
		for (AbstractListener listener : listeners) {
			ParticleListener particleListener = (ParticleListener) listener;
			particleListener.onParticle(this);
		}
	}

	@Override
	public Class<ParticleListener> GetListenerClassType() {
		return ParticleListener.class;
	}
}
