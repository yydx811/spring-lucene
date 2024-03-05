package org.example.lucene.domain;

import org.apache.lucene.search.SortField;

import java.util.List;

public class QueryParam {
    private Long id;
    private String title;
    private List<String> statusList;
    private Long startTime;
    private Long endTime;
    private int pageNum = 1;
    private int pageSize = 10;
    private List<QueryParam.Sort> sorts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<String> statusList) {
        this.statusList = statusList;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public void setSorts(List<Sort> sorts) {
        this.sorts = sorts;
    }

    public static class Sort {
        private String field;
        private boolean reverse;
        private SortField.Type type;

        public Sort() {
        }

        public Sort(String field, boolean reverse, SortField.Type type) {
            this.field = field;
            this.reverse = reverse;
            this.type = type;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public boolean getReverse() {
            return reverse;
        }

        public void setReverse(boolean reverse) {
            this.reverse = reverse;
        }

        public SortField.Type getType() {
            return type;
        }

        public void setType(SortField.Type type) {
            this.type = type;
        }
    }
}
