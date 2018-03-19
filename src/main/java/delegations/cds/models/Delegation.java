package delegations.cds.models;

import static javax.persistence.GenerationType.TABLE;

import java.sql.Timestamp;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import delegations.cds.views.DelegationView;
import delegations.cds.views.ImmutableDelegationView;

@Table(name = "delegations")
@Entity
public class Delegation extends BaseModel<Long> {


    @Id
    @TableGenerator(name="delegationGen")
    @GeneratedValue(strategy=TABLE, generator="delegationGen")
    private Long id;

    @Basic
    private String crn;

    @Basic
    private String metadata;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(optional = false)
    @JoinColumn(name = "delegation_type_id")
    private DelegationType delegationType;

    @OneToOne
    @JoinColumn(name = "rendezvous_id")
    private Rendezvous rendezvous;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Basic
    @Column(name = "owner_consent_time")
    private Timestamp ownerConsentTime;

    @ManyToOne
    @JoinColumn(name = "delegate_id")
    private User delegate;

    @Basic
    @Column(name = "delegate_consent_time")
    private Timestamp delegateConsentTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getExternalId() {
        return crn;
    }

    public DelegationView buildPublicView() {
        return ImmutableDelegationView.builder()
                .createTime(getCreateTime()).updateTime(getUpdateTime()).deleteTime(getDeleteTime())
                .id(id)
                .crn(crn)
                .metadata(metadata)
                .resource(getResource().buildPublicView())
                .delegationType(getDelegationType().buildPublicView())
                .rendezvous(rendezvous == null ? null : rendezvous.buildPublicView())
                .build();
    }

    public boolean hasBothConsents() {
        return ownerConsentTime != null && delegateConsentTime != null;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(final Client client) {
        this.client = client;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    public DelegationType getDelegationType() {
        return delegationType;
    }

    public void setDelegationType(final DelegationType delegationType) {
        this.delegationType = delegationType;
    }


    public String getCrn() {
        return crn;
    }

    public void setCrn(final String crn) {
        this.crn = crn;
    }

    public Rendezvous getRendezvous() {
        return rendezvous;
    }

    public void setRendezvous(final Rendezvous rendezvous) {
        this.rendezvous = rendezvous;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(final User owner) {
        this.owner = owner;
    }

    public Timestamp getOwnerConsentTime() {
        return ownerConsentTime;
    }

    public void setOwnerConsentTime(final Timestamp ownerConsentTime) {
        this.ownerConsentTime = ownerConsentTime;
    }

    public User getDelegate() {
        return delegate;
    }

    public void setDelegate(final User delegate) {
        this.delegate = delegate;
    }

    public Timestamp getDelegateConsentTime() {
        return delegateConsentTime;
    }

    public void setDelegateConsentTime(final Timestamp delegateConsentTime) {
        this.delegateConsentTime = delegateConsentTime;
    }

    @Override
    public String toString() {
    	return String.format("Delegation [id=%s, metadata=%s, client=%s, resource=%s, delegationType=%s, rendezvous=%s, owner=%s, ownerConsentTime=%s, delegate=%s, delegateConsentTime=%s, crn=%s]",
    			id, metadata, client, resource, delegationType, rendezvous, owner, ownerConsentTime, delegate, delegateConsentTime, crn);
    }
}
