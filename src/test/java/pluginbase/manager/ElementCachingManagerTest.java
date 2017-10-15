package pluginbase.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.logging.Logger;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.PluginConfig;
import org.generallib.pluginbase.PluginManager;
import org.generallib.pluginbase.manager.ElementCachingManager;
import org.generallib.pluginbase.manager.ElementCachingManager.NamedElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ElementCachingManagerTest.TestPlugin.class)
public class ElementCachingManagerTest {

    @Test
    public void testSaveLoad() throws Exception {
        TestPlugin mockBase = PowerMock.createMock(TestPlugin.class);
        Logger mockLogger = PowerMock.createMock(Logger.class);

        EasyMock.expect(mockBase.isEnabled()).andReturn(true).anyTimes();
        ;
        EasyMock.expect(mockBase.getDataFolder()).andReturn(new File("ElementCachingManagerTestSuite1")).anyTimes();
        EasyMock.expect(mockBase.getPluginConfig()).andReturn(new TestConfig());
        EasyMock.expect(mockBase.getLogger()).andStubReturn(mockLogger);

        mockLogger.info(EasyMock.anyString());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>() {

            @Override
            public Void answer() throws Throwable {
                System.out.println(EasyMock.getCurrentArguments()[0]);
                return null;
            }

        }).anyTimes();

        PowerMock.replay(mockBase);

        TestManager manager = new TestManager(mockBase, PluginManager.NORM_PRIORITY);
        manager.createFileDB().clear();
        Whitebox.invokeMethod(manager, "onEnable");

        UUID uuid = UUID.randomUUID();
        System.out.println("GeneratedUUID: " + uuid);

        TestValue testValue1In = new TestValue("TestValue1");
        System.out.println(testValue1In.value + " save");
        Whitebox.invokeMethod(manager, "save", uuid, testValue1In);

        TestValue testValue1Out;

        testValue1Out = Whitebox.invokeMethod(manager, "get", "TestValue1", true);
        assertNotNull(testValue1Out);
        assertEquals(testValue1Out.value, testValue1In.value);

        testValue1Out = Whitebox.invokeMethod(manager, "get", uuid, true);
        assertNotNull(testValue1Out);
        assertEquals(testValue1Out.value, testValue1In.value);

        Whitebox.invokeMethod(manager, "onDisable");
    }

    @Test
    public void testSaveLoadAsync() throws Exception {
        TestPlugin mockBase = PowerMock.createMock(TestPlugin.class);
        Logger mockLogger = PowerMock.createMock(Logger.class);

        EasyMock.expect(mockBase.isEnabled()).andReturn(true).anyTimes();
        EasyMock.expect(mockBase.getDataFolder()).andReturn(new File("ElementCachingManagerTestSuite2")).anyTimes();
        EasyMock.expect(mockBase.getPluginConfig()).andReturn(new TestConfig());
        EasyMock.expect(mockBase.getLogger()).andStubReturn(mockLogger);

        mockLogger.info(EasyMock.anyString());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>() {

            @Override
            public Void answer() throws Throwable {
                System.out.println(EasyMock.getCurrentArguments()[0]);
                return null;
            }

        }).anyTimes();

        PowerMock.replay(mockBase);

        TestManager manager = new TestManager(mockBase, PluginManager.NORM_PRIORITY);
        manager.createFileDB().clear();
        Whitebox.invokeMethod(manager, "onEnable");

        UUID uuid = UUID.randomUUID();
        System.out.println("GeneratedUUID: " + uuid);

        TestValue testValue1In = new TestValue("TestValue1");
        System.out.println(testValue1In.value + " save");
        Whitebox.invokeMethod(manager, "save", uuid, testValue1In);

        TestValue testValue1Out;

        System.out.println("Sleep for 0.1secs.");
        Thread.sleep(100L);
        System.out.println("Resumed");

        testValue1Out = Whitebox.invokeMethod(manager, "get", "TestValue1", false);
        assertNotNull(testValue1Out);
        assertEquals(testValue1Out.value, testValue1In.value);

        testValue1Out = Whitebox.invokeMethod(manager, "get", uuid, false);
        assertNotNull(testValue1Out);
        assertEquals(testValue1Out.value, testValue1In.value);

        Whitebox.invokeMethod(manager, "onDisable");
    }

    public static class TestPlugin extends PluginBase {

        @Override
        protected void preEnable() {

        }

    }

    public static class TestConfig extends PluginConfig {

    }

    public static class TestManager extends ElementCachingManager<UUID, TestValue> {

        public TestManager(PluginBase base, int loadPriority) {
            super(base, loadPriority);
        }

        @Override
        protected String getTableName() {
            return "Test";
        }

        @Override
        protected Type getType() {
            return TestValue.class;
        }

        @Override
        protected UUID createKeyFromString(String str) {
            return UUID.fromString(str);
        }

        @Override
        protected CacheUpdateHandle<UUID, TestValue> getUpdateHandle() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected CacheDeleteHandle<UUID> getDeleteHandle() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public static class TestValue implements NamedElement {
        private final String value;

        public TestValue(String value) {
            super();
            this.value = value;
        }

        @Override
        public String getName() {
            return value;
        }

    }
}
