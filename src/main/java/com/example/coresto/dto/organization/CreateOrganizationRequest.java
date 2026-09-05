package com.example.coresto.dto.organization;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    /** Display name — e.g. "Pizza Palace" */
    @NotBlank(message = "Organization name is required")
    private String name;

    /**
     * URL-friendly slug — e.g. "pizza-palace".
     * Auto-generated from {@code name} if not provided.
     */
    private String alias;

    /** Email domains associated with this organization. */
    private List<String> domains;
}
