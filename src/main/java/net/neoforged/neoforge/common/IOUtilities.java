package net.neoforged.neoforge.common;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains helpers for performing file I/O in a resilient manner.
 */
public final class IOUtilities {
    private static final String TEMP_FILE_SUFFIX = ".neoforge-tmp";
    private static final OpenOption[] OPEN_OPTIONS = {
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
    };
    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtilities.class);

    private static CompletableFuture<Void> saveDataTasks = CompletableFuture.completedFuture(null);

    private IOUtilities() {}

    /**
     * Tries to clean up any temporary files that may have been left over.
     */
    public static void tryCleanupTempFiles(Path targetPath, @Nullable String prefix) {
        for (var file : tryListTempFiles(targetPath, prefix)) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                LOGGER.error("Could not delete temp file {}: {}", file, e.toString());
            }
        }
    }

    private static List<Path> tryListTempFiles(Path targetPath, @Nullable String prefix) {
        try (var stream = Files.find(targetPath, 1, createPredicate(prefix))) {
            return stream.toList();
        } catch (IOException e) {
            LOGGER.error("Failed to list temporary files in {}", targetPath, e);
            return List.of();
        }
    }

    @Deprecated(forRemoval = true)
    public static void cleanupTempFiles(Path targetPath, @Nullable String prefix) throws IOException {
        try (var filesToDelete = Files.find(targetPath, 1, createPredicate(prefix))) {
            for (var file : filesToDelete.toList()) {
                Files.deleteIfExists(file);
            }
        }
    }

    private static BiPredicate<Path, BasicFileAttributes> createPredicate(@Nullable String prefix) {
        return (file, attributes) -> {
            final var fileName = file.getFileName().toString();
            return fileName.endsWith(TEMP_FILE_SUFFIX) && (prefix == null || fileName.startsWith(prefix));
        };
    }

    /**
     * Writes NBT compressed data to the given path using atomic write.
     */
    public static void writeNbtCompressed(CompoundTag tag, Path path) throws IOException {
        atomicWrite(path, stream -> {
            try (var bufferedStream = new BufferedOutputStream(stream)) {
                NbtIo.writeCompressed(tag, bufferedStream);
            }
        });
    }

    /**
     * Writes NBT data to the given path using atomic write.
     */
    public static void writeNbt(CompoundTag tag, Path path) throws IOException {
        atomicWrite(path, stream -> {
            try (var bufferedStream = new BufferedOutputStream(stream);
                    var dataStream = new DataOutputStream(bufferedStream)) {
                NbtIo.write(tag, dataStream);
            }
        });
    }

    /**
     * Writes data to the given path "atomically", using a temp file and rename.
     * A crash will not leave the file containing corrupted or otherwise half-written data.
     */
    public static void atomicWrite(Path targetPath, WriteCallback writeCallback) throws IOException {
        final var tempPath = Files.createTempFile(
                targetPath.getParent(),
                targetPath.getFileName().toString(),
                TEMP_FILE_SUFFIX);

        try {
            try (var channel = FileChannel.open(tempPath, OPEN_OPTIONS)) {
                var stream = Channels.newOutputStream(channel);
                writeCallback.write(stream);
                channel.force(true);
            }

            try {
                Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception first) {
            try {
                Files.deleteIfExists(tempPath);
            } catch (Exception second) {
                first.addSuppressed(second);
            }
            throw first;
        }
    }

    public static void withIOWorker(Runnable task) {
        saveDataTasks = saveDataTasks.thenRunAsync(task, Util.ioPool());
    }

    public static void waitUntilIOWorkerComplete() {
        saveDataTasks.join();
        saveDataTasks = CompletableFuture.completedFuture(null);
    }

    /**
     * Functional interface equivalent to Consumer but allows throwing IOException.
     */
    public interface WriteCallback {
        void write(OutputStream stream) throws IOException;
    }
}
