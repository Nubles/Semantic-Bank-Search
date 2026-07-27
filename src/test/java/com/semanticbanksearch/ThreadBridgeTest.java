package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ThreadBridgeTest
{
    @Test
    public void clientCommandsUseOnlyClientExecutor()
    {
        List<Runnable> clientTasks = new ArrayList<>();
        List<Runnable> swingTasks = new ArrayList<>();
        AtomicInteger executions = new AtomicInteger();
        ThreadBridge bridge = new ThreadBridge(clientTasks::add, swingTasks::add);

        bridge.submitClient(executions::incrementAndGet);

        assertEquals(1, clientTasks.size());
        assertEquals(0, swingTasks.size());
        assertEquals(0, executions.get());

        clientTasks.get(0).run();

        assertEquals(1, executions.get());
    }

    @Test
    public void rendersUseOnlySwingExecutor()
    {
        List<Runnable> clientTasks = new ArrayList<>();
        List<Runnable> swingTasks = new ArrayList<>();
        AtomicInteger executions = new AtomicInteger();
        ThreadBridge bridge = new ThreadBridge(clientTasks::add, swingTasks::add);

        bridge.submitSwing(executions::incrementAndGet);

        assertEquals(0, clientTasks.size());
        assertEquals(1, swingTasks.size());
        assertEquals(0, executions.get());

        swingTasks.get(0).run();

        assertEquals(1, executions.get());
    }

    @Test
    public void nullCommandsAreIgnored()
    {
        List<Runnable> clientTasks = new ArrayList<>();
        List<Runnable> swingTasks = new ArrayList<>();
        ThreadBridge bridge = new ThreadBridge(clientTasks::add, swingTasks::add);

        bridge.submitClient(null);
        bridge.submitSwing(null);

        assertEquals(0, clientTasks.size());
        assertEquals(0, swingTasks.size());
    }
}
