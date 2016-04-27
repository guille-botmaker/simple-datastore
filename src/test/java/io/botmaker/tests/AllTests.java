package io.botmaker.tests;

import io.botmaker.tests.dao.DAOTest;
import io.botmaker.tests.dao.DataObjectTest;
import io.botmaker.tests.dao.RetryingExecutorTest;
import io.botmaker.tests.dao.SerializationHelperTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DAOTest.class,
        SerializationHelperTest.class,
        RetryingExecutorTest.class,
        DataObjectTest.class
})
public class AllTests {

}
