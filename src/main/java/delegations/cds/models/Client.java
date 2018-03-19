package delegations.cds.models;

import com.fasterxml.jackson.annotation.JsonView;
import delegations.cds.api.support.Views;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
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
import javax.validation.constraints.NotNull;

import static javax.persistence.GenerationType.TABLE;


@Entity
@Table(name = "clients")
@NamedQueries({
        @NamedQuery(name = "Client.findAll", query = "SELECT c FROM Client c"),
        @NamedQuery(name = "Client.findByCrn", query = "SELECT c FROM Client c where c.crn = :crn")
})
public class Client extends BaseModel<Long> {

    @Id
    @TableGenerator(name="clientGen")
    @GeneratedValue(strategy=TABLE, generator="clientGen")
    private Long id;

    @Basic @NotNull
    private String crn;

    @Basic
    @Column(name = "access_key")
    @JsonView(Views.Admin.class)
    private String accessKey;

    @Basic
    @Column(name = "secret_key")
    @JsonView(Views.Admin.class)
    private String secretKey;

    @Basic @NotNull
    @Column(name = "privacy_domain")
    private String privacyDomain;

    @Basic @NotNull
    private String service;

    @Basic
    @Column(name = "external_id")
    private String externalId;

    @OneToMany
    @JoinColumn(name = "client_id")
    @JsonView(Views.Internal.class)
    private List<Resource> resources;

    @Basic @NotNull
    private Boolean admin;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getExternalId() {
        return externalId;
    }

    public String generateDelegationCrn() {
        return generateCrn("delegation");
    }

    public String generateRendezvousCrn() {
        return generateCrn("rendezvous");
    }

    private String generateCrn(final String resourceType) {
        return MessageFormat.format("crn:{0}:{1}::{2}/{3}", privacyDomain, service, resourceType, UUID.randomUUID());
    }

    public static Client build(String privacyDomain, String service, String externalId) {
        Client c = new Client();
        c.setAdmin(false);
        c.setPrivacyDomain(privacyDomain);
        c.setService(service);
        c.setExternalId(externalId);
        c.setCrn(MessageFormat.format("crn:{0}:{1}", privacyDomain, service));
        return c;
    }

    public static Client buildFrom(Client client) {
        return build(client.privacyDomain, client.service, client.externalId);
    }

    @Override
    public String toString() {
        return "Client{" +
                "crn='" + crn + '\'' +
                '}';
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(final String crn) {
        this.crn = crn;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(final String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public String getPrivacyDomain() {
        return privacyDomain;
    }

    public void setPrivacyDomain(final String privacyDomain) {
        this.privacyDomain = privacyDomain;
    }

    public String getService() {
        return service;
    }

    public void setService(final String service) {
        this.service = service;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(final List<Resource> resources) {
        this.resources = resources;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(final Boolean admin) {
        this.admin = admin;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }
}
