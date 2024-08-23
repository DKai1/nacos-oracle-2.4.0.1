/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.distributed.distro;

import com.alibaba.nacos.common.event.ServerConfigChangeEvent;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistroConfigTest {
    
    private DistroConfig distroConfig;
    
    private long syncDelayMillis = 2000L;
    
    private long syncTimeoutMillis = 2000L;
    
    private long syncRetryDelayMillis = 4000L;
    
    private long verifyIntervalMillis = 6000L;
    
    private long verifyTimeoutMillis = 500L;
    
    private long loadDataRetryDelayMillis = 80000L;
    
    @BeforeEach
    void setUp() {
        EnvUtil.setEnvironment(new MockEnvironment());
        distroConfig = DistroConfig.getInstance();
    }
    
    @Test
    void testSetSyncDelayMillis() {
        distroConfig.setSyncDelayMillis(syncDelayMillis);
        assertEquals(syncDelayMillis, distroConfig.getSyncDelayMillis());
    }
    
    @Test
    void testSetSyncRetryDelayMillis() {
        distroConfig.setSyncRetryDelayMillis(syncRetryDelayMillis);
        assertEquals(syncRetryDelayMillis, distroConfig.getSyncRetryDelayMillis());
    }
    
    @Test
    void testSetVerifyIntervalMillis() {
        distroConfig.setVerifyIntervalMillis(verifyIntervalMillis);
        assertEquals(verifyIntervalMillis, distroConfig.getVerifyIntervalMillis());
    }
    
    @Test
    void testSetLoadDataRetryDelayMillis() {
        distroConfig.setLoadDataRetryDelayMillis(loadDataRetryDelayMillis);
        assertEquals(loadDataRetryDelayMillis, distroConfig.getLoadDataRetryDelayMillis());
    }
    
    @Test
    void testUpgradeConfig() throws InterruptedException {
        assertEquals(DistroConstants.DEFAULT_DATA_SYNC_DELAY_MILLISECONDS, distroConfig.getSyncDelayMillis());
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(DistroConstants.DATA_SYNC_DELAY_MILLISECONDS, String.valueOf(syncDelayMillis));
        EnvUtil.setEnvironment(environment);
        NotifyCenter.publishEvent(ServerConfigChangeEvent.newEvent());
        TimeUnit.SECONDS.sleep(1);
        assertEquals(syncDelayMillis, distroConfig.getSyncDelayMillis());
    }
    
    @Test
    void testInitConfigFormEnv() throws ReflectiveOperationException {
        MockEnvironment environment = new MockEnvironment();
        EnvUtil.setEnvironment(environment);
        environment.setProperty(DistroConstants.DATA_SYNC_DELAY_MILLISECONDS, String.valueOf(syncDelayMillis));
        environment.setProperty(DistroConstants.DATA_SYNC_TIMEOUT_MILLISECONDS, String.valueOf(syncTimeoutMillis));
        environment.setProperty(DistroConstants.DATA_SYNC_RETRY_DELAY_MILLISECONDS, String.valueOf(syncRetryDelayMillis));
        environment.setProperty(DistroConstants.DATA_VERIFY_INTERVAL_MILLISECONDS, String.valueOf(verifyIntervalMillis));
        environment.setProperty(DistroConstants.DATA_VERIFY_TIMEOUT_MILLISECONDS, String.valueOf(verifyTimeoutMillis));
        environment.setProperty(DistroConstants.DATA_LOAD_RETRY_DELAY_MILLISECONDS, String.valueOf(loadDataRetryDelayMillis));
        
        Constructor<DistroConfig> declaredConstructor = DistroConfig.class.getDeclaredConstructor();
        declaredConstructor.setAccessible(true);
        DistroConfig distroConfig = declaredConstructor.newInstance();
        
        assertEquals(distroConfig.getSyncDelayMillis(), syncDelayMillis);
        assertEquals(distroConfig.getSyncTimeoutMillis(), syncTimeoutMillis);
        assertEquals(distroConfig.getSyncRetryDelayMillis(), syncRetryDelayMillis);
        assertEquals(distroConfig.getVerifyIntervalMillis(), verifyIntervalMillis);
        assertEquals(distroConfig.getVerifyTimeoutMillis(), verifyTimeoutMillis);
        assertEquals(distroConfig.getLoadDataRetryDelayMillis(), loadDataRetryDelayMillis);
        
    }
}
