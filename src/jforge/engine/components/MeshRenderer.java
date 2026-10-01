package jforge.engine.components;

import jforge.engine.rendering.material.Material;
import jforge.engine.rendering.geometry.Mesh;

public class MeshRenderer {

    private Mesh mesh;

    private Material material;

    public MeshRenderer(
            Mesh mesh,
            Material material) {

        this.mesh = mesh;

        this.material = material;
    }

    public Mesh getMesh() {

        return mesh;
    }

    public Material getMaterial() {

        return material;
    }

    public void setMesh(Mesh mesh) {

        this.mesh = mesh;
    }

    public void setMaterial(
            Material material) {

        this.material = material;
    }
}