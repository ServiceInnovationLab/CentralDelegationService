package delegations.cds.api;

import static delegations.cds.api.support.ApiConstants.ASCENDING;
import static delegations.cds.api.support.ApiConstants.NONE;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import io.swagger.annotations.ApiParam;

// Trying to follow https://cloud.google.com/apis/design/standard_fields
public class ListParams {

    @DefaultValue(NONE)
    @QueryParam("filter")
    @ApiParam
    private String filter;

    @DefaultValue(ASCENDING)
    @QueryParam("sort")
    @ApiParam
    private String sort;

    @DefaultValue("50")
    @QueryParam("page_size")
    @ApiParam
    private Integer pageSize;

    @DefaultValue("1")
    @QueryParam("page_token")
    @ApiParam
    private Integer pageToken;

    public String getFilter() {
        return filter;
    }

    public void setFilter(final String filter) {
        this.filter = filter;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(final String sort) {
        this.sort = sort;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(final Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageToken() {
        return pageToken;
    }

    public void setPageToken(final Integer pageToken) {
        this.pageToken = pageToken;
    }

}
