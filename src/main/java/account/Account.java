package account;

import java.io.Serializable;

/**
 * Created by BiColorCat on 2016/11s.
 */
public class Account implements Serializable{
    private static final long serialVersionUID = 2053627932884240141L;
    private int id;
    private String name;

    public Account(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
