package org.demo.server.infra.common.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ListResponse<T> {

    private int size;
    private List<T> data;

    public ListResponse(List<T> data) {
        this.size = data.size();
        this.data = data;
    }
}
