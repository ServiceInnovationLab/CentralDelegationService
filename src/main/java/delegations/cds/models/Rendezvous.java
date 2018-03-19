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

import delegations.cds.views.ImmutableRendezvousView;
import delegations.cds.views.RendezvousView;

@Entity
@Table(name = "rendezvouses")
public class Rendezvous extends BaseModel<Long> {

    @Id
    @TableGenerator(name="rendezvousGen")
    @GeneratedValue(strategy=TABLE, generator="rendezvousGen")
    private Long id;

    @Basic
    private String crn;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Basic
    @Column(name = "owner_code")
    private String ownerCode;

    @Basic
    @Column(name = "owner_code_consumed")
    private boolean ownerCodeConsumed;

    @Basic
    @Column(name = "delegate_code")
    private String delegateCode;

    @Basic
    @Column(name = "delegate_code_consumed")
    private boolean delegateCodeConsumed;

    @Basic
    @Column(name = "owner_email")
    private String ownerEmail;

    @Basic
    @Column(name = "delegate_email")
    private String delegateEmail;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getExternalId() {
        return crn;
    }

    public RendezvousView buildPublicView() {
        return ImmutableRendezvousView.builder()
                .createTime(getCreateTime()).updateTime(getUpdateTime()).deleteTime(getDeleteTime())
                .id(id)
                .crn(crn)
                .ownerCode(ownerCodeConsumed ? null : ownerCode)
                .delegateCode(delegateCodeConsumed ? null : delegateCode)
                .build();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(final Client client) {
        this.client = client;
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(final String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getDelegateCode() {
        return delegateCode;
    }

    public void setDelegateCode(final String delegateCode) {
        this.delegateCode = delegateCode;
    }

    public void setCrn(final String crn) {
        this.crn = crn;
    }

    public boolean isOwnerCodeConsumed() {
        return ownerCodeConsumed;
    }

    public void setOwnerCodeConsumed(final boolean ownerCodeConsumed) {
        this.ownerCodeConsumed = ownerCodeConsumed;
    }

    public boolean isDelegateCodeConsumed() {
        return delegateCodeConsumed;
    }

    public void setDelegateCodeConsumed(final boolean delegateCodeConsumed) {
        this.delegateCodeConsumed = delegateCodeConsumed;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(final String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getDelegateEmail() {
        return delegateEmail;
    }

    public void setDelegateEmail(final String delegateEmail) {
        this.delegateEmail = delegateEmail;
    }

}
