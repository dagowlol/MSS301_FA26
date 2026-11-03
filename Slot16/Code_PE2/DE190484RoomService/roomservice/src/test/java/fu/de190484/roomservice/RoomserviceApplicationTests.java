package fu.de190484.roomservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Disabled in local test environment when MS SQL Server is offline")
class RoomserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
