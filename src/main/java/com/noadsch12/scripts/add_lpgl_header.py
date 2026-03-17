import os

HEADER = """/*
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
"""

HEADER_MARKER = "12th Client"

def process_java_file(path):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    # Skip if header already exists
    if HEADER_MARKER in content[:500]:
        return

    with open(path, "w", encoding="utf-8") as f:
        f.write(HEADER + "\n" + content)

    print(f"Updated: {path}")

def main():
    for root, _, files in os.walk("."):
        for file in files:
            if file.endswith(".java"):
                process_java_file(os.path.join(root, file))

if __name__ == "__main__":
    main()
