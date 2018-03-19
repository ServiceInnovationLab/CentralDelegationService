package delegations.cds.models;


import static javax.persistence.GenerationType.TABLE;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import delegations.cds.views.ImmutableResourceView;
import delegations.cds.views.ResourceView;

@Entity
@Table(name = "resources")
@NamedQueries({
        @NamedQuery(name = "Resource.findAll", query = "SELECT r FROM Resource r"),
        @NamedQuery(name = "Resource.findByCrn", query = "SELECT r FROM Resource r where r.crn = :crn")
})
public class Resource extends BaseModel<Long> {

    @Id
    @TableGenerator(name = "resourceGen", initialValue = 100)
    @GeneratedValue(strategy=TABLE, generator="resourceGen")
    private Long id;

    @Basic
    @Column(unique = true)
    private String crn;

    @Basic
    @Column(name = "openam_id")
    private String openamId;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany
    @JoinColumn(name = "resource_id")
    private List<Delegation> delegations;

    public ResourceView buildPublicView() {
        return ImmutableResourceView.builder()
                .id(id)
                .crn(crn)
                .clientCrn(getClient().getCrn())
                .user(user == null ? null : user.buildPublicView())
                .build();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getExternalId() {
        return crn;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(final String crn) {
        this.crn = crn;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(final Client client) {
        this.client = client;
    }


    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public List<Delegation> getDelegations() {
        return delegations;
    }

    public void setDelegations(final List<Delegation> delegations) {
        this.delegations = delegations;
    }

    public String getOpenamId() {
        return openamId;
    }

    public void setOpenamId(final String openamId) {
        this.openamId = openamId;
    }

    @Override
    public String toString() {
    	return String.format("Resource [id=%s, crn=%s, openamId=%s, client=%s, user=%s, delegations=%s]",
    			id, crn, openamId, client, user, delegations);
    }

}
