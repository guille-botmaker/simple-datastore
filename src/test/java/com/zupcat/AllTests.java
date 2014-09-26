package com.zupcat;

import com.zupcat.dao.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DAOTest.class,
        SerializationHelperTest.class,
        ResourceTest.class,
        RetryingExecutorTest.class,
        DataObjectTest.class
})
public class AllTests {

}
