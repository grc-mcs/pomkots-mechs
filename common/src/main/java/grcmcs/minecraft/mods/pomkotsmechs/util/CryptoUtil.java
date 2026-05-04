package grcmcs.minecraft.mods.pomkotsmechs.util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;

public class CryptoUtil {
    private static final byte[] KEY = buildKey();

    private static byte[] buildKey() {
        byte[] k = new byte[] {
                (byte)(0x12 ^ 0x5A),
                (byte)(0x34 ^ 0x5A),
                (byte)(0x56 ^ 0x5A),
                (byte)(0x78 ^ 0x5A),
                (byte)(0x9A ^ 0x5A),
                (byte)(0xBC ^ 0x5A),
                (byte)(0xDE ^ 0x5A),
                (byte)(0xF0 ^ 0x5A),
                (byte)(0x11 ^ 0x5A),
                (byte)(0x22 ^ 0x5A),
                (byte)(0x33 ^ 0x5A),
                (byte)(0x44 ^ 0x5A),
                (byte)(0x55 ^ 0x5A),
                (byte)(0x66 ^ 0x5A),
                (byte)(0x77 ^ 0x5A),
                (byte)(0x88 ^ 0x5A)
        };

        for (int i = 0; i < k.length; i++) {
            k[i] ^= 0x5A;
        }
        return k;
    }

    public static InputStream decrypt(InputStream input) {
        try {
            byte[] iv = input.readNBytes(16);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            return new CipherInputStream(input, cipher);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
