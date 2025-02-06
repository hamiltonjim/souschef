package xyz.jimh.souschef.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface WeightDao : JpaRepository<Weight, Long> {
    @Query("select w from weights w where w.name = :name or w.abbrev = :name")
    fun findByAnyName(name: String): Weight?
    fun findAllByInGramsGreaterThanOrderByInGrams(gramsGreaterThan: Double): List<Weight>
    fun findAllByIntlIsFalseOrderByInGrams() : List<Weight>
    fun findAllByIntlIsTrueOrderByInGrams() : List<Weight>
}