package net.neoforged.neoforge.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Self-test utility for CI testing. Launches client/server and writes
 * a file to signal that the game loaded successfully.
 */
@ApiStatus.Internal
public final class SelfTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfTest.class);

    private SelfTest() {}

    public static void initCommon() {
        var serverSelfTestDestination = System.getenv("NEOFORGE_DEDICATED_SERVER_SELFTEST");
        if (serverSelfTestDestination != null) {
            NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre e) -> {
                if (e.getServer().isRunning()) {
                    writeSelfTestReport(serverSelfTestDestination);
                    e.getServer().halt(false);
                }
            });
        }
    }

    public static void writeSelfTestReport(String path) {
        try {
            Files.createFile(Paths.get(path));
            LOGGER.info("Wrote self-test report to '{}'", path);
        } catch (IOException e) {
            LOGGER.error("Failed to write self-test to '{}'", path, e);
            System.exit(1);
        }
    }
}
