package com.zupcat;

import com.zupcat.dao.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DAOTests.class,
        SerializationHelperTests.class,
        ResourceTests.class,
        RetryingExecutorTests.class,
        ObjectHolderTests.class
})
public class AllTests {

}
