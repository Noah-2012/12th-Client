package com.noadsch12.look;

import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ObjModel {

    public final List<Vector3f> vertices = new ArrayList<>();
    public final List<Vector3f> normals = new ArrayList<>();
    public final List<int[]> faces = new ArrayList<>();

    public ObjModel(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Vertex Normal
                if (line.startsWith("vn ")) {
                    String[] tokens = line.split("\\s+");
                    normals.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    ));
                }
                // Vertex Position
                else if (line.startsWith("v ")) {
                    String[] tokens = line.split("\\s+");
                    vertices.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    ));
                }
                // Face
                else if (line.startsWith("f ")) {
                    String[] tokens = line.split("\\s+");

                    int[] indices = new int[tokens.length - 1];
                    for (int i = 1; i < tokens.length; i++) {
                        // OBJ Format: vertex/texture/normal oder vertex//normal oder nur vertex
                        String[] parts = tokens[i].split("/");
                        indices[i - 1] = Integer.parseInt(parts[0]) - 1;
                    }

                    // --- TRIANGULATION (triangle fan) ---
                    for (int i = 1; i < indices.length - 1; i++) {
                        faces.add(new int[]{
                                indices[0],
                                indices[i],
                                indices[i + 1]
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}