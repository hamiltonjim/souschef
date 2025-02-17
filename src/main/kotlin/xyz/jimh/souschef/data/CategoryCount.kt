package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
@Schema(description = "Count of recipes for a category")
data class CategoryCount(@Id val category: String, @Column(name="cnt") val count: Int)
