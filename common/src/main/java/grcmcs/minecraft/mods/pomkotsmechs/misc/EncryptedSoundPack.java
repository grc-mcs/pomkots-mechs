package grcmcs.minecraft.mods.pomkotsmechs.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.CryptoUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class EncryptedSoundPack implements PackResources {
    private static final String TARGET_NAMESPACE = PomkotsMechs.MODID;
    private static final String TARGET_PREFIX = "sounds/bgm/";

    private final PackResources delegate;

    public EncryptedSoundPack(PackResources delegate) {
        this.delegate = delegate;
    }

    private boolean isTarget(PackType type, ResourceLocation loc) {
        return type == PackType.CLIENT_RESOURCES
                && loc.getNamespace().equals(TARGET_NAMESPACE)
                && loc.getPath().startsWith(TARGET_PREFIX)
                && loc.getPath().endsWith(".ogg");
    }

    public IoSupplier<InputStream> getResource2(PackType type, ResourceLocation location) {
        if (location.getNamespace().startsWith("pomkotsmechs"))System.out.println("getResource called: " + location);
        if (isTarget(type, location)) {

            ResourceLocation encrypted = new ResourceLocation(
                    location.getNamespace(),
                    location.getPath().replace(".ogg", ".dat")
            );

            IoSupplier<InputStream> raw = delegate.getResource(type, encrypted);

            if (raw != null) {
                return () -> {
                    Path p = Path.of("debug.ogg");
                    System.out.println(p.toAbsolutePath());

                    InputStream in = CryptoUtil.decrypt(raw.get());

                    // デバッグ用
                    byte[] data = in.readAllBytes();
                    Files.write(Path.of("debug.ogg"), data);

                    return new ByteArrayInputStream(data);
                };
            }
        }

        return delegate.getResource(type, location);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        delegate.listResources(type, namespace, path, (location, supplier) -> {
            if (location.getNamespace().startsWith("pomkotsmechs"))System.out.println("list s called: " + location);
            // まずそのまま流す

            // 自分の対象だけ「名前だけ偽装」
            if (type == PackType.CLIENT_RESOURCES
                    && location.getNamespace().equals(TARGET_NAMESPACE)
                    && location.getPath().endsWith(".dat")) {

                ResourceLocation fake = new ResourceLocation(
                        location.getNamespace(),
                        location.getPath().replace(".dat", ".ogg")
                );
                if (location.getNamespace().startsWith("pomkotsmechs"))System.out.println("fake: " + fake.toString());

                output.accept(fake, () -> {
                    return CryptoUtil.decrypt(supplier.get());
                });

            } else {
                output.accept(location, supplier);
            }
        });
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... strings) {
        return delegate.getRootResource(strings);
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        return delegate.getResource(type, location);
    }
    @Override public String packId() { return delegate.packId(); }
    @Override public boolean isBuiltin() { return delegate.isBuiltin(); }
    @Override public Set<String> getNamespaces(PackType type) { return delegate.getNamespaces(type); }
    @Override public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException { return delegate.getMetadataSection(serializer); }
    @Override public void close() { delegate.close(); }
}
