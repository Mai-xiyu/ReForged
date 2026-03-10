package net.neoforged.neoforge.common;

/**
 * Tag convention log warning utility.
 */
public class TagConventionLogWarning {
    private TagConventionLogWarning() {}

    public enum LogWarningMode {
        SILENCED,
        DEV_SHORT,
        DEV_VERBOSE,
        PROD_SHORT,
        PROD_VERBOSE
    }
}
