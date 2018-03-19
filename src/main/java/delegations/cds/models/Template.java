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

import delegations.cds.views.ImmutableTemplateView;
import delegations.cds.views.TemplateView;

@Entity
@Table(name = "templates")
public class Template extends BaseModel<Long> {

    @Id
    @TableGenerator(name="templateGen")
    @GeneratedValue(strategy=TABLE, generator="templateGen")
    private Long id;

    @Basic
    private String name;

    @Basic
    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Basic
    private Integer version;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Basic
    @Column(name = "is_default")
    private Boolean isDefault;

    @Basic
    private String crn;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getExternalId() {
        return id.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }


    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }


    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }


    public Client getClient() {
        return client;
    }

    public void setClient(final Client client) {
        this.client = client;
    }

    public TemplateView buildPublicView() {
        return ImmutableTemplateView.builder()
                .createTime(getCreateTime()).updateTime(getUpdateTime()).deleteTime(getDeleteTime())
                .name(name)
                .crn(crn)
                .content(content)
                .version(version)
                    .build();
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(final Boolean aDefault) {
        isDefault = aDefault;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(final String crn) {
        this.crn = crn;
    }

    @Override
    public String toString() {
    	return String.format("Template [id=%s, crn=%s, name=%s, version=%s]", id, crn, name, version);
    }
}
