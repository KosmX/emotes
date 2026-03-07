package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.relocation;

import io.github.kosmx.emotes.common.CommonData;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;

public final class RemappingClassLoader extends URLClassLoader {
    private final GeyserRelocationIndex index;

    public RemappingClassLoader(ClassLoader parent, Path path, Class<?> geyserAnchor) throws MalformedURLException {
        super(new URL[]{path.toUri().toURL()}, parent);
        this.index = GeyserRelocationIndex.fromGeyserJar(geyserAnchor);
        if (!this.index.isEmpty()) CommonData.LOGGER.info("Detected platform-specific Geyser relocations: {}", this.index);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> already = findLoadedClass(name);
        if (already != null) {
            if (resolve) resolveClass(already);
            return already;
        }

        URL res = findResource(name.replace('.', '/').concat(".class"));
        if (res != null) {
            try {
                Class<?> c = findClass(name);
                if (resolve) resolveClass(c);
                return c;
            } catch (ClassNotFoundException ignored) {}
        }

        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (this.index.isEmpty()) return super.findClass(name);

        Class<?> already = findLoadedClass(name);
        if (already != null) return already;

        final URL classResource = findResource(name.replace('.', '/').concat(".class"));
        if (classResource == null) return super.findClass(name);

        try (InputStream is = classResource.openStream()) {
            byte[] original = is.readAllBytes();
            byte[] transformed = remapBytecode(original);

            if (!Arrays.equals(original, transformed)) {
                CommonData.LOGGER.info("[Remap] Transforming {}!", name);
            }

            return defineClass(name, transformed, 0, transformed.length);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ex) {
            throw new ClassNotFoundException(name, ex);
        }
    }

    private byte[] remapBytecode(byte[] in) {
        ClassReader cr = new ClassReader(in);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new ClassRemapper(cw, index);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}
