package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.relocation;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;

import java.io.File;
import java.net.URI;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.jar.JarFile;

public final class GeyserRelocationIndex extends Remapper {
    private final Map<String, String> rootToTarget;
    private final Function<String, Boolean> hasOwnClass;

    private static final String GEYSER_SHADED_BASE = "org/geysermc/geyser/shaded/";
    private static final String GEYSER_PLATFORM_BASE = "org/geysermc/geyser/platform/";
    private static final String SHADED_SEGMENT = "shaded/";

    private GeyserRelocationIndex(Map<String, String> rootToTarget, Function<String, Boolean> hasOwnClass) {
        super(Opcodes.ASM9);
        this.rootToTarget = rootToTarget;
        this.hasOwnClass = hasOwnClass;
    }

    public static GeyserRelocationIndex fromGeyserJar(Class<?> geyserAnchor, Function<String, Boolean> hasOwnClass) {
        File jarFile = jarFileOf(geyserAnchor);
        Map<String, String> map = new HashMap<>(512);

        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream().forEach(e -> {
                String n = e.getName();
                if (!n.endsWith(".class")) return;

                // Case 1: org/geysermc/geyser/shaded/<canon...>.class
                if (n.startsWith(GEYSER_SHADED_BASE)) {
                    int canonStart = GEYSER_SHADED_BASE.length();
                    addRoot(map, "org/geysermc/geyser/", n, canonStart);
                    return;
                }

                // Case 2: org/geysermc/geyser/platform/<p>/shaded/<canon...>.class
                if (n.startsWith(GEYSER_PLATFORM_BASE)) {
                    int p = GEYSER_PLATFORM_BASE.length();
                    int platformSlash = n.indexOf('/', p);
                    if (platformSlash < 0) return;

                    int shadedStart = platformSlash + 1; // start of "shaded/..."
                    if (!n.regionMatches(shadedStart, SHADED_SEGMENT, 0, SHADED_SEGMENT.length())) return;

                    int canonStart = shadedStart + SHADED_SEGMENT.length();
                    String prefixBeforeShaded = n.substring(0, shadedStart); // ".../platform/<p>/"
                    addRoot(map, prefixBeforeShaded, n, canonStart);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Failed to index Geyser relocations from " + jarFile, ex);
        }

        return new GeyserRelocationIndex(map, hasOwnClass);
    }

    @Override
    public String map(String internalName) {
        if (internalName == null) return null;
        if (this.hasOwnClass.apply(internalName)) return internalName;
        String root = twoSegmentRoot(internalName);
        if (root == null) return internalName;
        String targetRoot = rootToTarget.get(root);
        if (targetRoot == null) return internalName;
        return targetRoot + internalName.substring(root.length());
    }

    public boolean isEmpty() {
        return this.rootToTarget.isEmpty();
    }

    private static void addRoot(Map<String, String> map, String prefixBeforeShaded, String fullEntry, int canonStart) {
        int firstSlash = fullEntry.indexOf('/', canonStart);
        if (firstSlash < 0) return;
        int secondSlash = fullEntry.indexOf('/', firstSlash + 1);
        if (secondSlash < 0) return;
        String root = fullEntry.substring(canonStart, secondSlash + 1); // "a/b/"
        map.putIfAbsent(root, prefixBeforeShaded + SHADED_SEGMENT + root);
    }

    private static String twoSegmentRoot(String internalName) {
        int first = internalName.indexOf('/');
        if (first < 0) return null;
        int second = internalName.indexOf('/', first + 1);
        if (second < 0) return null;
        return internalName.substring(0, second + 1);
    }

    private static File jarFileOf(Class<?> anchor) {
        try {
            CodeSource cs = anchor.getProtectionDomain().getCodeSource();
            if (cs == null) throw new IllegalStateException("No CodeSource for " + anchor.getName());
            URI uri = cs.getLocation().toURI();
            File f = new File(uri);
            if (!f.isFile()) throw new IllegalStateException("Not a jar: " + f);
            return f;
        } catch (Exception e) {
            throw new RuntimeException("Cannot locate jar for " + anchor.getName(), e);
        }
    }

    @Override
    public String toString() {
        return this.rootToTarget.toString();
    }
}
