package delegations.cds.models;

import static javax.persistence.GenerationType.TABLE;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import delegations.cds.models.enums.UmaTokenType;

@Entity
@Table(name = "uma_tokens")
public class UmaToken extends BaseModel<Integer> {

    @Id
    @TableGenerator(name="umaTokenGen")
    @GeneratedValue(strategy=TABLE, generator="umaTokenGen")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Basic
    @Column(name="token_type")
    private UmaTokenType tokenType;
    
    @Basic
    @Column(name="token_value")
    private String tokenValue;
    
    public Integer getId() {
        return id;
    }

    @Override
    public String getExternalId() {
        return id.toString();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UmaTokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(UmaTokenType tokenType) {
        this.tokenType = tokenType;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }
}
