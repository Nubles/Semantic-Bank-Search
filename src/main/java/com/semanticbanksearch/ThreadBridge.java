package com.semanticbanksearch;

import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import net.runelite.client.callback.ClientThread;

final class ThreadBridge
{
    private final Consumer<Runnable> clientExecutor;
    private final Consumer<Runnable> swingExecutor;

    ThreadBridge(Consumer<Runnable> clientExecutor, Consumer<Runnable> swingExecutor)
    {
        this.clientExecutor = Objects.requireNonNull(clientExecutor, "clientExecutor");
        this.swingExecutor = Objects.requireNonNull(swingExecutor, "swingExecutor");
    }

    static ThreadBridge runtime(ClientThread clientThread)
    {
        Objects.requireNonNull(clientThread, "clientThread");
        return new ThreadBridge(clientThread::invoke, SwingUtilities::invokeLater);
    }

    void submitClient(Runnable command)
    {
        if (command != null)
        {
            clientExecutor.accept(command);
        }
    }

    void submitSwing(Runnable render)
    {
        if (render != null)
        {
            swingExecutor.accept(render);
        }
    }
}
