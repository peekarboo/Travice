
package org.travice.database;

import io.netty.buffer.ByteBuf;
import org.travice.helper.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MediaManager {

    private String path;

    public MediaManager(String path) {
        this.path = path;
    }

    private File createFile(String uniqueId, String name) throws IOException {
        Path filePath = Paths.get(path, uniqueId, name);
        Path directoryPath = filePath.getParent();
        if (directoryPath != null) {
            Files.createDirectories(directoryPath);
        }
        return filePath.toFile();
    }

    public String writeFile(String uniqueId, ByteBuf buf, String extension) {
        if (path != null) {
            int size = buf.readableBytes();
            String name = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + "." + extension;
            try (FileOutputStream output = new FileOutputStream(createFile(uniqueId, name));
                    FileChannel fileChannel = output.getChannel()) {
                    ByteBuffer byteBuffer = buf.nioBuffer();
                int written = 0;
                while (written < size) {
                    written += fileChannel.write(byteBuffer);
                }
                fileChannel.force(false);
                return name;
            } catch (IOException e) {
                Log.warning(e);
            }
        }
        return null;
    }

}
