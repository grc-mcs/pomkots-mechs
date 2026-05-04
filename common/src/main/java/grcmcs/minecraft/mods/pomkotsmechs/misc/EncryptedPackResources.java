package grcmcs.minecraft.mods.pomkotsmechs.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.CryptoUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class EncryptedPackResources implements PackResources {
    private static final String TARGET_NAMESPACE = PomkotsMechs.MODID;
    private static final String TARGET_PREFIX = "sounds/bgm/";

    private final PackResources delegate; // 元のPackをラップする

    public EncryptedPackResources(PackResources wrapped) {
        this.delegate = wrapped;
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        PomkotsMechs.LOGGER.info("listResources1 called: namespace=" + namespace + " path=" + path);

        delegate.listResources(type, namespace, path, (location, supplier) -> {
            if (location.getNamespace().startsWith("pomkotsmechs"))System.out.println("list s called: " + location);
            // まずそのまま流す

            // 自分の対象だけ「名前だけ偽装」
            if (type == PackType.CLIENT_RESOURCES
                    && location.getNamespace().equals(TARGET_NAMESPACE)
                    && location.getPath().endsWith(".dat")) {

System.out.println(delegate.getClass().getName());

                ResourceLocation fake = new ResourceLocation(
                        location.getNamespace(),
                        location.getPath().replace(".dat", ".ogg")
                );

System.out.println("fake: " + fake.toString());

                output.accept(fake, () -> {
                    return CryptoUtil.decrypt(supplier.get());
                });

            } else {
//                if (location.getPath().endsWith(".json")) {
//                    PomkotsMechs.LOGGER.info("listResources2 json: " + location);
//                }
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
//        PomkotsMechs.LOGGER.info("getResource called: " + location);

        var res =  delegate.getResource(type, location);

//        PomkotsMechs.LOGGER.info("getResource res: " + res);

        return res;
    }
    @Override public String packId() { return delegate.packId(); }
    @Override public boolean isBuiltin() { return delegate.isBuiltin(); }
    @Override public Set<String> getNamespaces(PackType type) { return delegate.getNamespaces(type); }
    @Override public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException { return delegate.getMetadataSection(serializer); }
    @Override public void close() { delegate.close(); }
}
