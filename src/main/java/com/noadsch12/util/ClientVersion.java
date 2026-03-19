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

package com.noadsch12.util;

public class ClientVersion {
    // Version numbers stored as integers for safety and easy comparisons
    private final int major;
    private final int minor;
    private final int patch;

    public final int STRING_WITH_V = 1;
    public final int STRING_WITHOUT_V = 2;
    public final int STRING_POINTS = 3;
    public final int STRING_DASHES = 4;

    // Constructor
    public ClientVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    // Return version as a standard string "1.0.0"
    public String asString(int format, int separator) {
        String form = "";
        String sep = "";

        form = switch (format) {
            case STRING_WITH_V -> "v";
            case STRING_WITHOUT_V -> "";
            default -> form;
        };

        sep = switch (separator) {
            case STRING_POINTS -> ".";
            case STRING_DASHES -> "-";
            default -> sep;
        };

        return form + major + sep + minor + sep + patch;
    }

    // Compare with another version
    public boolean isNewerThan(ClientVersion other) {
        if (this.major != other.major) return this.major > other.major;
        if (this.minor != other.minor) return this.minor > other.minor;
        return this.patch > other.patch;
    }

    public boolean isOlderThan(ClientVersion other) {
        return !isNewerThan(other) && !this.equals(other);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClientVersion other)) return false;
        return this.major == other.major && this.minor == other.minor && this.patch == other.patch;
    }

    @Override
    public String toString() {
        return asString(STRING_WITHOUT_V, STRING_POINTS);
    }
}