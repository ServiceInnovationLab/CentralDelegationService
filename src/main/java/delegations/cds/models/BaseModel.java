package delegations.cds.models;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.sql.Timestamp;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;


@MappedSuperclass
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public abstract class BaseModel<T> {

    @Basic
    @Column(name = "create_time")
    private Timestamp createTime;

    @Basic
    @Column(name = "update_time")
    private Timestamp updateTime;

    @Basic
    @Column(name = "delete_time")
    private Timestamp deleteTime;

    public abstract T getId();

    public abstract String getExternalId();

    @PreUpdate
    void updateModificationTimestamp() {
        updateTime = new Timestamp(System.currentTimeMillis());
    }

    @PrePersist
    void updateCreateTimestamp() {
        createTime = new Timestamp(System.currentTimeMillis());
        updateTime = new Timestamp(System.currentTimeMillis());
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public Timestamp getDeleteTime() {
        return deleteTime;
    }

    public void setDeletedAt() {
        deleteTime = new Timestamp(System.currentTimeMillis());
    }
}
