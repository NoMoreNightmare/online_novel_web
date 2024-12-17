import cn.hutool.crypto.digest.BCrypt;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserTest {
    @Test
    public void bcryptTest() {
        String a = BCrypt.hashpw("123456", BCrypt.gensalt());
        System.out.println(a);
        String b = BCrypt.hashpw("123456", BCrypt.gensalt());
        System.out.println(b);

        String originPass = new String("123456");

        System.out.println(BCrypt.checkpw(originPass, a));
    }
}
