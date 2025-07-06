package def_pkg;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        ClientBasicTest.class,
        ClientTransactionTest.class
})
public class ClientTestSuite {
}
