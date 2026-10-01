package jforge.engine.rendering.material;

import jforge.engine.rendering.geometry.Mesh;

import java.io.File;

public class LoadedModel {

    private final Mesh mesh;

    private final File diffuseTextureFile;

    public LoadedModel(
            Mesh mesh,
            File diffuseTextureFile) {

        this.mesh =
                mesh;

        this.diffuseTextureFile =
                diffuseTextureFile;
    }

    public Mesh getMesh() {

        return mesh;
    }

    public File getDiffuseTextureFile() {

        return diffuseTextureFile;
    }
}