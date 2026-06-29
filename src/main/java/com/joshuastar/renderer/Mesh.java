package com.joshuastar.renderer;

import java.util.ArrayList;
import java.util.List;

public class Mesh {

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();

    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    public void addIndex(int index) {
        indices.add(index);
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getIndexCount() {
        return indices.size();
    }

    public void clear() {
        vertices.clear();
        indices.clear();
    }
}