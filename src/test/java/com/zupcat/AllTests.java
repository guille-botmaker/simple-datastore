package com.zupcat;

import com.zupcat.dao.DAOTests;
import com.zupcat.dao.ObjectHolderTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DAOTests.class,
        ObjectHolderTests.class
})
public class AllTests {

}
