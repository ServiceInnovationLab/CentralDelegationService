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

import delegations.cds.views.DelegationTypeView;
import delegations.cds.views.ImmutableDelegationTypeView;

@Table(name = "delegation_types")
@Entity
public class DelegationType extends BaseModel<Long> {

    @Id
    @TableGenerator(name="delegationTypeGen")
    @GeneratedValue(strategy=TABLE, generator="delegationTypeGen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Basic
    private String name;

    @Basic
    @Column(name = "metadata_keys")
    private String metadataKeys;

    @Basic
    @Column(unique = true)
    private String crn;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private Template template;
    
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getExternalId() {
        return crn;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(final Client client) {
        this.client = client;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(final Template template) {
        this.template = template;
    }

    public String getMetadataKeys() {
        return metadataKeys;
    }

    public void setMetadataKeys(final String metadataKeys) {
        this.metadataKeys = metadataKeys;
    }


    public String getCrn() {
        return crn;
    }

    public void setCrn(final String crn) {
        this.crn = crn;
    }

    public DelegationTypeView buildPublicView() {
        ImmutableDelegationTypeView.Builder builder = ImmutableDelegationTypeView.builder();

        builder = builder.createTime(getCreateTime()).updateTime(getUpdateTime()).deleteTime(getDeleteTime())
                .id(id)
                .name(name)
                .crn(crn)
                .metadataKeys(metadataKeys);

        if (template != null) {
            builder = builder.template(template.buildPublicView());
        }



        return builder.build();
    }
}
