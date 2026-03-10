package net.neoforged.neoforge.event;

import java.lang.reflect.Method;
import java.util.Set;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired to register game test methods.
 * Fired on the mod event bus.
 */
public class RegisterGameTestsEvent extends Event implements IModBusEvent {
    private final Set<Method> gameTestMethods;

    public RegisterGameTestsEvent(Set<Method> gameTestMethods) {
        this.gameTestMethods = gameTestMethods;
    }

    /**
     * Register all game test methods from the given class.
     */
    public void register(Class<?> testClass) {
        for (Method method : testClass.getDeclaredMethods()) {
            gameTestMethods.add(method);
        }
    }

    /**
     * Register a single game test method.
     */
    public void register(Method testMethod) {
        gameTestMethods.add(testMethod);
    }
}
