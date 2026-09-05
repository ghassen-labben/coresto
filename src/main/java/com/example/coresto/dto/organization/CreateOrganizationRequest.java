package com.example.coresto.dto.organization;

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
    private String name;

    /**
     * URL-friendly slug — e.g. "pizza-palace".
     * Auto-generated from {@code name} if not provided.
     */
    private String alias;

    /** Email domains associated with this organization. */
    private List<String> domains;
}
