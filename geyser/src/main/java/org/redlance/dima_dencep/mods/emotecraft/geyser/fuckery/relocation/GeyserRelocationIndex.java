package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.relocation;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;

import java.io.File;
import java.net.URI;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

public final class GeyserRelocationIndex extends Remapper {
    private final Map<String, String> mappings;

    private static final String GEYSER_SHADED_BASE = "org/geysermc/geyser/shaded/";
    private static final String GEYSER_PLATFORM_BASE = "org/geysermc/geyser/platform/";
    private static final String SHADED_SEGMENT = "shaded/";

    private GeyserRelocationIndex(Map<String, String> mappings) {
        super(Opcodes.ASM9);
        this.mappings = mappings;
    }

    public static GeyserRelocationIndex fromGeyserJar(Class<?> geyserAnchor) {
        File jarFile = jarFileOf(geyserAnchor);
        Map<String, String> map = new HashMap<>(4096);

        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream().forEach(e -> {
                String n = e.getName();
                if (!n.endsWith(".class")) return;

                String relocatedInternalName = n.substring(0, n.length() - 6);

                if (n.startsWith(GEYSER_SHADED_BASE)) {
                    int canonStart = GEYSER_SHADED_BASE.length();
                    String originalInternalName = n.substring(canonStart, n.length() - 6);
                    map.put(originalInternalName, relocatedInternalName);
                    return;
                }

                if (n.startsWith(GEYSER_PLATFORM_BASE)) {
                    int p = GEYSER_PLATFORM_BASE.length();
                    int platformSlash = n.indexOf('/', p);
                    if (platformSlash < 0) return;

                    int shadedStart = platformSlash + 1;
                    if (!n.regionMatches(shadedStart, SHADED_SEGMENT, 0, SHADED_SEGMENT.length())) return;

                    int canonStart = shadedStart + SHADED_SEGMENT.length();
                    String originalInternalName = n.substring(canonStart, n.length() - 6);
                    map.put(originalInternalName, relocatedInternalName);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Failed to index Geyser relocations from " + jarFile, ex);
        }

        return new GeyserRelocationIndex(map);
    }

    @Override
    public String map(String internalName) {
        if (internalName == null) return null;
        return this.mappings.getOrDefault(internalName, internalName);
    }

    public boolean isEmpty() {
        return this.mappings.isEmpty();
    }

    private static File jarFileOf(Class<?> anchor) {
        try {
            CodeSource cs = anchor.getProtectionDomain().getCodeSource();
            if (cs == null) throw new IllegalStateException("No CodeSource for " + anchor.getName());
            URI uri = cs.getLocation().toURI();
            if (uri.getFragment() != null) { // ViaProxy workaround
                uri = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
            }
            File f = new File(uri);
            if (!f.isFile()) throw new IllegalStateException("Not a jar: " + f);
            return f;
        } catch (Exception e) {
            throw new RuntimeException("Cannot locate jar for " + anchor.getName(), e);
        }
    }

    @Override
    public String toString() {
        return "GeyserRelocationIndex{size=" + this.mappings.size() + "}";
    }
}
