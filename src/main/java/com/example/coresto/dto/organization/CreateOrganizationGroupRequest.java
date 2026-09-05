package com.example.coresto.dto.organization;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Branch name is required")
    private String name;
}
