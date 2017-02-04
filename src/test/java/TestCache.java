import account.AccountServiceCache;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Qiu,Yibo on 2016/11/30.
 */
public class TestCache extends BaseUnit {

    @Autowired
    private AccountServiceCache accountServiceCache;

    @org.junit.Test
    public void testRedisCache(){
        System.out.println(accountServiceCache.getAccountName("1").getName());
        System.out.println(accountServiceCache.getAccountName("2").getName());
    }

}
