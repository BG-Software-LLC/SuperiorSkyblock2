package com.bgsoftware.superiorskyblock.platform;

import com.bgsoftware.superiorskyblock.platform.scheduler.IScheduler;
import com.bgsoftware.superiorskyblock.platform.server.IServerManager;

public interface IPlatform {

    IScheduler getScheduler();

    IServerManager getServerManager();

}
