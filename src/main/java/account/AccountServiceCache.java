package account;

import cache.CacheDuration;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by BiColorCat on 2016/11/30.
 */
@Service
public class AccountServiceCache {

    @Cacheable(value = "accountCache1",key = "'accountName_' + #name")
    @CacheDuration(duration = 60)
    public Account getAccountName(String name){
        Optional<Account> accountOptional = getFromDB(name);
        if(!accountOptional.isPresent()){
            throw new RuntimeException("Error");
        }
        return accountOptional.get();
    }

    private Optional<Account> getFromDB(String name){
        System.out.println("get from db_"+name);
        return Optional.ofNullable(new Account(name));
    }

}
