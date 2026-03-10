package net.neoforged.neoforge.client.event;

/**
 * Fired to allow modification of the player's mouse sensitivity and cinematic camera.
 */
public class CalculatePlayerTurnEvent extends net.neoforged.bus.api.Event {
    private double mouseSensitivity;
    private boolean cinematicCameraEnabled;

    public CalculatePlayerTurnEvent(double mouseSensitivity, boolean cinematicCameraEnabled) {
        this.mouseSensitivity = mouseSensitivity;
        this.cinematicCameraEnabled = cinematicCameraEnabled;
    }

    public double getMouseSensitivity() { return mouseSensitivity; }
    public void setMouseSensitivity(double mouseSensitivity) { this.mouseSensitivity = mouseSensitivity; }
    public boolean getCinematicCameraEnabled() { return cinematicCameraEnabled; }
    public void setCinematicCameraEnabled(boolean cinematicCameraEnabled) { this.cinematicCameraEnabled = cinematicCameraEnabled; }
}
