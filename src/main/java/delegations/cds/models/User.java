package delegations.cds.models;

import static javax.persistence.GenerationType.TABLE;

import java.util.List;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.fasterxml.jackson.annotation.JsonView;

import delegations.cds.api.support.Views;
import delegations.cds.views.ImmutableUserView;
import delegations.cds.views.UserView;

@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
        @NamedQuery(name = "User.findById", query = "SELECT u FROM User u where u.id = :id"),
        @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u where u.email = :email"),
        @NamedQuery(name = "User.findByRealmeFlt", query = "SELECT u FROM User u where u.realmeFlt = :realmeFlt")
})
public class User extends BaseModel<Integer> {

    @Id
    @TableGenerator(name = "userGen", initialValue = 100)
    @GeneratedValue(strategy = TABLE, generator = "userGen")
    private Integer id;

    @Basic
    @Column(name = "realme_flt")
    private String realmeFlt;

    @Basic
    private String email;

    @OneToMany
    @JoinColumn(name = "user_id")
    @JsonView(Views.Internal.class)
    private List<Resource> resources;

    @Basic
    @Column(name = "openam_uid")
    private String openamUid;

    @Basic
    @Column(name = "openam_password")
    private String openamPassword;

    @Basic
    @Column(name = "uma_pat")
    private String umaPat;

    public UserView buildPublicView() {
        return ImmutableUserView.builder()
                .id(id)
                .email(email)
                .build();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getExternalId() {
        return Integer.toString(id);
    }

    public String getRealmeFlt() {
        return realmeFlt;
    }

    public void setRealmeFlt(final String realmeFlt) {
        this.realmeFlt = realmeFlt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(final List<Resource> resources) {
        this.resources = resources;
    }

    public String getOpenamUid() {
        return openamUid;
    }

    public void setOpenamUid(final String openamUid) {
        this.openamUid = openamUid;
    }

    public String getOpenamPassword() {
        return openamPassword;
    }

    public void setOpenamPassword(final String openamPassword) {
        this.openamPassword = openamPassword;
    }

    public String getUmaPat() {
        return umaPat;
    }

    public void setUmaPat(final String umaPat) {
        this.umaPat = umaPat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        return Objects.equals(id, other.id);
    }
}
