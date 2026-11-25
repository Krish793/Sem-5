package com.kalasetu.domain.model;

import java.util.UUID;

public class CraftCategory {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String regionFocus;

    public CraftCategory() {}

    public CraftCategory(UUID id, String name, String slug, String description, String regionFocus) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.regionFocus = regionFocus;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRegionFocus() { return regionFocus; }
    public void setRegionFocus(String regionFocus) { this.regionFocus = regionFocus; }
}
