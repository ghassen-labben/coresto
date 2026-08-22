package com.example.coresto.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationGroupRequest {

    /** Branch name — e.g. "Downtown Branch" */
    private String name;
}
