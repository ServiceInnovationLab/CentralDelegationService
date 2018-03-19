package delegations.cds.models;

import static javax.persistence.GenerationType.TABLE;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import delegations.cds.models.enums.PolicyType;


@Entity
@Table(name = "policies")
public class Policy extends BaseModel<Integer> {

    @Id
    @TableGenerator(name="policyGen")
    @GeneratedValue(strategy=TABLE, generator="policyGen")
    private Integer id;

    @Basic
    @Column(name="policy_type")
    private PolicyType policyType;

    @Basic
    @Column(name="external_id")
    private String externalId;
    
    public Integer getId() {
        return id;
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
